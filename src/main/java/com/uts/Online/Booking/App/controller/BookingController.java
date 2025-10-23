package com.uts.Online.Booking.App.controller;

import com.uts.Online.Booking.App.DAO.BookingDAO;
import com.uts.Online.Booking.App.DAO.UserDAO;
import com.uts.Online.Booking.App.model.Booking;
import com.uts.Online.Booking.App.model.User;
import com.uts.Online.Booking.App.service.BookingService;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;


@Controller
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingDAO bookingDAO;

    @Autowired
    private UserDAO userDAO;

    //get logged in user
    private User getUser(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userDAO.findByEmail(auth.getName()).orElse(null);
    }

    @SuppressWarnings("null")
    // Main processing for booking
    @PostMapping("/book")
    public String bookSlots(@RequestParam(value = "selectedSlots", required = false) List<String> selectedSlots,
                           RedirectAttributes redirectAttributes) {
        
        if (selectedSlots == null || selectedSlots.isEmpty()) {
            logger.warn("Booking attempt with no slots selected");
            redirectAttributes.addFlashAttribute("error", "Please select at least one time slot");
            return "redirect:/venues";
        }
        
        logger.info("Processing booking for {} slot(s)", selectedSlots.size());
        
        int successCount = 0;
        int failureCount = 0;
        StringBuilder errorMessages = new StringBuilder();
        Booking savedBooking = null;
        Double totalAmount = bookingService.calculateTotalAmount(selectedSlots);
        
        try {
            for (String slot : selectedSlots) {
                logger.debug("Processing slot: {}", slot);
                
                String[] parts = slot.split("-", 3);
                
                if (parts.length != 3) {
                    logger.error("Invalid slot format: {}", slot);
                    failureCount++;
                    errorMessages.append("Invalid slot format: ").append(slot).append("; ");
                    continue;
                }
                
                try {
                    Long courtId = Long.parseLong(parts[0]);
                    Long timeslotId = Long.parseLong(parts[1]);
                    LocalDate bookingDate = LocalDate.parse(parts[2]);
                    
                    logger.info("Booking - Court: {}, Timeslot: {}, Date: {}", courtId, timeslotId, bookingDate);
                    
                     
                    savedBooking = bookingService.createBooking(courtId, timeslotId, bookingDate, getUser().getId());
                    successCount++;
                    
                } catch (NumberFormatException e) {
                    logger.error("Invalid number format in slot: {}", slot, e);
                    failureCount++;
                    errorMessages.append("Invalid number in slot data; ");
                } catch (RuntimeException e) {
                    logger.error("Error booking slot {}: {}", slot, e.getMessage());
                    failureCount++;
                    errorMessages.append(e.getMessage()).append("; ");
                }
            }

            if (successCount > 0 && failureCount == 0) {
                redirectAttributes.addFlashAttribute("success", 
                    "Your booking has been confirmed successfully! Booked " + successCount + " slot(s).");
                redirectAttributes.addFlashAttribute("bookingCount", successCount);
                logger.info("Booking successful: {} slot(s) booked", successCount);
                return "redirect:/payment?bookingId=" + savedBooking.getBookingId() + "&amount=" + totalAmount;
                
            } else if (successCount > 0 && failureCount > 0) {
                redirectAttributes.addFlashAttribute("success", 
                    successCount + " slot(s) booked successfully.");
                redirectAttributes.addFlashAttribute("error", 
                    failureCount + " slot(s) failed: " + errorMessages.toString());
                logger.warn("Partial booking success: {} succeeded, {} failed", successCount, failureCount);
                return "redirect:/payment?bookingId=" + savedBooking.getBookingId() + "&amount=" + totalAmount;
                
            } else {
                redirectAttributes.addFlashAttribute("error", 
                    "All bookings failed: " + errorMessages.toString());
                logger.error("All bookings failed");
                return "redirect:/venues";
            }
            
        } catch (Exception e) {
            logger.error("Unexpected error processing bookings", e);
            redirectAttributes.addFlashAttribute("error", 
                "Technical error occurred: " + e.getMessage());
            return "redirect:/venues";
        }
    }

    @GetMapping("/booking-confirmation")
    public String showBookingConfirmation() {
        logger.debug("Displaying booking confirmation page");
        return "booking-confirmation";
    }

    //get all Bookings history
    @GetMapping("/myBookings")
    public String getBookingHistory(Model m, HttpServletRequest request){

        if(getUser() != null){
            List<Booking> bookings = bookingDAO.findByUserId(getUser().getId());
            m.addAttribute("bookings", bookings);
            m.addAttribute("user", getUser());
            CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

            if(csrfToken != null){
                m.addAttribute("_csrf", csrfToken);
            }
            return "booking-history";
        }

        return "redirect:/login";
    }

    //edit booking
    @PostMapping("/bookings/cancel/{id}")
    public String cancelBooking(@PathVariable Long id, Model m, RedirectAttributes redirectAttributes) {
        User user = getUser();

        if(user == null){
            return "redirect:/login";
        }

        try {
            Booking booking = bookingService.getBookingById(id);

            //verify booking belongs to user
            if(!booking.getUserId().equals(user.getId())){
                logger.warn("Unauthorized cancel attempt - User: {}, Booking: {}", user.getId(), id);
                redirectAttributes.addFlashAttribute("error", "Unauthorized action");
                return "redirect:/myBookings";
            }

            if(!booking.canBeModified()){
                redirectAttributes.addFlashAttribute("error", "This booking can only be cancelled 24 hours in advance");
                return "redirect:/myBookings";
            }

            //Cancel with refund
            Double refundAmount = booking.getCourt().getHourlyRate();
            bookingService.cancelBookingWithRefund(id);
            redirectAttributes.addFlashAttribute("success", "Booking cancelled successfully. $" + refundAmount + " has been added to credit balance.");
            logger.info("Booking {} cancelled by user {}", id, user.getId());

        } catch (Exception e) {
            logger.error("Error cancelling booking {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to cancel booking" + e.getMessage());
        }

        return "redirect:/myBookings";
    }

    //show edit form
    @GetMapping("/bookings/edit/{id}")
    public String editBookingForm(@PathVariable Long id, Model m, RedirectAttributes redirectAttributes) {
        User user = getUser();

        if(user == null){
            return "redirect:/login";
        }

        try {
            Booking booking = bookingService.getBookingById(id);

            //verify user
            if(!booking.getUserId().equals(user.getId())){
                logger.warn("Unauthorized edit attempt - User: {}, Booking: {}", user.getId(), id);
                redirectAttributes.addFlashAttribute("error", "Unauthorized action");
                return "redirect:/myBookings";
            }

            if(!booking.canBeModified()){
                redirectAttributes.addFlashAttribute("error", "This booking cannot be modified");
                return "redirect:/myBookings";
            }

            m.addAttribute("booking", booking);
            m.addAttribute("availableTimeslots", bookingService.getAvailableTimeslotsForEdit(id));

            return "edit-booking";

        } catch (Exception e) {
            logger.error("Error loading edit form for booking {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to load booking");
            return "redirect:/myBookings";
        }
    }

     //process edit
    @PostMapping("/bookings/edit/{id}")
    public String update(@PathVariable Long id, @RequestParam Long timeslotId, @RequestParam String date, RedirectAttributes redirectAttributes) {
        User user = getUser();

        if(user == null){
            return "redirect:/login";
        }

        try {
            Booking booking = bookingService.getBookingById(id);

            //verify user
            if(!booking.getUserId().equals(user.getId())){
                redirectAttributes.addFlashAttribute("error", "Unauthorized action");
                return "redirect:/myBookings";
            }

            bookingService.updateBookingForUsers(id, timeslotId, LocalDate.parse(date));
            redirectAttributes.addFlashAttribute("success", "Booking updated successfully");
            logger.info("Booking {} updating by user {}", id, user.getId());

        } catch (Exception e) {
            logger.error("Error updating booking {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to update booking");
        }

        return "redirect:/myBookings";
    }
}