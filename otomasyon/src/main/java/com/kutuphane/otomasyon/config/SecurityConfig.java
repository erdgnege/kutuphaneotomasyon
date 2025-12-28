package com.kutuphane.otomasyon.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthFilter;
        private final AuthenticationProvider authenticationProvider;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS ayarları
                                .csrf(csrf -> csrf.disable()) // API projelerinde kanka csrf'i kapatıyoruz
                                .authorizeHttpRequests(auth -> auth
                                                // 1. Herkese açık olan yollar
                                                .requestMatchers("/api/auth/**").permitAll()
                                                .requestMatchers("/api/kitap", "/api/kitap/{id}").permitAll() // Kitapları
                                                                                                              // herkes
                                                                                                              // görebilir
                                                                                                              // (GET)
                                                .requestMatchers("/api/kullanici/uye-no/**").permitAll() // Üye numarası
                                                                                                         // ile arama
                                                                                                         // (login için)
                                                .requestMatchers("/api/kullanici/email/**").authenticated() // Email ile
                                                                                                            // kullanıcı
                                                                                                            // bulma
                                                                                                            // (token
                                                                                                            // gerekli)

                                                // 2. ADMIN ÖZEL İŞLEMLER: Kitap yönetimi ve ödünç verme/iade alma
                                                .requestMatchers("/api/admin/**").permitAll() // Admin login endpoint'i
                                                                                              // herkese açık
                                                .requestMatchers("/api/kitap/ekle").hasRole("ADMIN") // Admin kitap
                                                                                                     // ekleyebilir
                                                .requestMatchers("/api/kitap/sil/**").hasRole("ADMIN") // Admin kitap
                                                                                                       // silebilir
                                                .requestMatchers("/api/kitap/guncelle/**").hasRole("ADMIN") // Admin
                                                                                                            // kitap
                                                                                                            // güncelleyebilir
                                                .requestMatchers("/api/odunc/ver").hasRole("ADMIN") // Admin ödünç
                                                                                                    // verebilir
                                                .requestMatchers("/api/odunc/iade/**").hasRole("ADMIN") // Admin iade
                                                                                                        // alabilir
                                                .requestMatchers("/api/odunc/aktif").hasRole("ADMIN") // Admin aktif
                                                                                                      // ödünçleri
                                                                                                      // görebilir
                                                .requestMatchers("/api/odunc/tum-kayitlar").hasRole("ADMIN") // Admin
                                                                                                             // tüm
                                                                                                             // kayıtları
                                                                                                             // görebilir
                                                .requestMatchers("/api/odunc/tum-islem-loglari").hasRole("ADMIN") // Admin
                                                                                                                  // tüm
                                                                                                                  // işlem
                                                                                                                  // loglarını
                                                                                                                  // görebilir
                                                .requestMatchers("/api/kullanici/hepsi").hasRole("ADMIN") // Admin tüm
                                                                                                          // kullanıcıları
                                                                                                          // görebilir
                                                .requestMatchers("/api/kullanici/uyeler").hasRole("ADMIN") // Admin tüm
                                                                                                           // üyeleri
                                                                                                           // görebilir
                                                .requestMatchers("/api/kullanici/sil/**").hasRole("ADMIN") // Admin
                                                                                                           // kullanıcı
                                                                                                           // silebilir
                                                .requestMatchers("/api/kullanici/tip-degistir/**").hasRole("ADMIN") // Admin
                                                                                                                    // kullanıcı
                                                                                                                    // tipini
                                                                                                                    // değiştirebilir

                                                // 3. ÜYELERİN VE PERSONELİN kendi işlemlerini yapabileceği yollar
                                                // (Personel artık normal üye gibi, sadece daha fazla kitap ödünç
                                                // alabilir)
                                                .requestMatchers("/api/odunc/kullanici-iste")
                                                .hasAnyRole("UYE", "PERSONEL") // Üye ve Personel ödünç alabilir
                                                .requestMatchers("/api/odunc/kullanici-iade/**")
                                                .hasAnyRole("UYE", "PERSONEL") // Üye ve Personel kendi kitabını iade
                                                                               // edebilir
                                                .requestMatchers("/api/odunc/kullanici/**")
                                                .hasAnyRole("UYE", "PERSONEL") // Üye ve Personel kendi ödünçlerini
                                                                               // görebilir

                                                // 4. Geri kalan her şey için giriş yapmış olmak şart
                                                .anyRequest().authenticated())
                                // Session tutmuyoruz kanka, her şey JWT üzerinden dönecek (Stateless)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authenticationProvider(authenticationProvider)
                                // Bizim yazdığımız JWT filtresini, standart şifre filtresinden önceye koyuyoruz
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOriginPatterns(Arrays.asList("*")); // Tüm origin'lere izin ver (development
                                                                            // için)
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(Arrays.asList("*"));
                configuration.setAllowCredentials(false); // Wildcard origin ile credentials kullanılamaz
                configuration.setExposedHeaders(Arrays.asList("Authorization"));

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}