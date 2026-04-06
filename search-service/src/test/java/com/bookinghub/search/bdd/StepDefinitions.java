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

    @Given("um estabelecimento {string} em {string} está indexado")
    public void estabelecimentoIndexado(String name, String city) {
        var doc = EstablishmentDocument.builder()
                .id("test-" + name.toLowerCase().replace(" ", "-"))
                .name(name)
                .city(city)
                .state("SP")
                .lat(-23.55)
                .lon(-46.63)
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
                .name("GeoEstab")
                .city("SaoPaulo")
                .state("SP")
                .lat(lat)
                .lon(lon)
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
                .professionalId("prof-1")
                .name(profName)
                .specialties(Collections.emptyList())
                .build();
        var doc = EstablishmentDocument.builder()
                .id("test-" + estabName.toLowerCase().replace(" ", "-"))
                .name(estabName)
                .city("SP").state("SP")
                .lat(-23.55).lon(-46.63)
                .services(Collections.emptyList())
                .professionals(List.of(prof))
                .totalReviews(0)
                .build();
        searchRepository.upsert(doc);
        sleepForRefresh();
    }

    @When("uma query GraphQL busca estabelecimentos com city {string}")
    public void buscaPorCidade(String city) {
        String query = String.format(
                "{\"query\":\"{searchEstablishments(filter:{city:\\\"%s\\\"}){results{id name}totalHits}}\"}",
                city);
        lastResponse = RestAssured.given()
                .contentType("application/json")
                .body(query)
                .post("/graphql");
    }

    @When("uma query GraphQL filtra por minRating {double}")
    public void filtraPorRating(double minRating) {
        String query = String.format(
                "{\"query\":\"{searchEstablishments(filter:{minRating:%s}){results{id name averageRating}totalHits}}\"}",
                minRating);
        lastResponse = RestAssured.given()
                .contentType("application/json")
                .body(query)
                .post("/graphql");
    }

    @When("uma query busca establishments com query {string}")
    public void buscaPorTexto(String queryText) {
        String query = String.format(
                "{\"query\":\"{searchEstablishments(filter:{query:\\\"%s\\\"}){results{id name professionals{name}}totalHits}}\"}",
                queryText);
        lastResponse = RestAssured.given()
                .contentType("application/json")
                .body(query)
                .post("/graphql");
    }

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
        List<Double> ratings = lastResponse.jsonPath().getList("data.searchEstablishments.results.averageRating");
        assertThat(ratings).allMatch(r -> r >= rating);
    }

    @Then("o {string} aparece nos resultados")
    public void estabelecimentoApareceNosResultados(String name) {
        lastResponse.then().statusCode(200);
        List<String> names = lastResponse.jsonPath().getList("data.searchEstablishments.results.name");
        assertThat(names).contains(name);
    }

    private void sleepForRefresh() {
        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
