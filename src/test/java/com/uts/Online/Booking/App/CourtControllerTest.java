package com.uts.Online.Booking.App;

import com.uts.Online.Booking.App.DAO.CourtDAO;
import com.uts.Online.Booking.App.DAO.TimeslotDAO;
import com.uts.Online.Booking.App.DAO.VenueDAO;
import com.uts.Online.Booking.App.config.SecurityConfig;
import com.uts.Online.Booking.App.controller.CourtController;
import com.uts.Online.Booking.App.model.*;
import com.uts.Online.Booking.App.service.BookingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourtController.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("CourtController Test")
public class CourtControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourtDAO courtDAO;

    @MockBean
    private VenueDAO venueDAO;

    @MockBean
    private TimeslotDAO timeslotDAO;

    @MockBean
    private BookingService bookingService;

    private Venue testVenue;
    private Court testCourt;
    private Timeslot testTimeslot;

    @BeforeEach
    public void setUp() {
        testVenue = new Venue();
        testVenue.setVenueId(1L);
        testVenue.setVenueName("UTS Badminton Hall");
        testVenue.setAddress("15 Broadway, Ultimo");

        testCourt = new Court();
        testCourt.setCourtId(101L);
        testCourt.setCourtName("Court A");
        testCourt.setCourtType("Indoor");
        testCourt.setHourlyRate(25.0);
        testCourt.setVenue(testVenue);

        testTimeslot = new Timeslot();
        testTimeslot.setTimeslotId(201L);
        testTimeslot.setStartTime(LocalTime.of(9, 0));
        testTimeslot.setEndTime(LocalTime.of(10, 0));
    }

    @Test
    @Order(1)
    @WithMockUser(username = "player1@example.com", roles = {"PLAYER"})
    @DisplayName("Should display courts when venue exists")
    public void testShowVenueCourts_Success() throws Exception {
        when(venueDAO.findById(1L)).thenReturn(Optional.of(testVenue));
        when(courtDAO.findByVenueVenueId(1L)).thenReturn(List.of(testCourt));
        when(timeslotDAO.findAll()).thenReturn(List.of(testTimeslot));
        when(bookingService.getAvailabilityMap(anyLong(), any(LocalDate.class), anyList(), anyList()))
                .thenReturn(Map.of("101-201", true));

        mockMvc.perform(get("/venue/1/courts"))
                .andExpect(status().isOk())
                .andExpect(view().name("court"))
                .andExpect(model().attributeExists("venue", "courts", "timeSlots", "availability"))
                .andExpect(model().attribute("venue", testVenue));

        verify(venueDAO, times(1)).findById(1L);
        verify(bookingService, times(1)).getAvailabilityMap(anyLong(), any(LocalDate.class), anyList(), anyList());
    }

    @Test
    @Order(2)
    @WithMockUser(username = "player1@example.com", roles = {"PLAYER"})
    @DisplayName("Should redirect when venue not found")
    public void testShowVenueCourts_VenueNotFound() throws Exception {
        when(venueDAO.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/venue/999/courts"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/venues"));

        verify(venueDAO, times(1)).findById(999L);
    }

    @Test
    @Order(3)
    @WithMockUser(username = "player1@example.com", roles = {"PLAYER"})
    @DisplayName("Should redirect when no courts found for venue")
    public void testShowVenueCourts_NoCourtsFound() throws Exception {
        when(venueDAO.findById(1L)).thenReturn(Optional.of(testVenue));
        when(courtDAO.findByVenueVenueId(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/venue/1/courts"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/venues"));

        verify(courtDAO, times(1)).findByVenueVenueId(1L);
    }

    @Test
    @Order(4)
    @WithMockUser(username = "player1@example.com", roles = {"PLAYER"})
    @DisplayName("Should redirect when no timeslots found")
    public void testShowVenueCourts_NoTimeslotsFound() throws Exception {
        when(venueDAO.findById(1L)).thenReturn(Optional.of(testVenue));
        when(courtDAO.findByVenueVenueId(1L)).thenReturn(List.of(testCourt));
        when(timeslotDAO.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/venue/1/courts"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/venues"));

        verify(timeslotDAO, times(1)).findAll();
    }

    @Test
    @Order(5)
    @WithMockUser(username = "player1@example.com", roles = {"PLAYER"})
    @DisplayName("Should handle invalid date input gracefully")
    public void testShowVenueCourts_InvalidDate() throws Exception {
        when(venueDAO.findById(1L)).thenReturn(Optional.of(testVenue));
        when(courtDAO.findByVenueVenueId(1L)).thenReturn(List.of(testCourt));
        when(timeslotDAO.findAll()).thenReturn(List.of(testTimeslot));
        when(bookingService.getAvailabilityMap(anyLong(), any(LocalDate.class), anyList(), anyList()))
                .thenReturn(Map.of("101-201", true));

        mockMvc.perform(get("/venue/1/courts").param("date", "invalid-date"))
                .andExpect(status().isOk())
                .andExpect(view().name("court"))
                .andExpect(model().attributeExists("venue", "courts", "timeSlots", "availability"))
                .andExpect(model().attribute("venue", testVenue));

        verify(bookingService, times(1)).getAvailabilityMap(anyLong(), any(LocalDate.class), anyList(), anyList());
    }
}
