package com.kutuphane.otomasyon.controller;

import com.kutuphane.otomasyon.model.Kullanici;
import com.kutuphane.otomasyon.model.Uye;
import com.kutuphane.otomasyon.service.KullaniciService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kullanici")
@RequiredArgsConstructor
public class KullaniciController {

    private final KullaniciService kullaniciService;
    private final PasswordEncoder passwordEncoder;

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
        // Kanka burada normalde bir üye sadece kendi ID'sini çekebilmeli,
        // güvenliği bir tık daha artırmak istersen ileride oraya check koyarız.
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

        // Kanka eğer şifre değişecekse mutlaka tekrar encode etmeliyiz!
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
        return ResponseEntity.ok(Map.of("message", "Kullanıcı sistemden silindi brom."));
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
}