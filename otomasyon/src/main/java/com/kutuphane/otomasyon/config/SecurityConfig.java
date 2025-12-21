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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;
import java.util.Arrays;

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
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                                // URL bazlı yetkilendirme kurallarını başlatır
                                .authorizeHttpRequests(authorize -> authorize
                                                // Admin işlemleri için ADMIN rolü gerekli
                                                .requestMatchers("/api/odunc/ver", "/api/odunc/iade/**",
                                                                "/api/odunc/aktif")
                                                .hasRole("ADMIN")

                                                // Kullanıcılar kendi adına ödünç isteği yapabilir ve iade edebilir
                                                .requestMatchers("/api/odunc/kullanici-iste",
                                                                "/api/odunc/kullanici-iade/**")
                                                .permitAll()

                                                // Kullanıcılar kendi bilgilerini görebilir (GET isteği için public)
                                                .requestMatchers(HttpMethod.GET, "/api/kullanicilar/**").permitAll()
                                                
                                                // Üye numarası ile üye bulma endpoint'i public
                                                .requestMatchers("/api/kullanicilar/uye-no/**").permitAll()

                                                // E-posta doğrulama endpoint'leri public
                                                .requestMatchers("/api/kullanicilar/email/**").permitAll()
                                                
                                                // Bildirim endpoint'leri ADMIN rolü gerektirir
                                                .requestMatchers("/api/kullanicilar/bildirimler/**").hasRole("ADMIN")
                                                
                                                // Kullanıcılar üye olabilir (POST /api/kullanicilar/uye public)
                                                .requestMatchers(HttpMethod.POST, "/api/kullanicilar/uye").permitAll()

                                                // Kullanıcılar kendi bilgilerini güncelleyebilir (PUT /api/kullanicilar/kendi-bilgilerim)
                                                .requestMatchers(HttpMethod.PUT, "/api/kullanicilar/kendi-bilgilerim/**").permitAll()

                                                // Kullanıcı yönetimi (PUT, DELETE) için ADMIN rolü gerekli
                                                .requestMatchers("/api/kullanicilar/**").hasRole("ADMIN")

                                                // Kullanıcı kendi ödünçlerini görebilir (authentication gerekli değil)
                                                .requestMatchers("/api/odunc/kullanici/**").permitAll()

                                                // Kitapları herkes görebilir
                                                .requestMatchers("/api/kitaplar/**").permitAll()

                                                // Diğer tüm istekler kimlik doğrulaması gerektirir
                                                .anyRequest().authenticated())

                                // HTTP Basic Auth'u etkinleştirir. Kullanıcı adı/şifre Header ile gönderilir.
                                .httpBasic(httpBasic -> {
                                });

                return http.build(); // Yapılandırılmış SecurityFilterChain nesnesini döndürür
        }

        // Password encoder bean'i
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        // Kullanıcı adı ve rolleri bellekte (in-memory) tutan servisi tanımlar (Geçici
        // kullanıcılar)
        @Bean
        public UserDetailsService userDetailsService() {
                PasswordEncoder encoder = passwordEncoder();

                // ADMIN rolüne sahip kullanıcı
                UserDetails admin = User.builder()
                                .username("admin")
                                .password(encoder.encode("123456"))
                                .roles("ADMIN")
                                .build();

                // USER rolüne sahip normal kullanıcı (Kütüphane üyesini temsil edebilir)
                UserDetails user = User.builder()
                                .username("user")
                                .password(encoder.encode("sifre"))
                                .roles("USER")
                                .build();

                // Bellekteki kullanıcıları yöneten servisi döndürür
                return new InMemoryUserDetailsManager(admin, user);
        }

        // CORS yapılandırması - Frontend'den gelen isteklere izin verir
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:8080",
                                "http://127.0.0.1:5500", "file://")); // Frontend portları
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(Arrays.asList("*"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/api/**", configuration);
                return source;
        }
}