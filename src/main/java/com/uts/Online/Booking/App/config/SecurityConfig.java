package com.uts.Online.Booking.App.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.uts.Online.Booking.App.service.CustomerDetailsService;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    private final CustomerDetailsService userDetailService;

    public SecurityConfig(CustomerDetailsService userDetailService) {
        this.userDetailService = userDetailService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/", "/index", "/login", "/register", "/registration_email", "/activate", "/css/**", "/js/**", "/images/**", "/error").permitAll()
                    .requestMatchers("/admin/**", "/roles/**", "/dashboard/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
                )
                .formLogin(form -> form
                    .loginPage("/login")
                    .successHandler((request, respone, authentication) ->{
                        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                            respone.sendRedirect("/dashboard");  
                        } else {
                            respone.sendRedirect("/main");
                        }
                    })
                    .failureUrl("/login?error=true")
                    .permitAll()
                )
                .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID", "BADMINSESSION")
                    .permitAll()
                )
                .userDetailsService(userDetailService);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}