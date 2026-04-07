package com.bookinghub.search.infrastructure.adapters.in.messaging;

import static org.mockito.Mockito.verify;

import com.bookinghub.search.core.usecases.IndexReviewUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewEventListenerTest {

    @Mock
    IndexReviewUseCase useCase;
    @InjectMocks
    ReviewEventListener listener;

    @Test
    void onReviewCreated_shouldForwardEstablishmentRatingToUseCase() {
        var event = new ReviewEvent("rev1", "book1", "client1", "prof1", "est1", 5.0, 4.0);

        listener.onReviewCreated(event);

        verify(useCase).execute("est1", 4.0);
    }

    @Test
    void onReviewCreated_shouldHandleNullEstablishmentRating() {
        var event = new ReviewEvent("rev1", "book1", "client1", "prof1", "est1", 5.0, null);

        listener.onReviewCreated(event);

        verify(useCase).execute("est1", null);
    }
}
