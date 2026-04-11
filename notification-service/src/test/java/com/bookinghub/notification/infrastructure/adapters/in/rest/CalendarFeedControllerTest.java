package com.bookinghub.notification.infrastructure.adapters.in.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookinghub.notification.core.usecases.GenerateCalendarFeedUseCase;
import com.bookinghub.notification.core.usecases.GetOrCreateFeedTokenUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class CalendarFeedControllerTest {

    @Mock
    private GetOrCreateFeedTokenUseCase getOrCreateFeedToken;

    @Mock
    private GenerateCalendarFeedUseCase generateCalendarFeed;

    private CalendarFeedController controller;

    @BeforeEach
    void setUp() {
        controller = new CalendarFeedController(getOrCreateFeedToken, generateCalendarFeed);
    }

    @Test
    void getOrCreateToken_shouldReturn200WithFeedUrl() {
        when(getOrCreateFeedToken.execute("user-1"))
                .thenReturn("http://localhost/calendar/feed/user-1/token123/bookings.ics");

        ResponseEntity<?> response = controller.getOrCreateToken("user-1");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(getOrCreateFeedToken).execute("user-1");
    }

    @Test
    void getCalendarFeed_shouldReturn200WithIcsContent() {
        String icsContent = "BEGIN:VCALENDAR\r\nEND:VCALENDAR\r\n";
        when(generateCalendarFeed.execute("user-1", "token123")).thenReturn(icsContent);

        ResponseEntity<String> response = controller.getCalendarFeed("user-1", "token123");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(icsContent);
        assertThat(response.getHeaders().getFirst("Content-Disposition"))
                .contains("bookings.ics");
    }
}
