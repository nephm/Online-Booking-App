// package com.uts.Online.Booking.App.config;

// import org.springframework.context.annotation.Configuration;
// import org.springframework.context.annotation.Bean;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.web.SecurityFilterChain;

// @Configuration
// public class SecurityConfig {
    
//     @Bean
//     public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
//         http
//                 .csrf(csrf -> csrf.disable())
//                 .authorizeHttpRequests(auth -> auth
//                 .requestMatchers("/", "/index", "/login", "/register", "/css/**", "/js/**", "/images/**", "/error").permitAll().anyRequest().authenticated())
//                 .formLogin(form -> form
//                     .loginPage("/login")
//                     .defaultSuccessUrl("/main", true)
//                     .permitAll()
//                 )
//                 .logout(logout -> logout
//                 .logoutSuccessUrl("/")
//                 .permitAll());
//         return http.build();
//     }
// }
