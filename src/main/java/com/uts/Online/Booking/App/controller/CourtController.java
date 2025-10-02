package com.uts.Online.Booking.App.controller;

import com.uts.Online.Booking.App.model.Booking;
import com.uts.Online.Booking.App.model.Court;
import com.uts.Online.Booking.App.model.Timeslot;
import com.uts.Online.Booking.App.model.Venue;
import com.uts.Online.Booking.App.DAO.BookingDAO;
import com.uts.Online.Booking.App.DAO.CourtDAO;
import com.uts.Online.Booking.App.DAO.TimeslotDAO;
import com.uts.Online.Booking.App.DAO.VenueDAO;
import com.uts.Online.Booking.App.service.BookingService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;
import java.time.*;
import java.time.format.DateTimeFormatter;

@Controller
public class CourtController {
    
    private static final Logger logger = LoggerFactory.getLogger(CourtController.class);
    
    @Autowired
    private CourtDAO courtDAO;

    @Autowired
    private TimeslotDAO timeslotDAO;

    @Autowired
    private BookingDAO bookingDAO;

    @Autowired
    private VenueDAO venueDAO;

    @Autowired
    private BookingService bookingService;

    @GetMapping("/venue/{venueId}/courts")
    public String showVenueCourts(@PathVariable Long venueId,
                                @RequestParam(required = false) String date,
                                @RequestParam(required = false) Long editBookingId,
                                @RequestParam(required = false) Long originalCourtId,
                                @RequestParam(required = false) Long originalTimeslotId,
                                @RequestParam(required = false) Long originalUserId,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        
        // Get venue info first
        Venue venue = venueDAO.findById(venueId).orElse(null);
        if (venue == null) {
            logger.error("Venue not found with ID: {}", venueId);
            redirectAttributes.addFlashAttribute("error", "Venue not found");
            return "redirect:/venues";
        }

        // Get selected date or use today with validation
        LocalDate selectedDate;
        try {
            if (date != null && !date.trim().isEmpty()) {
                selectedDate = LocalDate.parse(date);
                // Validate date is not in the past
                if (selectedDate.isBefore(LocalDate.now())) {
                    logger.warn("Attempted to select past date: {}", date);
                    redirectAttributes.addFlashAttribute("error", "Cannot select dates in the past");
                    selectedDate = LocalDate.now();
                }
            } else {
                selectedDate = LocalDate.now();
            }
        } catch (Exception e) {
            logger.error("Invalid date format: {}", date, e);
            redirectAttributes.addFlashAttribute("error", "Invalid date format. Using today's date.");
            selectedDate = LocalDate.now();
        }

        // Generate available dates (next 60 days)
        List<LocalDate> availableDates = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            availableDates.add(LocalDate.now().plusDays(i));
        }

        // Get courts for this venue
        List<Court> courts = courtDAO.findByVenueVenueId(venueId);
        if (courts == null || courts.isEmpty()) {
            logger.warn("No courts found for venue ID: {}", venueId);
            redirectAttributes.addFlashAttribute("error", "No courts available for this venue");
            return "redirect:/venues";
        }
        
        // Get all timeslots
        List<Timeslot> timeslots = timeslotDAO.findAll();
        if (timeslots == null || timeslots.isEmpty()) {
            logger.error("No timeslots found in the system");
            redirectAttributes.addFlashAttribute("error", "No time slots configured");
            return "redirect:/venues";
        }

        // Format header slots
        List<String> headerSlots = timeslots.stream()
            .map(slot -> slot.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")))
            .collect(Collectors.toList());

        // Get availability map using BookingService
        Map<String, Boolean> availability = bookingService.getAvailabilityMap(venueId, selectedDate, courts, timeslots);

        // Add everything to model
        model.addAttribute("venue", venue);
        model.addAttribute("courts", courts);
        model.addAttribute("timeSlots", timeslots);
        model.addAttribute("headerSlots", headerSlots);
        model.addAttribute("availability", availability);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("availableDates", availableDates);

        // Check for edit mode - prioritize request parameters over flash attributes
        boolean isEditMode = editBookingId != null || model.containsAttribute("editMode");
        
        if (isEditMode) {
            // Use request params if available, otherwise fall back to flash attributes
            Long bookingId = editBookingId != null ? editBookingId : (Long) model.asMap().get("editBookingId");
            Long courtId = originalCourtId != null ? originalCourtId : (Long) model.asMap().get("originalCourtId");
            Long timeslotId = originalTimeslotId != null ? originalTimeslotId : (Long) model.asMap().get("originalTimeslotId");
            Long userId = originalUserId != null ? originalUserId : (Long) model.asMap().get("originalUserId");
            
            // Validate that we have all required edit parameters
            if (bookingId == null || courtId == null || timeslotId == null || userId == null) {
                logger.error("Missing edit parameters - bookingId: {}, courtId: {}, timeslotId: {}, userId: {}", 
                    bookingId, courtId, timeslotId, userId);
                redirectAttributes.addFlashAttribute("error", "Invalid edit request. Missing parameters.");
                return "redirect:/admin";
            }

            try {
                // Get original booking for display
                Booking originalBooking = bookingService.getBookingById(bookingId);
                if (originalBooking == null) {
                    logger.error("Booking not found with ID: {}", bookingId);
                    redirectAttributes.addFlashAttribute("error", "Booking not found");
                    return "redirect:/admin";
                }

                // Validate booking belongs to the correct venue
                if (originalBooking.getCourt() == null || 
                    originalBooking.getCourt().getVenue() == null || 
                    !originalBooking.getCourt().getVenue().getVenueId().equals(venueId)) {
                    logger.error("Booking {} does not belong to venue {}", bookingId, venueId);
                    redirectAttributes.addFlashAttribute("error", "Invalid booking for this venue");
                    return "redirect:/admin";
                }

                // Format original date for display
                String originalDateStr = "Unknown";
                if (originalBooking.getBookingDate() != null && originalBooking.getTimeslot() != null) {
                    originalDateStr = originalBooking.getBookingDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) + 
                                    " at " + originalBooking.getTimeslot().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"));
                }

                model.addAttribute("editMode", true);
                model.addAttribute("editBookingId", bookingId);
                model.addAttribute("originalCourtId", courtId);
                model.addAttribute("originalTimeslotId", timeslotId);
                model.addAttribute("originalUserId", userId);
                model.addAttribute("originalDate", originalDateStr);
                model.addAttribute("pageTitle", "Edit Booking - " + venue.getVenueName());
                model.addAttribute("submitButtonText", "Update Booking");
                model.addAttribute("submitAction", "/admin/update-booking");
                
                String originalSlotKey = courtId + "-" + timeslotId;
                model.addAttribute("preSelectedSlot", originalSlotKey);
                logger.info("Edit mode active for booking {}, preSelectedSlot: {}", bookingId, originalSlotKey);
                
            } catch (Exception e) {
                logger.error("Error loading booking for edit: {}", bookingId, e);
                redirectAttributes.addFlashAttribute("error", "Error loading booking: " + e.getMessage());
                return "redirect:/admin";
            }
        } else {
            model.addAttribute("editMode", false);
            model.addAttribute("pageTitle", venue.getVenueName());
            model.addAttribute("submitButtonText", "Proceed to Book");
            model.addAttribute("submitAction", "/book");
        }
      
        return "court";
    }
}