package com.bookinghub.search.bdd;

import com.bookinghub.search.core.domain.EstablishmentDocument;
import com.bookinghub.search.core.ports.EstablishmentSearchRepository;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class StepDefinitions {

    @LocalServerPort
    private int port;

    @Autowired
    private EstablishmentSearchRepository searchRepository;

    private Response lastResponse;

    @Before
    public void setup() {
        RestAssured.port = port;
    }

    // ─── Given ───────────────────────────────────────────────────────────────

    @Given("um estabelecimento {string} em {string} está indexado")
    public void estabelecimentoIndexado(String name, String city) {
        var doc = EstablishmentDocument.builder()
                .id("test-" + name.toLowerCase().replace(" ", "-"))
                .name(name).city(city).state("SP")
                .lat(-23.55).lon(-46.63)
                .services(Collections.emptyList())
                .professionals(Collections.emptyList())
                .totalReviews(0)
                .build();
        searchRepository.upsert(doc);
        sleepForRefresh();
    }

    @Given("um estabelecimento indexado em lat {double} lon {double}")
    public void estabelecimentoIndexadoComGeo(double lat, double lon) {
        var doc = EstablishmentDocument.builder()
                .id("test-geo")
                .name("GeoEstab").city("SaoPaulo").state("SP")
                .lat(lat).lon(lon)
                .services(Collections.emptyList())
                .professionals(Collections.emptyList())
                .totalReviews(0)
                .build();
        searchRepository.upsert(doc);
        sleepForRefresh();
    }

    @Given("dois estabelecimentos indexados com ratings {double} e {double}")
    public void doisEstabelecimentosComRatings(double rating1, double rating2) {
        var doc1 = EstablishmentDocument.builder()
                .id("rating-low").name("LowRating").city("SP").state("SP")
                .averageRating(rating1).totalReviews(1)
                .services(Collections.emptyList()).professionals(Collections.emptyList()).build();
        var doc2 = EstablishmentDocument.builder()
                .id("rating-high").name("HighRating").city("SP").state("SP")
                .averageRating(rating2).totalReviews(1)
                .services(Collections.emptyList()).professionals(Collections.emptyList()).build();
        searchRepository.upsert(doc1);
        searchRepository.upsert(doc2);
        sleepForRefresh();
    }

    @Given("o profissional {string} está afiliado ao {string}")
    public void profissionalAfiliadoAoEstabelecimento(String profName, String estabName) {
        var prof = EstablishmentDocument.ProfessionalEntry.builder()
                .professionalId("prof-1").name(profName)
                .specialties(Collections.emptyList()).build();
        var doc = EstablishmentDocument.builder()
                .id("test-" + estabName.toLowerCase().replace(" ", "-"))
                .name(estabName).city("SP").state("SP")
                .lat(-23.55).lon(-46.63)
                .services(Collections.emptyList())
                .professionals(List.of(prof))
                .totalReviews(0)
                .build();
        searchRepository.upsert(doc);
        sleepForRefresh();
    }

    @Given("um estabelecimento indexado com preço mínimo {double} e máximo {double}")
    public void estabelecimentoComPreco(double minPrice, double maxPrice) {
        String id = "price-" + (int) minPrice;
        var doc = EstablishmentDocument.builder()
                .id(id).name("Estab Preco " + (int) minPrice)
                .city("SP").state("SP")
                .minPrice(minPrice).maxPrice(maxPrice)
                .services(Collections.emptyList()).professionals(Collections.emptyList())
                .totalReviews(0).build();
        searchRepository.upsert(doc);
        sleepForRefresh();
    }

    @Given("um estabelecimento {string} em {string} com serviço {string} está indexado")
    public void estabelecimentoComServico(String name, String city, String serviceTitle) {
        var service = EstablishmentDocument.ServiceEntry.builder()
                .serviceId("svc-1").title(serviceTitle)
                .minPrice(50.0).maxPrice(80.0).build();
        var doc = EstablishmentDocument.builder()
                .id("test-svc-" + name.toLowerCase().replace(" ", "-"))
                .name(name).city(city).state("SP")
                .lat(-23.55).lon(-46.63)
                .services(List.of(service))
                .professionals(Collections.emptyList())
                .minPrice(50.0).maxPrice(80.0)
                .totalReviews(0).build();
        searchRepository.upsert(doc);
        sleepForRefresh();
    }

    @Given("{int} estabelecimentos indexados no estado {string}")
    public void variosEstabelecimentosNoEstado(int count, String state) {
        IntStream.range(0, count).forEach(i -> {
            var doc = EstablishmentDocument.builder()
                    .id("pag-" + state + "-" + i)
                    .name("Estab " + i).city("Cidade").state(state)
                    .services(Collections.emptyList()).professionals(Collections.emptyList())
                    .totalReviews(0).build();
            searchRepository.upsert(doc);
        });
        sleepForRefresh();
    }

    // ─── When ─────────────────────────────────────────────────────────────────

    @When("uma query GraphQL busca estabelecimentos com city {string}")
    public void buscaPorCidade(String city) {
        lastResponse = postGraphQL(
                "{ searchEstablishments(filter: { city: \"%s\" }) { results { id name } totalHits } }"
                        .formatted(city));
    }

    @When("uma query GraphQL filtra por minRating {double}")
    public void filtraPorRating(double minRating) {
        lastResponse = postGraphQL(
                "{ searchEstablishments(filter: { minRating: %s }) { results { id name averageRating } totalHits } }"
                        .formatted(minRating));
    }

    @When("uma query busca establishments com query {string}")
    public void buscaPorTexto(String queryText) {
        lastResponse = postGraphQL(
                "{ searchEstablishments(filter: { query: \"%s\" }) { results { id name professionals { name } } totalHits } }"
                        .formatted(queryText));
    }

    @When("uma query GraphQL busca por texto {string}")
    public void buscaPorTextoLivre(String queryText) {
        lastResponse = postGraphQL(
                "{ searchEstablishments(filter: { query: \"%s\" }) { results { id name } totalHits } }"
                        .formatted(queryText));
    }

    @When("uma query GraphQL filtra por maxPrice {double}")
    public void filtraPorPrecoMaximo(double maxPrice) {
        lastResponse = postGraphQL(
                "{ searchEstablishments(filter: { maxPrice: %s }) { results { id name minPrice maxPrice } totalHits } }"
                        .formatted(maxPrice));
    }

    @When("uma query GraphQL busca por geo lat {double} lon {double} raio {double}")
    public void buscaPorGeo(double lat, double lon, double radius) {
        lastResponse = postGraphQL(
                "{ searchEstablishments(filter: { geo: { lat: %s, lon: %s, radiusKm: %s } sortBy: DISTANCE }) { results { id name distanceKm } totalHits } }"
                        .formatted(lat, lon, radius));
    }

    @When("uma query GraphQL filtra por serviço {string}")
    public void filtraPorServico(String service) {
        lastResponse = postGraphQL(
                "{ searchEstablishments(filter: { services: [\"%s\"] }) { results { id name services { title } } totalHits } }"
                        .formatted(service));
    }

    @When("uma query GraphQL busca no estado {string} com page {int} e size {int}")
    public void buscaComPaginacao(String state, int page, int size) {
        lastResponse = postGraphQL(
                "{ searchEstablishments(filter: { state: \"%s\" } page: { page: %d, size: %d }) { results { id name } totalHits page size } }"
                        .formatted(state, page, size));
    }

    // ─── Then / And ───────────────────────────────────────────────────────────

    @Then("a resposta contém ao menos {int} resultado")
    public void respostaContemResultados(int minCount) {
        lastResponse.then().statusCode(200);
        int totalHits = lastResponse.jsonPath().getInt("data.searchEstablishments.totalHits");
        assertThat(totalHits).isGreaterThanOrEqualTo(minCount);
    }

    @And("o resultado inclui o nome {string}")
    public void resultadoIncluiNome(String name) {
        List<String> names = lastResponse.jsonPath().getList("data.searchEstablishments.results.name");
        assertThat(names).contains(name);
    }

    @Then("apenas o estabelecimento com rating {double} aparece nos resultados")
    public void apenasEstabelecimentoComRating(double rating) {
        lastResponse.then().statusCode(200);
        List<Number> ratings = lastResponse.jsonPath().getList("data.searchEstablishments.results.averageRating");
        assertThat(ratings).allMatch(r -> r.doubleValue() >= rating);
    }

    @Then("o {string} aparece nos resultados")
    public void estabelecimentoApareceNosResultados(String name) {
        lastResponse.then().statusCode(200);
        List<String> names = lastResponse.jsonPath().getList("data.searchEstablishments.results.name");
        assertThat(names).contains(name);
    }

    @Then("o resultado não inclui estabelecimentos com minPrice acima de {double}")
    public void resultadoNaoIncluiPrecosAcima(double maxAllowed) {
        lastResponse.then().statusCode(200);
        List<Number> prices = lastResponse.jsonPath().getList("data.searchEstablishments.results.minPrice");
        if (prices != null) {
            assertThat(prices).allMatch(p -> p == null || p.doubleValue() <= maxAllowed);
        }
    }

    @Then("a resposta retorna {int} resultados na página")
    public void respostaRetornaResultadosNaPagina(int expectedSize) {
        lastResponse.then().statusCode(200);
        List<?> results = lastResponse.jsonPath().getList("data.searchEstablishments.results");
        assertThat(results).hasSize(expectedSize);
    }

    @And("totalHits é ao menos {int}")
    public void totalHitsEhAoMenos(int min) {
        int totalHits = lastResponse.jsonPath().getInt("data.searchEstablishments.totalHits");
        assertThat(totalHits).isGreaterThanOrEqualTo(min);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Response postGraphQL(String query) {
        String body = "{\"query\":\"%s\"}".formatted(query.replace("\"", "\\\""));
        return RestAssured.given()
                .contentType("application/json")
                .body(body)
                .post("/graphql");
    }

    private void sleepForRefresh() {
        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
