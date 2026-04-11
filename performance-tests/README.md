# Testes de Performance - Booking Hub

Este diretório contém os scripts de teste de carga e performance utilizando o **k6**.

## Pré-requisitos

- [k6](https://k6.io/docs/getting-started/installation/) instalado.
- Sistema Booking Hub rodando (Docker Compose ou local).

## Como Executar

### 1. Garantir que o sistema está no ar
Os testes rodam via API Gateway (porta 8080 por padrão).

```bash
docker-compose up -d
```

### 2. Executar o teste de carga

#### Opção A: k6 instalado localmente
Abra o terminal na **raiz do projeto** e execute:

```bash
cd performance-tests/k6
k6 run booking-load-test.js
```

#### Opção B: Via Docker (Recomendado se não quiser instalar o k6)
A partir da **raiz do projeto**, execute o comando correspondente ao seu sistema operacional:

**Windows (PowerShell):**
```powershell
docker run --rm -i -v "${PWD}/performance-tests/k6:/scripts" grafana/k6 run /scripts/booking-load-test.js --env BASE_URL=http://host.docker.internal:8080         
```

**Linux/macOS:**
```bash
docker run --rm -i -v "$(pwd)/performance-tests/k6:/scripts" --network="host" grafana/k6 run /scripts/booking-load-test.js
```

> **Dica:** Se você já estiver dentro da pasta `performance-tests/k6`, substitua `${PWD}/performance-tests/k6` apenas por `${PWD}` no comando acima.

> **Nota sobre Portas:** O teste ataca o **API Gateway (Porta 8080)**. Certifique-se de que o Docker Compose está de pé (`docker-compose ps` deve mostrar todos os serviços como *Up*). No Windows, usamos `host.docker.internal` para que o container do k6 consiga enxergar o Gateway que está exposto no seu host na porta 8080.

Para rodar contra um ambiente diferente (ex: Staging/Prod):
 
```bash
k6 run -e BASE_URL=https://meu-ambiente.com booking-load-test.js
```

## Cenários Testados

### Booking Load Test
Simula o fluxo crítico de agendamento:
1. **Setup**: Registra um Owner, cria um Estabelecimento, um Profissional e um Serviço. Registra um Cliente.
2. **Carga**: Vários usuários simultâneos realizam agendamentos em horários aleatórios para o profissional criado.
3. **Thresholds**:
   - 95% das requisições devem responder em menos de **500ms**.
   - Taxa de falha deve ser inferior a **5%**.

## Integração Contínua (CI)

Os testes são executados automaticamente no GitHub Actions em cada Push para a branch principal ou via Pull Request, garantindo que novas alterações não degradem a performance do sistema.
