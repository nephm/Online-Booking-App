package com.uts.Online.Booking.App;

import com.uts.Online.Booking.App.DAO.AdminDAO;
import com.uts.Online.Booking.App.DAO.PlayerDAO;
import com.uts.Online.Booking.App.DAO.UserDAO;
import com.uts.Online.Booking.App.config.SecurityConfig;
import com.uts.Online.Booking.App.controller.AuthController;
import com.uts.Online.Booking.App.model.User;
import com.uts.Online.Booking.App.service.CustomerDetailsService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
@DisplayName("AuthController Test")
public class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlayerDAO playerDAO;

    @MockitoBean
    private UserDAO userDAO;

    @MockitoBean
    private AdminDAO adminDAO;

    @MockitoBean
    private CustomerDetailsService uService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JavaMailSender mailSender;
    

    @BeforeEach
    void setUp() {
    }

    @Test
    @Order(1)
    @WithMockUser(username = "newuser@test.com")
    @DisplayName("Test User registeration - Success")
    public void testRegisterUser_Success() throws Exception{
        
        when(userDAO.findByEmail("newuser@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("12345678")).thenReturn("encodedPass");

        mockMvc.perform(post("/register")
            .with(csrf())
            .param("firstName", "Bob")
            .param("lastName", "Lele")
            .param("email", "newuser@test.com")
            .param("password", "12345678")
            .param("phoneNumber", "0435344345"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/registration_email"));

        // check if user is saved
        ArgumentCaptor<User> userCheck = ArgumentCaptor.forClass(User.class);
        verify(userDAO, times(1)).save(userCheck.capture());
        User savedUser = userCheck.getValue();

        assertThat(savedUser.getEmail()).isEqualTo("newuser@test.com");
        assertThat(savedUser.getPassword()).isEqualTo("encodedPass");
        assertThat(savedUser.isActive()).isFalse();
    }

    @Test
    @Order(2)
    @WithMockUser(username = "existing@test.com")
    @DisplayName("Test user registration when email exists")
    public void testRegisterUser_EmailExists() throws Exception{
        when(userDAO.findByEmail("existing@test.com")).thenReturn(Optional.of(new User()));

        mockMvc.perform(post("/register")
            .with(csrf())
            .param("firstName", "Bob")
            .param("lastName", "Lele")
            .param("email", "existing@test.com")
            .param("password", "12345678")
            .param("phoneNumber", "0435344345"))
            .andExpect(status().isOk())
            .andExpect(view().name("register"))
            .andExpect(model().attribute("error", "Email already exists!"));

    }

    @Test
    @Order(3)
    @WithMockUser(username = "existing@test.com")
    @DisplayName("Test user registration if password is invalid")
    public void testRegisterUser_InvalidPassword() throws Exception{
        when(userDAO.findByEmail("existing@test.com")).thenReturn(Optional.of(new User()));

        mockMvc.perform(post("/register")
            .with(csrf())
            .param("firstName", "Bob")
            .param("lastName", "Lele")
            .param("email", "bleeb@test.com")
            .param("password", "1234")
            .param("phoneNumber", "0435344345"))
            .andExpect(status().isOk())
            .andExpect(view().name("register"))
            .andExpect(model().attribute("error", "Password must be at least 8 characters long"));

    }   

    @Test 
    @Order(4)
    @WithMockUser(username = "existing@test.com")
    @DisplayName("Test display login page")
    public void testLoginPage() throws Exception{
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk())
            .andExpect(view().name("login"));
    }
}
