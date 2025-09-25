package com.uts.Online.Booking.App.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.uts.Online.Booking.App.DAO.PaymentDAO;
import com.uts.Online.Booking.App.model.Payment;
import com.uts.Online.Booking.App.model.PaymentType;
import com.uts.Online.Booking.App.model.Player;
import com.uts.Online.Booking.App.service.CustomerDetailsService;

import jakarta.servlet.http.HttpSession;

import org.springframework.ui.Model;

public class PaymentController {
    
    @Autowired
    private final PaymentDAO paymentDAO;

    @Autowired
    private final CustomerDetailsService userService;

    public PaymentController (PaymentDAO paymentDAO, CustomerDetailsService userService){
        this.paymentDAO = paymentDAO;
        this.userService = userService;
    }

    //get payment details by paymentId
    @GetMapping("/{paymentId}")
    public Payment getPayment(@PathVariable Integer paymentId){
        return paymentDAO.findById(paymentId).orElse(null);

    }

    //get all payment history
    @GetMapping("/myPayments")
    public List<Payment> getPayment(){
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String uName = auth.getName();
        Integer userId = 1;

        return paymentDAO.findByUserId(userId);

    }

    @PostMapping("/pay")
    public Payment processPayment(@RequestParam Integer bookingId, @RequestParam Double amount, @RequestParam PaymentType type, @RequestParam(required = false) String creditCardNumber,
                @RequestParam(required = false) String creditCardExpiry, @RequestParam(required = false) String creditCardSecurityCode, Model m, HttpSession session) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String uName = auth.getName();
        Integer userId = 1;
        
        Payment payment = new Payment();
        payment.setBookingId(bookingId);
        payment.setUserId(1);
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
            Player player = userService.findById(userId);
            if(player.getCreditBalance() >= amount){
                player.setCreditBalance(player.getCreditBalance() - amount);
                payment.setPaymentType(PaymentType.CREDIT_BALANCE);
                payment.setStatus("SUCCESS");
            } else{
                payment.setPaymentType(PaymentType.CREDIT_BALANCE);
                payment.setStatus("FAILED");
            }
    
        }
        return paymentDAO.save(payment);
    }

}
