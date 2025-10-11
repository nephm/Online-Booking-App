package com.uts.Online.Booking.App;

import com.uts.Online.Booking.App.DAO.VenueDAO;
import com.uts.Online.Booking.App.model.Venue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "testuser", roles = {"USER"})
public class VenueDisplayTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VenueDAO venueDAO;

    @Test
    public void testVenueListDisplay() throws Exception {
        mockMvc.perform(get("/venues"))
               .andExpect(status().isOk())
               .andExpect(view().name("venues"))
               .andExpect(content().string(containsString("Elite Badminton Center")))
               .andExpect(content().string(containsString("SportsPlex Arena")))
               .andExpect(content().string(containsString("Champion Courts")));
    }

    @Test
    public void testInvalidVenueAccess() throws Exception {
        mockMvc.perform(get("/venue/999/courts"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/venues"));
    }
}
