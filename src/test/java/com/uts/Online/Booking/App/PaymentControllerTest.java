package com.uts.Online.Booking.App;

import com.uts.Online.Booking.App.DAO.PaymentDAO;
import com.uts.Online.Booking.App.DAO.UserDAO;
import com.uts.Online.Booking.App.config.SecurityConfig;
import com.uts.Online.Booking.App.controller.AuthController;
import com.uts.Online.Booking.App.controller.PaymentController;
import com.uts.Online.Booking.App.model.Booking;
import com.uts.Online.Booking.App.model.Court;
import com.uts.Online.Booking.App.model.Payment;
import com.uts.Online.Booking.App.model.Player;
import com.uts.Online.Booking.App.model.Timeslot;
import com.uts.Online.Booking.App.model.Venue;
import com.uts.Online.Booking.App.service.BookingService;
import com.uts.Online.Booking.App.service.CustomerDetailsService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@Import(SecurityConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("PaymentController Test")
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

    @MockitoBean
    private AuthController authController;

    @MockitoBean
    private JavaMailSender mailSender;

    private Player testPlayer;
    private Payment testPayment;
    private Booking testBooking;
    private Court testCourt;
    private Venue testVenue;
    private Timeslot testTimeSlot;

    @BeforeEach
    public void setUp() {
        //setup test models
        testPlayer = new Player();
        testPlayer.setId(1L);
        testPlayer.setEmail("player@example.com");
        testPlayer.setCreditBalance(25.0);

        testVenue = new Venue();
        testVenue.setVenueId(1L);
        testVenue.setVenueName("Test Venue");
        testVenue.setAddress("123 Test St");

        testCourt = new Court();
        testCourt.setCourtId(1L);
        testCourt.setCourtName("Test Court");
        testCourt.setCourtType("Indoor");
        testCourt.setLocation("Building A");
        testCourt.setHourlyRate(50.0);
        testCourt.setVenue(testVenue);

        testTimeSlot = new Timeslot();
        testTimeSlot.setTimeslotId(1L);
        testTimeSlot.setStartTime(LocalTime.of(14, 0));
        testTimeSlot.setEndTime(LocalTime.of(15, 0));

        testBooking = new Booking();
        testBooking.setBookingId(1L);
        testBooking.setCourt(testCourt);
        testBooking.setTimeslot(testTimeSlot);
        testBooking.setBookingDate(LocalDate.of(2025, 10, 10));
        testBooking.setUserId(1L);
        testBooking.setStatus("CONFIRMED");

        testPayment = new Payment();
        testPayment.setPaymentId(1L);
        testPayment.setBookingId(1L);
        testPayment.setUserId(1L);
        testPayment.setAmount(35.0);
        testPayment.setStatus("SUCCESS");
    }

    @Test //test display payment form
    @Order(1)
    @WithMockUser(username = "player@example.com")
    @DisplayName("Should display payment form")
    public void testShowPaymentForm_Success() throws Exception{
        when(userDAO.findByEmail("player@example.com")).thenReturn(Optional.of(testPlayer));
        when(bookingService.getBookingById(1L)).thenReturn(testBooking);

        mockMvc.perform(get("/payment")
                .param("bookingId", "1")
                .param("amount", "35.0"))
                .andExpect(status().isOk())
                .andExpect(view().name("payment"))
                .andExpect(model().attributeExists("bookingId", "amount", "booking", "creditBalance"));

        verify(bookingService, times(1)).getBookingById(1L);
    }

    @Test //test credit card payment without credit
    @Order(2)
    @WithMockUser(username = "player@example.com")
    @DisplayName("Should process credit card payment without credit")
    public void testCreditCardPayment_Success() throws Exception{

        testPlayer.setCreditBalance(0.0);

        when(userDAO.findByEmail("player@example.com")).thenReturn(Optional.of(testPlayer));
        when(paymentDAO.save(any(Payment.class))).thenReturn(testPayment);

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
        
        verify(bookingService, times(1)).updateBookingStatus(1L, "CONFIRMED");
        verify(paymentDAO, times(1)).save(any(Payment.class));
    }

    @Test //test credit card payment with partial credit
    @Order(3)
    @WithMockUser(username = "player@example.com")
    @DisplayName("Should process payment partially by credit")
    public void testCreditCardPaymentWithPartialCredit_Success() throws Exception{

        testPlayer.setCreditBalance(15.0);

        when(userDAO.findByEmail("player@example.com")).thenReturn(Optional.of(testPlayer));
        when(paymentDAO.save(any(Payment.class))).thenReturn(testPayment);
        when(userDAO.save(any(Player.class))).thenReturn(testPlayer);

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
        verify(userDAO, times(1)).save(argThat(player -> 
            player instanceof Player && 
            ((Player) player).getCreditBalance() == 0.0
        ));
        verify(bookingService, times(1)).updateBookingStatus(1L, "CONFIRMED");
    }

    @Test //test payment with credit when insufficient funds/fails
    @Order(4)
    @WithMockUser(username = "player@example.com")
    @DisplayName("Should reject payment due with insufficient credit balance")
    public void testCreditBalancePayment_failed() throws Exception{

 
        testPlayer.setCreditBalance(10.0);


        when(userDAO.findByEmail("player@example.com")).thenReturn(Optional.of(testPlayer));

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

        verify(userDAO, never()).save(any(Player.class));
        verify(bookingService, never()).updateBookingStatus(anyLong(), anyString());
    }

     @Test //test credit card payment with full credit
    @Order(5)
    @WithMockUser(username = "player@example.com")
    @DisplayName("Should process payment fully covered by credit")
    public void testCreditCardPaymentWithFullCredit_Success() throws Exception{

        testPlayer.setCreditBalance(35.0);

        when(userDAO.findByEmail("player@example.com")).thenReturn(Optional.of(testPlayer));
        when(paymentDAO.save(any(Payment.class))).thenReturn(testPayment);
        when(userDAO.save(any(Player.class))).thenReturn(testPlayer);

        // test
        mockMvc.perform(post("/payment/process")
                .with(csrf())
                .param("bookingId", "1")
                .param("amount", "35.0")
                .param("creditApplied", "35.0")
                .param("creditCardNumber", "1234567890123456")
                .param("creditCardExpiry", "12/25")
                .param("creditCardSecurityCode", "123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/booking-confirmation"));
        
        //verify if credit was deducted
        verify(userDAO, times(1)).save(argThat(player -> 
            player instanceof Player && 
            ((Player) player).getCreditBalance() == 0.0
        ));
        verify(bookingService, times(1)).updateBookingStatus(1L, "CONFIRMED");
    }

}