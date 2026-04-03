package com.bookinghub.booking.bdd;

import com.bookinghub.booking.core.domain.DaySchedule;
import com.bookinghub.booking.core.domain.ScheduleInfo;
import com.bookinghub.booking.core.ports.BookingEventPublisher;
import com.bookinghub.booking.core.ports.CatalogServiceClient;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class StepDefinitions {

    @LocalServerPort
    private int port;

    @MockBean
    private BookingEventPublisher bookingEventPublisher;

    @MockBean
    private CatalogServiceClient catalogServiceClient;

    private Response response;
    private UUID professionalId;
    private UUID establishmentId;
    private UUID serviceId;

    @Before
    public void setUp() {
        RestAssured.port = port;
        professionalId = UUID.randomUUID();
        establishmentId = UUID.randomUUID();
        serviceId = UUID.randomUUID();
    }

    @Given("the professional works on the requested day")
    public void theProfessionalWorksOnTheRequestedDay() {
        // Find next Monday
        LocalDate nextMonday = LocalDate.now().plusDays(1);
        while (nextMonday.getDayOfWeek().getValue() != 1) {
            nextMonday = nextMonday.plusDays(1);
        }
        int dayOfWeek = nextMonday.getDayOfWeek().getValue();
        DaySchedule schedule = new DaySchedule(dayOfWeek, LocalTime.of(9, 0), LocalTime.of(18, 0));
        ScheduleInfo info = new ScheduleInfo(true, new BigDecimal("80.00"), 60, List.of(schedule));
        when(catalogServiceClient.getSchedule(any(), any(), any())).thenReturn(info);
    }

    @When("a client requests available slots")
    public void aClientRequestsAvailableSlots() {
        LocalDate nextMonday = LocalDate.now().plusDays(1);
        while (nextMonday.getDayOfWeek().getValue() != 1) {
            nextMonday = nextMonday.plusDays(1);
        }
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .queryParam("establishmentId", establishmentId.toString())
                .queryParam("professionalId", professionalId.toString())
                .queryParam("serviceId", serviceId.toString())
                .queryParam("date", nextMonday.toString())
                .when()
                .get("/bookings/availability");
    }

    @Then("the response status is {int}")
    public void theResponseStatusIs(int statusCode) {
        response.then().statusCode(statusCode);
    }

    @Then("the response contains available slots")
    public void theResponseContainsAvailableSlots() {
        response.then()
                .body("availableSlots", notNullValue())
                .body("durationMinutes", equalTo(60))
                .body("price", equalTo(80.0F));
    }

    @Given("the professional does not work on the requested day")
    public void theProfessionalDoesNotWorkOnTheRequestedDay() {
        ScheduleInfo info = new ScheduleInfo(true, new BigDecimal("80.00"), 60, List.of());
        when(catalogServiceClient.getSchedule(any(), any(), any())).thenReturn(info);
    }

    @Then("the response contains an empty list of available slots")
    public void theResponseContainsAnEmptyListOfAvailableSlots() {
        response.then().body("availableSlots", hasSize(0));
    }
}
