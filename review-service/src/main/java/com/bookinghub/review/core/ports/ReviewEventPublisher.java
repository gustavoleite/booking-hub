package com.bookinghub.review.core.ports;

import com.bookinghub.review.core.domain.Review;

public interface ReviewEventPublisher {
    void publishReviewCreated(Review review);
}
