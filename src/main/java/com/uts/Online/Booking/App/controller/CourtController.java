package com.uts.Online.Booking.App.controller;

import com.uts.Online.Booking.App.model.Booking;
import com.uts.Online.Booking.App.model.Court;
import com.uts.Online.Booking.App.model.Timeslot;
import com.uts.Online.Booking.App.model.Venue;
import com.uts.Online.Booking.App.DAO.BookingDAO;
import com.uts.Online.Booking.App.DAO.CourtDAO;
import com.uts.Online.Booking.App.DAO.TimeslotDAO;
import com.uts.Online.Booking.App.DAO.VenueDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

import java.util.*;
import java.util.stream.Collectors;
import java.time.*;
import java.time.format.DateTimeFormatter;

@Controller
public class CourtController {
    @Autowired
    private CourtDAO courtDAO;

    @Autowired
    private TimeslotDAO timeslotDAO;

    @Autowired
    private BookingDAO bookingDAO;

    @Autowired
    private VenueDAO venueDAO;

    @GetMapping("/venue/{venueId}/courts")
    public String showVenueCourts(@PathVariable Long venueId,
                                @RequestParam(required = false) String date,
                                Model model) {
        // Get selected date or use today
        LocalDate selectedDate;
        try {
            selectedDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        } catch (Exception e) {
            selectedDate = LocalDate.now();
        }

        // Get venue info
        Venue venue = venueDAO.findById(venueId).orElse(null);
        if (venue == null) {
            return "redirect:/venues";
        }

        // Get courts for this venue
        List<Court> courts = courtDAO.findByVenueVenueId(venueId);
        
        // Get all timeslots
        List<Timeslot> timeslots = timeslotDAO.findAll();
        
        // Get bookings for selected date
        List<Booking> bookings = bookingDAO.findBookingsForDate(selectedDate);

        // Format header slots
        List<String> headerSlots = timeslots.stream()
            .map(slot -> slot.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")))

            .collect(Collectors.toList());

        // Create availability map
        Map<String, Boolean> availability = new HashMap<>();
        
        // Initialize all slots as available
        courts.forEach(court -> {
            timeslots.forEach(slot -> {
                String key = court.getCourtId() + "-" + slot.getTimeslotId();
                availability.put(key, true);
            });
        });

        // Mark booked slots
        bookings.forEach(booking -> {
            String key = booking.getCourt().getCourtId() + "-" + booking.getTimeslot().getTimeslotId();
            availability.put(key, false);
        });

        // Add everything to model
        model.addAttribute("venue", venue);
        model.addAttribute("courts", courts);
        model.addAttribute("timeSlots", timeslots);
        model.addAttribute("headerSlots", headerSlots);
        model.addAttribute("availability", availability);
        model.addAttribute("selectedDate", selectedDate);

        // NEW: Check if we're in edit mode (from admin redirect)
        if (model.containsAttribute("editMode")) {
            model.addAttribute("pageTitle", "Edit Booking - " + venue.getVenueName());
            model.addAttribute("submitButtonText", "Update Booking");
            model.addAttribute("submitAction", "/admin/update-booking");
            
            // Pre-select the original slot
            Long originalCourtId = (Long) model.asMap().get("originalCourtId");
            Long originalTimeslotId = (Long) model.asMap().get("originalTimeslotId");
            if (originalCourtId != null && originalTimeslotId != null) {
                String originalSlotKey = originalCourtId + "-" + originalTimeslotId;
                model.addAttribute("preSelectedSlot", originalSlotKey);
            }
        } else {
            // Normal booking mode
            model.addAttribute("pageTitle", venue.getVenueName());
            model.addAttribute("submitButtonText", "Proceed to Book");
            model.addAttribute("submitAction", "/book");
        }

        return "court";
    }
}