package com.uts.Online.Booking.App.DAO;

import com.uts.Online.Booking.App.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingDAO extends JpaRepository<Booking, Long> {

    List<Booking> findByBookingDate(LocalDate date);

    @Query("SELECT b FROM Booking b WHERE b.bookingDate = ?1")
    List<Booking> findBookingsForDate(LocalDate date);

    List<Booking> findByUserId(Long userId);

    List<Booking> findByCourtCourtIdAndTimeslotTimeslotIdAndBookingDate(Long courtId, Long timeslotId, LocalDate bookingDate);

    boolean existsByCourtCourtIdAndTimeslotTimeslotIdAndBookingDate(Long courtId, Long timeslotId, LocalDate bookingDate);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.bookingDate = :date")
    long countByDate(@Param("date") LocalDate date);
}