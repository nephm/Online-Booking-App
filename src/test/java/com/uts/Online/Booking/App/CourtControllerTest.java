package com.uts.Online.Booking.App;

import com.uts.Online.Booking.App.DAO.BookingDAO;
import com.uts.Online.Booking.App.DAO.CourtDAO;
import com.uts.Online.Booking.App.DAO.TimeslotDAO;
import com.uts.Online.Booking.App.DAO.VenueDAO;
import com.uts.Online.Booking.App.model.Booking;
import com.uts.Online.Booking.App.model.Court;
import com.uts.Online.Booking.App.model.Timeslot;
import com.uts.Online.Booking.App.model.Venue;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Court Controller Integration Tests")
class CourtControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VenueDAO venueDAO;

    @Autowired
    private CourtDAO courtDAO;

    @Autowired
    private TimeslotDAO timeslotDAO;

    @Autowired
    private BookingDAO bookingDAO;

    private Venue testVenue;
    private Court testCourt;
    private Timeslot testTimeslot;

    @BeforeEach
    void setUp() {
        testVenue = new Venue();
        testVenue.setVenueName("Test Venue");
        testVenue.setAddress("123 Test St");
        testVenue = venueDAO.save(testVenue);

        testCourt = new Court();
        testCourt.setCourtName("Test Court");
        testCourt.setCourtType("Indoor");
        testCourt.setLocation("Building A");
        testCourt.setHourlyRate(50.0);
        testCourt.setVenue(testVenue);
        testCourt = courtDAO.save(testCourt);

        testTimeslot = new Timeslot();
        testTimeslot.setStartTime(LocalTime.of(14, 0));
        testTimeslot.setEndTime(LocalTime.of(15, 0));
        testTimeslot = timeslotDAO.save(testTimeslot);
    }

    @Test
    @Order(1)
    @DisplayName("Should display venue courts page with today's date")
    void testShowVenueCourts_DefaultDate() throws Exception {
        mockMvc.perform(get("/venue/" + testVenue.getVenueId() + "/courts"))
                .andExpect(status().isOk())
                .andExpect(view().name("court"))
                .andExpect(model().attributeExists("venue"))
                .andExpect(model().attributeExists("courts"))
                .andExpect(model().attributeExists("timeSlots"))
                .andExpect(model().attributeExists("availability"))
                .andExpect(model().attribute("editMode", false));
    }

    @Test
    @Order(2)
    @DisplayName("Should display venue courts page with specific date")
    void testShowVenueCourts_SpecificDate() throws Exception {
        String date = "2025-10-15";

        mockMvc.perform(get("/venue/" + testVenue.getVenueId() + "/courts")
                        .param("date", date))
                .andExpect(status().isOk())
                .andExpect(view().name("court"))
                .andExpect(model().attributeExists("selectedDate"))
                .andExpect(model().attribute("selectedDate", LocalDate.parse(date)));
    }

    @Test
    @Order(3)
    @DisplayName("Should handle invalid venue ID")
    void testShowVenueCourts_InvalidVenueId() throws Exception {
        mockMvc.perform(get("/venue/99999/courts"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/venues"));
    }

    @Test
    @Order(4)
    @DisplayName("Should show availability map with booked slots")
    void testShowVenueCourts_WithBookedSlots() throws Exception {
        // Create a booking
        Booking booking = new Booking();
        booking.setCourt(testCourt);
        booking.setTimeslot(testTimeslot);
        booking.setBookingDate(LocalDate.now().plusDays(1));
        booking.setUserId(1L);
        booking.setStatus("CONFIRMED");
        bookingDAO.save(booking);

        String date = LocalDate.now().plusDays(1).toString();

        mockMvc.perform(get("/venue/" + testVenue.getVenueId() + "/courts")
                        .param("date", date))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("availability"));
    }

    @Test
    @Order(5)
    @DisplayName("Should enter edit mode with correct parameters")
    void testShowVenueCourts_EditMode() throws Exception {
        // Create a booking to edit
        Booking booking = new Booking();
        booking.setCourt(testCourt);
        booking.setTimeslot(testTimeslot);
        booking.setBookingDate(LocalDate.of(2025, 10, 20));
        booking.setUserId(1L);
        booking.setStatus("CONFIRMED");
        booking = bookingDAO.save(booking);

        mockMvc.perform(get("/venue/" + testVenue.getVenueId() + "/courts")
                        .param("date", "2025-10-20")
                        .param("editBookingId", booking.getBookingId().toString())
                        .param("originalCourtId", testCourt.getCourtId().toString())
                        .param("originalTimeslotId", testTimeslot.getTimeslotId().toString())
                        .param("originalUserId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("court"))
                .andExpect(model().attribute("editMode", true))
                .andExpect(model().attributeExists("editBookingId"))
                .andExpect(model().attributeExists("preSelectedSlot"))
                .andExpect(model().attributeExists("originalDate"));
    }
    
    @Test
    @Order(8)
    @DisplayName("Should redirect when edit booking not found")
    void testShowVenueCourts_EditModeBookingNotFound() throws Exception {
        mockMvc.perform(get("/venue/" + testVenue.getVenueId() + "/courts")
                        .param("editBookingId", "99999")
                        .param("originalCourtId", "1")
                        .param("originalTimeslotId", "1")
                        .param("originalUserId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @Order(9)
    @DisplayName("Should generate available dates list")
    void testShowVenueCourts_AvailableDates() throws Exception {
        mockMvc.perform(get("/venue/" + testVenue.getVenueId() + "/courts"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("availableDates"));
    }

    @Test
    @Order(10)
    @DisplayName("Should set correct submit action for normal mode")
    void testShowVenueCourts_NormalModeSubmitAction() throws Exception {
        mockMvc.perform(get("/venue/" + testVenue.getVenueId() + "/courts"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("submitAction", "/book"))
                .andExpect(model().attribute("submitButtonText", "Proceed to Book"));
    }

    @Test
    @Order(11)
    @DisplayName("Should set correct submit action for edit mode")
    void testShowVenueCourts_EditModeSubmitAction() throws Exception {
        // Create a booking to edit
        Booking booking = new Booking();
        booking.setCourt(testCourt);
        booking.setTimeslot(testTimeslot);
        booking.setBookingDate(LocalDate.now().plusDays(1));
        booking.setUserId(1L);
        booking.setStatus("CONFIRMED");
        booking = bookingDAO.save(booking);

        mockMvc.perform(get("/venue/" + testVenue.getVenueId() + "/courts")
                        .param("editBookingId", booking.getBookingId().toString())
                        .param("originalCourtId", testCourt.getCourtId().toString())
                        .param("originalTimeslotId", testTimeslot.getTimeslotId().toString())
                        .param("originalUserId", "1"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("submitAction", "/admin/update-booking"))
                .andExpect(model().attribute("submitButtonText", "Update Booking"));
    }
}