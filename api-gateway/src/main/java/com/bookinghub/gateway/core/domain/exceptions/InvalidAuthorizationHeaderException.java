package com.bookinghub.gateway.core.domain.exceptions;

public class InvalidAuthorizationHeaderException extends UnauthorizedException {
    public InvalidAuthorizationHeaderException(String message) {
        super(message);
    }
}
