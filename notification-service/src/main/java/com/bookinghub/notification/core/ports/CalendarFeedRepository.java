package com.bookinghub.notification.core.ports;

import com.bookinghub.notification.core.domain.CalendarFeed;
import java.util.Optional;

public interface CalendarFeedRepository {

  void save(CalendarFeed feed);

  Optional<CalendarFeed> findByUserId(String userId);

  Optional<CalendarFeed> findByUserIdAndFeedToken(String userId, String feedToken);
}
