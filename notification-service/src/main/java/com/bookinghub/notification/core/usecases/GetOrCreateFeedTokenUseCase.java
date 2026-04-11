package com.bookinghub.notification.core.usecases;

import com.bookinghub.notification.core.domain.CalendarFeed;
import com.bookinghub.notification.core.ports.CalendarFeedRepository;

public class GetOrCreateFeedTokenUseCase {

    private final CalendarFeedRepository repository;
    private final String baseUrl;

    public GetOrCreateFeedTokenUseCase(CalendarFeedRepository repository, String baseUrl) {
        this.repository = repository;
        this.baseUrl = baseUrl;
    }

    public String execute(String userId) {
        CalendarFeed feed = repository.findByUserId(userId)
                .orElseGet(() -> {
                    CalendarFeed newFeed = CalendarFeed.create(userId);
                    repository.save(newFeed);
                    return newFeed;
                });

        return buildFeedUrl(feed);
    }

    private String buildFeedUrl(CalendarFeed feed) {
        return baseUrl + "/feed/" + feed.getUserId() + "/" + feed.getFeedToken() + "/bookings.ics";
    }
}
