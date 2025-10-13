package com.uts.Online.Booking.App.service;

import com.uts.Online.Booking.App.DAO.BookingDAO;
import com.uts.Online.Booking.App.DAO.CourtDAO;
import com.uts.Online.Booking.App.DAO.TimeslotDAO;
import com.uts.Online.Booking.App.DAO.UserDAO;
import com.uts.Online.Booking.App.model.Booking;
import com.uts.Online.Booking.App.model.Court;
import com.uts.Online.Booking.App.model.Player;
import com.uts.Online.Booking.App.model.Timeslot;
import com.uts.Online.Booking.App.model.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Autowired
    private BookingDAO bookingDAO;

    @Autowired
    private CourtDAO courtDAO;

    @Autowired
    private TimeslotDAO timeslotDAO;

    @Autowired 
    private UserDAO userDAO;

    // update booking after payment
    public void updateBookingStatus(Long bookingId, String status){
        Booking booking = bookingDAO.findById(bookingId).orElseThrow();
        booking.setStatus(status);
        bookingDAO.save(booking);
    }

    //calculate total amount for multiple slots
    public Double calculateTotalAmount(List<String> selectedSlots){
        return selectedSlots.stream()
            .mapToDouble(slot ->{
                String[] parts = slot.split("-", 3);
                
                if (parts.length != 3) {
                    return 0.0;
                }
                
                Long courtId = Long.parseLong(parts[0]);
                Court court = courtDAO.findById(courtId).orElse(null);
                return court != null ? court.getHourlyRate() : 0.0;
            })
            .sum();
    }

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
        booking.setBookingDate(bookingDate);
        booking.setUserId(userId);
        booking.setStatus("CONFIRMED");

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

    //get all bookings by user ID
    public List<Booking> getBookingsByUserId(Long userId){
        if(userId == null){
            throw new IllegalArgumentException("User ID cannot be null");
        }
        logger.debug("Fetching bookings for user ID: ");
        return bookingDAO.findByUserId(userId);
    }

    //cancel booking
    @Transactional
    public void cancelBooking(Long bookingId){
        if(bookingId == null){
            throw new IllegalArgumentException("Booking ID cannot be null");
        }

        logger.info("Cancelling booking with ID: {}", bookingId);

        Booking booking = getBookingById(bookingId);
        booking.setStatus("CANCELLED");
        bookingDAO.save(booking);

        logger.info("Successfully cancelled booking with ID: {}", bookingId);
    }

    //update booking with new timeslot and date
    @Transactional
    public void updateBookingForUsers(Long bookingId, Long newTimeslotId, LocalDate newDate){
        logger.info("Update booking {} - New Timeslot: {}, New Date: {}",
            bookingId, newTimeslotId, newDate);

        if(bookingId == null || newTimeslotId == null || newDate == null){
            throw new IllegalArgumentException("All parameters are required");
        }

        //check if new date is in the past
        if(newDate.isBefore(LocalDate.now())){
            throw new RuntimeException("Cannot book dates in the past");
        }

        Booking booking = getBookingById(bookingId);
        Timeslot newTimeslot = timeslotDAO.findById(newTimeslotId)
            .orElseThrow(() -> new RuntimeException("Timeslot not found"));
        
        //check if new timeslot is available
        if(isSlotBookedExcluding(booking.getCourt().getCourtId(), newTimeslotId, newDate, bookingId)){
            throw new RuntimeException("The selected time slot is already booked");
        }

        booking.setTimeslot(newTimeslot);
        booking.setBookingDate(newDate);
        bookingDAO.save(booking);

        logger.info("Successfully updated booking with ID: {}", bookingId);
    }

    //get available timeslots for editing
    public List<Timeslot> getAvailableTimeslotsForEdit(Long bookingId){
        logger.info("Fetching available timeslots for editing booking {}", bookingId);

        if(bookingId == null){
            throw new IllegalArgumentException("Booking ID is required");
        }

        Booking currentBooking = getBookingById(bookingId);
        Long courtId = currentBooking.getCourt().getCourtId();
        LocalDate date = currentBooking.getBookingDate();

        List<Timeslot> allTimeslots = timeslotDAO.findAll();

        //get bookings for this court and date 
        List<Booking> bookingsForDate = bookingDAO.findByCourtCourtIdAndBookingDate(courtId, date)
            .stream()
            .filter(booking -> !booking.getBookingId().equals(bookingId))
            .collect(Collectors.toList());

        //filter booked timeslots
        List<Timeslot> availableTimeslots = allTimeslots.stream()
            .filter(timeslot -> {
                //include current booking timeslot as available
                if(timeslot.getTimeslotId().equals(currentBooking.getTimeslot().getTimeslotId())){
                    return true;
                }
                //check if timeslot is not booked
                return bookingsForDate.stream()
                    .noneMatch(booking -> booking.getTimeslot().getTimeslotId().equals(timeslot.getTimeslotId()));
            })
            .collect(Collectors.toList());

        logger.info("Found {} available timeslots for editing", availableTimeslots.size());

        return availableTimeslots;
    }

    //Refund payments and turn into credits for cancelled bookings
    @Transactional
    public void cancelBookingWithRefund(Long bookingId){
        if(bookingId == null){
            throw new IllegalArgumentException("Booking ID cannot be null");
        }

        Booking booking = getBookingById(bookingId);
        
        //calculate refund amount based on hourly rate
        Double refundAmount = booking.getCourt().getHourlyRate();

        //Update booking status
        booking.setStatus("CANCELLED");
        bookingDAO.save(booking);

        //add refund to player's credit balance
        User u = userDAO.findById(booking.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));

        if(u instanceof Player){
            Player player = (Player) u;
            Double currentBalance = player.getCreditBalance();
            player.setCreditBalance(currentBalance + refundAmount);
            userDAO.save(player);

            logger.info("Added ${} credit to user {}'s account.", refundAmount, u.getId());
        }

        logger.info("Successfully cancelled booking with ID: {}.");
    }
}