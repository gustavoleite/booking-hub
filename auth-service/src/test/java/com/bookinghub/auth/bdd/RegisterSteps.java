package com.bookinghub.auth.bdd;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class RegisterSteps {

    @LocalServerPort
    private int port;

    private Response response;

    @Dado("que o e-mail {string} já está cadastrado no banco de dados")
    public void que_o_email_ja_esta_cadastrado_no_banco_de_dados(String email) {
        RestAssured.port = port;
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", "SenhaForte123!");
        body.put("role", "ROLE_CLIENT");

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/register");
    }

    @Quando("eu envio uma requisição POST para {string} com este e-mail")
    public void eu_envio_uma_requisicao_post_para_com_este_email(String path) {
        RestAssured.port = port;
        Map<String, Object> body = new HashMap<>();
        body.put("email", "cliente@teste.com");
        body.put("password", "SenhaForte123!");
        body.put("role", "ROLE_CLIENT");

        response = given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post(path);
    }

    @Quando("eu envio uma requisição POST para {string} com a senha {string}")
    public void eu_envio_uma_requisicao_post_para_com_a_senha(String path, String password) {
        RestAssured.port = port;
        Map<String, Object> body = new HashMap<>();
        body.put("email", "novo@teste.com");
        body.put("password", password);
        body.put("role", "ROLE_CLIENT");

        response = given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post(path);
    }

    @Quando("eu envio uma requisição POST para {string} com a role {string}")
    public void eu_envio_uma_requisicao_post_para_com_a_role(String path, String role) {
        RestAssured.port = port;
        Map<String, Object> body = new HashMap<>();
        body.put("email", "hacker@teste.com");
        body.put("password", "SenhaForte123!");
        body.put("role", role);

        response = given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post(path);
    }

    @Entao("o status da resposta deve ser {int} CONFLICT")
    public void o_status_da_resposta_deve_ser_conflict(Integer statusCode) {
        response.then().statusCode(statusCode);
    }

    @Entao("o status da resposta deve ser {int} BAD REQUEST")
    public void o_status_da_resposta_deve_ser_bad_request(Integer statusCode) {
        response.then().statusCode(statusCode);
    }

    @Entao("o corpo da resposta deve conter o título {string}")
    public void o_corpo_da_resposta_deve_conter_o_titulo(String title) {
        response.then().body("title", equalTo(title));
    }

    @Entao("o corpo da resposta deve informar que a senha deve conter no mínimo 8 caracteres")
    public void o_corpo_da_resposta_deve_informar_senha_curta() {
        response.then().body("detail", containsString("mínimo 8 caracteres"));
    }

    @Entao("o corpo da resposta deve informar os valores permitidos")
    public void o_corpo_da_resposta_deve_informar_roles_permitidas() {
        // Se cair na nossa exceção do UseCase, a mensagem é customizada.
        // Se cair no Jackson (ROLE_HACKER não é Role), pode ser outra mensagem.
        // Mas a RFC pede "O corpo da resposta deve informar os valores permitidos".
        response.then().body("detail", containsString("Valores permitidos"));
    }
}
