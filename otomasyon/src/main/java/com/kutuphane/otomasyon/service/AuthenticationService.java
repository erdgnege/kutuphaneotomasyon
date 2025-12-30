package com.kutuphane.otomasyon.service;

import com.kutuphane.otomasyon.dto.AuthRequest;
import com.kutuphane.otomasyon.dto.AuthResponse;
import com.kutuphane.otomasyon.model.Kullanici;
import com.kutuphane.otomasyon.repository.KullaniciRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

        private final KullaniciRepository repository;
        private final JwtService jwtService;
        private final AuthenticationManager authenticationManager;

        public AuthResponse authenticate(AuthRequest request) {
                Kullanici user = null;
                String username = request.getEmail(); // Email alanı email veya üye no içerebilir

                // 1. Email veya üye numarası ile kullanıcıyı bul
                // Önce email formatında mı kontrol et
                if (username.contains("@")) {
                        // Email ile kullanıcı bul
                        user = repository.findByEmail(username)
                                        .orElse(null);
                } else {
                        // Üye numarası ile kullanıcı bul (sadece Uye olabilir)
                        user = repository.findByUyeNo(username)
                                        .orElse(null);
                }

                if (user == null) {
                        throw new UsernameNotFoundException("Kullanıcı bulunamadı!");
                }

                // 2. Spring Security'nin kendi mekanizmasıyla şifre kontrolü yapıyoruz.
                // Kullanıcının email'ini username olarak kullanıyoruz (Spring Security email
                // ile çalışır)
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                user.getEmail(), // Spring Security için email kullanılmalı
                                                request.getPassword()));

                // 3. Token üretiyoruz
                String jwtToken = jwtService.generateToken(user);

                // 4. Token'ı paketleyip dönüyoruz (Lombok @Builder kullanarak)
                return AuthResponse.builder()
                                .token(jwtToken)
                                .build();
        }
}
