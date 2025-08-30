package com.uts.Online.Booking.App.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.uts.Online.Booking.App.DAO.UserDAO;
import com.uts.Online.Booking.App.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.htpp.HttpSession;


@Controller
public class RegisterController {
    
    private final UserDAO userDAO;

    @GetMapping("/register")
    public String registerPage(){
        return "register";
    }

    
}