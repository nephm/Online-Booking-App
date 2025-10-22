/*package com.uts.Online.Booking.App;

import com.uts.Online.Booking.App.DAO.PaymentDAO;
import com.uts.Online.Booking.App.DAO.UserDAO;
import com.uts.Online.Booking.App.controller.PaymentController;
import com.uts.Online.Booking.App.model.Payment;
import com.uts.Online.Booking.App.model.Player;
import com.uts.Online.Booking.App.service.BookingService;
import com.uts.Online.Booking.App.service.CustomerDetailsService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
public class PaymentControllerTest{

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentDAO paymentDAO;

    @MockitoBean
    private UserDAO userDAO;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private CustomerDetailsService uService;


    @Test //test credit card payment without credit
    @WithMockUser(username = "player@example.com")
    public void testCreditCardPayment_Success() throws Exception{

        Player mockPlayer = new Player();
        mockPlayer.setId(1L);
        mockPlayer.setEmail("player@example.com");
        mockPlayer.setCreditBalance(0.0);

        Payment mockPayment = new Payment();
        mockPayment.setStatus("SUCCESS");
        mockPayment.setPaymentId(1L);

        when(userDAO.findByEmail("player@example.com")).thenReturn(Optional.of(mockPlayer));
        when(paymentDAO.save(any(Payment.class))).thenReturn(mockPayment);

        // test
        mockMvc.perform(post("/payment/process")
                .with(csrf())
                .param("bookingId", "1")
                .param("amount", "35.0")
                .param("creditApplied", "0")
                .param("creditCardNumber", "1234567890123456")
                .param("creditCardExpiry", "12/25")
                .param("creditCardSecurityCode", "123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/booking-confirmation"));
        
        verify(bookingService).updateBookingStatus(1L, "CONFIRMED");
    }

    @Test //test credit card payment with partial credit
    @WithMockUser(username = "player@example.com")
    public void testCreditCardPaymentWithCredit_Success() throws Exception{

        Player mockPlayer = new Player();
        mockPlayer.setId(1L);
        mockPlayer.setEmail("player@example.com");
        mockPlayer.setCreditBalance(15.0);

        Payment mockPayment = new Payment();
        mockPayment.setStatus("SUCCESS");
        mockPayment.setPaymentId(1L);

        when(userDAO.findByEmail("player@example.com")).thenReturn(Optional.of(mockPlayer));
        when(paymentDAO.save(any(Payment.class))).thenReturn(mockPayment);

        // test
        mockMvc.perform(post("/payment/process")
                .with(csrf())
                .param("bookingId", "1")
                .param("amount", "35.0")
                .param("creditApplied", "15.0")
                .param("creditCardNumber", "1234567890123456")
                .param("creditCardExpiry", "12/25")
                .param("creditCardSecurityCode", "123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/booking-confirmation"));
        
        //verify if credit was deducted
        verify(userDAO).save(argThat(player -> 
            player instanceof Player && 
            ((Player) player).getCreditBalance() == 0.0
        ));
        verify(bookingService).updateBookingStatus(1L, "CONFIRMED");
    }

    @Test //test payment with credit when insufficient funds/fails
    @WithMockUser(username = "player@example.com")
    public void testCreditBalancePayment_failed() throws Exception{

        Player mockPlayer = new Player();
        mockPlayer.setId(1L);
        mockPlayer.setEmail("player@example.com");
        mockPlayer.setCreditBalance(10.0);


        when(userDAO.findByEmail("player@example.com")).thenReturn(Optional.of(mockPlayer));

        // test
        mockMvc.perform(post("/payment/process")
                .with(csrf())
                .param("bookingId", "1")
                .param("amount", "35.0")
                .param("creditApplied", "35.0")
                .param("creditCardNumber", "1234567890123456")
                .param("creditCardExpiry", "12/25")
                .param("creditCardSecurityCode", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("payment_failed"))
                .andExpect(model().attributeExists("error"));
    }

}*/