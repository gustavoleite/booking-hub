# RFC — Pipeline de Integração Contínua (CI) no GitHub Actions

**Projeto:** Booking Hub  
**Fase:** Tech Challenge Fase 3  
**Requisito atendido:** Item 2 — Qualidade de Software (alíneas a, b, c e d)  
**Status:** Proposta  
**Autor:** Equipe Booking Hub  

---

## 1. Contexto e Motivação

O requisito 2 do Tech Challenge exige:

| Alínea | Exigência |
|--------|-----------|
| **a** | Testes unitários com TDD e alta cobertura (JUnit) |
| **b** | Testes de integração entre módulos + análise estática de código |
| **c** | Testes de controllers + CI integrado + BDD (Cucumber) |
| **d** | Testes não-funcionais de performance e carga |

O projeto já possui a infraestrutura de testes implementada:
- **Testes unitários** em todos os 5 módulos (JUnit 5 + Mockito)
- **BDD** com Cucumber + RestAssured em `auth-service`, `catalog-service`, `booking-service`, `search-service` e `api-gateway`
- **Testes de integração** com Testcontainers (PostgreSQL) no `auth-service`
- **Cobertura** configurada via JaCoCo em `booking-service` e `catalog-service`
- **Infraestrutura** dockerizada (PostgreSQL, Elasticsearch, RabbitMQ)

O que **não existe** hoje é a orquestração automática desses testes num pipeline de CI, nem a análise estática de código e os testes de carga. Esta RFC propõe a criação do arquivo `.github/workflows/ci.yml` para resolver isso.

---

## 2. Decisões de Design

### 2.1 Tecnologias escolhidas

