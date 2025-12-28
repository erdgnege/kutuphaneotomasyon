package com.kutuphane.otomasyon.controller;

import com.kutuphane.otomasyon.dto.AuthResponse;
import com.kutuphane.otomasyon.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final JwtService jwtService;

    // Sabit admin kullanıcı adı ve şifresi (application.properties'den okunabilir,
    // şimdilik hardcoded)
    // Production'da bu bilgileri environment variable veya
    // application.properties'den okuyun!
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123"; // Gerçek projede mutlaka değiştirin!

    // Admin login endpoint'i
    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        // Kullanıcı adı ve şifre kontrolü
        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            // Admin için UserDetails oluştur (ROLE_ADMIN yetkisi ile)
            UserDetails adminUser = User.builder()
                    .username(ADMIN_USERNAME)
                    .password("") // Şifre token'da saklanmaz
                    .authorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                    .build();

            // Admin için 5 dakikalık token oluştur
            String token = jwtService.generateAdminToken(adminUser);

            return ResponseEntity.ok(AuthResponse.builder()
                    .token(token)
                    .build());
        } else {
            return ResponseEntity.status(401).body(Map.of("message", "Kullanıcı adı veya şifre hatalı!"));
        }
    }
}
