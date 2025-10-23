package com.uts.Online.Booking.App.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import com.uts.Online.Booking.App.DAO.PaymentDAO;
import com.uts.Online.Booking.App.DAO.UserDAO;
import com.uts.Online.Booking.App.model.Booking;
import com.uts.Online.Booking.App.model.Payment;
import com.uts.Online.Booking.App.model.PaymentType;
import com.uts.Online.Booking.App.model.Player;
import com.uts.Online.Booking.App.model.User;
import com.uts.Online.Booking.App.service.BookingService;
import com.uts.Online.Booking.App.service.CustomerDetailsService;

import org.springframework.ui.Model;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private JavaMailSender mailSender;

    private final PaymentDAO paymentDAO;
    private final UserDAO userDAO;
    private final BookingService bookingService;

    public PaymentController (PaymentDAO paymentDAO, CustomerDetailsService userService, UserDAO userDAO, BookingService bookingService){
        this.paymentDAO = paymentDAO;
        this.userDAO = userDAO;
        this.bookingService = bookingService;
    }

    //get logged in user
    private User getUser(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userDAO.findByEmail(auth.getName()).orElse(null);
    }

    //show payment form
    @GetMapping("")
    public String showPaymentForm(@RequestParam Long bookingId, @RequestParam Double amount, Model m) {

        //get booking details
        Booking booking = bookingService.getBookingById(bookingId);

        if(booking == null){
            m.addAttribute("error", "booking not found");
            return "payment-failed";
        }

        m.addAttribute("booking", booking);
        m.addAttribute("bookingId", bookingId);
        m.addAttribute("amount", amount);

        //get current user's credit balance if required
        
        if(getUser() instanceof Player){
            m.addAttribute("creditBalance", ((Player) getUser()).getCreditBalance());
        }

        return "payment";
    }
    

    //get payment details by paymentId
    @GetMapping("/{paymentId}")
    public String getPayment(@PathVariable Long paymentId, Model m){
        Payment payment = paymentDAO.findById(paymentId).orElse(null);
        m.addAttribute("payment", payment);
        return "payment/details";

    }

    //get all payment history
    @GetMapping("/myPayments")
    public String getPaymentHistory(Model m){
        if(getUser() != null){
            List<Payment> payments = paymentDAO.findByUserId(getUser().getId());
            m.addAttribute("payments", payments);
            m.addAttribute("user", getUser());
            return "payment-history";
        }

        return "redirect:/login";
    }

    //start payment and check what type of payment the user is doing
    @PostMapping("/process")
    public String processPayment(@RequestParam Long bookingId, @RequestParam Double amount, @RequestParam(defaultValue = "0") Double creditApplied, @RequestParam(required = false) String creditCardNumber,
                @RequestParam(required = false) String creditCardExpiry, @RequestParam(required = false) String creditCardSecurityCode, Model m) {

        if(getUser() == null){
            m.addAttribute("user", getUser());
            return "payment/error";
        }

        //calcute final amount after credit used
        Double finalAmount = amount - creditApplied;

        if(creditApplied > 0){
            if(!(getUser() instanceof Player)){
                m.addAttribute("error", "User is not a player");
                return "payment_failed";
            }

            Player player = (Player) getUser();
            if(player.getCreditBalance() < creditApplied){
                m.addAttribute("error", "Insufficient credit available");
                return "payment_failed";
            }

            //deduct credit from palyer's balance
            player.setCreditBalance(player.getCreditBalance() - creditApplied);
            userDAO.save(player);
        }

        //handle credit card payment
        Payment payment = new Payment();
        payment.setBookingId(bookingId);
        payment.setUserId(getUser().getId());
        payment.setAmount(amount);
        payment.setCreatedAt(LocalDateTime.now());
        
        payment.setPaymentType(PaymentType.CREDIT_CARD);
        payment.setCreditCardNumber(creditCardNumber);
        payment.setCreditCardExpiry(creditCardExpiry);
        payment.setCreditCardSecurityCode(creditCardSecurityCode);
        

        if(finalAmount <= 0){
            payment.setStatus("SUCCESS");
        } else{
            //this is where payment gateway integration would occur but for this application we will simulate scuccess
            payment.setStatus("SUCCESS");
        }

        Payment saved_payment = paymentDAO.save(payment);
        m.addAttribute("payment", saved_payment);
        m.addAttribute("creditUsed", creditApplied);
        m.addAttribute("finalAmountPaid", finalAmount);

        if("SUCCESS".equals(saved_payment.getStatus())){
            bookingService.updateBookingStatus(bookingId, "CONFIRMED");

            try {
                User u = getUser();

                // Build simple confirmation email
                org.springframework.mail.SimpleMailMessage message = new org.springframework.mail.SimpleMailMessage();
                message.setTo(u.getEmail());
                message.setSubject("Booking Confirmation");
                message.setText("Thank you for booking with CourtBooker! Your booking has been confirmed.");

                mailSender.send(message);

                System.out.println("Confirmation email sent directly to " + u.getEmail());
                System.out.println("Confirmation email sent directly to " + u.getEmail());System.out.println("Confirmation email sent directly to " + u.getEmail());System.out.println("Confirmation email sent directly to " + u.getEmail());System.out.println("Confirmation email sent directly to " + u.getEmail());System.out.println("Confirmation email sent directly to " + u.getEmail());System.out.println("Confirmation email sent directly to " + u.getEmail());System.out.println("Confirmation email sent directly to " + u.getEmail());System.out.println("Confirmation email sent directly to " + u.getEmail());System.out.println("Confirmation email sent directly to " + u.getEmail());System.out.println("Confirmation email sent directly to " + u.getEmail());System.out.println("Confirmation email sent directly to " + u.getEmail());System.out.println("Confirmation email sent directly to " + u.getEmail());System.out.println("Confirmation email sent directly to " + u.getEmail());System.out.println("Confirmation email sent directly to " + u.getEmail());System.out.println("Confirmation email sent directly to " + u.getEmail());
            } catch (Exception e) {
                System.out.println("Failed to send confirmation email: " + e.getMessage());
                e.printStackTrace();
            }

            return "redirect:/booking-confirmation";
        } else{
            bookingService.updateBookingStatus(bookingId, "FAILED");
            return "payment_failed";
        }
    }
}
