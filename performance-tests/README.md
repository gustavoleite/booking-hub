# Testes de Performance — Booking Hub

Este diretório contém os scripts de teste de carga e performance utilizando o **k6**.

## Pré-requisitos

- [k6](https://k6.io/docs/getting-started/installation/) instalado.
- Sistema Booking Hub rodando (Docker Compose ou local).

## Como Executar

### 1. Garantir que o sistema está no ar

```bash
docker compose up -d
```

Os testes rodam via API Gateway (porta 8080 por padrão).

### 2. Executar o teste

#### Opção A — k6 instalado localmente

```bash
k6 run performance-tests/k6/booking-load-test.js
```

Para apontar para outro ambiente (ex: staging):

```bash
k6 run performance-tests/k6/booking-load-test.js --env BASE_URL=https://meu-ambiente.com
```

#### Opção B — Via Docker (sem instalar o k6)

**Linux/macOS:**
```bash
docker run --rm -i \
  -v "$(pwd)/performance-tests/k6:/scripts" \
  --network host \
  grafana/k6 run /scripts/booking-load-test.js
```

**Windows (PowerShell):**
```powershell
docker run --rm -i `
  -v "${PWD}/performance-tests/k6:/scripts" `
  grafana/k6 run /scripts/booking-load-test.js `
  --env BASE_URL=http://host.docker.internal:8080
```

> No Windows usa-se `host.docker.internal` porque o container k6 não compartilha a rede do host.

---

## Cenários

O script `booking-load-test.js` executa dois cenários sequenciais que, juntos, cobrem os dois eixos do requisito não-funcional: **prevenção de double-booking** e **throughput sem degradação**.

### Cenário 1 — Thundering Herd (concorrência)

**Objetivo:** provar que o Booking Service previne double-booking sob carga máxima, mantendo o banco estável.

**Comportamento:**
- Registra um pool de **50 clientes distintos** no setup e distribui os tokens pelos VUs (`__VU % pool`), simulando usuários reais diferentes.
- Todos os 500 VUs atacam **exatamente o mesmo slot** (gerado dinamicamente como `hoje + 60 dias, 10h00`) para forçar colisão máxima.
- O slot é calculado em runtime — não é uma data fixa — tornando o teste **idempotente entre execuções de CI**.

**Estágios:**

| Duração | VUs   |
|---------|-------|
| 30s     | 0→100 |
| 1m      | 500   |
| 30s     | 500→0 |

**Output esperado:**

```
status_201_created : 1      ✓  (apenas 1 booking passa)
status_409_conflict: ~499   ✓  (double-booking bloqueado corretamente)
status_500_error   : 0      ✓  (banco estável, sem crash)
```

> Retornar `409 Conflict` **não é falha** — é a prova de que a restrição de concorrência funcionou. Tirar um print desse resultado para o relatório PDF comprova o requisito de concorrência.

---

### Cenário 2 — Normal Load (throughput)

**Objetivo:** validar que o sistema suporta volume elevado de agendamentos normais sem degradação de performance.

Inicia automaticamente após o Thundering Herd (`startTime: 2m10s`).

**Comportamento:**
- Cada VU usa um token diferente do pool de clientes.
- Cada VU agenda um **slot aleatório** (1–30 dias à frente, horário comercial), sem colisão intencional.
- Simula o tráfego cotidiano de uma plataforma em produção.

**Estágios:**

| Duração | VUs    |
|---------|--------|
| 30s     | 0→50   |
| 2m      | 200    |
| 30s     | 200→0  |

**Output esperado:**

```
status_201_created: alto    ✓  (bookings criados com sucesso)
p95 latência      : < 500ms ✓  (sem degradação sob carga)
status_500_error  : 0       ✓  (sistema estável)
```

---

## Thresholds

| Threshold | Valor | Cenário |
|-----------|-------|---------|
| `http_req_duration` p(95) | < 500ms | Geral |
| `http_req_duration{scenario:normal_load}` p(95) | < 500ms | Normal Load |
| `status_500_error` | == 0 | Ambos |
| `status_201_created` | > 0 | Thundering Herd |

---

## Setup Automático

O `setup()` do k6 executa **uma única vez** antes de todos os VUs e provisiona toda a infraestrutura de teste:

1. Registra e autentica um **Owner**
2. Cria um **Estabelecimento** com horário de funcionamento (08h–20h todos os dias)
3. Registra e autentica um **Profissional**, cria perfil e afilia ao estabelecimento
4. Registra um **pool de 50 clientes** e retorna os tokens para distribuição entre VUs
5. Calcula o `fixedSlot` (`hoje + 60 dias, T10:00:00`) para o Thundering Herd

---

## Integração Contínua (CI)

Os testes são executados automaticamente no GitHub Actions em pushes e PRs para `main`. O k6 é instalado diretamente no runner via `apt` (não como container) para que `localhost:8080` resolva corretamente para os serviços levantados pelo `docker compose`.

```yaml
- name: Install k6
  run: |
    curl -fsSL https://dl.k6.io/key.gpg \
      | sudo gpg --dearmor -o /usr/share/keyrings/k6-archive-keyring.gpg
    echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] \
      https://dl.k6.io/deb stable main" \
      | sudo tee /etc/apt/sources.list.d/k6.list
    sudo apt-get update --quiet
    sudo apt-get install --quiet -y k6

- name: Run k6 Load Test
  run: |
    k6 run performance-tests/k6/booking-load-test.js \
      --env BASE_URL=http://localhost:8080
```
