package com.bookinghub.review.bdd;

import com.bookinghub.review.core.ports.ReviewEventPublisher;
import com.bookinghub.review.infrastructure.adapters.out.database.EligibleBookingDocument;
import com.bookinghub.review.infrastructure.adapters.out.database.MongoEligibleBookingRepository;
import com.bookinghub.review.infrastructure.adapters.out.database.MongoReviewRepository;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class StepDefinitions {

    @LocalServerPort
    private int port;

    @MockBean
    private ReviewEventPublisher reviewEventPublisher;

    @Autowired
    private MongoReviewRepository mongoReviewRepository;

    @Autowired
    private MongoEligibleBookingRepository mongoEligibleBookingRepository;

    // Per-scenario state
    private UUID professionalId;
    private UUID establishmentId;
    private UUID bookingId;
    private String clientId;
    private Response response;

    @Before
    public void setUp() {
        RestAssured.port = port;
        professionalId = UUID.randomUUID();
        establishmentId = UUID.randomUUID();
        bookingId = UUID.randomUUID();
        clientId = "bdd-client-" + UUID.randomUUID().toString().substring(0, 8);
        response = null;
        mongoReviewRepository.deleteAll();
        mongoEligibleBookingRepository.deleteAll();
        Mockito.reset(reviewEventPublisher);
    }

    // ── Background helpers ────────────────────────────────────────────────────

    @Given("a completed booking exists for the client")
    public void aCompletedBookingExistsForTheClient() {
        EligibleBookingDocument entity = EligibleBookingDocument.builder()
                .bookingId(bookingId.toString())
                .clientId(clientId)
                .professionalId(professionalId.toString())
                .establishmentId(establishmentId.toString())
                .completedAt(LocalDateTime.now().minusHours(1))
                .build();
        mongoEligibleBookingRepository.save(entity);
    }

    @Given("the client has already reviewed the booking")
    public void theClientHasAlreadyReviewedTheBooking() {
        response = postReview(clientId, bookingId, 5, 4, "Great!");
        response.then().statusCode(201);
    }

    // ── When — create review ──────────────────────────────────────────────────

    @When("the client submits a review with professionalRating {int} and establishmentRating {int}")
    public void clientSubmitsReviewWithBothRatings(int profRating, int estRating) {
        response = postReview(clientId, bookingId, profRating, estRating, "Good service");
    }

    @When("the client submits a review with professionalRating {int} and no establishment rating")
    public void clientSubmitsReviewWithOnlyProfRating(int profRating) {
        response = postReviewRaw(clientId, """
                {"bookingId":"%s","professionalRating":%d}
                """.formatted(bookingId, profRating));
    }

    @When("another client tries to submit a review for the booking")
    public void anotherClientTriesToSubmitReview() {
        response = postReview("other-client", bookingId, 5, null, null);
    }

    @When("the client submits a review for a non-eligible booking")
    public void clientSubmitsReviewForNonEligibleBooking() {
        response = postReview(clientId, UUID.randomUUID(), 5, null, null);
    }

    @When("the client submits a review with no ratings")
    public void clientSubmitsReviewWithNoRatings() {
        response = postReviewRaw(clientId, """
                {"bookingId":"%s","comment":"no rating"}
                """.formatted(bookingId));
    }

    // ── When — list reviews ───────────────────────────────────────────────────

    @When("a request is made to list reviews for the professional")
    public void listReviewsForProfessional() {
        response = RestAssured.given()
                .when()
                .get("/reviews/professional/" + professionalId);
    }

    @When("a request is made to list reviews for the establishment")
    public void listReviewsForEstablishment() {
        response = RestAssured.given()
                .when()
                .get("/reviews/establishment/" + establishmentId);
    }

    @When("the client requests the review by booking id")
    public void clientRequestsReviewByBookingId() {
        response = RestAssured.given()
                .header("X-User-Id", clientId)
                .header("X-User-Role", "ROLE_CLIENT")
                .when()
                .get("/reviews/booking/" + bookingId);
    }

    @When("another client requests the review by booking id")
    public void anotherClientRequestsReviewByBookingId() {
        response = RestAssured.given()
                .header("X-User-Id", "other-client")
                .header("X-User-Role", "ROLE_CLIENT")
                .when()
                .get("/reviews/booking/" + bookingId);
    }

    @When("the client requests the review for a non-existent booking")
    public void clientRequestsReviewForNonExistentBooking() {
        response = RestAssured.given()
                .header("X-User-Id", clientId)
                .header("X-User-Role", "ROLE_CLIENT")
                .when()
                .get("/reviews/booking/" + UUID.randomUUID());
    }

    // ── When — stats ──────────────────────────────────────────────────────────

    @When("a request is made for professional stats with no reviews")
    public void requestProfessionalStatsNoReviews() {
        response = RestAssured.given()
                .when()
                .get("/reviews/professional/" + UUID.randomUUID() + "/stats");
    }

    @When("a request is made for establishment stats with no reviews")
    public void requestEstablishmentStatsNoReviews() {
        response = RestAssured.given()
                .when()
                .get("/reviews/establishment/" + UUID.randomUUID() + "/stats");
    }

    @When("a request is made for professional stats")
    public void requestProfessionalStats() {
        response = RestAssured.given()
                .when()
                .get("/reviews/professional/" + professionalId + "/stats");
    }

    // ── Then / assertions ─────────────────────────────────────────────────────

    @Then("the response status is {int}")
    public void theResponseStatusIs(int status) {
        response.then().statusCode(status);
    }

    @And("the review professionalRating is {int}")
    public void theReviewProfessionalRatingIs(int rating) {
        response.then().body("professionalRating", equalTo(rating));
    }

    @And("the review establishmentRating is {int}")
    public void theReviewEstablishmentRatingIs(int rating) {
        response.then().body("establishmentRating", equalTo(rating));
    }

    @And("the response contains at least 1 review")
    public void theResponseContainsAtLeastOneReview() {
        response.then().body("reviews.size()", greaterThanOrEqualTo(1));
    }

    @And("the review booking id matches")
    public void theReviewBookingIdMatches() {
        response.then().body("bookingId", equalTo(bookingId.toString()));
    }

    @And("the totalReviews is {int}")
    public void theTotalReviewsIs(int total) {
        response.then().body("totalReviews", equalTo(total));
    }

    @And("the averageRating is null")
    public void theAverageRatingIsNull() {
        response.then().body("averageRating", nullValue());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Response postReview(String userId, UUID bId, Integer profRating,
                                 Integer estRating, String comment) {
        StringBuilder body = new StringBuilder("{\"bookingId\":\"").append(bId).append("\"");
        if (profRating != null) body.append(",\"professionalRating\":").append(profRating);
        if (estRating != null) body.append(",\"establishmentRating\":").append(estRating);
        if (comment != null) body.append(",\"comment\":\"").append(comment).append("\"");
        body.append("}");
        return postReviewRaw(userId, body.toString());
    }

    private Response postReviewRaw(String userId, String body) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .header("X-User-Id", userId)
                .body(body)
                .when()
                .post("/reviews");
    }
}
