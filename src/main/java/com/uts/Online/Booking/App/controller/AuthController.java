package com.uts.Online.Booking.App.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.uts.Online.Booking.App.DAO.UserDAO;
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

    private void sendEmail(String to, String subject, String text){
        SimpleMailMessage message = new SimpleMailMessage();
         message.setTo(to);
         message.setSubject(subject);
         message.setText(text);
         mailSender.send(message);
    }

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

    @PostMapping("/register")
    public String RegisterUser(@RequestParam("firstName") String fname, @RequestParam("lastName") String lname, @RequestParam("email") String email, @RequestParam("password") String password, 
    @RequestParam("phoneNumber") String phoneNumber, Model m, HttpSession session){

        if(userDAO.findByEmail(email).isPresent()){
            m.addAttribute("error", "Email already exists!");
            return "register";
        } 

        if(password.length() < 6){
            m.addAttribute("error", "Password must be at least 8 characters long");
            return "register";
        }

        User newUser = new User();
        newUser.setFirstName(fname);
        newUser.setLastName(lname);
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setPhoneNumber(phoneNumber);
            
        //generate activation token
        String token = java.util.UUID.randomUUID().toString();
        newUser.setActivationToken(token);
        newUser.setActive(false);

        userDAO.save(newUser);

        //send notification
        String activationLink = "http://localhost:8080/activate?token=" + token;
        sendEmail(email, "Account Activation", "Click the link to activate your account: " + activationLink);

        return "redirect:/registration_email";
    }
}
