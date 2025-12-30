package com.kutuphane.otomasyon.controller;

import com.kutuphane.otomasyon.model.Kullanici;
import com.kutuphane.otomasyon.model.Uye;
import com.kutuphane.otomasyon.service.EmailVerificationService;
import com.kutuphane.otomasyon.service.KullaniciService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kullanicilar")
@RequiredArgsConstructor
public class KullaniciController {

    private final KullaniciService kullaniciService;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService verificationService;

    // 1. SADECE ADMIN: Tüm kullanıcıları (Üye + Personel) listele
    @GetMapping("/hepsi")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Kullanici>> tumKullanicilar() {
        return ResponseEntity.ok(kullaniciService.tumKullanicilariGetir());
    }

    // 2. SADECE ADMIN: Sadece üyeleri listele
    @GetMapping("/uyeler")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Uye>> tumUyeler() {
        return ResponseEntity.ok(kullaniciService.tumUyeleriGetir());
    }

    // 3. GENEL: ID ile kullanıcı profili getir
    @GetMapping("/{id}")
    public ResponseEntity<Kullanici> kullaniciGetir(@PathVariable Long id) {
        // Not: Güvenlik için üyeler sadece kendi ID'lerini görebilmeli
        return ResponseEntity.ok(kullaniciService.kullaniciBulById(id));
    }

    // 3.5. GENEL: Üye numarası ile üye bul (Login için)
    @GetMapping("/uye-no/{uyeNo}")
    public ResponseEntity<Uye> uyeBulByUyeNo(@PathVariable String uyeNo) {
        return ResponseEntity.ok(kullaniciService.uyeBulByUyeNo(uyeNo));
    }

    // 3.6. GENEL: Email ile kullanıcı bul (Token ile login sonrası)
    @GetMapping("/email/{email}")
    public ResponseEntity<Kullanici> kullaniciGetirByEmail(@PathVariable String email) {
        return ResponseEntity.ok(kullaniciService.kullaniciBulByEmail(email));
    }

    // 4. GENEL: Profil Güncelleme
    @PutMapping("/guncelle/{id}")
    public ResponseEntity<Kullanici> kullaniciGuncelle(@PathVariable Long id,
            @RequestBody Map<String, Object> guncellemeVerisi) {
        Kullanici mevcutKullanici = kullaniciService.kullaniciBulById(id);

        // Ad Soyad güncellemesi
        if (guncellemeVerisi.containsKey("adSoyad"))
            mevcutKullanici.setAdSoyad((String) guncellemeVerisi.get("adSoyad"));

        // Email güncellemesi
        if (guncellemeVerisi.containsKey("email"))
            mevcutKullanici.setEmail((String) guncellemeVerisi.get("email"));

        // Telefon güncellemesi
        if (guncellemeVerisi.containsKey("telefon"))
            mevcutKullanici.setTelefon((String) guncellemeVerisi.get("telefon"));

        // Şifre değişikliğinde mutlaka yeniden encode edilmeli
        if (guncellemeVerisi.containsKey("sifre")) {
            String hamSifre = (String) guncellemeVerisi.get("sifre");
            mevcutKullanici.setSifre(passwordEncoder.encode(hamSifre));
        }

        Kullanici guncellenmisKullanici = kullaniciService.kullaniciKaydet(mevcutKullanici);
        return ResponseEntity.ok(guncellenmisKullanici);
    }

    // 5. SADECE ADMIN: Kullanıcı Sil
    @DeleteMapping("/sil/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> kullaniciSil(@PathVariable Long id) {
        kullaniciService.kullaniciSil(id);
        return ResponseEntity.ok(Map.of("message", "Kullanıcı sistemden silindi."));
    }

    // 6. SADECE ADMIN: Kullanıcı tipini değiştir (UYE <-> PERSONEL)
    @PutMapping("/tip-degistir/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Kullanici> kullaniciTipiniDegistir(
            @PathVariable Long id,
            @RequestParam String yeniTip) {
        Kullanici guncellenmisKullanici = kullaniciService.kullaniciTipiniDegistir(id, yeniTip);
        return ResponseEntity.ok(guncellenmisKullanici);
    }

    // 7. GENEL: E-posta doğrulama kodu gönder (Kayıt için)
    @PostMapping("/email/kod-gonder")
    public ResponseEntity<?> kodGonder(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "E-posta adresi gereklidir."));
        }

        String code = verificationService.generateAndSendCode(email);
        return ResponseEntity.ok(Map.of(
                "message", "Doğrulama kodu e-postanıza gönderildi.",
                "code", code // Test için kod döndürülüyor (production'da kaldırılabilir)
        ));
    }

    // 8. GENEL: E-posta doğrulama kodu doğrula (Kayıt için)
    @PostMapping("/email/kod-dogrula")
    public ResponseEntity<?> kodDogrula(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "E-posta adresi gereklidir."));
        }

        if (code == null || code.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Doğrulama kodu gereklidir."));
        }

        if (verificationService.verifyCode(email, code)) {
            return ResponseEntity.ok(Map.of("message", "E-posta doğrulandı."));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Kod hatalı veya süresi dolmuş!"));
        }
    }

    // 9. GENEL: Yeni üye kaydı (E-posta doğrulaması sonrası)
    @PostMapping("/uye")
    public ResponseEntity<?> uyeKaydet(@RequestBody Uye uye) {
        // E-posta doğrulaması kontrolü - kod doğrulanmış olmalı
        if (!verificationService.hasCode(uye.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Lütfen önce e-posta adresinizi doğrulayın."));
        }

        // Şifre kontrolü - şifre zorunludur
        if (uye.getSifre() == null || uye.getSifre().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Şifre gereklidir."));
        }

        // Şifre en az 6 karakter olmalı
        if (uye.getSifre().length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("message", "Şifre en az 6 karakter olmalıdır."));
        }

        // Şifre BCrypt ile encode edilir
        uye.setSifre(passwordEncoder.encode(uye.getSifre()));

        Kullanici kaydedilenUye = kullaniciService.kullaniciKaydet(uye);
        verificationService.clearCode(uye.getEmail()); // Kayıt bitince kodu temizle

        return ResponseEntity.ok(kaydedilenUye);
    }
}