package com.bookinghub.notification.infrastructure.adapters.in.rest;

import com.bookinghub.notification.application.dto.FeedUrlResponse;
import com.bookinghub.notification.core.usecases.GenerateCalendarFeedUseCase;
import com.bookinghub.notification.core.usecases.GetOrCreateFeedTokenUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/calendar")
@RequiredArgsConstructor
@Tag(name = "Calendar Feed", description = "ICS calendar feed for external calendar sync")
public class CalendarFeedController {

    private final GetOrCreateFeedTokenUseCase getOrCreateFeedToken;
    private final GenerateCalendarFeedUseCase generateCalendarFeed;

    @PostMapping("/feed/token")
    @Operation(summary = "Get or generate the personal calendar feed URL")
    public ResponseEntity<FeedUrlResponse> getOrCreateToken(
      @RequestHeader("X-User-Id") String userId) {

        String feedUrl = getOrCreateFeedToken.execute(userId);
        return ResponseEntity.ok(new FeedUrlResponse(feedUrl));
    }

    @GetMapping(value = "/feed/{userId}/{feedToken}/bookings.ics",
            produces = "text/calendar")
    @Operation(summary = "Download the ICS calendar feed (no auth required — token in URL)")
    public ResponseEntity<String> getCalendarFeed(
      @PathVariable("userId") String userId,
      @PathVariable("feedToken") String feedToken) {

        String ics = generateCalendarFeed.execute(userId, feedToken);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/calendar; charset=utf-8"))
                .header("Content-Disposition", "inline; filename=\"bookings.ics\"")
                .body(ics);
    }
}
