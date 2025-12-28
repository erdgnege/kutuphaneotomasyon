package com.kutuphane.otomasyon.controller;

import com.kutuphane.otomasyon.dto.AuthRequest;
import com.kutuphane.otomasyon.dto.AuthResponse;
import com.kutuphane.otomasyon.model.Kullanici;
import com.kutuphane.otomasyon.model.Uye;
import com.kutuphane.otomasyon.service.AuthenticationService;
import com.kutuphane.otomasyon.service.EmailVerificationService;
import com.kutuphane.otomasyon.service.KullaniciService;
import com.kutuphane.otomasyon.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authService;
    private final KullaniciService kullaniciService;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService verificationService;
    private final PasswordResetService passwordResetService;

    // 1. ADIM: E-posta doğrulama kodu iste
    @PostMapping("/kod-iste")
    public ResponseEntity<?> kodGonder(@RequestParam String email) {
        verificationService.generateAndSendCode(email);
        return ResponseEntity.ok(Map.of("message", "Doğrulama kodu e-postanıza gönderildi kanka."));
    }

    // 2. ADIM: Kayıt Ol (Uye olarak)
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody Uye uye,
            @RequestParam String kod) {

        // Kanka önce kod doğru mu diye bakıyoruz
        if (!verificationService.verifyCode(uye.getEmail(), kod)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Kod hatalı veya süresi dolmuş brom!"));
        }

        // Şifreyi BCrypt ile şifrelemeden kaydetmiyoruz adaş, güvenlik şart!
        uye.setSifre(passwordEncoder.encode(uye.getSifre()));

        kullaniciService.kullaniciKaydet(uye);
        verificationService.clearCode(uye.getEmail()); // Kayıt bitince kodu temizle

        return ResponseEntity.ok(Map.of("message", "Kayıt başarıyla tamamlandı kardeşim. Giriş yapabilirsin."));
    }

    // 3. ADIM: Giriş Yap (Token Al)
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    // 4. ŞİFRE SIFIRLAMA: Şifre sıfırlama kodu iste
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        // Email'e kayıtlı kullanıcı var mı kontrol et
        try {
            kullaniciService.kullaniciBulByEmail(email);
            // Kullanıcı bulundu, şifre sıfırlama kodu gönder
            passwordResetService.generateAndSendResetCode(email);
        } catch (Exception e) {
            // Kullanıcı bulunamadı, ama güvenlik için aynı mesajı döndür (email enumeration saldırısını önlemek için)
            // Gerçek projede kullanıcı yoksa da aynı mesaj döndürülmeli
        }
        // Güvenlik için her durumda aynı mesajı döndür
        return ResponseEntity.ok(Map.of("message", "Şifre sıfırlama kodu e-postanıza gönderildi."));
    }

    // 5. ŞİFRE SIFIRLAMA: Şifreyi sıfırla (kod ve yeni şifre ile)
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam String email,
            @RequestParam String code,
            @RequestParam String newPassword) {
        
        // Kod doğrulaması
        if (!passwordResetService.verifyResetCode(email, code)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Kod hatalı veya süresi dolmuş!"));
        }
        
        // Kullanıcıyı bul
        Kullanici kullanici = kullaniciService.kullaniciBulByEmail(email);
        
        // Yeni şifreyi encode et ve kaydet
        kullanici.setSifre(passwordEncoder.encode(newPassword));
        kullaniciService.kullaniciKaydet(kullanici);
        
        return ResponseEntity.ok(Map.of("message", "Şifreniz başarıyla sıfırlandı. Giriş yapabilirsiniz."));
    }
}