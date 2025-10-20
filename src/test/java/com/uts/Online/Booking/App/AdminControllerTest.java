package com.uts.Online.Booking.App;

import com.uts.Online.Booking.App.DAO.BookingDAO;
import com.uts.Online.Booking.App.DAO.CourtDAO;
import com.uts.Online.Booking.App.DAO.TimeslotDAO;
import com.uts.Online.Booking.App.DAO.VenueDAO;
import com.uts.Online.Booking.App.config.SecurityConfig;
import com.uts.Online.Booking.App.controller.AdminController;
import com.uts.Online.Booking.App.model.Booking;
import com.uts.Online.Booking.App.model.Court;
import com.uts.Online.Booking.App.model.Timeslot;
import com.uts.Online.Booking.App.model.Venue;
import com.uts.Online.Booking.App.service.CustomerDetailsService;

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

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Admin Controller Tests")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingDAO bookingDAO;

    @MockitoBean
    private CourtDAO courtDAO;

    @MockitoBean
    private TimeslotDAO timeslotDAO;

    @MockitoBean
    private VenueDAO venueDAO;

    @MockitoBean
    private CustomerDetailsService customerDetailsService;

    private Venue testVenue;
    private Court testCourt;
    private Timeslot testTimeslot;
    private Booking testBooking;

    @BeforeEach
    void setUp() {
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

        testTimeslot = new Timeslot();
        testTimeslot.setTimeslotId(1L);
        testTimeslot.setStartTime(LocalTime.of(14, 0));
        testTimeslot.setEndTime(LocalTime.of(15, 0));

        testBooking = new Booking();
        testBooking.setBookingId(1L);
        testBooking.setCourt(testCourt);
        testBooking.setTimeslot(testTimeslot);
        testBooking.setBookingDate(LocalDate.of(2025, 10, 10));
        testBooking.setUserId(1L);
        testBooking.setStatus("CONFIRMED");
    }

    @Test
    @Order(1)
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should display admin page with bookings")
    void testAdminPage_DisplaysBookings() throws Exception {
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingDAO.findAll()).thenReturn(bookings);

        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"))
                .andExpect(model().attributeExists("bookings"));

        verify(bookingDAO, times(1)).findAll();
    }

    @Test
    @Order(2)
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should redirect to edit page with correct parameters")
    void testEditBooking_RedirectsCorrectly() throws Exception {
        when(bookingDAO.findById(1L)).thenReturn(Optional.of(testBooking));

        mockMvc.perform(get("/admin/edit/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/venue/1/courts?date=*&editBookingId=*"));

        verify(bookingDAO, times(1)).findById(1L);
    }

    @Test
    @Order(3)
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should handle edit request for non-existent booking")
    void testEditBooking_NotFound() throws Exception {
        when(bookingDAO.findById(99999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/edit/99999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("error"));

        verify(bookingDAO, times(1)).findById(99999L);
    }

    @Test
    @Order(4)
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should update booking successfully")
    void testUpdateBooking_Success() throws Exception {
        Timeslot newTimeslot = new Timeslot();
        newTimeslot.setTimeslotId(2L);
        newTimeslot.setStartTime(LocalTime.of(16, 0));
        newTimeslot.setEndTime(LocalTime.of(17, 0));

        when(bookingDAO.findById(1L)).thenReturn(Optional.of(testBooking));
        when(courtDAO.findById(1L)).thenReturn(Optional.of(testCourt));
        when(timeslotDAO.findById(2L)).thenReturn(Optional.of(newTimeslot));
        when(bookingDAO.existsByCourtCourtIdAndTimeslotTimeslotIdAndBookingDate(
                eq(1L), eq(2L), any(LocalDate.class)))
                .thenReturn(false);
        when(bookingDAO.save(any(Booking.class))).thenReturn(testBooking);

        String newSlot = "1-2-2025-10-10";

        mockMvc.perform(post("/admin/update-booking")
                        .with(csrf())
                        .param("selectedSlots", newSlot)
                        .param("editBookingId", "1")
                        .param("originalUserId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("success"));

        verify(bookingDAO, times(1)).save(any(Booking.class));
    }

    @Test
    @Order(5)
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should reject update with no slots selected")
    void testUpdateBooking_NoSlots() throws Exception {
        when(bookingDAO.findById(1L)).thenReturn(Optional.of(testBooking));

        mockMvc.perform(post("/admin/update-booking")
                        .with(csrf())
                        .param("editBookingId", "1")
                        .param("originalUserId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("error"));

        verify(bookingDAO, never()).save(any(Booking.class));
    }

    @Test
    @Order(6)
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should reject update with multiple slots")
    void testUpdateBooking_MultipleSlots() throws Exception {
        when(bookingDAO.findById(1L)).thenReturn(Optional.of(testBooking));

        String slot1 = "1-1-2025-10-10";
        String slot2 = "1-2-2025-10-10";

        mockMvc.perform(post("/admin/update-booking")
                        .with(csrf())
                        .param("selectedSlots", slot1, slot2)
                        .param("editBookingId", "1")
                        .param("originalUserId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("error"));

        verify(bookingDAO, never()).save(any(Booking.class));
    }

    @Test
    @Order(7)
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should prevent updating to already booked slot")
    void testUpdateBooking_SlotAlreadyBooked() throws Exception {
        Timeslot timeslot2 = new Timeslot();
        timeslot2.setTimeslotId(2L);
        timeslot2.setStartTime(LocalTime.of(16, 0));
        timeslot2.setEndTime(LocalTime.of(17, 0));

        // Create a conflicting booking
        Booking conflictingBooking = new Booking();
        conflictingBooking.setBookingId(2L);
        conflictingBooking.setCourt(testCourt);
        conflictingBooking.setTimeslot(timeslot2);
        conflictingBooking.setBookingDate(LocalDate.of(2025, 10, 15));

        when(bookingDAO.findById(1L)).thenReturn(Optional.of(testBooking));
        when(courtDAO.findById(1L)).thenReturn(Optional.of(testCourt));
        when(timeslotDAO.findById(2L)).thenReturn(Optional.of(timeslot2));
        when(bookingDAO.findByCourtCourtIdAndTimeslotTimeslotIdAndBookingDate(
                eq(1L), eq(2L), any(LocalDate.class)))
                .thenReturn(Arrays.asList(conflictingBooking));

        String slot = "1-2-2025-10-15";

        mockMvc.perform(post("/admin/update-booking")
                        .with(csrf())
                        .param("selectedSlots", slot)
                        .param("editBookingId", "1")
                        .param("originalUserId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("error"));

        verify(bookingDAO, never()).save(any(Booking.class));
    }

    @Test
    @Order(8)
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should delete booking successfully")
    void testDeleteBooking_Success() throws Exception {
        when(bookingDAO.existsById(1L)).thenReturn(true);
        doNothing().when(bookingDAO).deleteById(1L);

        mockMvc.perform(post("/admin/delete/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("success"));

        verify(bookingDAO, times(1)).deleteById(1L);
    }

    @Test
    @Order(9)
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should handle delete request for non-existent booking")
    void testDeleteBooking_NotFound() throws Exception {
        when(bookingDAO.existsById(99999L)).thenReturn(false);

        mockMvc.perform(post("/admin/delete/99999")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("error"));

        verify(bookingDAO, never()).deleteById(anyLong());
    }

    @Test
    @Order(10)
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should delete booking via AJAX successfully")
    void testDeleteBookingAjax_Success() throws Exception {
        when(bookingDAO.existsById(1L)).thenReturn(true);
        doNothing().when(bookingDAO).deleteById(1L);

        mockMvc.perform(delete("/admin/api/booking/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("SUCCESS"));

        verify(bookingDAO, times(1)).deleteById(1L);
    }

    @Test
    @Order(11)
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should handle AJAX delete for non-existent booking")
    void testDeleteBookingAjax_NotFound() throws Exception {
        when(bookingDAO.existsById(99999L)).thenReturn(false);

        mockMvc.perform(delete("/admin/api/booking/99999")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.startsWith("ERROR")));

        verify(bookingDAO, never()).deleteById(anyLong());
    }
}