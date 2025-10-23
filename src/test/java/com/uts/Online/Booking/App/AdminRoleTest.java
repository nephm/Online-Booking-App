package com.uts.Online.Booking.App;

import com.uts.Online.Booking.App.config.SecurityConfig;
import com.uts.Online.Booking.App.controller.AdminController;
import com.uts.Online.Booking.App.model.User;
import com.uts.Online.Booking.App.service.BookingService;
import com.uts.Online.Booking.App.service.UserService;
import com.uts.Online.Booking.App.service.CustomerDetailsService;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Admin Role Management Tests")
public class AdminRoleTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CustomerDetailsService customerDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("Bobby");
        testUser.setRole("ROLE_PLAYER");
    }

    @Test
    @Order(1)
    @WithMockUser(username = "admin1", roles = {"ADMIN"})
    @DisplayName("Should allow admin to access roles management page")
    void testAdminCanAccessRolesPage() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(testUser));

        mockMvc.perform(get("/admin/roles"))
                .andExpect(status().isOk())
                .andExpect(view().name("roles"))
                .andExpect(model().attributeExists("users"));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @Order(2)
    @WithMockUser(username = "player1", roles = {"PLAYER"})
    @DisplayName("Should restrict player access to admin roles page")
    void testPlayerCannotAccessRolesPage() throws Exception {
        mockMvc.perform(get("/admin/roles"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(3)
    @WithMockUser(username = "admin1", roles = {"ADMIN"})
    @DisplayName("Should allow admin to update user roles successfully")
    void testAdminCanUpdateUserRoleSuccessfully() throws Exception {
        mockMvc.perform(post("/admin/setRole")
                        .with(csrf())
                        .param("userId", "1")
                        .param("role", "ROLE_ADMIN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/roles"));

        verify(userService, times(1)).updateUserRole(1L, "ROLE_ADMIN");
    }

    @Test
    @Order(4)
    @WithMockUser(username = "admin1", roles = {"ADMIN"})
    @DisplayName("Should handle exceptions gracefully during role update")
    void testAdminSetRoleThrowsError() throws Exception {
        doThrow(new RuntimeException("User not found"))
                .when(userService).updateUserRole(anyLong(), anyString());

        mockMvc.perform(post("/admin/setRole")
                        .with(csrf())
                        .param("userId", "99")
                        .param("role", "ROLE_PLAYER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/roles"));

        verify(userService, times(1)).updateUserRole(99L, "ROLE_PLAYER");
    }
}
