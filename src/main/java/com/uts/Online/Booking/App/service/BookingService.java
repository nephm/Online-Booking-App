package com.uts.Online.Booking.App.service;

import com.uts.Online.Booking.App.DAO.BookingDAO;
import com.uts.Online.Booking.App.DAO.CourtDAO;
import com.uts.Online.Booking.App.DAO.TimeslotDAO;
import com.uts.Online.Booking.App.model.Booking;
import com.uts.Online.Booking.App.model.Court;
import com.uts.Online.Booking.App.model.Timeslot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class BookingService {

    @Autowired
    private BookingDAO bookingDAO;

    @Autowired
    private CourtDAO courtDAO;

    @Autowired
    private TimeslotDAO timeslotDAO;
    

    public Booking createBooking(Long courtId, Long timeslotId, LocalDate bookingDate, Long userId) {
        Court court = courtDAO.findById(courtId).orElseThrow(() -> new RuntimeException("Court not found"));
        Timeslot timeslot = timeslotDAO.findById(timeslotId).orElseThrow(() -> new RuntimeException("Timeslot not found"));

        if (isSlotBooked(courtId, timeslotId, bookingDate)) {
            throw new RuntimeException("This time slot is already booked");
        }

        Booking booking = new Booking();
        booking.setCourt(court);
        booking.setTimeslot(timeslot);
        booking.setBookingDate(bookingDate);
        booking.setUserId(userId);
        booking.setStatus("CONFIRMED");

        return bookingDAO.save(booking);
    }

    public List<Booking> getAllBookings() {
        return bookingDAO.findAll();
    }

    public Booking getBookingById(Long bookingId) {
        return bookingDAO.findById(bookingId).orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    public Booking updateBooking(Long bookingId, Long courtId, Long timeslotId, LocalDate bookingDate, Long userId) {
        Booking existingBooking = getBookingById(bookingId);
        Court court = courtDAO.findById(courtId).orElseThrow(() -> new RuntimeException("Court not found"));
        Timeslot timeslot = timeslotDAO.findById(timeslotId).orElseThrow(() -> new RuntimeException("Timeslot not found"));

        if (isSlotBookedExcluding(courtId, timeslotId, bookingDate, bookingId)) {
            throw new RuntimeException("The new time slot is already booked");
        }

        existingBooking.setCourt(court);
        existingBooking.setTimeslot(timeslot);
        existingBooking.setBookingDate(bookingDate);
        existingBooking.setUserId(userId);

        return bookingDAO.save(existingBooking);
    }

    public void deleteBooking(Long bookingId) {
        bookingDAO.deleteById(bookingId);
    }

    public boolean isSlotBooked(Long courtId, Long timeslotId, LocalDate date) {
        return bookingDAO.existsByCourtCourtIdAndTimeslotTimeslotIdAndBookingDate(courtId, timeslotId, date);
    }

    private boolean isSlotBookedExcluding(Long courtId, Long timeslotId, LocalDate date, Long excludeBookingId) {
        List<Booking> existingBookings = bookingDAO.findByCourtCourtIdAndTimeslotTimeslotIdAndBookingDate(courtId, timeslotId, date);
        return existingBookings.stream().anyMatch(booking -> !booking.getBookingId().equals(excludeBookingId));
    }

    public Map<String, Boolean> getAvailabilityMap(Long venueId, LocalDate date, List<Court> courts, List<Timeslot> timeslots) {
        Map<String, Boolean> availability = new HashMap<>();
        for (Court court : courts) {
            for (Timeslot timeslot : timeslots) {
                String key = court.getCourtId() + "-" + timeslot.getTimeslotId();
                availability.put(key, !isSlotBooked(court.getCourtId(), timeslot.getTimeslotId(), date));
            }
        }
        return availability;
    }
}