package com.bookinghub.notification.core.ports;

public interface EmailPort {
    void send(String to, String subject, String body);
}
