package com.uts.Online.Booking.App;

import com.uts.Online.Booking.App.DAO.PaymentDAO;
import com.uts.Online.Booking.App.DAO.UserDAO;
import com.uts.Online.Booking.App.controller.PaymentController;
import com.uts.Online.Booking.App.model.Payment;
import com.uts.Online.Booking.App.model.PaymentType;
import com.uts.Online.Booking.App.model.Player;
import com.uts.Online.Booking.App.service.BookingService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;


import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
public class PaymentControllerTest{

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentDAO paymentDAO;

    @MockBean
    private UserDAO userDAO;

    @MockBean
    private BookingService bookingService;

    @Test
    @WithMockUser(username = "player@example.com")
    public void testCreditCardPayment_Success() throws Exception{

        Player mockPlayer = new Player();
        mockPlayer.setId(1L);
        mockPlayer.setEmail("player@example.com");

        Payment mockPayment = new Payment();
        mockPayment.setStatus("SUCCESS");
        mockPayment.setPaymentId(1L);

        when(userDAO.findByEmail("player@example.com")).thenReturn(Optional.of(mockPlayer));
        when(paymentDAO.save(any(Payment.class))).thenReturn(mockPayment);

        // test
        mockMvc.perform(post("/payment/process")
                .param("bookingId", "1")
                .param("amount", "35.0")
                .param("type", "CREDIT_CARD")
                .param("creditCardNumber", "1234567890123456")
                .param("creditCardExpiry", "12/25")
                .param("creditCardSecuirtyCode", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("booking-confirmation"));
    }

    @Test //test payment with credit when insufficient funds/fails
    @WithMockUser(username = "player@example.com")
    public void testCreditBalancePayment_failed() throws Exception{

        Player mockPlayer = new Player();
        mockPlayer.setId(1L);
        mockPlayer.setEmail("player@example.com");
        mockPlayer.setCreditBalance(10.0);

        Payment mockPayment = new Payment();
        mockPayment.setStatus("FAILED");
        mockPayment.setPaymentId(1L);

        when(userDAO.findByEmail("player@example.com")).thenReturn(Optional.of(mockPlayer));
        when(paymentDAO.save(any(Payment.class))).thenReturn(mockPayment);

        // test
        mockMvc.perform(post("/payment/process")
                .param("bookingId", "1")
                .param("amount", "35.0")
                .param("type", "CREDIT_BALANCE"))
                .andExpect(status().isOk())
                .andExpect(view().name("payment_failed"));
    }

}

