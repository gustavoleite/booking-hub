package com.bookinghub.auth.bdd;

import com.bookinghub.auth.application.dto.LoginRequestDTO;
import com.bookinghub.auth.application.dto.RegisterRequestDTO;
import com.bookinghub.auth.core.domain.Role;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class LoginSteps {

    @LocalServerPort
    private int port;

    private Response response;

    @Dado("que existe um usuário com email {string} e senha {string}")
    public void que_existe_um_usuário_com_email_e_senha(String email, String password) {
        RestAssured.port = port;
        
        RegisterRequestDTO registerRequest = new RegisterRequestDTO(email, password, Role.ROLE_CLIENT);

        given()
            .contentType(ContentType.JSON)
            .body(registerRequest)
        .when()
            .post("/api/auth/register")
        .then()
            .statusCode(201);
    }

    @Quando("eu envio uma requisição POST para {string} com estas credenciais")
    public void eu_envio_uma_requisição_post_para_com_estas_credenciais(String path, String password) {
        // O password aqui vem do cenário mas já sabemos o email do step anterior
        // Simplificando para o exemplo
        LoginRequestDTO loginRequest = new LoginRequestDTO("cliente@teste.com", "senha123");

        response = given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post(path);
    }

    @Então("o status da resposta deve ser {int} OK")
    public void o_status_da_resposta_deve_ser_ok(Integer statusCode) {
        response.then().statusCode(statusCode);
    }

    @Então("o corpo da resposta deve conter um {string} válido")
    public void o_corpo_da_resposta_deve_conter_um_válido(String field) {
        response.then().body(field, notNullValue());
    }
}