| Necessidade | Ferramenta | Justificativa |
|-------------|-----------|---------------|
| CI Runner | GitHub Actions | Gratuito para repos públicos, integrado ao GitHub |
| Análise estática | [Checkstyle](https://checkstyle.org/) + [SpotBugs](https://spotbugs.github.io/) | Zero custo, plug-in Maven, sem servidor externo necessário |
| Cobertura | JaCoCo (já configurado) | Já existe no `booking-service` e `catalog-service`; estender aos demais |
| Testes de carga | [k6](https://k6.io/) | Gratuito, CLI simples, scripts em JS, roda bem no GitHub Actions |
| Services no CI | `services:` do GitHub Actions (Docker) | Provisiona PostgreSQL, RabbitMQ e Elasticsearch nativamente |

### 2.2 Estratégia de jobs

O pipeline é dividido em **5 jobs sequenciais/paralelos** que formam um gate de qualidade:

```
[build] → [unit-tests + static-analysis] → [bdd-integration] → [load-tests] → [docker-build-check]
              (paralelos entre si)
```

---

## 3. Estrutura do Pipeline

### Arquivo: `.github/workflows/ci.yml`

```yaml
name: CI — Booking Hub

on:
  push:
    branches: ["main", "develop", "feature/**"]
  pull_request:
    branches: ["main", "develop"]

env:
  JAVA_VERSION: "21"
  MAVEN_OPTS: "-Xmx2g -XX:+TieredCompilation -XX:TieredStopAtLevel=1"

jobs:

  # ============================================================
  # JOB 1 — BUILD E COMPILAÇÃO
  # ============================================================
  build:
    name: "1. Build & Compile"
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Setup Java ${{ env.JAVA_VERSION }}
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: temurin
          cache: maven

      - name: Compile all modules
        run: mvn -B compile --no-transfer-progress

      - name: Cache compiled classes
        uses: actions/cache/save@v4
        with:
          path: |
            **/target/classes
            ~/.m2/repository
          key: build-${{ github.sha }}

  # ============================================================
  # JOB 2 — TESTES UNITÁRIOS + COBERTURA (alínea a)
  # Roda em paralelo com Job 3
  # ============================================================
  unit-tests:
    name: "2. Unit Tests & Coverage (JaCoCo)"
    runs-on: ubuntu-latest
    needs: build

    steps:
      - uses: actions/checkout@v4

      - name: Setup Java ${{ env.JAVA_VERSION }}
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: temurin
          cache: maven

      # Exclui os testes de integração/BDD para rodar apenas unitários
      - name: Run unit tests
        run: |
          mvn -B test \
            -Dexclude="**/*IT.java,**/CucumberTest.java" \
            --no-transfer-progress

      - name: Generate JaCoCo coverage report
        run: mvn -B jacoco:report --no-transfer-progress

      - name: Upload coverage reports
        uses: actions/upload-artifact@v4
        with:
          name: jacoco-reports
          path: "**/target/site/jacoco/"
          retention-days: 7

      # Gate de cobertura — falha se cobertura global < 70%
      - name: Enforce minimum coverage (70%)
        run: |
          mvn -B jacoco:check \
            -Djacoco.haltOnFailure=true \
            -Djacoco.minimum.instruction.coveredRatio=0.70 \
            --no-transfer-progress

  # ============================================================
  # JOB 3 — ANÁLISE ESTÁTICA DE CÓDIGO (alínea b)
  # Roda em paralelo com Job 2
  # ============================================================
  static-analysis:
    name: "3. Static Analysis (Checkstyle + SpotBugs)"
    runs-on: ubuntu-latest
    needs: build

    steps:
      - uses: actions/checkout@v4

      - name: Setup Java ${{ env.JAVA_VERSION }}
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: temurin
          cache: maven

      - name: Run Checkstyle
        run: mvn -B checkstyle:check --no-transfer-progress

      - name: Run SpotBugs
        run: mvn -B spotbugs:check --no-transfer-progress

      - name: Upload SpotBugs report
        if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: spotbugs-report
          path: "**/target/spotbugsXml.xml"

  # ============================================================
  # JOB 4 — TESTES DE INTEGRAÇÃO E BDD (alíneas b e c)
  # Requer serviços de infraestrutura (PostgreSQL, RabbitMQ, ES)
  # ============================================================
  bdd-integration:
    name: "4. Integration & BDD Tests (Cucumber + Testcontainers)"
    runs-on: ubuntu-latest
    needs: [unit-tests, static-analysis]

    services:
      postgres:
        image: postgres:16-alpine
        env:
          POSTGRES_USER: admin
          POSTGRES_PASSWORD: admin123
          POSTGRES_DB: test_db
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 5s
          --health-timeout 5s
          --health-retries 5

      rabbitmq:
        image: rabbitmq:3-management-alpine
        env:
          RABBITMQ_DEFAULT_USER: guest
          RABBITMQ_DEFAULT_PASS: guest
        ports:
          - 5672:5672
        options: >-
          --health-cmd "rabbitmq-diagnostics check_running"
          --health-interval 15s
          --health-timeout 10s
          --health-retries 5

      elasticsearch:
        image: elasticsearch:8.13.0
        env:
          discovery.type: single-node
          xpack.security.enabled: false
          ES_JAVA_OPTS: "-Xms512m -Xmx512m"
        ports:
          - 9200:9200
        options: >-
          --health-cmd "curl -sf http://localhost:9200/_cluster/health?wait_for_status=yellow&timeout=5s"
          --health-interval 10s
          --health-timeout 10s
          --health-retries 10

    steps:
      - uses: actions/checkout@v4

      - name: Setup Java ${{ env.JAVA_VERSION }}
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: temurin
          cache: maven

      # Testes de integração com banco real via Testcontainers
      # (auth-service usa Testcontainers internamente, ignora o service acima)
      - name: Run integration tests (IT suffix)
        run: |
          mvn -B test \
            -Dtest="**/*IT.java" \
            -DfailIfNoTests=false \
            --no-transfer-progress
        env:
          SPRING_PROFILES_ACTIVE: test
          DB_HOST: localhost
          DB_PORT: 5432
          DB_USER: admin
          DB_PASS: admin123
          RABBIT_HOST: localhost
          ELASTICSEARCH_HOST: localhost
          ELASTICSEARCH_PORT: 9200

      # Testes BDD com Cucumber
      - name: Run BDD tests (Cucumber)
        run: |
          mvn -B test \
            -Dtest="**/CucumberTest.java,**/StepDefinitions.java" \
            -DfailIfNoTests=false \
            --no-transfer-progress
        env:
          SPRING_PROFILES_ACTIVE: test
          DB_HOST: localhost
          DB_PORT: 5432
          DB_USER: admin
          DB_PASS: admin123
          RABBIT_HOST: localhost

      - name: Upload Cucumber HTML report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: cucumber-reports
          path: "**/target/cucumber-reports/"
          retention-days: 7

  # ============================================================
  # JOB 5 — TESTES DE CARGA E PERFORMANCE (alínea d)
  # Executado apenas em push para main/develop
  # ============================================================
  load-tests:
    name: "5. Load & Performance Tests (k6)"
    runs-on: ubuntu-latest
    needs: bdd-integration
    if: github.event_name == 'push' && (github.ref == 'refs/heads/main' || github.ref == 'refs/heads/develop')

    steps:
      - uses: actions/checkout@v4

      - name: Install k6
        run: |
          sudo gpg -k
          sudo gpg --no-default-keyring \
            --keyring /usr/share/keyrings/k6-archive-keyring.gpg \
            --keyserver hkp://keyserver.ubuntu.com:80 \
            --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
          echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" \
            | sudo tee /etc/apt/sources.list.d/k6.list
          sudo apt-get update && sudo apt-get install k6 -y

      - name: Setup Java ${{ env.JAVA_VERSION }}
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: temurin
          cache: maven

      # Sobe apenas o serviço alvo dos testes de carga
      - name: Build and start booking-service for load test
        run: |
          mvn -B package -pl booking-service -am \
            -DskipTests --no-transfer-progress
          # Inicia em background com H2 em memória (profile test)
          java -jar booking-service/target/*.jar \
            --spring.profiles.active=test &
          echo "Aguardando serviço subir..."
          timeout 60 bash -c 'until curl -sf http://localhost:8082/actuator/health; do sleep 2; done'

      - name: Run k6 load test — booking endpoint
        run: k6 run infra/load-tests/booking-load-test.js

      - name: Upload k6 results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: k6-load-test-results
          path: infra/load-tests/results/
          retention-days: 7

  # ============================================================
  # JOB 6 — VALIDAÇÃO DO BUILD DOCKER (alínea c — entregável)
  # Roda apenas em PR para main
  # ============================================================
  docker-build-check:
    name: "6. Docker Build Validation"
    runs-on: ubuntu-latest
    needs: bdd-integration
    if: github.event_name == 'pull_request' && github.base_ref == 'main'

    strategy:
      matrix:
        service: [auth-service, catalog-service, booking-service, search-service, api-gateway]

    steps:
      - uses: actions/checkout@v4

      - name: Build Docker image — ${{ matrix.service }}
        run: |
          docker build \
            -f ${{ matrix.service }}/Dockerfile \
            -t bookinghub/${{ matrix.service }}:ci-check \
            .
```

---

## 4. Etapas do Pipeline — Visão Detalhada

### Etapa 1 — Build & Compile
- **Trigger:** todo push/PR
- **O que faz:** compila todos os 5 módulos do Maven multi-module; falha rápido antes de gastar tempo com testes
- **Gate:** build quebrado = pipeline para; nenhum job subsequente roda

### Etapa 2 — Unit Tests & Coverage
- **Trigger:** após build OK
- **O que faz:**
  - Executa todos os testes unitários (`@Test` puro, sem `@SpringBootTest` pesado)
  - Gera relatórios JaCoCo por módulo
  - Aplica **gate de cobertura mínima de 70%** de instruções — falha o build se não atingir
- **Artefatos publicados:** relatórios HTML do JaCoCo em `Actions > Artifacts`
- **Cobre:** alínea **a** (TDD + cobertura)

### Etapa 3 — Static Analysis
- **Trigger:** após build OK (paralelo à etapa 2)
- **O que faz:**
  - **Checkstyle:** valida formatação e convenções de código Java (regras Google Style ou Sun)
  - **SpotBugs:** detecta bug patterns estáticos (null pointers, recursos não fechados, etc.)
- **Cobre:** alínea **b** (análise de código estático)

### Etapa 4 — Integration & BDD Tests
- **Trigger:** após etapas 2 e 3 passarem
- **O que faz:**
  - Provisiona PostgreSQL, RabbitMQ e Elasticsearch como services Docker nativos do GitHub Actions
  - Executa testes `*IT.java` (integração com banco real, incluindo Testcontainers do `auth-service`)
  - Executa `CucumberTest.java` de todos os módulos (cenários BDD escritos em Gherkin)
  - Publica relatório HTML do Cucumber como artefato
- **Cobre:** alíneas **b** (integração entre módulos) e **c** (BDD + CI)

### Etapa 5 — Load & Performance Tests
- **Trigger:** push em `main` ou `develop` (não em PRs — economiza minutos de CI)
- **O que faz:**
  - Instala k6
  - Sobe o `booking-service` com profile de test (H2 em memória)
  - Executa script k6 (`infra/load-tests/booking-load-test.js`) com carga simulada
  - Publica resultados como artefato
- **Cobre:** alínea **d** (testes não-funcionais de performance e carga)

### Etapa 6 — Docker Build Validation
- **Trigger:** apenas em PRs para `main`
- **O que faz:** constrói as imagens Docker dos 5 serviços em paralelo (matrix) sem publicar, apenas para garantir que os Dockerfiles continuam funcionais
- **Cobre:** suporte ao item 3 (Deploy) do Tech Challenge

---

## 5. Artefatos pendentes a criar

Para o pipeline funcionar completamente, além do `ci.yml`, precisam ser criados:

| Arquivo | Descrição |
|---------|-----------|
| `infra/load-tests/booking-load-test.js` | Script k6 com cenário de carga no endpoint de criação de booking |
| `checkstyle.xml` | Regras do Checkstyle (pode usar Google Java Style pré-definido) |
| Configuração `spotbugs-maven-plugin` no `pom.xml` pai | Plugin SpotBugs adicionado ao `pluginManagement` |
| Configuração `checkstyle-maven-plugin` no `pom.xml` pai | Plugin Checkstyle adicionado ao `pluginManagement` |
| `jacoco` no `pom.xml` pai | Mover o JaCoCo do `booking-service`/`catalog-service` para o pai, aplicando a todos os módulos |

---

## 6. Exemplo de Script k6 (esboço)

```javascript
// infra/load-tests/booking-load-test.js
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 20 },  // ramp-up
    { duration: '1m',  target: 50 },  // carga sustentada
    { duration: '30s', target: 0 },   // ramp-down
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% das respostas < 500ms
    http_req_failed:   ['rate<0.01'], // menos de 1% de falhas
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8082';

export default function () {
  const payload = JSON.stringify({
    professionalId: '00000000-0000-0000-0000-000000000001',
    serviceId:      '00000000-0000-0000-0000-000000000002',
    clientId:       '00000000-0000-0000-0000-000000000003',
    scheduledAt:    '2026-06-01T10:00:00',
  });

  const params = { headers: { 'Content-Type': 'application/json' } };
  const res = http.post(`${BASE_URL}/bookings`, payload, params);

  check(res, {
    'status is 201 or 409': (r) => r.status === 201 || r.status === 409,
    'response time OK':     (r) => r.timings.duration < 500,
  });

  sleep(1);
}
```

---

## 7. Configurações a adicionar no `pom.xml` pai

```xml
<!-- No <pluginManagement> do pom.xml raiz -->

<!-- Checkstyle -->
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-checkstyle-plugin</artifactId>
  <version>3.3.1</version>
  <configuration>
    <configLocation>google_checks.xml</configLocation>
    <failsOnError>true</failsOnError>
    <consoleOutput>true</consoleOutput>
    <excludes>**/target/**,**/dto/**</excludes>
  </configuration>
</plugin>

<!-- SpotBugs -->
<plugin>
  <groupId>com.github.spotbugs</groupId>
  <artifactId>spotbugs-maven-plugin</artifactId>
  <version>4.8.3.1</version>
  <configuration>
    <effort>Max</effort>
    <threshold>Low</threshold>
    <failOnError>true</failOnError>
  </configuration>
</plugin>

<!-- JaCoCo (mover aqui do booking/catalog) -->
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <version>0.8.11</version>
  <executions>
    <execution><goals><goal>prepare-agent</goal></goals></execution>
    <execution>
      <id>report</id>
      <phase>test</phase>
      <goals><goal>report</goal></goals>
    </execution>
    <execution>
      <id>check</id>
      <goals><goal>check</goal></goals>
      <configuration>
        <rules>
          <rule>
            <element>BUNDLE</element>
            <limits>
              <limit>
                <counter>INSTRUCTION</counter>
                <value>COVEREDRATIO</value>
                <minimum>0.70</minimum>
              </limit>
            </limits>
          </rule>
        </rules>
      </configuration>
    </execution>
  </executions>
</plugin>
```

---

## 8. Badges para o README

Após a criação do workflow, adicionar ao `README.md`:

```markdown
[![CI](https://github.com/<org>/booking-hub/actions/workflows/ci.yml/badge.svg)](https://github.com/<org>/booking-hub/actions/workflows/ci.yml)
```

---

## 9. Trade-offs e Limitações

| Trade-off | Decisão |
|-----------|---------|
| SonarCloud vs Checkstyle+SpotBugs | Optamos por ferramentas locais (zero custo, zero configuração de servidor). SonarCloud pode ser adicionado depois com token. |
| Testes de carga só em `main`/`develop` | Evita consumir minutos de CI em feature branches; o gate de qualidade real já é o BDD. |
| Testcontainers vs services do GitHub | O `auth-service` já usa Testcontainers, os demais usam os services nativos do Actions — ambas as abordagens coexistem. |
| H2 no load test | O `booking-service` usa H2 no profile `test` para subir rapidamente no CI sem provisionar Postgres. |

---

## 10. Próximos Passos

1. Criar `.github/workflows/ci.yml` com o conteúdo desta RFC
2. Adicionar `checkstyle-maven-plugin` e `spotbugs-maven-plugin` ao `pom.xml` pai
3. Mover configuração do JaCoCo para o `pom.xml` pai (cobrir todos os módulos)
4. Criar `infra/load-tests/booking-load-test.js`
5. Criar `checkstyle.xml` (ou referenciar o `google_checks.xml` built-in)
6. Validar pipeline em branch de feature antes de mergear na `main`
