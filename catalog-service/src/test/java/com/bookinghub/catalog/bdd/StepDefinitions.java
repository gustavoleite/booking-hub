package com.bookinghub.catalog.bdd;

import com.bookinghub.catalog.core.domain.Address;
import com.bookinghub.catalog.core.domain.BusinessHour;
import com.bookinghub.catalog.core.domain.Establishment;
import com.bookinghub.catalog.core.domain.Professional;
import com.bookinghub.catalog.core.ports.EstablishmentRepository;
import com.bookinghub.catalog.core.ports.ProfessionalRepository;
import com.bookinghub.catalog.infrastructure.adapters.in.rest.dto.EstablishmentRequest;
import com.bookinghub.catalog.infrastructure.adapters.in.rest.dto.ProfessionalProfileRequest;
import com.bookinghub.catalog.infrastructure.adapters.out.database.JpaEstablishmentRepository;
import com.bookinghub.catalog.infrastructure.adapters.out.database.JpaProfessionalRepository;
import com.bookinghub.catalog.infrastructure.adapters.out.database.ProfessionalEntity;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class StepDefinitions {

    @LocalServerPort
    private int port;

    @Autowired
    private EstablishmentRepository establishmentRepository;

    @Autowired
    private JpaEstablishmentRepository jpaEstablishmentRepository;

    @Autowired
    private JpaProfessionalRepository jpaProfessionalRepository;

    private String currentUserId;
    private Response response;

    private void setupRestAssured() {
        RestAssured.port = port;
    }

    @Before
    public void setup() {
        setupRestAssured();
        this.currentUserId = null;
        jpaEstablishmentRepository.deleteAll();
        jpaProfessionalRepository.deleteAll();
        RestAssured.reset();
        setupRestAssured();
    }

    @Dado("que a API está no ar")
    public void apiIsUp() {
        setupRestAssured();
    }

    @Dado("que eu me autentico enviando o header {string} com o valor {string}")
    public void authenticate(String header, String value) {
        setupRestAssured();
        this.currentUserId = value;
    }

    @Quando("eu envio uma requisição POST para {string} com o CNPJ {string} e horários válidos")
    public void createEstablishmentSuccess(String endpoint, String cnpj) {
        EstablishmentRequest request = new EstablishmentRequest();
        request.setName("Salon Test");
        request.setCnpj(cnpj);
        
        EstablishmentRequest.AddressDto address = new EstablishmentRequest.AddressDto();
        address.setStreet("Main St");
        address.setNumber("123");
        address.setZipCode("12345678");
        request.setAddress(address);

        EstablishmentRequest.BusinessHourDto bh = new EstablishmentRequest.BusinessHourDto();
        bh.setDayOfWeek(1);
        bh.setOpenTime(LocalTime.of(9, 0));
        bh.setCloseTime(LocalTime.of(18, 0));
        request.setBusinessHours(List.of(bh));

        EstablishmentRequest.ProvidedServiceDto ps = new EstablishmentRequest.ProvidedServiceDto();
        ps.setTitle("Haircut");
        request.setServices(List.of(ps));

        response = RestAssured.given()
                .header("X-User-Id", currentUserId)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(endpoint);
    }

    @Então("o status da resposta deve ser {int} CREATED")
    public void checkStatusCreated(int status) {
        response.then().statusCode(status);
    }

    @Então("o corpo da resposta deve conter o {string} do salão gerado")
    public void checkIdGenerated(String field) {
        response.then().body(field, notNullValue());
    }

    @Dado("que já existe um salão salvo no banco com o CNPJ {string}")
    public void existingEstablishment(String cnpj) {
        setupRestAssured();
        this.currentUserId = "some-owner-" + UUID.randomUUID();
        EstablishmentRequest request = new EstablishmentRequest();
        request.setName("Existing");
        request.setCnpj(cnpj);
        EstablishmentRequest.AddressDto address = new EstablishmentRequest.AddressDto();
        address.setStreet("Street");
        address.setNumber("1");
        address.setZipCode("12345678");
        request.setAddress(address);
        request.setServices(List.of(new EstablishmentRequest.ProvidedServiceDto() {{ setTitle("Service"); }}));

        RestAssured.given()
                .header("X-User-Id", currentUserId)
                .contentType(ContentType.JSON)
                .body(request)
                .post("/establishments");
    }

    @Quando("eu envio uma requisição POST para {string} com o mesmo CNPJ {string}")
    public void createEstablishmentConflict(String endpoint, String cnpj) {
        EstablishmentRequest request = new EstablishmentRequest();
        request.setName("Salon Test");
        request.setCnpj(cnpj);
        
        EstablishmentRequest.AddressDto address = new EstablishmentRequest.AddressDto();
        address.setStreet("Main St");
        request.setAddress(address);
        
        EstablishmentRequest.ProvidedServiceDto ps = new EstablishmentRequest.ProvidedServiceDto();
        ps.setTitle("Haircut");
        request.setServices(List.of(ps));

        response = RestAssured.given()
                .header("X-User-Id", currentUserId)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(endpoint);
    }

    @Então("o status da resposta deve ser {int} CONFLICT")
    public void checkStatusConflict(int status) {
        response.then().statusCode(status);
    }

    @Então("o corpo da resposta deve informar que houve {string}")
    public void checkErrorTitle(String title) {
        response.then().body("title", containsString(title));
    }

    @Quando("eu envio uma requisição POST para {string} com o horário de abertura {string} e fechamento {string}")
    public void createEstablishmentInvalidHours(String endpoint, String open, String close) {
        EstablishmentRequest request = new EstablishmentRequest();
        request.setName("Salon Test");
        request.setCnpj("12345678901234");
        
        EstablishmentRequest.AddressDto address = new EstablishmentRequest.AddressDto();
        address.setStreet("Main St");
        request.setAddress(address);

        EstablishmentRequest.BusinessHourDto bh = new EstablishmentRequest.BusinessHourDto();
        bh.setDayOfWeek(1);
        bh.setOpenTime(LocalTime.parse(open));
        bh.setCloseTime(LocalTime.parse(close));
        request.setBusinessHours(List.of(bh));

        EstablishmentRequest.ProvidedServiceDto ps = new EstablishmentRequest.ProvidedServiceDto();
        ps.setTitle("Haircut");
        request.setServices(List.of(ps));

        response = RestAssured.given()
                .header("X-User-Id", currentUserId)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(endpoint);
    }

    @Então("o status da resposta deve ser {int} BAD REQUEST")
    public void checkStatusBadRequest(int status) {
        response.then().statusCode(status);
    }

    @Então("o corpo da resposta deve conter a mensagem sobre {string}")
    public void checkErrorMessage(String msg) {
        response.then().body("detail", containsString(msg));
    }

    @Dado("que eu tenho {int} salões cadastrados no meu {string}")
    public void myEstablishments(int count, String header) {
        setupRestAssured();
        this.currentUserId = "owner-unique-" + UUID.randomUUID();
        
        for (int i = 0; i < count; i++) {
            Establishment establishment = Establishment.builder()
                    .id(UUID.randomUUID())
                    .ownerId(currentUserId)
                    .name("My Salon " + i)
                    .cnpj(String.format("%014d", i + 2000 + (int)(Math.random() * 1000)))
                    .address(Address.builder().street("Street").number("1").zipCode("12345").build())
                    .defaultBusinessHours(List.of())
                    .providedServices(List.of())
                    .active(true)
                    .build();
            establishmentRepository.save(establishment);
        }
    }

    @Quando("eu envio uma requisição GET para {string}")
    public void sendGetRequest(String endpoint) {
        setupRestAssured();
        response = RestAssured.given()
                .header("X-User-Id", currentUserId != null ? currentUserId : "")
                .when()
                .get(endpoint);
    }

    @Então("o status da resposta deve ser {int} OK")
    public void checkStatusOK(int status) {
        response.then().log().ifValidationFails().statusCode(status);
    }

    @Então("a lista de resposta deve conter exatamente {int} itens")
    public void checkListSize(int size) {
        response.then().body("$", hasSize(size));
    }

    @Dado("que existe um salão com ID {string} que pertence ao usuário {string}")
    public void otherEstablishment(String id, String owner) {
        setupRestAssured();
        this.currentUserId = "123e4567-e89b-12d3-a456-426614174000";
        
        EstablishmentRequest request = new EstablishmentRequest();
        request.setName("Other Salon");
        request.setCnpj("11122233344455");
        EstablishmentRequest.AddressDto address = new EstablishmentRequest.AddressDto();
        address.setStreet("Street");
        address.setNumber("1");
        address.setZipCode("12345678");
        request.setAddress(address);
        request.setServices(List.of(new EstablishmentRequest.ProvidedServiceDto() {{ setTitle("Service"); }}));

        Response res = RestAssured.given()
                .header("X-User-Id", owner)
                .contentType(ContentType.JSON)
                .body(request)
                .post("/establishments");
        
        String actualId = res.jsonPath().getString("id");
        this.response = null; // Reset for next step
        // We will replace "salao-456" with actualId in the Quando step if it matches
    }

    @Quando("eu envio uma requisição PUT para {string}")
    public void sendPutRequest(String endpoint) {
        setupRestAssured();
        // Simple update data
        EstablishmentRequest request = new EstablishmentRequest();
        request.setName("Updated Name");
        
        // Use a known existing ID from our DB since we can't easily capture it from previous step without a variable
        UUID salaoId = jpaEstablishmentRepository.findAll().stream()
                .filter(e -> !e.getOwnerId().equals(currentUserId))
                .map(e -> e.getId())
                .findFirst().orElse(UUID.randomUUID());

        response = RestAssured.given()
                .header("X-User-Id", currentUserId)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put("/establishments/" + salaoId);
    }

    @Então("o status da resposta deve ser {int} FORBIDDEN")
    public void checkStatusForbidden(int status) {
        response.then().statusCode(status);
    }

    // Professional steps

    @Quando("eu envio uma requisição PUT para {string} informando o nome {string} e especialidade {string}")
    public void upsertProfessionalSuccess(String endpoint, String name, String specialty) {
        setupRestAssured();
        ProfessionalProfileRequest request = new ProfessionalProfileRequest();
        request.setName(name);
        request.setSpecialties(List.of(specialty));

        response = RestAssured.given()
                .header("X-User-Id", currentUserId)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(endpoint);
    }

    @Quando("eu envio uma requisição PUT para {string} informando a bio mas com o nome vazio")
    public void upsertProfessionalNoName(String endpoint) {
        setupRestAssured();
        ProfessionalProfileRequest request = new ProfessionalProfileRequest();
        request.setBio("Some bio");
        request.setName("");

        response = RestAssured.given()
                .header("X-User-Id", currentUserId)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(endpoint);
    }

    @Então("o corpo da resposta deve pedir a obrigatoriedade do nome")
    public void checkNameRequired() {
        response.then().body("detail", containsString("nome do profissional é obrigatório"));
    }

    @Dado("que o profissional {string} possui o nome {string} salvo no banco")
    public void professionalInDb(String id, String name) {
        setupRestAssured();
        this.currentUserId = id;
        ProfessionalProfileRequest request = new ProfessionalProfileRequest();
        request.setName(name);
        request.setBio("Bio");

        RestAssured.given()
                .header("X-User-Id", currentUserId)
                .contentType(ContentType.JSON)
                .body(request)
                .put("/professionals/me");
    }

    @Quando("qualquer usuário envia uma requisição GET para {string} sem enviar header de autenticação")
    public void getProfessionalPublic(String endpoint) {
        setupRestAssured();
        response = RestAssured.given()
                .when()
                .get(endpoint);
    }

    @Então("o corpo da resposta deve conter o nome {string}")
    public void checkNameInBody(String name) {
        response.then().body("name", is(name));
    }

    @Quando("qualquer usuário envia uma requisição GET para {string}")
    public void getProfessionalInexistent(String endpoint) {
        setupRestAssured();
        response = RestAssured.given()
                .log().all()
                .when()
                .get(endpoint);
        response.then().log().all();
    }

    @Então("o status da resposta deve ser {int} NOT FOUND")
    public void checkStatusNotFound(int status) {
        response.then().statusCode(status);
    }
}
