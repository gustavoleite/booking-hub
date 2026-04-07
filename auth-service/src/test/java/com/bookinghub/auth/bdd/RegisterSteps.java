package com.bookinghub.auth.bdd;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

public class RegisterSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestContext testContext;

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

        Response response = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(path);

        testContext.setResponse(response);
    }

    @Quando("eu envio uma requisição POST para {string} com a senha {string}")
    public void eu_envio_uma_requisicao_post_para_com_a_senha(String path, String password) {
        RestAssured.port = port;
        Map<String, Object> body = new HashMap<>();
        body.put("email", "novo@teste.com");
        body.put("password", password);
        body.put("role", "ROLE_CLIENT");

        Response response = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(path);

        testContext.setResponse(response);
    }

    @Quando("eu envio uma requisição POST para {string} com a role {string}")
    public void eu_envio_uma_requisicao_post_para_com_a_role(String path, String role) {
        RestAssured.port = port;
        Map<String, Object> body = new HashMap<>();
        body.put("email", "hacker@teste.com");
        body.put("password", "SenhaForte123!");
        body.put("role", role);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(path);

        testContext.setResponse(response);
    }

    @Quando("eu envio uma requisição POST para {string} com email {string}, senha {string} e role {string}")
    public void eu_envio_uma_requisicao_post_para_com_email_senha_e_role(String path, String email, String password, String role) {
        RestAssured.port = port;
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        body.put("role", role);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(path);

        testContext.setResponse(response);
    }

    @Entao("o corpo da resposta deve conter o id do usuário e o email {string}")
    public void o_corpo_da_resposta_deve_conter_o_id_e_o_email(String email) {
        testContext.getResponse().then()
                .body("id", notNullValue())
                .body("email", equalTo(email));
    }

    @Entao("o corpo da resposta deve informar que a senha é fraca")
    public void o_corpo_da_resposta_deve_informar_senha_fraca() {
        testContext.getResponse().then().body("detail", containsString("mínimo 8 caracteres, uma letra maiúscula e um número"));
    }

    @Entao("o corpo da resposta deve informar os valores permitidos")
    public void o_corpo_da_resposta_deve_informar_roles_permitidas() {
        testContext.getResponse().then().body("detail", containsString("Valores permitidos"));
    }
}
