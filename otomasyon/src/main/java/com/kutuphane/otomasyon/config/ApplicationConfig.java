package com.kutuphane.otomasyon.config;

import com.kutuphane.otomasyon.repository.KullaniciRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final KullaniciRepository repository;

    @Bean
    public UserDetailsService userDetailsService() {
        // Kanka burada Spring'e diyoruz ki:
        // "Birisi giriş yapmaya çalıştığında git bizim repository'den email ile bul"
        return username -> repository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı, email: " + username));
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        // Giriş (Login) işlemlerini yöneten ana müdür
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Şifreleri veritabanında açık halde tutmuyoruz kanka, BCrypt ile maskeliyoruz
        return new BCryptPasswordEncoder();
    }
}