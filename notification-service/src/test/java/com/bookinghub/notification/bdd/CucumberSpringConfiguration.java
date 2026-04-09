package com.bookinghub.notification.bdd;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest
@ActiveProfiles("test")
public class CucumberSpringConfiguration {

  @MockBean
  ConnectionFactory connectionFactory;
}
