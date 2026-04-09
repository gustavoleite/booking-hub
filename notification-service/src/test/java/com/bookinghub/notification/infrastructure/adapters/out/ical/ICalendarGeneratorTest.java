package com.bookinghub.notification.infrastructure.adapters.out.ical;

import static org.assertj.core.api.Assertions.assertThat;

import com.bookinghub.notification.core.domain.BookingSnapshot;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ICalendarGeneratorTest {

  private ICalendarGenerator generator;

  @BeforeEach
  void setUp() {
    generator = new ICalendarGenerator();
  }

  @Test
  void shouldGenerateValidIcsStructure() {
    List<BookingSnapshot> snapshots = List.of();

    String ics = generator.generate(snapshots);

    assertThat(ics).contains("BEGIN:VCALENDAR");
    assertThat(ics).contains("VERSION:2.0");
    assertThat(ics).contains("END:VCALENDAR");
  }

  @Test
  void shouldIncludeVeventForEachSnapshot() {
    UUID bookingId = UUID.randomUUID();
    BookingSnapshot snapshot = BookingSnapshot.builder()
        .bookingId(bookingId)
        .clientId("client-1")
        .professionalId(UUID.randomUUID())
        .startDatetime(LocalDateTime.of(2026, 5, 1, 10, 0))
        .endDatetime(LocalDateTime.of(2026, 5, 1, 11, 0))
        .status("CONFIRMED")
        .updatedAt(LocalDateTime.of(2026, 4, 1, 8, 0))
        .build();

    String ics = generator.generate(List.of(snapshot));

    assertThat(ics).contains("BEGIN:VEVENT");
    assertThat(ics).contains("END:VEVENT");
    assertThat(ics).contains("UID:" + bookingId + "@bookinghub");
    assertThat(ics).contains("STATUS:CONFIRMED");
  }

  @Test
  void shouldMapCancelledStatusCorrectly() {
    BookingSnapshot snapshot = BookingSnapshot.builder()
        .bookingId(UUID.randomUUID())
        .clientId("client-1")
        .professionalId(UUID.randomUUID())
        .startDatetime(LocalDateTime.now().plusDays(1))
        .endDatetime(LocalDateTime.now().plusDays(1).plusHours(1))
        .status("CANCELLED")
        .updatedAt(LocalDateTime.now())
        .build();

    String ics = generator.generate(List.of(snapshot));

    assertThat(ics).contains("STATUS:CANCELLED");
  }

  @Test
  void shouldMapCompletedStatusAsConfirmed() {
    BookingSnapshot snapshot = BookingSnapshot.builder()
        .bookingId(UUID.randomUUID())
        .clientId("client-1")
        .professionalId(UUID.randomUUID())
        .startDatetime(LocalDateTime.now().minusDays(1))
        .endDatetime(LocalDateTime.now().minusDays(1).plusHours(1))
        .status("COMPLETED")
        .updatedAt(LocalDateTime.now())
        .build();

    String ics = generator.generate(List.of(snapshot));

    assertThat(ics).contains("STATUS:CONFIRMED");
  }
}
