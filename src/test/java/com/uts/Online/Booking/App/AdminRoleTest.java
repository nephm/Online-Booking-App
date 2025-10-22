package com.uts.Online.Booking.App;

import com.uts.Online.Booking.App.config.SecurityConfig;
import com.uts.Online.Booking.App.controller.AdminController;
import com.uts.Online.Booking.App.model.User;
import com.uts.Online.Booking.App.service.BookingService;
import com.uts.Online.Booking.App.service.UserService;
import com.uts.Online.Booking.App.service.CustomerDetailsService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
public class AdminRoleTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private UserService userService;

    // ✅ Mock Security dependency
    @MockBean
    private CustomerDetailsService customerDetailsService;

    private User testUser;

    @BeforeEach
    void setup() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("Bobby");
        testUser.setRole("ROLE_PLAYER");
    }

    // Test: Admin can access /admin/roles
    @Test
    @WithMockUser(username = "admin1", roles = {"ADMIN"})
    void adminCanAccessRolesPage() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(testUser));

        mockMvc.perform(MockMvcRequestBuilders.get("/admin/roles"))
                .andExpect(status().isOk())
                .andExpect(view().name("roles"))
                .andExpect(model().attributeExists("users"));
    }

    // Test: Player cannot access /admin/roles
    @Test
    @WithMockUser(username = "player1", roles = {"PLAYER"})
    void playerCannotAccessRolesPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/roles"))
                .andExpect(status().isForbidden());
    }

    // Test: Admin updates user role successfully
    @Test
    @WithMockUser(username = "admin1", roles = {"ADMIN"})
    void adminCanUpdateUserRoleSuccessfully() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/setRole")
                        .with(csrf())
                        .param("userId", "1")
                        .param("role", "ROLE_ADMIN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/roles"));

        verify(userService, times(1)).updateUserRole(1L, "ROLE_ADMIN");
    }

    // Test: Exception when role update fails
    @Test
    @WithMockUser(username = "admin1", roles = {"ADMIN"})
    void adminSetRoleThrowsError() throws Exception {
        doThrow(new RuntimeException("User not found"))
                .when(userService).updateUserRole(anyLong(), anyString());

        mockMvc.perform(MockMvcRequestBuilders.post("/admin/setRole")
                        .with(csrf())
                        .param("userId", "99")
                        .param("role", "ROLE_PLAYER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/roles"));

        verify(userService, times(1)).updateUserRole(99L, "ROLE_PLAYER");
    }
}
