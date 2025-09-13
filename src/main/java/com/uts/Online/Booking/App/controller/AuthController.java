package com.uts.Online.Booking.App.controller;

import org.springframework.boot.autoconfigure.graphql.GraphQlProperties.Http;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Autowired;

import com.uts.Online.Booking.App.DAO.UserDAO;
import com.uts.Online.Booking.App.model.User;

import com.uts.Online.Booking.App.DAO.AdminDAO;
import com.uts.Online.Booking.App.model.Admin;

import jakarta.servlet.http.HttpSession;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
public class AuthController {

    private final UserDAO userDAO;
    private final AdminDAO adminDAO;
    @Autowired
    private JavaMailSender mailSender;

    AuthController(UserDAO userDAO, AdminDAO adminDAO) {
        this.userDAO = userDAO;
        this.adminDAO = adminDAO;
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

             //check if user is admin
             if(adminDAO.existsByAdminId(u.getId())){
                 return "redirect:/admin";
             } else{
                 return "redirect:/main"; //user is player
             }
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
      if(u == null){
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
}
