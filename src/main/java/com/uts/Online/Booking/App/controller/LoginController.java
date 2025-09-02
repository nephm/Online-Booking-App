package com.uts.Online.Booking.App.controller;

import org.springframework.boot.autoconfigure.graphql.GraphQlProperties.Http;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.uts.Online.Booking.App.DAO.UserDAO;
import com.uts.Online.Booking.App.model.User;

import jakarta.servlet.http.HttpSession;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
public class LoginController {

    private final UserDAO userDAO;

    LoginController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
    
    @GetMapping("/login")
    public String showLogin() {
        return "login"; // looks in templates/login.html
    }

    @PostMapping("/login")
    public String login(@RequestParam("email") String email, @RequestParam("password") String password, Model m, HttpSession session) {
        
        if(userDAO.findByEmail(email).isPresent()){
            User u = userDAO.findByEmail(email).get();
            if(u.getPassword().equals(password)){
                session.setAttribute("loggedIn", u);
                return "redirect:/main";
            } else{
                m.addAttribute("error", "Invalid password!");
                return "login";
            }
        } else{
            m.addAttribute("error", "Email not found");
            return "login";
        }
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession s) {
        s.invalidate();
        return "redirect:/login?logout";
    }
    

}
