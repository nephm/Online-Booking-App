package com.uts.Online.Booking.App.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.uts.Online.Booking.App.DAO.UserDAO;
import com.uts.Online.Booking.App.model.Player;
import com.uts.Online.Booking.App.model.User;

import jakarta.servlet.http.HttpSession;

import org.springframework.ui.Model;


@Controller
public class AuthController {

    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    public AuthController(UserDAO userDAO, PasswordEncoder passwordEncoder){
        this.passwordEncoder = passwordEncoder;
        this.userDAO = userDAO;
    }
    
    //show the login page and add error messages 
    @GetMapping("/login")
    public String showLogin(@RequestParam(value = "error", required = false) String error, @RequestParam(value = "logout", required = false) String logout, Model m, HttpSession session) {
        
        if (error != null) {
            m.addAttribute("error", "Invalid email or password");
        }
        if (logout != null) {
            m.addAttribute("message", "You have been logged out successfully");
        }
        return "login"; // looks in templates/login.html
    }

    //create a new email to be sent
    private void sendEmail(String to, String subject, String text){
        SimpleMailMessage message = new SimpleMailMessage();
         message.setTo(to);
         message.setSubject(subject);
         message.setText(text);
         mailSender.send(message);
    }

    //set the account to be active afteremail verification and save the user into the database
    @GetMapping("/activate")
    public String activateAccount(@RequestParam("token") String token, Model m){
        User u = userDAO.findByActivationToken(token);
        if(u != null){
            u.setActive(true);
            u.setActivationToken(null);
            userDAO.save(u);
            m.addAttribute("status", "success");
            m.addAttribute("message", "Your account has been successfully  activated"); 
        } else{
            m.addAttribute("status", "failed");
            m.addAttribute("error", "Invalid activation link");
        }

        return "activation_status";
    }

    @GetMapping("/register")
    public String registerPage(){
        return "register";
    }

    //create a new user in the database based on the given user details during regsiter
    @PostMapping("/register")
    public String RegisterUser(@RequestParam("firstName") String fname, @RequestParam("lastName") String lname, @RequestParam("email") String email, @RequestParam("password") String password, 
    @RequestParam("phoneNumber") String phoneNumber, Model m, HttpSession session){

        System.out.println("=== REGISTRATION ATTEMPT ===");
        System.out.println("Name: " + fname + " " + lname);
        System.out.println("Email: " + email);
        System.out.println("Phone: " + phoneNumber);

        // Validation: Check if email already exists
        if(userDAO.findByEmail(email).isPresent()){
            System.out.println("ERROR: Email already exists");
            m.addAttribute("error", "Email already exists!");
            m.addAttribute("firstName", fname);
            m.addAttribute("lastName", lname);
            m.addAttribute("phoneNumber", phoneNumber);
            return "register";
        } 

        if(userDAO.findByEmail(email).isPresent()){
            System.out.println("ERROR: Email Existst");
            m.addAttribute("error", "Email already exists!");
            return "register";
        } 

        if(password.length() < 8){
            System.out.println("ERROR: Password too short");
            m.addAttribute("error", "Password must be at least 8 characters long");
            return "register";
        }

        if(phoneNumber.length() < 10 || phoneNumber.length() > 15 || phoneNumber.matches(".*[a-zA-Z]+.*")){
            System.out.println("ERROR: Phone number is invalid");
            m.addAttribute("error", "Phone number is invalid");
            return "register";
        }

        //create new player by default
        Player newUser = new Player();
        newUser.setFirstName(fname);
        newUser.setLastName(lname);
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setPhoneNumber(phoneNumber);
        newUser.setCreditBalance(0.0);
            
        //generate activation token
        String token = java.util.UUID.randomUUID().toString();
        newUser.setActivationToken(token);
        newUser.setActive(false);

        //save the user to the database
        userDAO.save(newUser);

        //send notification
        try {
            String activationLink = "http://localhost:8080/activate?token=" + token;
            sendEmail(email, "Account Activation", "Click the link to activate your account: " + activationLink);

        } catch (Exception e) {
            e.printStackTrace();
            m.addAttribute("error", "failed to send activation email.");
            return "register";
        }
        return "redirect:/registration_email";
    }

    @GetMapping("/registration_email")
    public String getRegistrationEmailPage() {
        return "registration_email";
    }

    //show the profile page for the logged in user
    @GetMapping("/profile")
    public String getProfilePage(Model m) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userDAO.findByEmail(email).orElse(null);

        if(user == null){
            return "redirect:/login";
        }

        m.addAttribute("user", user);
        return "profile";
    }
    
}
