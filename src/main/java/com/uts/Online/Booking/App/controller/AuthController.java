// package com.uts.Online.Booking.App.controller;

// import org.springframework.boot.autoconfigure.graphql.GraphQlProperties.Http;
// import org.springframework.stereotype.Controller;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestParam;

// import com.uts.Online.Booking.App.DAO.UserDAO;
// import com.uts.Online.Booking.App.model.User;

// import com.uts.Online.Booking.App.DAO.AdminDAO;
// import com.uts.Online.Booking.App.model.Admin;

// import jakarta.servlet.http.HttpSession;

// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.RequestBody;


// @Controller
// public class AuthController {

//     private final PlayerDAO PlayerDAO;
//     private final AdminDAO AdminDAO;

//     AuthController(UserDAO userDAO) {
//         this.userDAO = userDAO;
//     }
    
//     @GetMapping("/login")
//     public String showLogin() {
//         return "login"; // looks in templates/login.html
//     }

//     @PostMapping("/login")
//     public String login(@RequestParam("email") String email, @RequestParam("password") String password, Model m, HttpSession session) {
        
//         if(userDAO.findByEmail(email).isPresent()){
//             User u = userDAO.findByEmail(email).get();
//             if(u.getPassword().equals(password)){
//                 session.setAttribute("loggedIn", u);

//              //check if user is admin
//              if(adminDAO.existsByAdminId(u.getUserId())){
//                  return "redirect:/admin_Dashboard"
//              } else{
//                  return "redirect:/main"; //user is player
//              }
//             } else{
//                 m.addAttribute("error", "Invalid password!");
//                 return "login";
//             }
//         } else{
//             m.addAttribute("error", "Email not found");
//             return "login";
//         }
//     }
    
//     @GetMapping("/logout")
//     public String logout(HttpSession s) {
//         s.invalidate();
//         return "redirect:/login?logout";
//     }


//     @GetMapping("/register")
//     public String registerPage(){
//         return "register";
//     }

//     @PostMapping("/register")
//     public String RegisterUser(@RequestParam("firstName") String fname, @RequestParam("lastName") String lname, @RequestParam("email") String email, @RequestParam("password") String password, 
//     @RequestParam("phoneNumber") int phoneNumber, Model m, HttpSession session){

//         if(userDAO.findByEmail(email).isPresent()){
//             m.addAttribute("error", "Email already exists!");
//             return "register";
//         } else{
//             User newUser = new User();
//             newUser.setFirstName(fname);
//             newUser.setlastName(lname);
//             newUser.setEmail(email);
//             newUser.setPassword(password);
//             newUser.setPhoneNumber(phoneNumber);
//             userDAO.save(newUser);

//             session.setAttribute("user", newUser);

//             return "redirect:/main";
//         }
//     }

    

// }
