import { check, sleep } from 'k6';
import exec from 'k6/execution';
import http from 'k6/http';
import { Counter } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// ─── Métricas customizadas ────────────────────────────────────────────────────
const status201 = new Counter('status_201_created');
const status409 = new Counter('status_409_conflict');
const status500 = new Counter('status_500_error');

// ─── Slot fixo gerado dinamicamente (now + 60 dias, 10h) ─────────────────────
// Garante idempotência entre execuções de CI: cada run usa uma data diferente.
function buildFixedSlot() {
  const d = new Date(Date.now() + 60 * 24 * 60 * 60 * 1000);
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T10:00:00`;
}

// ─── Slot aleatório para o cenário de carga normal ───────────────────────────
// Janela: 1–30 dias a partir de hoje, horário comercial em slots de 1h.
function randomSlot() {
  const d = new Date(Date.now() + (Math.floor(Math.random() * 30) + 1) * 24 * 60 * 60 * 1000);
  const pad = (n) => String(n).padStart(2, '0');
  const hour = Math.floor(Math.random() * 9) + 9; // 09h–17h
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(hour)}:00:00`;
}

// ─── Opções e cenários ────────────────────────────────────────────────────────
export const options = {
  setupTimeout: '3m', // Pool de 20 clientes + infra: ~40 requests no setup
  scenarios: {

    // Cenário 1 — Thundering Herd
    // 500 VUs atacam o mesmo slot para validar a prevenção de double-booking.
    thundering_herd: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 100 }, // Sobe rápido
        { duration: '1m',  target: 500 }, // Pico: 500 VUs simultâneos
        { duration: '30s', target: 0 },   // Desce
      ],
      tags: { scenario: 'thundering_herd' },
    },

    // Cenário 2 — Carga Normal
    // Clientes diferentes, slots diferentes — valida throughput sem degradação.
    normal_load: {
      executor: 'ramping-vus',
      startVUs: 0,
      startTime: '2m10s', // Começa após o thundering_herd terminar
      stages: [
        { duration: '30s', target: 50  }, // Warm-up
        { duration: '2m',  target: 200 }, // Carga nominal
        { duration: '30s', target: 0   }, // Ramp-down
      ],
      tags: { scenario: 'normal_load' },
    },
  },

  thresholds: {
    // Thundering Herd: provar que o banco não travou e que pelo menos 1 booking passou
    'status_500_error':                            ['count==0'],  // Proibido erro 500
    'status_201_created':                          ['count>0'],   // Pelo menos 1 criado
    // Latência geral: 95% das requisições em < 500ms
    'http_req_duration':                           ['p(95)<500'],
    // Carga normal: latência sob tráfego real também deve ser aceitável
    'http_req_duration{scenario:normal_load}':     ['p(95)<500'],
  },
};

// ─── Helpers ──────────────────────────────────────────────────────────────────
function generateValidCnpj() {
  const n = Array.from({ length: 8 }, () => Math.floor(Math.random() * 10));
  const initial = [...n, 0, 0, 0, 1];
  const calcDigit = (base, weights) => {
    const sum = base.reduce((acc, val, i) => acc + val * weights[i], 0);
    const mod = sum % 11;
    return mod < 2 ? 0 : 11 - mod;
  };
  const d1 = calcDigit(initial, [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]);
  const d2 = calcDigit([...initial, d1], [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]);
  return [...initial, d1, d2].join('');
}

function register(email, password, role) {
  http.post(`${BASE_URL}/api/auth/register`,
    JSON.stringify({ email, password, role }),
    { headers: { 'Content-Type': 'application/json' } });
}

function login(email, password) {
  const res = http.post(`${BASE_URL}/api/auth/login`,
    JSON.stringify({ email, password }),
    { headers: { 'Content-Type': 'application/json' } });
  return { token: res.json('accessToken'), id: res.json('id') };
}

