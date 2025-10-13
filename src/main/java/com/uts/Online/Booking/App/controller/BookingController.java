package com.uts.Online.Booking.App.controller;

import com.uts.Online.Booking.App.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private BookingService bookingService;

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
                    
                    // TODO: Replace hardcoded userId with actual authenticated user
                    bookingService.createBooking(courtId, timeslotId, bookingDate, 1L);
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
            
            // Giving user status of booking action
            if (successCount > 0 && failureCount == 0) {
                redirectAttributes.addFlashAttribute("success", 
                    "Your booking has been confirmed successfully! Booked " + successCount + " slot(s).");
                redirectAttributes.addFlashAttribute("bookingCount", successCount);
                logger.info("Booking successful: {} slot(s) booked", successCount);
                return "redirect:/booking-confirmation";
                
            } else if (successCount > 0 && failureCount > 0) {
                redirectAttributes.addFlashAttribute("success", 
                    successCount + " slot(s) booked successfully.");
                redirectAttributes.addFlashAttribute("error", 
                    failureCount + " slot(s) failed: " + errorMessages.toString());
                logger.warn("Partial booking success: {} succeeded, {} failed", successCount, failureCount);
                return "redirect:/booking-confirmation";
                
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
}