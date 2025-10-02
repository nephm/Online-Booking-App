package com.uts.Online.Booking.App.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.uts.Online.Booking.App.service.UserService;

@Controller
public class ProfileController {

    @Autowired
    private UserService userService;
    
    @GetMapping("/profile")
    public String getProfilePage(Model m) {
        m.addAttribute("user", userService.getUser());
        return "profile";
    }
}
