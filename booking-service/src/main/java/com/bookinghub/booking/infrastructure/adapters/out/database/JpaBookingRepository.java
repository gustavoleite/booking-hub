package com.bookinghub.booking.infrastructure.adapters.out.database;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaBookingRepository extends JpaRepository<BookingEntity, UUID> {

  List<BookingEntity> findByClientIdOrderByStartDatetimeDesc(String clientId);

  List<BookingEntity> findByProfessionalIdOrderByStartDatetimeDesc(UUID professionalId);

  List<BookingEntity> findByEstablishmentIdOrderByStartDatetimeDesc(UUID establishmentId);

  @Query(value = """
            SELECT COUNT(*) > 0 FROM tb_bookings
            WHERE professional_id = :professionalId
              AND start_datetime = :startDatetime
              AND status NOT IN ('CANCELLED', 'NO_SHOW')
            """, nativeQuery = true)
  boolean existsActiveSlot(@Param("professionalId") UUID professionalId,
                             @Param("startDatetime") LocalDateTime startDatetime);

  @Query(value = """
            SELECT * FROM tb_bookings
            WHERE professional_id = :professionalId
              AND start_datetime >= :dayStart
              AND start_datetime < :dayEnd
              AND status NOT IN ('CANCELLED', 'NO_SHOW')
            ORDER BY start_datetime
            """, nativeQuery = true)
  List<BookingEntity> findByProfessionalAndDate(@Param("professionalId") UUID professionalId,
                                                  @Param("dayStart") LocalDateTime dayStart,
                                                  @Param("dayEnd") LocalDateTime dayEnd);
}
