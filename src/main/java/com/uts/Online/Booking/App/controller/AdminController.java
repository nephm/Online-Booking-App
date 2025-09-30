package com.uts.Online.Booking.App.controller;

import com.uts.Online.Booking.App.model.Booking;
import com.uts.Online.Booking.App.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private BookingService bookingService;

    @GetMapping
    public String admin(Model model) {
        try {
            List<Booking> bookings = bookingService.getAllBookings();
            model.addAttribute("bookings", bookings);
        } catch (Exception e) {
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
                redirectAttributes.addFlashAttribute("error", "Booking not found");
                return "redirect:/admin";
            }
            
            redirectAttributes.addFlashAttribute("editMode", true);
            redirectAttributes.addFlashAttribute("editBookingId", bookingId);
            redirectAttributes.addFlashAttribute("originalCourtId", booking.getCourt().getCourtId());
            redirectAttributes.addFlashAttribute("originalTimeslotId", booking.getTimeslot().getTimeslotId());
            redirectAttributes.addFlashAttribute("originalDate", booking.getBookingDate());
            redirectAttributes.addFlashAttribute("originalUserId", booking.getUserId());
            
            return "redirect:/venue/" + booking.getCourt().getVenue().getVenueId() + 
                   "/courts?date=" + booking.getBookingDate();
                   
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading booking: " + e.getMessage());
            return "redirect:/admin";
        }
    }

    @PostMapping("/update-booking")
    public String updateBooking(@RequestParam("selectedSlots") List<String> selectedSlots,
                               @RequestParam Long editBookingId,
                               @RequestParam Long originalUserId,
                               RedirectAttributes redirectAttributes) {
        
        if (selectedSlots == null || selectedSlots.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a time slot");
            return "redirect:/admin";
        }
        
        if (selectedSlots.size() > 1) {
            redirectAttributes.addFlashAttribute("error", "Please select only one time slot");
            return "redirect:/admin";
        }
        
        try {
            String slot = selectedSlots.get(0);
            String[] parts = slot.split("-", 3);
            
            if (parts.length != 3) {
                redirectAttributes.addFlashAttribute("error", "Invalid slot format");
                return "redirect:/admin";
            }
            
            Long courtId = Long.parseLong(parts[0]);
            Long timeslotId = Long.parseLong(parts[1]);
            LocalDate bookingDate = LocalDate.parse(parts[2]);
            
            bookingService.updateBooking(editBookingId, courtId, timeslotId, bookingDate, originalUserId);
            
            redirectAttributes.addFlashAttribute("success", 
                "Booking #" + editBookingId + " updated successfully!");
                
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error updating booking: " + e.getMessage());
        }
        
        return "redirect:/admin";
    }

    @PostMapping("/delete/{bookingId}")
    public String deleteBooking(@PathVariable Long bookingId, 
                               RedirectAttributes redirectAttributes) {
        try {
            bookingService.deleteBooking(bookingId);
            redirectAttributes.addFlashAttribute("success", "Booking deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting booking: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    @DeleteMapping("/api/booking/{bookingId}")
    @ResponseBody
    public String deleteBookingAjax(@PathVariable Long bookingId) {
        try {
            bookingService.deleteBooking(bookingId);
            return "SUCCESS";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}