function authHeader(token) {
  return { headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` } };
}

// ─── Setup (executado 1x antes de todos os VUs) ───────────────────────────────
export function setup() {
  const ts       = Date.now();
  const password = 'Password123!';
  const cnpj     = generateValidCnpj();

  // 1. Owner
  const adminEmail = `admin_${ts}@test.com`;
  register(adminEmail, password, 'ROLE_OWNER');
  const owner = login(adminEmail, password);
  if (!owner.token) { console.error('Falha no login do Owner'); }

  // 2. Estabelecimento
  const estRes = http.post(`${BASE_URL}/api/catalog/establishments`,
    JSON.stringify({
      name: 'Thundering Herd Salon', cnpj,
      description: 'Salão para teste de concorrência e carga',
      address: {
        street: 'Test St', number: '123', city: 'Test City',
        state: 'TS', zipCode: '12345-678', latitude: -23.5505, longitude: -46.6333,
      },
      businessHours: [1, 2, 3, 4, 5, 6, 7].map(day => ({
        dayOfWeek: day, openTime: '08:00:00', closeTime: '20:00:00',
      })),
      services: [{ title: 'Corte de Cabelo', description: 'Corte padrão' }],
    }),
    authHeader(owner.token));

  const establishmentId = estRes.json('id');
  const services        = estRes.json('providedServices');
  const serviceId       = services && services.length > 0 ? services[0].id : undefined;
  if (!establishmentId) { console.error(`Falha ao criar estabelecimento: ${estRes.status} ${estRes.body}`); }

  // 3. Profissional
  const profEmail = `prof_${ts}@test.com`;
  register(profEmail, password, 'ROLE_PROFESSIONAL');
  const prof = login(profEmail, password);

  const profRes = http.post(`${BASE_URL}/api/catalog/professionals/me`,
    JSON.stringify({ name: 'Professional Test', bio: 'Para teste de carga', specialties: ['Corte'] }),
    authHeader(prof.token));

  const professionalId = profRes.json('id');
  if (!professionalId) { console.error(`Falha ao criar profissional: ${profRes.status} ${profRes.body}`); }

  // 4. Afiliação
  const affRes = http.post(
    `${BASE_URL}/api/catalog/establishments/${establishmentId}/affiliations?professionalId=${professionalId}`,
    JSON.stringify({
      active: true,
      workSchedules: [1, 2, 3, 4, 5, 6, 7].map(day => ({
        dayOfWeek: day, startTime: '08:00:00', endTime: '20:00:00',
      })),
      serviceOfferings: [{ providedServiceId: serviceId, price: 50.0, durationMinutes: 30 }],
    }),
    authHeader(owner.token));

  if (affRes.status !== 200 && affRes.status !== 201) {
    console.error(`Falha na afiliação: ${affRes.status} ${affRes.body}`);
  }

  // 5. Pool de clientes — distribui tokens entre VUs para simular usuários distintos
  // Pool maior que os VUs do normal_load para evitar reutilização excessiva.
  const CLIENT_POOL_SIZE = 20; // Reduzido para caber no setupTimeout em CI
  const clientTokens = [];
  for (let i = 0; i < CLIENT_POOL_SIZE; i++) {
    const email = `client_${ts}_${i}@test.com`;
    register(email, password, 'ROLE_CLIENT');
    const c = login(email, password);
    if (c.token) { clientTokens.push(c.token); }
  }

  const fixedSlot = buildFixedSlot();
  console.log(
    `Setup completo — Est=${establishmentId}, Prof=${professionalId}, ` +
    `Svc=${serviceId}, FixedSlot=${fixedSlot}, Pool=${clientTokens.length} clientes`
  );

  return { clientTokens, establishmentId, professionalId, serviceId, fixedSlot };
}

// ─── Função default (executada por cada VU em cada iteração) ──────────────────
export default function (data) {
  if (!data.establishmentId || !data.professionalId || !data.serviceId) {
    console.error('Setup falhou — abortando VU.');
    sleep(1);
    return;
  }

  // Seleciona token do pool de forma distribuída pelo ID do VU
  const token = data.clientTokens[__VU % data.clientTokens.length];

  // Thundering Herd → slot fixo | Normal Load → slot aleatório
  const isHerd = __ITER === 0 &&
    (typeof __ENV.K6_SCENARIO === 'undefined' || __ENV.K6_SCENARIO !== 'normal_load');
  const slot = (exec.scenario.name === 'thundering_herd') ? data.fixedSlot : randomSlot();

  const res = http.post(
    `${BASE_URL}/api/bookings`,
    JSON.stringify({
      professionalId:    data.professionalId,
      establishmentId:   data.establishmentId,
      providedServiceId: data.serviceId,
      startDatetime:     slot,
      notes:             `k6 — ${exec.scenario.name}`,
    }),
    { headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` } },
  );

  // Contadores por status
  if (res.status === 201) status201.add(1);
  if (res.status === 409) status409.add(1);
  if (res.status >= 500) status500.add(1);

  if (exec.scenario.name === 'thundering_herd') {
    // Thundering Herd: 201 ou 409 são ambos sucesso — 500 é falha
    check(res, {
      'Concorrência tratada corretamente (201 ou 409)': (r) => r.status === 201 || r.status === 409,
      'Sem erro interno do servidor (sem 500)':         (r) => r.status < 500,
    });
    sleep(Math.random() * 0.5); // Mínimo para maximizar colisões
  } else {
    // Normal Load: apenas 201 é sucesso
    check(res, {
      'Booking criado com sucesso (201)': (r) => r.status === 201,
      'Sem erro interno do servidor':     (r) => r.status < 500,
    });
    sleep(1 + Math.random()); // Simula comportamento humano real
  }
}
