package com.bookinghub.notification.infrastructure.adapters.out.database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookinghub.notification.core.domain.CalendarFeed;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostgresCalendarFeedAdapterTest {

    @Mock
    private JpaCalendarFeedRepository jpa;

    private PostgresCalendarFeedAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PostgresCalendarFeedAdapter(jpa);
    }

    @Test
    void save_shouldPersistEntity() {
        CalendarFeed feed = CalendarFeed.builder()
                .id(UUID.randomUUID())
                .userId("user-1")
                .feedToken("token-abc")
                .createdAt(LocalDateTime.now())
                .build();

        adapter.save(feed);

        verify(jpa).save(any(CalendarFeedEntity.class));
    }

    @Test
    void findByUserId_shouldReturnMappedDomain_whenEntityExists() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        CalendarFeedEntity entity = CalendarFeedEntity.builder()
                .id(id).userId("user-1").feedToken("token-abc").createdAt(now).build();

        when(jpa.findByUserId("user-1")).thenReturn(Optional.of(entity));

        Optional<CalendarFeed> result = adapter.findByUserId("user-1");

        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo("user-1");
        assertThat(result.get().getFeedToken()).isEqualTo("token-abc");
    }

    @Test
    void findByUserId_shouldReturnEmpty_whenEntityNotFound() {
        when(jpa.findByUserId("user-x")).thenReturn(Optional.empty());

        Optional<CalendarFeed> result = adapter.findByUserId("user-x");

        assertThat(result).isEmpty();
    }

    @Test
    void findByUserIdAndFeedToken_shouldReturnMappedDomain_whenEntityExists() {
        UUID id = UUID.randomUUID();
        CalendarFeedEntity entity = CalendarFeedEntity.builder()
                .id(id).userId("user-1").feedToken("token-abc").createdAt(LocalDateTime.now()).build();

        when(jpa.findByUserIdAndFeedToken("user-1", "token-abc")).thenReturn(Optional.of(entity));

        Optional<CalendarFeed> result = adapter.findByUserIdAndFeedToken("user-1", "token-abc");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
    }

    @Test
    void findByUserIdAndFeedToken_shouldReturnEmpty_whenNotFound() {
        when(jpa.findByUserIdAndFeedToken("user-1", "wrong")).thenReturn(Optional.empty());

        Optional<CalendarFeed> result = adapter.findByUserIdAndFeedToken("user-1", "wrong");

        assertThat(result).isEmpty();
    }
}
