package com.uts.Online.Booking.App.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import com.uts.Online.Booking.App.DAO.UserDAO;
import com.uts.Online.Booking.App.model.User;
import com.uts.Online.Booking.App.service.PlayerService;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class PlayerController {

    private final PlayerService playerService;

    private final UserDAO userDAO;

    public PlayerController(PlayerService playerService, UserDAO userDAO){
        this.playerService = playerService;
        this.userDAO = userDAO;
    }
    
    @GetMapping("/credit")
    public String showBalance(Model m) {
        //retrieve the current user's email
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userDAO.findByEmail(email).orElse(null);

        if(user == null){
            return "redirect:/login";
        }

        //get player's credit balance from database
        double balance = playerService.getCreditBalance(email);

        m.addAttribute("user", user);
        m.addAttribute("balance", balance);
        return "credit";
    }
    
}
