package com.bookinghub.notification.infrastructure.adapters.out.ical;

import com.bookinghub.notification.core.domain.BookingSnapshot;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ICalendarGenerator {

    private static final DateTimeFormatter ICS_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    public String generate(List<BookingSnapshot> snapshots) {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR\r\n");
        sb.append("VERSION:2.0\r\n");
        sb.append("PRODID:-//BookingHub//BookingHub Calendar//PT\r\n");
        sb.append("CALNAME:BookingHub - Meus Agendamentos\r\n");
        sb.append("CALSCALE:GREGORIAN\r\n");
        sb.append("METHOD:PUBLISH\r\n");

        for (BookingSnapshot s : snapshots) {
            String dtStamp = ICS_FORMAT.format(
                    s.getUpdatedAt().toInstant(ZoneOffset.UTC).atZone(ZoneOffset.UTC));
            final String dtStart = ICS_FORMAT.format(
                    s.getStartDatetime().toInstant(ZoneOffset.UTC).atZone(ZoneOffset.UTC));
            final String dtEnd = ICS_FORMAT.format(
                    s.getEndDatetime().toInstant(ZoneOffset.UTC).atZone(ZoneOffset.UTC));

            sb.append("BEGIN:VEVENT\r\n");
            sb.append("UID:").append(s.getBookingId()).append("@bookinghub\r\n");
            sb.append("DTSTAMP:").append(dtStamp).append("\r\n");
            sb.append("DTSTART:").append(dtStart).append("\r\n");
            sb.append("DTEND:").append(dtEnd).append("\r\n");
            sb.append("SUMMARY:Agendamento - BookingHub\r\n");
            sb.append("STATUS:").append(mapStatus(s.getStatus())).append("\r\n");
            sb.append("END:VEVENT\r\n");
        }

        sb.append("END:VCALENDAR\r\n");
        return sb.toString();
    }

    private String mapStatus(String status) {
        return "CANCELLED".equals(status) ? "CANCELLED" : "CONFIRMED";
    }
}
