package com.uts.Online.Booking.App.controller;

import com.uts.Online.Booking.App.DAO.UserDAO;
import com.uts.Online.Booking.App.model.User;
import com.uts.Online.Booking.App.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Autowired
    private UserDAO userDAO;

    private User getUser(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userDAO.findByEmail(auth.getName()).orElse(null);
    }

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

            Double totalAmount = bookingService.calculateTotalAmount(selectedSlots);
            Long bookingId = null;

            if(getUser() == null){
                return "redirect:/login?message=Please log in to make a booking";
            }

            // Process each selected slot
            for (String slot : selectedSlots) {
                logger.debug("Processing slot: {}", slot);
                
                // Split slot format: "courtId-timeslotId-date"
                String[] parts = slot.split("-", 3);
                
                if (parts.length != 3) {

                    logger.error("Invalid slot format: {}", slot);
                    failureCount++;
                    errorMessages.append("Invalid slot format: ").append(slot).append("; ");
                    continue;
                }
                
                Long courtId = Long.parseLong(parts[0]);
                Long timeslotId = Long.parseLong(parts[1]);
                LocalDate bookingDate = LocalDate.parse(parts[2]);
                
                System.out.println("Court: " + courtId + ", Timeslot: " + timeslotId + ", Date: " + bookingDate);

                // Create the booking (assuming userId = 1 for now)
                bookingId = bookingService.createBooking(courtId, timeslotId, bookingDate, getUser().getId());
               
            }
            
            // Provide feedback based on results
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
    
    //error page for later
    // @GetMapping("/error")
    // public String showErrorPage(@RequestParam(value = "message", required = false) String message, 
    //                            Model model) {
    //     model.addAttribute("errorMessage", message != null ? message : "An unexpected error occurred");
    //     return "error"; // You'll need to create an error.html template
    // }
}