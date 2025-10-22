package com.uts.Online.Booking.App;

import com.uts.Online.Booking.App.controller.CourtController;
import com.uts.Online.Booking.App.DAO.CourtDAO;
import com.uts.Online.Booking.App.DAO.TimeslotDAO;
import com.uts.Online.Booking.App.DAO.VenueDAO;
import com.uts.Online.Booking.App.model.*;
import com.uts.Online.Booking.App.service.BookingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourtController.class)
public class CourtControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourtDAO courtDAO;

    @MockBean
    private TimeslotDAO timeslotDAO;

    @MockBean
    private VenueDAO venueDAO;

    @MockBean
    private BookingService bookingService;

    private Venue testVenue;
    private Court testCourt;
    private Timeslot testTimeslot;

    @BeforeEach
    void setup() {
        testVenue = new Venue();
        testVenue.setVenueId(1L);
        testVenue.setVenueName("UTS Badminton Hall");

        testCourt = new Court();
        testCourt.setCourtId(101L);
        testCourt.setCourtName("Court A");
        testCourt.setVenue(testVenue);

        testTimeslot = new Timeslot();
        testTimeslot.setTimeslotId(201L);
        testTimeslot.setStartTime(LocalTime.of(9, 0));
        testTimeslot.setEndTime(LocalTime.of(10, 0));
    }

    // Test: Normal case (venue exists, player logged in)
    @Test
    @WithMockUser(username = "player1", roles = {"PLAYER"})
    void showVenueCourts_shouldReturnCourtView_whenVenueExists() throws Exception {
        when(venueDAO.findById(1L)).thenReturn(Optional.of(testVenue));
        when(courtDAO.findByVenueVenueId(1L)).thenReturn(List.of(testCourt));
        when(timeslotDAO.findAll()).thenReturn(List.of(testTimeslot));
        when(bookingService.getAvailabilityMap(anyLong(), any(LocalDate.class), anyList(), anyList()))
                .thenReturn(Map.of("101-201", true));

        mockMvc.perform(MockMvcRequestBuilders.get("/venue/1/courts"))
                .andExpect(status().isOk())
                .andExpect(view().name("court"))
                .andExpect(model().attributeExists("venue"))
                .andExpect(model().attributeExists("courts"))
                .andExpect(model().attributeExists("timeSlots"))
                .andExpect(model().attributeExists("availability"))
                .andExpect(model().attribute("venue", testVenue));
    }

    //Test: Venue not found
    @Test
    @WithMockUser(username = "player1", roles = {"PLAYER"})
    void showVenueCourts_shouldRedirectToVenues_whenVenueNotFound() throws Exception {
        when(venueDAO.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/venue/999/courts"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/venues"));
    }

    // Test: No courts found for venue
    @Test
    @WithMockUser(username = "player1", roles = {"PLAYER"})
    void showVenueCourts_shouldRedirectToVenues_whenNoCourtsFound() throws Exception {
        when(venueDAO.findById(1L)).thenReturn(Optional.of(testVenue));
        when(courtDAO.findByVenueVenueId(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/venue/1/courts"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/venues"));
    }

    // Test: No timeslots found
    @Test
    @WithMockUser(username = "player1", roles = {"PLAYER"})
    void showVenueCourts_shouldRedirectToVenues_whenNoTimeslotsFound() throws Exception {
        when(venueDAO.findById(1L)).thenReturn(Optional.of(testVenue));
        when(courtDAO.findByVenueVenueId(1L)).thenReturn(List.of(testCourt));
        when(timeslotDAO.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/venue/1/courts"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/venues"));
    }

    // Test: Invalid date should default to today
    @Test
    @WithMockUser(username = "player1", roles = {"PLAYER"})
    void showVenueCourts_shouldHandleInvalidDate() throws Exception {
        when(venueDAO.findById(1L)).thenReturn(Optional.of(testVenue));
        when(courtDAO.findByVenueVenueId(1L)).thenReturn(List.of(testCourt));
        when(timeslotDAO.findAll()).thenReturn(List.of(testTimeslot));
        when(bookingService.getAvailabilityMap(anyLong(), any(LocalDate.class), anyList(), anyList()))
                .thenReturn(Map.of("101-201", true));

        mockMvc.perform(MockMvcRequestBuilders.get("/venue/1/courts")
                .param("date", "invalid-date"))
                .andExpect(status().isOk())
                .andExpect(view().name("court"))
                .andExpect(model().attributeExists("venue"))
                .andExpect(model().attributeExists("availability"));
    }
}
