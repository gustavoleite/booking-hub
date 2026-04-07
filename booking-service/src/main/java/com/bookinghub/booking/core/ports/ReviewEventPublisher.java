package com.bookinghub.booking.core.ports;

import com.bookinghub.booking.core.domain.Review;

public interface ReviewEventPublisher {
  void publishReviewCreated(Review review);
}
