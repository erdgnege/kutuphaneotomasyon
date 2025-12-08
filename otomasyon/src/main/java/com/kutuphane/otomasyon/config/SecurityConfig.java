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

@Configuration // Spring Konfigürasyon sınıfı olduğunu belirtir
@EnableWebSecurity // Security konfigürasyonunu aktif eder
public class SecurityConfig {

        // HTTP isteklerini ve güvenlik kurallarını tanımlar
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                // CSRF Korumasını kapatıyoruz. REST API'ler ve Postman gibi araçlar için
                                // gereklidir.
                                .csrf(csrf -> csrf.disable())

                                // CORS ayarını etkinleştirir. Farklı kaynaklardan (port/domain) gelen isteklere
                                // izin verir.
                                .cors(cors -> {
                                })

                                // URL bazlı yetkilendirme kurallarını başlatır
                                .authorizeHttpRequests(authorize -> authorize

                                                // 1. KULLANICI YÖNETİMİ: Kullanıcı ekleme/silme/güncelleme sadece ADMIN
                                                // yapmalı
                                                .requestMatchers("/api/kullanicilar/**").hasRole("ADMIN")

                                                // 2. ÖDÜNÇ İŞLEMLERİ: Ödünç verme/iade alma sadece ADMIN yapmalı
                                                .requestMatchers("/api/odunc/**").hasRole("ADMIN")

                                                // 3. KİTAP YÖNETİMİ: Kitap ekleme/silme gibi yönetim yolları sadece
                                                // ADMIN yapmalı
                                                .requestMatchers("/api/kitaplar/admin/**").hasRole("ADMIN")

                                                // 4. KİTAP LİSTELEME: Genel kitap sorgulama, USER veya ADMIN yapabilir
                                                .requestMatchers("/api/kitaplar/**").hasAnyRole("USER", "ADMIN")

                                                // Diğer tüm istekler kimlik doğrulaması gerektirir
                                                .anyRequest().authenticated())

                                // HTTP Basic Auth'u etkinleştirir. Kullanıcı adı/şifre Header ile gönderilir.
                                .httpBasic(httpBasic -> {
                                });

                return http.build(); // Yapılandırılmış SecurityFilterChain nesnesini döndürür
        }

        // Kullanıcı adı ve rolleri bellekte (in-memory) tutan servisi tanımlar (Geçici
        // kullanıcılar)
        @Bean
        public UserDetailsService userDetailsService() {

                // ADMIN rolüne sahip kullanıcı (password encoder, geliştirme için varsayılanı
                // kullanır)
                UserDetails admin = User.withDefaultPasswordEncoder()
                                .username("admin")
                                .password("123456")
                                .roles("ADMIN")
                                .build();

                // USER rolüne sahip normal kullanıcı (Kütüphane üyesini temsil edebilir)
                UserDetails user = User.withDefaultPasswordEncoder()
                                .username("user")
                                .password("sifre")
                                .roles("USER")
                                .build();

                // Bellekteki kullanıcıları yöneten servisi döndürür
                return new InMemoryUserDetailsManager(admin, user);
        }
}