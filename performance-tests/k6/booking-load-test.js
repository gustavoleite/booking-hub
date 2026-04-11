import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  stages: [
    { duration: '30s', target: 10 }, // Ramp-up
    { duration: '1m', target: 20 },  // Carga constante
    { duration: '30s', target: 50 }, // Stress
    { duration: '1m', target: 50 },  // Manter Stress
    { duration: '30s', target: 0 },  // Ramp-down
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% das requisições devem ser < 500ms
    http_req_failed: ['rate<0.05'],   // Taxa de erro deve ser < 5% (tolerando concorrência de horários)
  },
};

function getRandomFutureDate() {
  const date = new Date();
  date.setDate(date.getDate() + Math.floor(Math.random() * 30) + 1); // 30 dias de janela
  date.setHours(Math.floor(Math.random() * 8) + 9); // Entre 09:00 e 17:00
  date.setMinutes(Math.floor(Math.random() * 4) * 15); // Em slots de 15 min: 00, 15, 30, 45
  date.setSeconds(0);
  date.setMilliseconds(0);
  return date.toISOString().replace('Z', '');
}

function generateValidCnpj() {
  const n = Array.from({length: 8}, () => Math.floor(Math.random() * 10));
  const n9 = 0, n10 = 0, n11 = 0, n12 = 1; // 0001
  
  const initial = [...n, n9, n10, n11, n12];
  
  const calcDigit = (base, weights) => {
    const sum = base.reduce((acc, val, i) => acc + (val * weights[i]), 0);
    const mod = sum % 11;
    return mod < 2 ? 0 : 11 - mod;
  };

  const d1 = calcDigit(initial, [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]);
  const d2 = calcDigit([...initial, d1], [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]);

  return [...initial, d1, d2].join('');
}

