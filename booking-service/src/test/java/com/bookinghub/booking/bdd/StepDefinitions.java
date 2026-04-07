package com.bookinghub.booking.bdd;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.bookinghub.booking.core.domain.DaySchedule;
import com.bookinghub.booking.core.domain.ScheduleInfo;
import com.bookinghub.booking.core.ports.BookingEventPublisher;
import com.bookinghub.booking.core.ports.CatalogServiceClient;
import com.bookinghub.booking.infrastructure.adapters.out.database.JpaBookingRepository;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class StepDefinitions {

    // ── Infrastructure ────────────────────────────────────────────────────────
    @LocalServerPort
    private int port;

    @MockBean
    private BookingEventPublisher bookingEventPublisher;

    @MockBean
    private CatalogServiceClient catalogServiceClient;

    @Autowired
    private JpaBookingRepository jpaBookingRepository;

    // ── Per-scenario state ────────────────────────────────────────────────────
    private UUID professionalId;
    private UUID establishmentId;
    private UUID serviceId;
    private String clientId;
    private UUID bookingId;
    private LocalDate testDate;
    private Response response;

    // ─────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────────────────────────────────────

    @Before
    public void setUp() {
        RestAssured.port = port;

        // Fresh IDs per scenario
        professionalId = UUID.randomUUID();
        establishmentId = UUID.randomUUID();
        serviceId = UUID.randomUUID();
        clientId = "bdd-client-" + UUID.randomUUID().toString().substring(0, 8);
        bookingId = null;
        response = null;

        // Clean H2 between scenarios
        jpaBookingRepository.deleteAll();

        // Reset all mock stubs
        Mockito.reset(catalogServiceClient, bookingEventPublisher);

        // Next weekday (Mon–Fri) at least 1 day ahead
        testDate = LocalDate.now().plusDays(1);
        while (testDate.getDayOfWeek().getValue() > 5) {
            testDate = testDate.plusDays(1);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Catalog / schedule mocks
    // ─────────────────────────────────────────────────────────────────────────

    @Given("the catalog returns an active schedule for the next weekday")
    public void catalogReturnsActiveScheduleForNextWeekday() {
        int dow = testDate.getDayOfWeek().getValue();
        DaySchedule day = new DaySchedule(dow, LocalTime.of(9, 0), LocalTime.of(18, 0));
        ScheduleInfo info = new ScheduleInfo(true, new BigDecimal("80.00"), 60, List.of(day));
        when(catalogServiceClient.getSchedule(any(), any(), any())).thenReturn(info);
    }

    @Given("the catalog returns an active schedule with no days configured")
    public void catalogReturnsActiveScheduleWithNoDays() {
        ScheduleInfo info = new ScheduleInfo(true, new BigDecimal("80.00"), 60, List.of());
        when(catalogServiceClient.getSchedule(any(), any(), any())).thenReturn(info);
    }

    @Given("the catalog returns an inactive schedule")
    public void catalogReturnsInactiveSchedule() {
        int dow = testDate.getDayOfWeek().getValue();
        DaySchedule day = new DaySchedule(dow, LocalTime.of(9, 0), LocalTime.of(18, 0));
        ScheduleInfo info = new ScheduleInfo(false, new BigDecimal("80.00"), 60, List.of(day));
        when(catalogServiceClient.getSchedule(any(), any(), any())).thenReturn(info);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Background helpers – create a booking
    // ─────────────────────────────────────────────────────────────────────────

    @Given("a confirmed booking exists for the client")
    public void aConfirmedBookingExistsForTheClient() {
        response = postBooking(testDate + "T10:00:00", clientId);
        response.then().statusCode(201);
        bookingId = UUID.fromString(response.jsonPath().getString("id"));
    }

    @Given("a booking already exists for the next weekday at 10:00")
    public void aBookingAlreadyExistsForSlot() {
        response = postBooking(testDate + "T10:00:00", clientId);
        response.then().statusCode(201);
        bookingId = UUID.fromString(response.jsonPath().getString("id"));
    }

    @Given("the client has already cancelled the booking")
    public void theClientHasAlreadyCancelledTheBooking() {
        cancelBookingAs(clientId, "ROLE_CLIENT", "setup cancel");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Availability
    // ─────────────────────────────────────────────────────────────────────────

    @When("a client requests availability for the next weekday")
    public void clientRequestsAvailabilityForNextWeekday() {
        response = RestAssured.given()
                .queryParam("establishmentId", establishmentId.toString())
                .queryParam("professionalId", professionalId.toString())
                .queryParam("serviceId", serviceId.toString())
                .queryParam("date", testDate.toString())
                .when()
                .get("/bookings/availability");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Create booking
    // ─────────────────────────────────────────────────────────────────────────

    @When("a client creates a booking for the next weekday at 10:00")
    public void clientCreatesBookingAt10() {
        response = postBooking(testDate + "T10:00:00", clientId);
    }

    @When("a client creates a booking for the next weekday at 20:00")
    public void clientCreatesBookingAt20() {
        response = postBooking(testDate + "T20:00:00", clientId);
    }

    @When("a client creates a booking with a past datetime")
    public void clientCreatesBookingWithPastDatetime() {
        response = postBooking("2020-01-01T10:00:00", clientId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Cancel
    // ─────────────────────────────────────────────────────────────────────────

    @When("the client cancels the booking with reason {string}")
    public void clientCancelsBooking(String reason) {
        response = cancelBookingAs(clientId, "ROLE_CLIENT", reason);
    }

    @When("the owner cancels the booking with reason {string}")
    public void ownerCancelsBooking(String reason) {
        response = cancelBookingAs("owner-id", "ROLE_OWNER", reason);
    }

    @When("another client tries to cancel the booking")
    public void anotherClientTriesToCancelBooking() {
        response = cancelBookingAs("other-client-id", "ROLE_CLIENT", "attempted cancel");
    }

    @When("a professional tries to cancel the booking")
    public void professionalTriesToCancelBooking() {
        response = cancelBookingAs(professionalId.toString(), "ROLE_PROFESSIONAL", "attempted cancel");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Complete
    // ─────────────────────────────────────────────────────────────────────────

    @When("the professional completes the booking")
    @When("the professional tries to complete the booking")
    public void professionalCompletesBooking() {
        response = RestAssured.given()
                .header("X-User-Id", professionalId.toString())
                .header("X-User-Role", "ROLE_PROFESSIONAL")
                .when()
                .patch("/bookings/" + bookingId + "/complete");
    }

    @When("the owner completes the booking")
    public void ownerCompletesBooking() {
        response = RestAssured.given()
                .header("X-User-Id", "owner-id")
                .header("X-User-Role", "ROLE_OWNER")
                .when()
                .patch("/bookings/" + bookingId + "/complete");
    }

    @When("the client tries to complete the booking")
    public void clientTriesToCompleteBooking() {
        response = RestAssured.given()
                .header("X-User-Id", clientId)
                .header("X-User-Role", "ROLE_CLIENT")
                .when()
                .patch("/bookings/" + bookingId + "/complete");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // No-show
    // ─────────────────────────────────────────────────────────────────────────

    @When("the professional marks the booking as no-show")
    public void professionalMarksNoShow() {
        response = RestAssured.given()
                .header("X-User-Id", professionalId.toString())
                .header("X-User-Role", "ROLE_PROFESSIONAL")
                .when()
                .patch("/bookings/" + bookingId + "/no-show");
    }

    @When("the owner marks the booking as no-show")
    public void ownerMarksNoShow() {
        response = RestAssured.given()
                .header("X-User-Id", "owner-id")
                .header("X-User-Role", "ROLE_OWNER")
                .when()
                .patch("/bookings/" + bookingId + "/no-show");
    }

    @When("the client tries to mark the booking as no-show")
    public void clientTriesToMarkNoShow() {
        response = RestAssured.given()
                .header("X-User-Id", clientId)
                .header("X-User-Role", "ROLE_CLIENT")
                .when()
                .patch("/bookings/" + bookingId + "/no-show");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Booking details
    // ─────────────────────────────────────────────────────────────────────────

    @When("the client requests the booking details")
    public void clientRequestsBookingDetails() {
        response = getBookingDetails(bookingId, clientId, "ROLE_CLIENT");
    }

    @When("the professional requests the booking details")
    public void professionalRequestsBookingDetails() {
        response = getBookingDetails(bookingId, professionalId.toString(), "ROLE_PROFESSIONAL");
    }

    @When("the owner requests the booking details")
    public void ownerRequestsBookingDetails() {
        response = getBookingDetails(bookingId, "owner-id", "ROLE_OWNER");
    }

    @When("another client requests the booking details")
    public void anotherClientRequestsBookingDetails() {
        response = getBookingDetails(bookingId, "other-client-id", "ROLE_CLIENT");
    }

    @When("the client requests details for a non-existent booking")
    public void clientRequestsNonExistentBooking() {
        response = getBookingDetails(UUID.randomUUID(), clientId, "ROLE_CLIENT");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Booking lists
    // ─────────────────────────────────────────────────────────────────────────

    @When("the client lists their bookings")
    public void clientListsTheirBookings() {
        response = RestAssured.given()
                .header("X-User-Id", clientId)
                .when()
                .get("/bookings/me");
    }

    @When("the professional lists their agenda")
    public void professionalListsTheirAgenda() {
        response = RestAssured.given()
                .header("X-User-Id", professionalId.toString())
                .when()
                .get("/bookings/professional");
    }

    @When("the owner lists establishment bookings")
    public void ownerListsEstablishmentBookings() {
        response = RestAssured.given()
                .header("X-User-Id", "owner-id")
                .header("X-User-Role", "ROLE_OWNER")
                .when()
                .get("/bookings/establishment/" + establishmentId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Then / assertions
    // ─────────────────────────────────────────────────────────────────────────

    @Then("the response status is {int}")
    public void theResponseStatusIs(int statusCode) {
        response.then().statusCode(statusCode);
    }

    @And("the booking status is {string}")
    public void theBookingStatusIs(String status) {
        response.then().body("status", equalTo(status));
    }

    @And("the booking price is {float}")
    public void theBookingPriceIs(float price) {
        response.then().body("price", equalTo(price));
    }

    @And("the cancel reason is {string}")
    public void theCancelReasonIs(String reason) {
        response.then().body("cancelReason", equalTo(reason));
    }

    @And("the booking id is present in the response")
    public void theBookingIdIsPresentInTheResponse() {
        response.then().body("id", equalTo(bookingId.toString()));
    }

    @And("the response contains available slots with duration {int} and price {float}")
    public void theResponseContainsAvailableSlots(int duration, float price) {
        response.then()
                .body("availableSlots", not(empty()))
                .body("durationMinutes", equalTo(duration))
                .body("price", equalTo(price));
    }

    @And("the response contains an empty list of available slots")
    public void theResponseContainsEmptySlots() {
        response.then().body("availableSlots", hasSize(0));
    }

    @And("the response contains at least 1 booking")
    public void theResponseContainsAtLeastOneBooking() {
        response.then().body("size()", greaterThanOrEqualTo(1));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private Response postBooking(String startDatetime, String userId) {
        String body = """
                {
                  "professionalId": "%s",
                  "establishmentId": "%s",
                  "providedServiceId": "%s",
                  "startDatetime": "%s"
                }
                """.formatted(professionalId, establishmentId, serviceId, startDatetime);

        return RestAssured.given()
                .contentType(ContentType.JSON)
                .header("X-User-Id", userId)
                .body(body)
                .when()
                .post("/bookings");
    }

    private Response cancelBookingAs(String userId, String role, String reason) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .header("X-User-Id", userId)
                .header("X-User-Role", role)
                .body("{\"reason\": \"" + reason + "\"}")
                .when()
                .patch("/bookings/" + bookingId + "/cancel");
    }

    private Response getBookingDetails(UUID id, String userId, String role) {
        return RestAssured.given()
                .header("X-User-Id", userId)
                .header("X-User-Role", role)
                .when()
                .get("/bookings/" + id);
    }
}
