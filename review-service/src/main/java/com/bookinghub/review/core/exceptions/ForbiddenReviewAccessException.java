package com.bookinghub.review.core.exceptions;

public class ForbiddenReviewAccessException extends RuntimeException {
    public ForbiddenReviewAccessException(String message) {
        super(message);
    }
}
