package com.uts.Online.Booking.App.service;

import com.uts.Online.Booking.App.DAO.BookingDAO;
import com.uts.Online.Booking.App.DAO.CourtDAO;
import com.uts.Online.Booking.App.DAO.TimeslotDAO;
import com.uts.Online.Booking.App.model.Booking;
import com.uts.Online.Booking.App.model.Court;
import com.uts.Online.Booking.App.model.Timeslot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import org.eclipse.angus.mail.handlers.multipart_mixed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Autowired
    private BookingDAO bookingDAO;
    @Autowired
    private CourtDAO courtDAO;
    @Autowired
    private TimeslotDAO timeslotDAO;
    
    public Booking createBooking(Long courtId, Long timeslotId, LocalDate bookingDate, Long userId) {
        logger.info("Creating booking - Court: {}, Timeslot: {}, Date: {}, User: {}", 
            courtId, timeslotId, bookingDate, userId);
        
        // Validate inputs
        if (courtId == null || timeslotId == null || bookingDate == null || userId == null) {
            throw new IllegalArgumentException("All booking parameters are required");
        }
        
        Court court = courtDAO.findById(courtId)
            .orElseThrow(() -> new RuntimeException("Court not found with ID: " + courtId));
        Timeslot timeslot = timeslotDAO.findById(timeslotId)
            .orElseThrow(() -> new RuntimeException("Timeslot not found with ID: " + timeslotId));

        // Check if date is in the past
        if (bookingDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot book dates in the past");
        }

        if (isSlotBooked(courtId, timeslotId, bookingDate)) {
            logger.warn("Attempt to book already booked slot - Court: {}, Timeslot: {}, Date: {}", 
                courtId, timeslotId, bookingDate);
            throw new RuntimeException("This time slot is already booked");
        }

        Booking booking = new Booking();
        booking.setCourt(court);
        booking.setTimeslot(timeslot);
        booking.setBooking_date(bookingDate);
        booking.setStatus("BOOKED");
        booking.setUserid(userId);
        Booking savedBooking = bookingDAO.save(booking);

        Booking savedBooking = bookingDAO.save(booking);
        logger.info("Successfully created booking with ID: {}", savedBooking.getBookingId());
        return savedBooking;
    }

    public List<Booking> getAllBookings() {
        logger.debug("Fetching all bookings");
        return bookingDAO.findAll();
    }

    public Booking getBookingById(Long bookingId) {
        if (bookingId == null) {
            throw new IllegalArgumentException("Booking ID cannot be null");
        }
        logger.debug("Fetching booking with ID: {}", bookingId);
        return bookingDAO.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + bookingId));
    }

    @Transactional
    public Booking updateBooking(Long bookingId, Long courtId, Long timeslotId, LocalDate bookingDate, Long userId) {
        logger.info("Updating booking {} - Court: {}, Timeslot: {}, Date: {}, User: {}", 
            bookingId, courtId, timeslotId, bookingDate, userId);
        
        // Validate inputs
        if (bookingId == null || courtId == null || timeslotId == null || bookingDate == null || userId == null) {
            throw new IllegalArgumentException("All booking parameters are required");
        }
        
        Booking existingBooking = getBookingById(bookingId);
        
        Court court = courtDAO.findById(courtId)
            .orElseThrow(() -> new RuntimeException("Court not found with ID: " + courtId));
        Timeslot timeslot = timeslotDAO.findById(timeslotId)
            .orElseThrow(() -> new RuntimeException("Timeslot not found with ID: " + timeslotId));

        // Check if date is in the past
        if (bookingDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot book dates in the past");
        }

        // Check if the new slot is available (excluding the current booking)
        if (isSlotBookedExcluding(courtId, timeslotId, bookingDate, bookingId)) {
            logger.warn("Attempt to update to already booked slot - Court: {}, Timeslot: {}, Date: {}", 
                courtId, timeslotId, bookingDate);
            throw new RuntimeException("The new time slot is already booked");
        }

        existingBooking.setCourt(court);
        existingBooking.setTimeslot(timeslot);
        existingBooking.setBookingDate(bookingDate);
        existingBooking.setUserId(userId);

        Booking updatedBooking = bookingDAO.save(existingBooking);
        logger.info("Successfully updated booking with ID: {}", bookingId);
        return updatedBooking;
    }

    public void deleteBooking(Long bookingId) {
        if (bookingId == null) {
            throw new IllegalArgumentException("Booking ID cannot be null");
        }
        
        logger.info("Deleting booking with ID: {}", bookingId);
        
        // Verify booking exists before attempting to delete
        if (!bookingDAO.existsById(bookingId)) {
            throw new RuntimeException("Booking not found with ID: " + bookingId);
        }
        
        bookingDAO.deleteById(bookingId);
        logger.info("Successfully deleted booking with ID: {}", bookingId);
    }

    public boolean isSlotBooked(Long courtId, Long timeslotId, LocalDate date) {
        if (courtId == null || timeslotId == null || date == null) {
            return false;
        }
        return bookingDAO.existsByCourtCourtIdAndTimeslotTimeslotIdAndBookingDate(courtId, timeslotId, date);
    }

    private boolean isSlotBookedExcluding(Long courtId, Long timeslotId, LocalDate date, Long excludeBookingId) {
        if (courtId == null || timeslotId == null || date == null || excludeBookingId == null) {
            return false;
        }
        
        List<Booking> existingBookings = bookingDAO.findByCourtCourtIdAndTimeslotTimeslotIdAndBookingDate(
            courtId, timeslotId, date);
        
        return existingBookings.stream()
            .anyMatch(booking -> !booking.getBookingId().equals(excludeBookingId));
    }

    public Map<String, Boolean> getAvailabilityMap(Long venueId, LocalDate date, List<Court> courts, List<Timeslot> timeslots) {
        logger.info("=== Generating availability map for venue {} on {} ===", venueId, date);
        
        Map<String, Boolean> availability = new HashMap<>();
        
        if (courts == null || timeslots == null) {
            logger.warn("Null courts or timeslots provided to getAvailabilityMap");
            return availability;
        }
        
        // Get all bookings for this date to check efficiently
        List<Booking> bookingsForDate = bookingDAO.findByBookingDate(date);
        logger.info("Found {} bookings for date {}", bookingsForDate.size(), date);
        
        for (Court court : courts) {
            if (court == null) continue;
            
            for (Timeslot timeslot : timeslots) {
                if (timeslot == null) continue;
                
                String key = court.getCourtId() + "-" + timeslot.getTimeslotId();
                
                // Check if this specific court-timeslot combination is booked
                boolean isBooked = bookingsForDate.stream()
                    .anyMatch(booking -> 
                        booking.getCourt() != null && 
                        booking.getTimeslot() != null &&
                        booking.getCourt().getCourtId().equals(court.getCourtId()) && 
                        booking.getTimeslot().getTimeslotId().equals(timeslot.getTimeslotId())
                    );
                
                boolean isAvailable = !isBooked;
                
                logger.info("Slot {} (Court ID: {}, Timeslot ID: {}) - Booked: {}, Available: {}", 
                    key, court.getCourtId(), timeslot.getTimeslotId(), isBooked, isAvailable);
                
                availability.put(key, isAvailable);
            }
        }
        
        logger.info("=== Generated availability map with {} entries ===", availability.size());
        return availability;
    }
}