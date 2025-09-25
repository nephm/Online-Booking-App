package com.uts.Online.Booking.App.controller;

import org.springframework.web.bind.annotation.GetMapping;

public class ProfileController {
    
    @GetMapping("/profile")
    public String getProfilePage() {
        return "profile";
    }
}
