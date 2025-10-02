package com.uts.Online.Booking.App.controller;

import com.uts.Online.Booking.App.model.Booking;
import com.uts.Online.Booking.App.service.BookingService;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class AdminController {
  
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private BookingService bookingService;

    @GetMapping
    public String admin(Model model) {
        try {
            List<Booking> bookings = bookingService.getAllBookings();
            model.addAttribute("bookings", bookings);
            logger.info("Loaded {} bookings for admin view", bookings != null ? bookings.size() : 0);
        } catch (Exception e) {
            logger.error("Error loading bookings for admin view", e);
            model.addAttribute("error", "Unable to load bookings: " + e.getMessage());
        }
        return "admin";
    }

    @GetMapping("/edit/{bookingId}")
    public String editBookingRedirect(@PathVariable Long bookingId, 
                                     RedirectAttributes redirectAttributes) {
        try {
            Booking booking = bookingService.getBookingById(bookingId);
            if (booking == null) {
                logger.error("Booking not found with ID: {}", bookingId);
                redirectAttributes.addFlashAttribute("error", "Booking not found");
                return "redirect:/admin";
            }
            
            // Validate nested objects to prevent NullPointerException
            if (booking.getCourt() == null) {
                logger.error("Booking {} has no court assigned", bookingId);
                redirectAttributes.addFlashAttribute("error", "Invalid booking data: No court assigned");
                return "redirect:/admin";
            }
            
            if (booking.getTimeslot() == null) {
                logger.error("Booking {} has no timeslot assigned", bookingId);
                redirectAttributes.addFlashAttribute("error", "Invalid booking data: No timeslot assigned");
                return "redirect:/admin";
            }
            
            // Pass parameters via URL query string
            Long venueId = booking.getCourt().getVenue().getVenueId();
            LocalDate bookingDate = booking.getBookingDate();
            Long courtId = booking.getCourt().getCourtId();
            Long timeslotId = booking.getTimeslot().getTimeslotId();
            Long userId = booking.getUserId();
            
            logger.info("Redirecting to edit booking {} at venue {}", bookingId, venueId);
            
            return "redirect:/venue/" + venueId + 
                   "/courts?date=" + bookingDate +
                   "&editBookingId=" + bookingId +
                   "&originalCourtId=" + courtId +
                   "&originalTimeslotId=" + timeslotId +
                   "&originalUserId=" + userId;
                   
        } catch (Exception e) {
            logger.error("Error loading booking for edit: {}", bookingId, e);
            redirectAttributes.addFlashAttribute("error", "Error loading booking: " + e.getMessage());
            return "redirect:/admin";
        }
    }

    @PostMapping("/update-booking")
    public String updateBooking(@RequestParam(value = "selectedSlots", required = false) List<String> selectedSlots,
                               @RequestParam(required = false) Long editBookingId,
                               @RequestParam(required = false) Long originalUserId,
                               RedirectAttributes redirectAttributes) {
        
        // Validate required parameters
        if (editBookingId == null) {
            logger.error("Update booking called without editBookingId");
            redirectAttributes.addFlashAttribute("error", "Missing booking ID");
            return "redirect:/admin";
        }
        
        if (originalUserId == null) {
            logger.error("Update booking called without originalUserId");
            redirectAttributes.addFlashAttribute("error", "Missing user ID");
            return "redirect:/admin";
        }
        
        if (selectedSlots == null || selectedSlots.isEmpty()) {
            logger.warn("No slots selected for booking update {}", editBookingId);
            redirectAttributes.addFlashAttribute("error", "Please select a time slot");
            return "redirect:/admin";
        }
        
        if (selectedSlots.size() > 1) {
            logger.warn("Multiple slots selected for booking update {}", editBookingId);
            redirectAttributes.addFlashAttribute("error", "Please select only one time slot");
            return "redirect:/admin";
        }
        
        try {
            String slot = selectedSlots.get(0);
            logger.info("Processing update for booking {} with slot: {}", editBookingId, slot);
            
            String[] parts = slot.split("-", 3);
            
            if (parts.length != 3) {
                logger.error("Invalid slot format: {}", slot);
                redirectAttributes.addFlashAttribute("error", "Invalid slot format");
                return "redirect:/admin";
            }
            
            Long courtId = Long.parseLong(parts[0]);
            Long timeslotId = Long.parseLong(parts[1]);
            LocalDate bookingDate = LocalDate.parse(parts[2]);
            
            logger.info("Updating booking {} to Court: {}, Timeslot: {}, Date: {}", 
                editBookingId, courtId, timeslotId, bookingDate);
            
            bookingService.updateBooking(editBookingId, courtId, timeslotId, bookingDate, originalUserId);
            
            redirectAttributes.addFlashAttribute("success", 
                "Booking #" + editBookingId + " updated successfully!");
            
            logger.info("Successfully updated booking {}", editBookingId);
                
        } catch (NumberFormatException e) {
            logger.error("Invalid number format in slot data for booking {}", editBookingId, e);
            redirectAttributes.addFlashAttribute("error", "Invalid booking data format");
        } catch (RuntimeException e) {
            logger.error("Error updating booking {}: {}", editBookingId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error updating booking: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error updating booking {}", editBookingId, e);
            redirectAttributes.addFlashAttribute("error", "Technical error occurred: " + e.getMessage());
        }
        
        return "redirect:/admin";
    }

    @PostMapping("/delete/{bookingId}")
    public String deleteBooking(@PathVariable Long bookingId, 
                               RedirectAttributes redirectAttributes) {
        if (bookingId == null) {
            logger.error("Delete booking called without bookingId");
            redirectAttributes.addFlashAttribute("error", "Missing booking ID");
            return "redirect:/admin";
        }
        
        try {
            logger.info("Attempting to delete booking {}", bookingId);
            bookingService.deleteBooking(bookingId);
            redirectAttributes.addFlashAttribute("success", "Booking #" + bookingId + " deleted successfully!");
            logger.info("Successfully deleted booking {}", bookingId);
        } catch (Exception e) {
            logger.error("Error deleting booking {}", bookingId, e);
            redirectAttributes.addFlashAttribute("error", "Error deleting booking: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    @GetMapping("/deleteBooking")
    @ResponseBody

    public String deleteBookingAjax(@PathVariable Long bookingId) {
        if (bookingId == null) {
            logger.error("AJAX delete called without bookingId");
            return "ERROR: Missing booking ID";
        }
        
        try {
            logger.info("AJAX delete request for booking {}", bookingId);
            bookingService.deleteBooking(bookingId);
            logger.info("Successfully deleted booking {} via AJAX", bookingId);
            return "SUCCESS";
        } catch (Exception e) {
            logger.error("Error in AJAX delete for booking {}", bookingId, e);
            return "ERROR: " + e.getMessage();
        }
    }
}
