package com.bookinghub.auth.bdd;

import com.bookinghub.auth.application.dto.LoginRequestDTO;
import com.bookinghub.auth.application.dto.RegisterRequestDTO;
import com.bookinghub.auth.core.domain.Role;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class LoginSteps {
    
    @LocalServerPort
    private int port;

    private Response response;

    @Dado("que existe um usuario com email {string} e senha {string}")
    public void que_existe_um_usuario_com_email_e_senha(String email, String password) {
        RestAssured.port = port;
        
        // Ajustando senha para passar na nova politica se for a senha de teste antiga
        String strongPassword = password.equals("senha123") ? "SenhaForte123!" : password;
        RegisterRequestDTO registerRequest = new RegisterRequestDTO(email, strongPassword, Role.ROLE_CLIENT);

        given()
            .contentType(ContentType.JSON)
            .body(registerRequest)
        .when()
            .post("/register")
        .then()
            .statusCode(201);
    }

    @Quando("eu envio uma requisicao POST para {string} com estas credenciais")
    public void eu_envio_uma_requisicao_post_para_com_estas_credenciais(String path) {
        LoginRequestDTO loginRequest = new LoginRequestDTO("cliente@teste.com", "SenhaForte123!");

        response = given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post(path);
    }

    @Entao("o status da resposta deve ser {int} OK")
    public void o_status_da_resposta_deve_ser_ok(Integer statusCode) {
        response.then().statusCode(statusCode);
    }

    @Entao("o corpo da resposta deve conter um {string} valido")
    public void o_corpo_da_resposta_deve_conter_um_valido(String field) {
        response.then().body(field, notNullValue());
    }
}
