package com.uts.Online.Booking.App.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import com.uts.Online.Booking.App.DAO.PaymentDAO;
import com.uts.Online.Booking.App.DAO.UserDAO;
import com.uts.Online.Booking.App.model.Payment;
import com.uts.Online.Booking.App.model.PaymentType;
import com.uts.Online.Booking.App.model.Player;
import com.uts.Online.Booking.App.model.User;
import com.uts.Online.Booking.App.service.BookingService;
import com.uts.Online.Booking.App.service.CustomerDetailsService;

import org.springframework.ui.Model;

@Controller
@RequestMapping("/")
public class PaymentController {
    
    private final PaymentDAO paymentDAO;
    private final UserDAO userDAO;
    private final BookingService bookingService;

    public PaymentController (PaymentDAO paymentDAO, CustomerDetailsService userService, UserDAO userDAO, BookingService bookingService){
        this.paymentDAO = paymentDAO;
        this.userDAO = userDAO;
        this.bookingService = bookingService;
    }

    //show payment form
    @GetMapping("/payment")
    public String showPaymentForm(@RequestParam Long bookingId, @RequestParam Double amount, Model m) {
        m.addAttribute("bookingId", bookingId);
        m.addAttribute("amount", amount);
        m.addAttribute("paymentTypes", PaymentType.values());

        //get current user's credit balance if required
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User u = userDAO.findByEmail(auth.getName()).orElse(null);

        if(u instanceof Player){
            m.addAttribute("creditBalance", ((Player) u).getCreditBalance());
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
    public String getPayment(Model m){
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User u = userDAO.findByEmail(auth.getName()).orElse(null);

        if(u != null){
            List<Payment> payments = paymentDAO.findByUserId(u.getId());
            m.addAttribute("payments", payments);
            m.addAttribute("user", u);
        }

        return "payment/history";

    }

    @PostMapping("/process")
    public String processPayment(@RequestParam Long bookingId, @RequestParam Double amount, @RequestParam PaymentType type, @RequestParam(required = false) String creditCardNumber,
                @RequestParam(required = false) String creditCardExpiry, @RequestParam(required = false) String creditCardSecurityCode, Model m) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User u = userDAO.findByEmail(auth.getName()).orElse(null);
        
        if(u == null){
            m.addAttribute("user", u);
            return "payment/error";
        }

        Payment payment = new Payment();
        payment.setBookingId(bookingId);
        payment.setUserId(u.getId());
        payment.setAmount(amount);
        payment.setCreatedAt(LocalDateTime.now());

        if(type == PaymentType.CREDIT_CARD){
            //handle credit card payment
            payment.setPaymentType(PaymentType.CREDIT_CARD);
            payment.setCreditCardNumber(creditCardNumber);
            payment.setCreditCardExpiry(creditCardExpiry);
            payment.setCreditCardSecurityCode(creditCardSecurityCode);
            payment.setStatus("SUCCESS");
        } else if (type == PaymentType.CREDIT_BALANCE){
            if(u instanceof Player){
                Player player = (Player) u;
                if(player.getCreditBalance() >= amount){
                    player.setCreditBalance(player.getCreditBalance() - amount);
                    userDAO.save(player);
                    payment.setPaymentType(PaymentType.CREDIT_BALANCE);
                    payment.setStatus("SUCCESS");
                } else{
                    payment.setPaymentType(PaymentType.CREDIT_BALANCE);
                    payment.setStatus("FAILED");
                    m.addAttribute("error", "Insufficient credit available");
                }
            } else{
                payment.setStatus("FAILED");
                m.addAttribute("error", "User is not a player");
            }
        }

        Payment saved_payment = paymentDAO.save(payment);
        m.addAttribute("payment", saved_payment);

        if("SUCCESS".equals(saved_payment.getStatus())){
            bookingService.updateBookingStatus(bookingId, "CONFIRMED");
            return "redirect:/booking-confirmation";
        } else{
            bookingService.updateBookingStatus(bookingId, "FAILED");
            return "payment_failed";
        }
    }

}