export function setup() {
  const timestamp = Date.now();
  const adminEmail = `admin_${timestamp}@test.com`;
  const profEmail = `prof_${timestamp}@test.com`;
  const clientEmail = `client_${timestamp}@test.com`;
  const password = 'Password123!';
  const cnpj = generateValidCnpj();

  console.log(`Iniciando Setup com: Admin=${adminEmail}, Prof=${profEmail}, CNPJ=${cnpj}`);

  // 1. Registrar e Logar Owner
  http.post(`${BASE_URL}/api/auth/register`, JSON.stringify({
    email: adminEmail,
    password: password,
    role: 'ROLE_OWNER'
  }), { headers: { 'Content-Type': 'application/json' } });

  const loginRes = http.post(`${BASE_URL}/api/auth/login`, JSON.stringify({
    email: adminEmail,
    password: password
  }), { headers: { 'Content-Type': 'application/json' } });

  const ownerToken = loginRes.json('accessToken');
  if (!ownerToken) console.error(`Falha no login do Owner: ${loginRes.status} ${loginRes.body}`);

  // 2. Criar Estabelecimento
  const estRes = http.post(`${BASE_URL}/api/catalog/establishments`, JSON.stringify({
    name: 'Performance Test Salon',
    cnpj: cnpj,
    description: 'Salon for load testing',
    address: {
      street: 'Test St',
      number: '123',
      city: 'Test City',
      state: 'TS',
      zipCode: '12345-678',
      latitude: -23.5505,
      longitude: -46.6333
    },
    businessHours: [
      { dayOfWeek: 1, openTime: '08:00:00', closeTime: '18:00:00' },
      { dayOfWeek: 2, openTime: '08:00:00', closeTime: '18:00:00' },
      { dayOfWeek: 3, openTime: '08:00:00', closeTime: '18:00:00' },
      { dayOfWeek: 4, openTime: '08:00:00', closeTime: '18:00:00' },
      { dayOfWeek: 5, openTime: '08:00:00', closeTime: '18:00:00' },
      { dayOfWeek: 6, openTime: '08:00:00', closeTime: '18:00:00' },
      { dayOfWeek: 7, openTime: '08:00:00', closeTime: '18:00:00' }
    ],
    services: [
      { title: 'Corte de Cabelo', description: 'Corte padrão' }
    ]
  }), { headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${ownerToken}` } });

  const establishmentId = estRes.json('id');
  const services = estRes.json('providedServices');
  const serviceId = (services && services.length > 0) ? services[0].id : undefined;

  if (!establishmentId) console.error(`Falha ao criar estabelecimento: ${estRes.status} ${estRes.body}`);

  // 4. Registrar e Logar Profissional
  http.post(`${BASE_URL}/api/auth/register`, JSON.stringify({
    email: profEmail,
    password: password,
    role: 'ROLE_PROFESSIONAL'
  }), { headers: { 'Content-Type': 'application/json' } });

  const profLoginRes = http.post(`${BASE_URL}/api/auth/login`, JSON.stringify({
    email: profEmail,
    password: password
  }), { headers: { 'Content-Type': 'application/json' } });

  const profToken = profLoginRes.json('accessToken');
  const profUserId = profLoginRes.json('id');

  // Criar perfil do profissional
  const profRes = http.post(`${BASE_URL}/api/catalog/professionals/me`, JSON.stringify({
    name: 'Professional Test',
    bio: 'Professional for load testing',
    specialties: ['Corte']
  }), { headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${profToken}` } });

  const professionalId = profRes.json('id');
  if (!professionalId) console.error(`Falha ao criar perfil profissional: ${profRes.status} ${profRes.body}`);

  // 5. Vincular Profissional ao Estabelecimento
  const affRes = http.post(`${BASE_URL}/api/catalog/establishments/${establishmentId}/affiliations?professionalId=${professionalId}`, JSON.stringify({
    active: true,
    workSchedules: [
      { dayOfWeek: 1, startTime: '08:00:00', endTime: '18:00:00' },
      { dayOfWeek: 2, startTime: '08:00:00', endTime: '18:00:00' },
      { dayOfWeek: 3, startTime: '08:00:00', endTime: '18:00:00' },
      { dayOfWeek: 4, startTime: '08:00:00', endTime: '18:00:00' },
      { dayOfWeek: 5, startTime: '08:00:00', endTime: '18:00:00' },
      { dayOfWeek: 6, startTime: '08:00:00', endTime: '18:00:00' },
      { dayOfWeek: 7, startTime: '08:00:00', endTime: '18:00:00' }
    ],
    serviceOfferings: [
      { providedServiceId: serviceId, price: 50.0, durationMinutes: 30 }
    ]
  }), { headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${ownerToken}` } });

  if (affRes.status !== 200 && affRes.status !== 201) {
    console.error(`Falha na afiliação: ${affRes.status} ${affRes.body}`);
  }

  // 6. Registrar e Logar Cliente
  http.post(`${BASE_URL}/api/auth/register`, JSON.stringify({
    email: clientEmail,
    password: password,
    role: 'ROLE_CLIENT'
  }), { headers: { 'Content-Type': 'application/json' } });

  const clientLoginRes = http.post(`${BASE_URL}/api/auth/login`, JSON.stringify({
    email: clientEmail,
    password: password
  }), { headers: { 'Content-Type': 'application/json' } });

  const clientToken = clientLoginRes.json('accessToken');

  console.log(`Setup completo: Est=${establishmentId}, Prof=${professionalId}, Svc=${serviceId}`);

  return {
    clientToken,
    establishmentId,
    professionalId,
    serviceId
  };
}

export default function (data) {
  // Fail-safe: Se o setup falhou, não adianta rodar o teste
  if (!data.establishmentId || !data.professionalId || !data.serviceId) {
    console.error('ERRO: Setup falhou ao obter IDs. Verifique se os serviços estão UP e se os logs mostram erros de negócio.');
    sleep(1);
    return;
  }

  const payload = JSON.stringify({
    professionalId: data.professionalId,
    establishmentId: data.establishmentId,
    providedServiceId: data.serviceId,
    startDatetime: getRandomFutureDate(),
    notes: 'K6 Performance Test Booking'
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${data.clientToken}`,
    },
  };

  const res = http.post(`${BASE_URL}/api/bookings`, payload, params);

  check(res, {
    'is status 201': (r) => r.status === 201,
    'has booking id': (r) => r.json('id') !== undefined,
  });

  // Aguarda um pouco entre as requisições para simular comportamento humano real
  // mas curto o suficiente para gerar carga.
  sleep(1);
}
