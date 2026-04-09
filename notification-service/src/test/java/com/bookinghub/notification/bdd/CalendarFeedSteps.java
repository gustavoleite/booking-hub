package com.bookinghub.notification.bdd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bookinghub.notification.core.domain.BookingSnapshot;
import com.bookinghub.notification.core.domain.CalendarFeed;
import com.bookinghub.notification.core.ports.BookingSnapshotRepository;
import com.bookinghub.notification.core.ports.CalendarFeedRepository;
import com.bookinghub.notification.core.usecases.GenerateCalendarFeedUseCase;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;

public class CalendarFeedSteps {

  @Autowired
  private BookingSnapshotRepository snapshotRepository;

  @Autowired
  private CalendarFeedRepository feedRepository;

  @Autowired
  private GenerateCalendarFeedUseCase generateCalendarFeedUseCase;

  private String currentUserId;
  private String currentFeedToken;
  private String icsResult;
  private UUID lastBookingId;

  @Given("the user {string} has a booking from {string} to {string} with status {string}")
  public void theUserHasABooking(String userId, String start, String end, String status) {
    currentUserId = userId;
    lastBookingId = UUID.randomUUID();
    BookingSnapshot snapshot = BookingSnapshot.builder()
        .bookingId(lastBookingId)
        .clientId(userId)
        .professionalId(UUID.randomUUID())
        .startDatetime(LocalDateTime.parse(start))
        .endDatetime(LocalDateTime.parse(end))
        .status(status)
        .updatedAt(LocalDateTime.now())
        .build();
    snapshotRepository.save(snapshot);
  }

  @And("the user {string} has a valid feed token {string}")
  public void theUserHasAValidFeedToken(String userId, String token) {
    currentUserId = userId;
    currentFeedToken = token;
    CalendarFeed feed = CalendarFeed.builder()
        .id(UUID.randomUUID())
        .userId(userId)
        .feedToken(token)
        .createdAt(LocalDateTime.now())
        .build();
    feedRepository.save(feed);
  }

  @When("the user requests the calendar feed")
  public void theUserRequestsTheCalendarFeed() {
    icsResult = generateCalendarFeedUseCase.execute(currentUserId, currentFeedToken);
  }

  @Then("the response should be a valid ICS calendar")
  public void theResponseShouldBeAValidIcsCalendar() {
    assertThat(icsResult).contains("BEGIN:VCALENDAR");
    assertThat(icsResult).contains("END:VCALENDAR");
    assertThat(icsResult).contains("VERSION:2.0");
  }

  @And("the ICS should contain a VEVENT for the booking")
  public void theIcsShouldContainAVevent() {
    assertThat(icsResult).contains("BEGIN:VEVENT");
    assertThat(icsResult).contains("UID:" + lastBookingId + "@bookinghub");
  }

  @And("the ICS should contain STATUS:CANCELLED")
  public void theIcsShouldContainStatusCancelled() {
    assertThat(icsResult).contains("STATUS:CANCELLED");
  }

  @Given("no feed token exists for user {string}")
  public void noFeedTokenExistsForUser(String userId) {
    currentUserId = userId;
  }

  @When("the user {string} requests the feed with token {string}")
  public void theUserRequestsFeedWithToken(String userId, String token) {
    currentUserId = userId;
    currentFeedToken = token;
  }

  @Then("the use case should throw an IllegalArgumentException")
  public void theUseCaseShouldThrow() {
    assertThatThrownBy(() -> generateCalendarFeedUseCase.execute(currentUserId, currentFeedToken))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid feed token");
  }
}
