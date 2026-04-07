package com.bookinghub.auth.bdd;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "classpath:com/bookinghub/auth/bdd",
        glue = "com.bookinghub.auth.bdd",
        plugin = {"pretty"}
)
public class CucumberTest {
}
