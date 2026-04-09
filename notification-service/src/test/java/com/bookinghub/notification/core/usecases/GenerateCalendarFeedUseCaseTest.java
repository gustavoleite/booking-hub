package com.bookinghub.notification.core.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.bookinghub.notification.core.domain.BookingSnapshot;
import com.bookinghub.notification.core.domain.CalendarFeed;
import com.bookinghub.notification.core.ports.BookingSnapshotRepository;
import com.bookinghub.notification.core.ports.CalendarFeedRepository;
import com.bookinghub.notification.infrastructure.adapters.out.ical.ICalendarGenerator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GenerateCalendarFeedUseCaseTest {

  @Mock
  private CalendarFeedRepository feedRepository;

  @Mock
  private BookingSnapshotRepository snapshotRepository;

  @Mock
  private ICalendarGenerator generator;

  private GenerateCalendarFeedUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new GenerateCalendarFeedUseCase(feedRepository, snapshotRepository, generator);
  }

  @Test
  void shouldThrowWhenTokenIsInvalid() {
    when(feedRepository.findByUserIdAndFeedToken("user1", "badtoken"))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute("user1", "badtoken"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid feed token");
  }

  @Test
  void shouldGenerateIcsForClientSnapshots() {
    String userId = "client-abc";
    String token = "validtoken";
    CalendarFeed feed = CalendarFeed.builder()
        .id(UUID.randomUUID())
        .userId(userId)
        .feedToken(token)
        .createdAt(LocalDateTime.now())
        .build();

    BookingSnapshot snapshot = BookingSnapshot.builder()
        .bookingId(UUID.randomUUID())
        .clientId(userId)
        .professionalId(UUID.randomUUID())
        .startDatetime(LocalDateTime.now().plusDays(1))
        .endDatetime(LocalDateTime.now().plusDays(1).plusHours(1))
        .status("CONFIRMED")
        .updatedAt(LocalDateTime.now())
        .build();

    when(feedRepository.findByUserIdAndFeedToken(userId, token)).thenReturn(Optional.of(feed));
    when(snapshotRepository.findByClientId(userId)).thenReturn(List.of(snapshot));
    when(generator.generate(List.of(snapshot))).thenReturn("BEGIN:VCALENDAR\r\nEND:VCALENDAR\r\n");

    String result = useCase.execute(userId, token);

    assertThat(result).contains("BEGIN:VCALENDAR");
  }

  @Test
  void shouldIncludeProfessionalSnapshotsWhenUserIdIsUuid() {
    UUID professionalId = UUID.randomUUID();
    String userId = professionalId.toString();
    String token = "validtoken";
    CalendarFeed feed = CalendarFeed.builder()
        .id(UUID.randomUUID())
        .userId(userId)
        .feedToken(token)
        .createdAt(LocalDateTime.now())
        .build();

    when(feedRepository.findByUserIdAndFeedToken(userId, token)).thenReturn(Optional.of(feed));
    when(snapshotRepository.findByClientId(userId)).thenReturn(List.of());
    when(snapshotRepository.findByProfessionalId(professionalId)).thenReturn(List.of());
    when(generator.generate(List.of())).thenReturn("BEGIN:VCALENDAR\r\nEND:VCALENDAR\r\n");

    String result = useCase.execute(userId, token);

    assertThat(result).isNotNull();
  }
}
