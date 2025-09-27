package com.uts.Online.Booking.App.controller;

import com.uts.Online.Booking.App.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/book")
    public String bookSlots(@RequestParam("selectedSlots") List<String> selectedSlots,
                           RedirectAttributes redirectAttributes) {
        
        // Check if no slots selected
        if(selectedSlots == null || selectedSlots.isEmpty()) {
            System.out.println("No slots selected");
            return "redirect:/error?message=Please select at least one time slot";
        }
        
        try {
            // Process each selected slot
            for (String slot : selectedSlots) {
                System.out.println("Processing slot: " + slot);
                
                // Split slot format: "courtId-timeslotId-date"
                String[] parts = slot.split("-", 3);
                
                if (parts.length != 3) {
                    System.err.println("Invalid slot format: " + slot);
                    continue; // Skip invalid slots but continue processing others
                }
                
                Long courtId = Long.parseLong(parts[0]);
                Long timeslotId = Long.parseLong(parts[1]);
                LocalDate bookingDate = LocalDate.parse(parts[2]);
                
                System.out.println("Court: " + courtId + ", Timeslot: " + timeslotId + ", Date: " + bookingDate);
                
                // Create the booking (assuming userId = 1 for now)
                bookingService.createBooking(courtId, timeslotId, bookingDate, 1);
            }
            
            // Success  add flash attributes and redirect to confirmation
            redirectAttributes.addFlashAttribute("success", "Your booking has been confirmed successfully!");
            redirectAttributes.addFlashAttribute("bookingCount", selectedSlots.size());
            
            System.out.println("Booking successful. Redirecting to confirmation page.");
            return "redirect:/booking-confirmation";
            
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format in slot data: " + e.getMessage());
            return "redirect:/error?message=Invalid booking data format. Please try again";
            
        } catch (Exception e) {
            System.err.println("Error processing booking: " + e.getMessage());
            e.printStackTrace(); // For debugging
            return "redirect:/error?message=Technical error occurred. Please try again or contact support";
        }
    }

    @GetMapping("/booking-confirmation")
    public String showBookingConfirmation() {
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