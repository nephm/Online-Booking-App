package com.uts.Online.Booking.App;

import com.uts.Online.Booking.App.DAO.AdminDAO;
import com.uts.Online.Booking.App.DAO.PlayerDAO;
import com.uts.Online.Booking.App.DAO.UserDAO;
import com.uts.Online.Booking.App.controller.AuthController;
import com.uts.Online.Booking.App.model.User;
import com.uts.Online.Booking.App.model.Admin;
import com.uts.Online.Booking.App.model.Payment;
import com.uts.Online.Booking.App.model.Player;
import com.uts.Online.Booking.App.service.CustomerDetailsService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlayerDAO playerDAO;

    @MockBean
    private UserDAO userDAO;

    @MockBean
    private AdminDAO adminDAO;

    @MockBean
    private CustomerDetailsService uService;

    @Test
    @WithMockUser(username = "player@example.com")
    public void testRegister() throws Exception{
        
        

        
    }
}
