package com.uts.Online.Booking.App.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.uts.Online.Booking.App.DAO.UserDAO;
import com.uts.Online.Booking.App.model.User;

import jakarta.servlet.http.HttpSession;

import org.springframework.ui.Model;


@Controller
public class RegisterController {
    
    private final UserDAO userDAO;

    RegisterController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @GetMapping("/register")
    public String registerPage(){
        return "register";
    }

    @PostMapping("/register")
    public String RegisterUser(@RequestParam("firstName") String fname, @RequestParam("lastName") String lname, @RequestParam("email") String email, @RequestParam("password") String password, 
    @RequestParam("phoneNumber") int phoneNumber, Model m, HttpSession session){

        if(userDAO.findByEmail(email).isPresent()){
            m.addAttribute("error", "Email already exists!");
            return "register";
        } else{
            User newUser = new User();
            newUser.setFirstName(fname);
            newUser.setlastName(lname);
            newUser.setEmail(email);
            newUser.setPassword(password);
            newUser.setPhoneNumber(phoneNumber);
            userDAO.save(newUser);

            session.setAttribute("user", newUser);

            return "redirect:/main";
        }
    }


}