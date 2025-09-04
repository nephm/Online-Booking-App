package com.uts.Online.Booking.App.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

public class AdminController {
    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/editBooking")
    @ResponseBody
    public String editBooking(String bookingId) {
        System.out.println("This button edits bookings");
        return "Edit booking";
    }

    @GetMapping("/deleteBooking")
    @ResponseBody
    public String deleteBooking(String bookingId) {
        System.out.println("This button deletes bookings");
        return "Deletes booking";
    }
}
