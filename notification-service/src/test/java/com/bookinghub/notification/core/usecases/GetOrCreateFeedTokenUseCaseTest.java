package com.bookinghub.notification.core.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookinghub.notification.core.domain.CalendarFeed;
import com.bookinghub.notification.core.ports.CalendarFeedRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetOrCreateFeedTokenUseCaseTest {

  @Mock
  private CalendarFeedRepository repository;

  private GetOrCreateFeedTokenUseCase useCase;

  private static final String BASE_URL = "http://localhost:8080/api/calendar";

  @BeforeEach
  void setUp() {
    useCase = new GetOrCreateFeedTokenUseCase(repository, BASE_URL);
  }

  @Test
  void shouldReturnExistingFeedUrl() {
    String userId = "user-42";
    CalendarFeed existing = CalendarFeed.builder()
        .id(UUID.randomUUID())
        .userId(userId)
        .feedToken("abc123token")
        .createdAt(LocalDateTime.now())
        .build();

    when(repository.findByUserId(userId)).thenReturn(Optional.of(existing));

    String url = useCase.execute(userId);

    assertThat(url).contains(userId);
    assertThat(url).contains("abc123token");
    assertThat(url).contains("bookings.ics");
    verify(repository, never()).save(any());
  }

  @Test
  void shouldCreateAndSaveNewFeedWhenNotExists() {
    String userId = "new-user";
    when(repository.findByUserId(userId)).thenReturn(Optional.empty());

    String url = useCase.execute(userId);

    assertThat(url).contains(userId);
    assertThat(url).contains("bookings.ics");
    assertThat(url).startsWith("webcal://");
    verify(repository).save(any());
  }

  @Test
  void shouldReplaceHttpWithWebcal() {
    String userId = "user-webcal";
    when(repository.findByUserId(userId)).thenReturn(Optional.empty());

    String url = useCase.execute(userId);

    assertThat(url).startsWith("webcal://");
    assertThat(url).doesNotContain("http://");
  }
}
