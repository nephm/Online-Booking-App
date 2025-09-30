package com.uts.Online.Booking.App.DAO;

import com.uts.Online.Booking.App.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;

public interface BookingDAO extends JpaRepository<Booking, Long> {
    List<Booking> findByBookingDate(LocalDate date);

    @Query("SELECT b FROM Booking b WHERE b.bookingDate = ?1")
    List<Booking> findBookingsForDate(LocalDate date);
}