package com.uts.Online.Booking.App;

import com.uts.Online.Booking.App.DAO.BookingDAO;
import com.uts.Online.Booking.App.DAO.CourtDAO;
import com.uts.Online.Booking.App.DAO.TimeslotDAO;
import com.uts.Online.Booking.App.DAO.UserDAO;
import com.uts.Online.Booking.App.DAO.VenueDAO;
import com.uts.Online.Booking.App.config.SecurityConfig;
import com.uts.Online.Booking.App.controller.BookingController;
import com.uts.Online.Booking.App.model.*;
import com.uts.Online.Booking.App.service.BookingService;
import com.uts.Online.Booking.App.service.CustomerDetailsService;
import com.uts.Online.Booking.App.service.UserService;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@Import(SecurityConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Booking Controller Tests")
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private BookingDAO bookingDAO;

    @MockitoBean
    private CourtDAO courtDAO;

    @MockitoBean
    private TimeslotDAO timeslotDAO;

    @MockitoBean
    private VenueDAO venueDAO;

    @MockitoBean
    private UserDAO userDAO;

    @MockitoBean
    private CustomerDetailsService customerDetailsService;

    @MockitoBean
    private UserService userService;

    private User testUser;
    private Player testPlayer;
    private Venue testVenue;
    private Court testCourt;
    private Timeslot testTimeslot;
    private Booking testBooking;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        // Setup test player
        testPlayer = new Player();
        testPlayer.setId(1L);
        testPlayer.setEmail("player@example.com");
        testPlayer.setFirstName("Test");
        testPlayer.setLastName("Player");
        testPlayer.setCreditBalance(100.0);

        // Setup test venue
        testVenue = new Venue();
        testVenue.setVenueId(1L);
        testVenue.setVenueName("Test Venue");
        testVenue.setAddress("123 Test St");

        // Setup test court
        testCourt = new Court();
        testCourt.setCourtId(1L);
        testCourt.setCourtName("Test Court");
        testCourt.setCourtType("Indoor");
        testCourt.setLocation("Building A");
        testCourt.setHourlyRate(50.0);
        testCourt.setVenue(testVenue);

        // Setup test timeslot
        testTimeslot = new Timeslot();
        testTimeslot.setTimeslotId(1L);
        testTimeslot.setStartTime(LocalTime.of(14, 0));
        testTimeslot.setEndTime(LocalTime.of(15, 0));

        // Setup test booking
        testBooking = new Booking();
        testBooking.setBookingId(1L);
        testBooking.setCourt(testCourt);
        testBooking.setTimeslot(testTimeslot);
        testBooking.setBookingDate(LocalDate.now().plusDays(2));
        testBooking.setUserId(1L);
        testBooking.setStatus("CONFIRMED");
    }

    @Test
    @Order(1)
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should create booking successfully with single slot")
    void testBookSlots_Success_SingleSlot() throws Exception {
        String slot = "1-1-" + LocalDate.now().plusDays(1);
        
        when(userDAO.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bookingService.calculateTotalAmount(anyList())).thenReturn(50.0);
        when(bookingService.createBooking(anyLong(), anyLong(), any(LocalDate.class), anyLong()))
                .thenReturn(testBooking);

        mockMvc.perform(post("/book")
                        .with(csrf())
                        .param("selectedSlots", slot))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/payment?bookingId=*&amount=*"))
                .andExpect(flash().attributeExists("success"))
                .andExpect(flash().attributeExists("bookingCount"));

        verify(bookingService, times(1)).createBooking(anyLong(), anyLong(), any(LocalDate.class), anyLong());
        verify(bookingService, times(1)).calculateTotalAmount(anyList());
    }

    @Test
    @Order(2)
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should create multiple bookings successfully")
    void testBookSlots_Success_MultipleSlots() throws Exception {
        String slot1 = "1-1-" + LocalDate.now().plusDays(1);
        String slot2 = "1-2-" + LocalDate.now().plusDays(1);
        
        when(userDAO.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bookingService.calculateTotalAmount(anyList())).thenReturn(100.0);
        when(bookingService.createBooking(anyLong(), anyLong(), any(LocalDate.class), anyLong()))
                .thenReturn(testBooking);

        mockMvc.perform(post("/book")
                        .with(csrf())
                        .param("selectedSlots", slot1, slot2))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/payment?bookingId=*&amount=*"))
                .andExpect(flash().attributeExists("success"));

        verify(bookingService, times(2)).createBooking(anyLong(), anyLong(), any(LocalDate.class), anyLong());
    }

    @Test
    @Order(3)
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should fail when no slots are selected")
    void testBookSlots_NoSlotsSelected() throws Exception {
        mockMvc.perform(post("/book")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/venues"))
                .andExpect(flash().attributeExists("error"));

        verify(bookingService, never()).createBooking(anyLong(), anyLong(), any(LocalDate.class), anyLong());
    }

    @Test
    @Order(4)
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should handle invalid slot format")
    void testBookSlots_InvalidSlotFormat() throws Exception {
        String invalidSlot = "invalid-format";
        
        when(userDAO.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/book")
                        .with(csrf())
                        .param("selectedSlots", invalidSlot))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/venues"))
                .andExpect(flash().attributeExists("error"));

        verify(bookingService, never()).createBooking(anyLong(), anyLong(), any(LocalDate.class), anyLong());
    }

    @Test
    @Order(5)
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should handle slot already booked error")
    void testBookSlots_SlotAlreadyBooked() throws Exception {
        String slot = "1-1-" + LocalDate.now().plusDays(1);
        
        when(userDAO.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bookingService.calculateTotalAmount(anyList())).thenReturn(50.0);
        when(bookingService.createBooking(anyLong(), anyLong(), any(LocalDate.class), anyLong()))
                .thenThrow(new RuntimeException("This time slot is already booked"));

        mockMvc.perform(post("/book")
                        .with(csrf())
                        .param("selectedSlots", slot))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/venues"))
                .andExpect(flash().attributeExists("error"));

        verify(bookingService, times(1)).createBooking(anyLong(), anyLong(), any(LocalDate.class), anyLong());
    }

    @Test
    @Order(6)
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should handle partial booking success")
    void testBookSlots_PartialSuccess() throws Exception {
        String slot1 = "1-1-" + LocalDate.now().plusDays(1);
        String slot2 = "1-2-" + LocalDate.now().plusDays(1);
        
        when(userDAO.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bookingService.calculateTotalAmount(anyList())).thenReturn(100.0);
        when(bookingService.createBooking(eq(1L), eq(1L), any(LocalDate.class), anyLong()))
                .thenReturn(testBooking);
        when(bookingService.createBooking(eq(1L), eq(2L), any(LocalDate.class), anyLong()))
                .thenThrow(new RuntimeException("This time slot is already booked"));

        mockMvc.perform(post("/book")
                        .with(csrf())
                        .param("selectedSlots", slot1, slot2))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/payment?bookingId=*&amount=*"))
                .andExpect(flash().attributeExists("success"))
                .andExpect(flash().attributeExists("error"));

        verify(bookingService, times(2)).createBooking(anyLong(), anyLong(), any(LocalDate.class), anyLong());
    }

    @Test
    @Order(7)
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should display booking confirmation page")
    void testShowBookingConfirmation() throws Exception {
        mockMvc.perform(get("/booking-confirmation"))
                .andExpect(status().isOk())
                .andExpect(view().name("booking-confirmation"));
    }

    @Test
    @Order(8)
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should display booking history for authenticated user")
    void testGetBookingHistory_Success() throws Exception {
        List<Booking> bookings = Arrays.asList(testBooking);
        
        when(userDAO.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bookingDAO.findByUserId(1L)).thenReturn(bookings);

        mockMvc.perform(get("/myBookings"))
                .andExpect(status().isOk())
                .andExpect(view().name("booking-history"))
                .andExpect(model().attributeExists("bookings"))
                .andExpect(model().attributeExists("user"));

        verify(bookingDAO, times(1)).findByUserId(1L);
    }

    @Test
    @Order(9)
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should redirect to login when user not found")
    void testGetBookingHistory_UserNotFound() throws Exception {
        when(userDAO.findByEmail("test@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/myBookings"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(bookingDAO, never()).findByUserId(anyLong());
    }

    @Test
    @Order(10)
    @WithMockUser(username = "player@example.com")
    @DisplayName("Should cancel booking successfully with refund")
    void testCancelBooking_Success() throws Exception {
        when(userDAO.findByEmail("player@example.com")).thenReturn(Optional.of(testPlayer));
        when(bookingService.getBookingById(1L)).thenReturn(testBooking);
        doNothing().when(bookingService).cancelBookingWithRefund(1L);

        mockMvc.perform(post("/bookings/cancel/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/myBookings"))
                .andExpect(flash().attributeExists("success"));

        verify(bookingService, times(1)).cancelBookingWithRefund(1L);
    }

    @Test
    @Order(11)
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should prevent cancellation by unauthorized user")
    void testCancelBooking_Unauthorized() throws Exception {
        User differentUser = new User();
        differentUser.setId(2L);
        differentUser.setEmail("test@example.com");
        
        when(userDAO.findByEmail("test@example.com")).thenReturn(Optional.of(differentUser));
        when(bookingService.getBookingById(1L)).thenReturn(testBooking);

        mockMvc.perform(post("/bookings/cancel/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/myBookings"))
                .andExpect(flash().attributeExists("error"));

        verify(bookingService, never()).cancelBookingWithRefund(anyLong());
    }

    @Test
    @Order(12)
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should prevent cancellation within 24 hours")
    void testCancelBooking_TooLate() throws Exception {
        // Create booking for tomorrow (less than 24 hours)
        Booking nearFutureBooking = new Booking();
        nearFutureBooking.setBookingId(1L);
        nearFutureBooking.setCourt(testCourt);
        nearFutureBooking.setTimeslot(testTimeslot);
        nearFutureBooking.setBookingDate(LocalDate.now().plusDays(1));
        nearFutureBooking.setUserId(1L);
        nearFutureBooking.setStatus("CONFIRMED");
        
        when(userDAO.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bookingService.getBookingById(1L)).thenReturn(nearFutureBooking);

        mockMvc.perform(post("/bookings/cancel/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/myBookings"))
                .andExpect(flash().attributeExists("error"));

        verify(bookingService, never()).cancelBookingWithRefund(anyLong());
    }

    @Test
    @Order(13)
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should handle cancellation error gracefully")
    void testCancelBooking_Error() throws Exception {
        when(userDAO.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bookingService.getBookingById(1L)).thenReturn(testBooking);
        doThrow(new RuntimeException("Database error"))
                .when(bookingService).cancelBookingWithRefund(1L);

        mockMvc.perform(post("/bookings/cancel/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/myBookings"))
                .andExpect(flash().attributeExists("error"));

        verify(bookingService, times(1)).cancelBookingWithRefund(1L);
    }

    @Test
    @Order(14)
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should display edit booking form")
    void testEditBookingForm_Success() throws Exception {
        List<Timeslot> availableSlots = Arrays.asList(testTimeslot);
        
        when(userDAO.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bookingService.getBookingById(1L)).thenReturn(testBooking);
        when(bookingService.getAvailableTimeslotsForEdit(1L)).thenReturn(availableSlots);

        mockMvc.perform(get("/bookings/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-booking"))
                .andExpect(model().attributeExists("booking"))
                .andExpect(model().attributeExists("availableTimeslots"));

        verify(bookingService, times(1)).getBookingById(1L);
        verify(bookingService, times(1)).getAvailableTimeslotsForEdit(1L);
    }

    @Test
    @Order(15)
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should prevent edit by unauthorized user")
    void testEditBookingForm_Unauthorized() throws Exception {
        User differentUser = new User();
        differentUser.setId(2L);
        differentUser.setEmail("test@example.com");
        
        when(userDAO.findByEmail("test@example.com")).thenReturn(Optional.of(differentUser));
        when(bookingService.getBookingById(1L)).thenReturn(testBooking);

        mockMvc.perform(get("/bookings/edit/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/myBookings"))
                .andExpect(flash().attributeExists("error"));

        verify(bookingService, never()).getAvailableTimeslotsForEdit(anyLong());
    }

    @Test
    @Order(16)
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should prevent editing within 24 hours")
    void testEditBookingForm_TooLate() throws Exception {
        Booking nearFutureBooking = new Booking();
        nearFutureBooking.setBookingId(1L);
        nearFutureBooking.setCourt(testCourt);
        nearFutureBooking.setTimeslot(testTimeslot);
        nearFutureBooking.setBookingDate(LocalDate.now().plusDays(1));
        nearFutureBooking.setUserId(1L);
        nearFutureBooking.setStatus("CONFIRMED");
        
        when(userDAO.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bookingService.getBookingById(1L)).thenReturn(nearFutureBooking);

        mockMvc.perform(get("/bookings/edit/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/myBookings"))
                .andExpect(flash().attributeExists("error"));

        verify(bookingService, never()).getAvailableTimeslotsForEdit(anyLong());
    }

    @Test
    @Order(17)
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should handle edit form error gracefully")
    void testEditBookingForm_Error() throws Exception {
        when(userDAO.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bookingService.getBookingById(1L)).thenThrow(new RuntimeException("Booking not found"));

        mockMvc.perform(get("/bookings/edit/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/myBookings"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @Order(18)
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should update booking successfully")
    void testUpdateBooking_Success() throws Exception {
        String newDate = LocalDate.now().plusDays(3).toString();
        
        when(userDAO.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bookingService.getBookingById(1L)).thenReturn(testBooking);
        doNothing().when(bookingService).updateBookingForUsers(1L, 2L, LocalDate.parse(newDate));

        mockMvc.perform(post("/bookings/edit/1")
                        .with(csrf())
                        .param("timeslotId", "2")
                        .param("date", newDate))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/myBookings"))
                .andExpect(flash().attributeExists("success"));

        verify(bookingService, times(1)).updateBookingForUsers(1L, 2L, LocalDate.parse(newDate));
    }

    @Test
    @Order(19)
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should prevent update by unauthorized user")
    void testUpdateBooking_Unauthorized() throws Exception {
        User differentUser = new User();
        differentUser.setId(2L);
        differentUser.setEmail("test@example.com");
        
        String newDate = LocalDate.now().plusDays(3).toString();
        
        when(userDAO.findByEmail("test@example.com")).thenReturn(Optional.of(differentUser));
        when(bookingService.getBookingById(1L)).thenReturn(testBooking);

        mockMvc.perform(post("/bookings/edit/1")
                        .with(csrf())
                        .param("timeslotId", "2")
                        .param("date", newDate))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/myBookings"))
                .andExpect(flash().attributeExists("error"));

        verify(bookingService, never()).updateBookingForUsers(anyLong(), anyLong(), any(LocalDate.class));
    }

    @Test
    @Order(20)
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should handle update to already booked slot")
    void testUpdateBooking_SlotAlreadyBooked() throws Exception {
        String newDate = LocalDate.now().plusDays(3).toString();
        
        when(userDAO.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bookingService.getBookingById(1L)).thenReturn(testBooking);
        doThrow(new RuntimeException("The selected time slot is already booked"))
                .when(bookingService).updateBookingForUsers(1L, 2L, LocalDate.parse(newDate));

        mockMvc.perform(post("/bookings/edit/1")
                        .with(csrf())
                        .param("timeslotId", "2")
                        .param("date", newDate))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/myBookings"))
                .andExpect(flash().attributeExists("error"));

        verify(bookingService, times(1)).updateBookingForUsers(1L, 2L, LocalDate.parse(newDate));
    }

    @Test
    @Order(21)
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should handle update error gracefully")
    void testUpdateBooking_Error() throws Exception {
        String newDate = LocalDate.now().plusDays(3).toString();
        
        when(userDAO.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bookingService.getBookingById(1L)).thenReturn(testBooking);
        doThrow(new RuntimeException("Database error"))
                .when(bookingService).updateBookingForUsers(1L, 2L, LocalDate.parse(newDate));

        mockMvc.perform(post("/bookings/edit/1")
                        .with(csrf())
                        .param("timeslotId", "2")
                        .param("date", newDate))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/myBookings"))
                .andExpect(flash().attributeExists("error"));

        verify(bookingService, times(1)).updateBookingForUsers(1L, 2L, LocalDate.parse(newDate));
    }

    @Test
    @Order(22)
    @DisplayName("Should redirect unauthenticated user to login for booking history")
    void testGetBookingHistory_Unauthenticated() throws Exception {
        mockMvc.perform(get("/myBookings"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @Order(23)
    @DisplayName("Should redirect unauthenticated user for cancel attempt")
    void testCancelBooking_Unauthenticated() throws Exception {
        mockMvc.perform(post("/bookings/cancel/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @Order(24)
    @DisplayName("Should redirect unauthenticated user for edit attempt")
    void testEditBooking_Unauthenticated() throws Exception {
        mockMvc.perform(get("/bookings/edit/1"))
                .andExpect(status().is3xxRedirection());
    }
}