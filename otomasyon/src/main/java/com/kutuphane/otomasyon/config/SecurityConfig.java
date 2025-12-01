package com.kutuphane.otomasyon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // Security konfigürasyonunu aktif eder
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF Korumasını kapatıyoruz (Postman'de POST/DELETE kullanmak için
                // zorunlu)
                .csrf(csrf -> csrf.disable())

                // 2. CORS ayarını açıyoruz (Farklı port veya domainlerden erişimi sağlar)
                .cors(cors -> {
                })

                .authorizeHttpRequests(authorize -> authorize
                        // ADMIN rolü, ADMIN yollarına erişebilir
                        .requestMatchers("/api/kitaplar/admin/**").hasRole("ADMIN")
                        // USER veya ADMIN rolü, genel API yollarına erişebilir
                        .requestMatchers("/api/kitaplar/**").hasAnyRole("USER", "ADMIN")
                        // Diğer tüm istekler kimlik doğrulaması gerektirir
                        .anyRequest().authenticated())
                // 3. HTTP Basic Auth'u etkinleştiriyoruz
                .httpBasic(httpBasic -> {
                });

        return http.build();
    }

    // HATA DÜZELTİLDİ: Metot bloğu düzeltildi ve return ifadesi içine alındı.
    @Bean
    public UserDetailsService userDetailsService() {

        // ADMIN kullanıcısı
        UserDetails admin = User.withDefaultPasswordEncoder()
                .username("admin")
                .password("123456")
                .roles("ADMIN")
                .build();

        // NORMAL kullanıcısı
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("user")
                .password("sifre")
                .roles("USER")
                .build();

        // Bellekteki kullanıcıları döndür
        return new InMemoryUserDetailsManager(admin, user);
    }
}