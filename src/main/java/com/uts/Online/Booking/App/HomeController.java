package com.uts.Online.Booking.App;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@Controller
public class HomeController {
    
    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/main")
    public String main() {
        return "main";
    }

    @GetMapping("/login")
    public String showLogin() {
        return "login"; // looks in templates/login.html
    }

    @PostMapping("/login")
    public String loginprocess(@RequestParam String email, @RequestParam String password, Model m) {
        
        if (email.equals("test@badmin.com") && password.equals("12345")){
            m.addAttribute("user", email);
            return "main";
        } else{
            m.addAttribute("error", "Invalid");
            return "login";
        }
    }
    
    
     @GetMapping("/register")
    public String registerPage(){
        return "register";
    }

    @PostMapping("/register")
    public String registerprocess(@RequestParam String firstName, @RequestParam String lastName, @RequestParam String email, @RequestParam String password, Model m) {
        
        m.addAttribute("message", "User registered" + email);
        return "login";
    }

    @GetMapping("/logout")
    public String logoutPage(){
        return "logout";
    }

    @PostMapping("/logout")
    public String logoutprocess(HttpSession session) {
        
        session.invalidate();
        return "logout";
    }
}
