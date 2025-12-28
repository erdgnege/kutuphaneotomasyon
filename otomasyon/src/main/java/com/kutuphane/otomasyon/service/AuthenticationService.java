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

    /**
     * Kullanıcı girişi (Login) işlemini yapar.
     * * @param request Kullanıcının girdiği email ve şifre
     * 
     * @return Token içeren yanıt
     */
    public AuthResponse authenticate(AuthRequest request) {

        // 1. Spring Security'nin kendi mekanizmasıyla şifre kontrolü yapıyoruz.
        // Eğer şifre yanlışsa veya kullanıcı yoksa burada otomatik hata fırlatır
        // (BadCredentialsException).
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        // 2. Giriş başarılıysa, token üretmek için kullanıcının detaylarını
        // veritabanından çekiyoruz.
        Kullanici user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı!"));

        // 3. Token üretiyoruz
        String jwtToken = jwtService.generateToken(user);

        // 4. Token'ı paketleyip dönüyoruz (Lombok @Builder kullanarak)
        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }
}
