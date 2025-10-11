package com.uts.Online.Booking.App.controller;

import com.uts.Online.Booking.App.model.User;
import com.uts.Online.Booking.App.service.UserService;
import com.uts.Online.Booking.App.DAO.UserDAO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final UserDAO userDAO;

    public AdminController(UserService userService, UserDAO userDAO) {
        this.userService = userService;
        this.userDAO = userDAO;
    }

    @GetMapping
    public String adminHome() {
        return "admin"; 
    }

    @GetMapping("/users")
    public String viewAllUsers(Model model) {
        List<User> users = userDAO.findAll();
        model.addAttribute("users", users);
        return "admin-users"; 
    }

    @GetMapping("/roles")
    public String viewRoles(Model model) {
        List<User> users = userDAO.findAll();
        model.addAttribute("users", users);
        return "roles";
    }

    @PostMapping("/setRole")
    public String setUserRole(@RequestParam Long userId, @RequestParam String role, RedirectAttributes redirectAttributes) {
        try {
            userService.updateUserRole(userId, role);
            redirectAttributes.addFlashAttribute("successMessage", "Role updated successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/roles"; 
    }

    @GetMapping("/editBooking")
    @ResponseBody
    public String editBooking(String bookingId) {
        System.out.println("This button edits bookings");
        return "Edit booking";
    }

    @GetMapping("/deleteBooking")
    @ResponseBody
    public String deleteBooking(String bookingId) {
        System.out.println("This button deletes bookings");
        return "Deletes booking";
    }
}