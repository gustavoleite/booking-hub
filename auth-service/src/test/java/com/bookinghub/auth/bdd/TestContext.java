package com.bookinghub.auth.bdd;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

import io.restassured.response.Response;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(SCOPE_CUCUMBER_GLUE)
public class TestContext {
    private Response response;

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }
}
