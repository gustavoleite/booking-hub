package com.bookinghub.auth.bdd;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

import com.bookinghub.auth.application.dto.LoginRequestDTO;
import com.bookinghub.auth.application.dto.RegisterRequestDTO;
import com.bookinghub.auth.core.domain.Role;
import com.bookinghub.auth.infrastructure.adapters.out.database.JpaUserRepository;
import com.bookinghub.auth.infrastructure.adapters.out.database.UserEntity;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class LoginSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private JpaUserRepository jpaUserRepository;

    @Autowired
    private TestContext testContext;

    @Dado("que existe um usuario com email {string} e senha {string}")
    public void que_existe_um_usuario_com_email_e_senha(String email, String password) {
        RestAssured.port = port;

        RegisterRequestDTO registerRequest = new RegisterRequestDTO(email, password, Role.ROLE_CLIENT);

        given()
                .contentType(ContentType.JSON)
                .body(registerRequest)
                .when()
                .post("/register")
                .then()
                .statusCode(201);
    }

    @Dado("que existe um usuario inativo com email {string} e senha {string}")
    public void que_existe_um_usuario_inativo_com_email_e_senha(String email, String password) {
        // Primeiro cria o usuário normalmente
        que_existe_um_usuario_com_email_e_senha(email, password);

        // Desativa no banco
        UserEntity user = jpaUserRepository.findByEmail(email).orElseThrow();
        user.setActive(false);
        jpaUserRepository.save(user);
    }

    @Quando("eu envio uma requisicao POST para {string} com email {string} e senha {string}")
    public void eu_envio_uma_requisicao_post_para_com_email_e_senha(String path, String email, String password) {
        LoginRequestDTO loginRequest = new LoginRequestDTO(email, password);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post(path);

        testContext.setResponse(response);
    }

    @Entao("o status da resposta deve ser {int} OK")
    public void o_status_da_resposta_deve_ser_ok(Integer statusCode) {
        testContext.getResponse().then().statusCode(statusCode);
    }

    @Entao("o status da resposta deve ser {int} UNAUTHORIZED")
    public void o_status_da_resposta_deve_ser_unauthorized(Integer statusCode) {
        testContext.getResponse().then().statusCode(statusCode);
    }

    @Entao("o status da resposta deve ser {int} FORBIDDEN")
    public void o_status_da_resposta_deve_ser_forbidden(Integer statusCode) {
        testContext.getResponse().then().statusCode(statusCode);
    }

    @Entao("o status da resposta deve ser {int} BAD REQUEST")
    public void o_status_da_resposta_deve_ser_bad_request(Integer statusCode) {
        testContext.getResponse().then().statusCode(statusCode);
    }

    @Entao("o status da resposta deve ser {int} CREATED")
    public void o_status_da_resposta_deve_ser_created(Integer statusCode) {
        testContext.getResponse().then().statusCode(statusCode);
    }

    @Entao("o status da resposta deve ser {int} CONFLICT")
    public void o_status_da_resposta_deve_ser_conflict(Integer statusCode) {
        testContext.getResponse().then().statusCode(statusCode);
    }

    @Entao("o corpo da resposta deve conter o titulo {string}")
    public void o_corpo_da_resposta_deve_conter_o_titulo(String title) {
        testContext.getResponse().then().body("title", org.hamcrest.Matchers.equalTo(title));
    }

    @Entao("o corpo da resposta deve conter um {string} valido")
    public void o_corpo_da_resposta_deve_conter_um_valido(String field) {
        testContext.getResponse().then().body(field, notNullValue());
    }
}
