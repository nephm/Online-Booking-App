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
@DisplayName("Admin Controller Integration Tests")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingDAO bookingDAO;

    @Autowired
    private CourtDAO courtDAO;

    @Autowired
    private TimeslotDAO timeslotDAO;

    @Autowired
    private VenueDAO venueDAO;

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
    @DisplayName("Should display admin page with bookings")
    void testAdminPage_DisplaysBookings() throws Exception {
        // Create test booking
        Booking booking = createTestBooking();
        bookingDAO.save(booking);

        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"))
                .andExpect(model().attributeExists("bookings"));
    }

    @Test
    @Order(2)
    @DisplayName("Should redirect to edit page with correct parameters")
    void testEditBooking_RedirectsCorrectly() throws Exception {
        // Create test booking
        Booking booking = createTestBooking();
        booking = bookingDAO.save(booking);

        mockMvc.perform(get("/admin/edit/" + booking.getBookingId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/venue/" + testVenue.getVenueId() + "/courts?date=*&editBookingId=*"));
    }

    @Test
    @Order(3)
    @DisplayName("Should handle edit request for non-existent booking")
    void testEditBooking_NotFound() throws Exception {
        mockMvc.perform(get("/admin/edit/99999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @Order(4)
    @DisplayName("Should update booking successfully")
    void testUpdateBooking_Success() throws Exception {
        // Create original booking
        Booking booking = createTestBooking();
        booking = bookingDAO.save(booking);

        // Create new timeslot for update
        Timeslot newTimeslot = new Timeslot();
        newTimeslot.setStartTime(LocalTime.of(16, 0));
        newTimeslot.setEndTime(LocalTime.of(17, 0));
        newTimeslot = timeslotDAO.save(newTimeslot);

        String newSlot = testCourt.getCourtId() + "-" + newTimeslot.getTimeslotId() + "-2025-10-10";

        mockMvc.perform(post("/admin/update-booking")
                        .param("selectedSlots", newSlot)
                        .param("editBookingId", booking.getBookingId().toString())
                        .param("originalUserId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("success"));

        // Verify update
        Booking updated = bookingDAO.findById(booking.getBookingId()).orElse(null);
        Assertions.assertNotNull(updated);
        Assertions.assertEquals(newTimeslot.getTimeslotId(), updated.getTimeslot().getTimeslotId());
    }

    @Test
    @Order(5)
    @DisplayName("Should reject update with no slots selected")
    void testUpdateBooking_NoSlots() throws Exception {
        Booking booking = createTestBooking();
        booking = bookingDAO.save(booking);

        mockMvc.perform(post("/admin/update-booking")
                        .param("editBookingId", booking.getBookingId().toString())
                        .param("originalUserId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @Order(6)
    @DisplayName("Should reject update with multiple slots")
    void testUpdateBooking_MultipleSlots() throws Exception {
        Booking booking = createTestBooking();
        booking = bookingDAO.save(booking);

        Timeslot timeslot2 = new Timeslot();
        timeslot2.setStartTime(LocalTime.of(16, 0));
        timeslot2.setEndTime(LocalTime.of(17, 0));
        timeslot2 = timeslotDAO.save(timeslot2);

        String slot1 = testCourt.getCourtId() + "-" + testTimeslot.getTimeslotId() + "-2025-10-10";
        String slot2 = testCourt.getCourtId() + "-" + timeslot2.getTimeslotId() + "-2025-10-10";

        mockMvc.perform(post("/admin/update-booking")
                        .param("selectedSlots", slot1, slot2)
                        .param("editBookingId", booking.getBookingId().toString())
                        .param("originalUserId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @Order(7)
    @DisplayName("Should prevent updating to already booked slot")
    void testUpdateBooking_SlotAlreadyBooked() throws Exception {
        // Create two bookings
        Booking booking1 = createTestBooking();
        booking1.setBookingDate(LocalDate.of(2025, 10, 15));
        booking1 = bookingDAO.save(booking1);

        Timeslot timeslot2 = new Timeslot();
        timeslot2.setStartTime(LocalTime.of(16, 0));
        timeslot2.setEndTime(LocalTime.of(17, 0));
        timeslot2 = timeslotDAO.save(timeslot2);

        Booking booking2 = new Booking();
        booking2.setCourt(testCourt);
        booking2.setTimeslot(timeslot2);
        booking2.setBookingDate(LocalDate.of(2025, 10, 15));
        booking2.setUserId(2L);
        booking2.setStatus("CONFIRMED");
        bookingDAO.save(booking2);

        // Try to update booking1 to booking2's slot
        String slot = testCourt.getCourtId() + "-" + timeslot2.getTimeslotId() + "-2025-10-15";

        mockMvc.perform(post("/admin/update-booking")
                        .param("selectedSlots", slot)
                        .param("editBookingId", booking1.getBookingId().toString())
                        .param("originalUserId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @Order(8)
    @DisplayName("Should delete booking successfully")
    void testDeleteBooking_Success() throws Exception {
        // Create test booking
        Booking booking = createTestBooking();
        booking = bookingDAO.save(booking);
        Long bookingId = booking.getBookingId();

        mockMvc.perform(post("/admin/delete/" + bookingId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("success"));

        // Verify deletion
        Assertions.assertFalse(bookingDAO.existsById(bookingId));
    }

    @Test
    @Order(9)
    @DisplayName("Should handle delete request for non-existent booking")
    void testDeleteBooking_NotFound() throws Exception {
        mockMvc.perform(post("/admin/delete/99999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @Order(10)
    @DisplayName("Should delete booking via AJAX successfully")
    void testDeleteBookingAjax_Success() throws Exception {
        // Create test booking
        Booking booking = createTestBooking();
        booking = bookingDAO.save(booking);
        Long bookingId = booking.getBookingId();

        mockMvc.perform(delete("/admin/api/booking/" + bookingId))
                .andExpect(status().isOk())
                .andExpect(content().string("SUCCESS"));

        // Verify deletion
        Assertions.assertFalse(bookingDAO.existsById(bookingId));
    }

    @Test
    @Order(11)
    @DisplayName("Should handle AJAX delete for non-existent booking")
    void testDeleteBookingAjax_NotFound() throws Exception {
        mockMvc.perform(delete("/admin/api/booking/99999"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.startsWith("ERROR")));
    }

    // Helper method
    private Booking createTestBooking() {
        Booking booking = new Booking();
        booking.setCourt(testCourt);
        booking.setTimeslot(testTimeslot);
        booking.setBookingDate(LocalDate.of(2025, 10, 10));
        booking.setUserId(1L);
        booking.setStatus("CONFIRMED");
        return booking;
    }
}