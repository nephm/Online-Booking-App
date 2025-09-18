package com.uts.Online.Booking.App.controller;

import com.uts.Online.Booking.App.model.Court;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;

@Controller
public class CourtController {
    @GetMapping("/court")
    public String showCourts(Model model) {
        // Hardcoded list of courts
        List<Court> courts = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            courts.add(new Court((long) i, "Court " + i, true));
        }

        // Fixed time slots
        List<String> timeSlots = List.of(
            "10:00am", "11:00am", "12:00pm", "1:00pm", "2:00pm",
            "3:00pm", "4:00pm", "5:00pm", "6:00pm", "7:00pm",
            "8:00pm", "9:00pm", "10:00pm"
        );

        model.addAttribute("courts", courts);
        model.addAttribute("timeSlots", timeSlots);

        return "court"; // loads templates/court.html
    }
}
