package com.uts.Online.Booking.App;

import com.uts.Online.Booking.App.DAO.CourtDAO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "testuser", roles = {"USER"})
public class CourtDisplayTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourtDAO courtDAO;

    @Test
    public void testCourtTableDisplay() throws Exception {
        mockMvc.perform(get("/venue/1/courts"))
               .andExpect(status().isOk())
               .andExpect(view().name("court"))
               .andExpect(model().attributeExists("courts"))
               .andExpect(model().attributeExists("timeSlots"))
               .andExpect(content().string(containsString("Court 1")))
               .andExpect(content().string(containsString("10:00")));
    }

    @Test
    public void testCourtDateSelection() throws Exception {
        String testDate = "2025-09-21";
        mockMvc.perform(get("/venue/1/courts").param("date", testDate))
               .andExpect(status().isOk())
               .andExpect(model().attribute("selectedDate", LocalDate.parse(testDate)));
    }

    @Test
    public void testCourtAvailabilityDisplay() throws Exception {
        mockMvc.perform(get("/venue/1/courts"))
               .andExpect(status().isOk())
               .andExpect(model().attributeExists("availability"));
    }
}