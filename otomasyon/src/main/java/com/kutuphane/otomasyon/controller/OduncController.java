package com.kutuphane.otomasyon.controller;

import com.kutuphane.otomasyon.dto.IslemLogDTO;
import com.kutuphane.otomasyon.model.Odunc;
import com.kutuphane.otomasyon.service.OduncService;
import com.kutuphane.otomasyon.service.TumIslemLoglariService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/odunc")
@RequiredArgsConstructor
public class OduncController {

    private final OduncService oduncService;
    private final TumIslemLoglariService tumIslemLoglariService;

    // 1. ADMIN ÖZEL: Kitap Ödünç Ver (Fiziksel teslimat anında admin yapar)
    @PostMapping("/ver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Odunc> kitapOduncVer(
            @RequestParam Long kitapId,
            @RequestParam Long userId) {

        // Personel yaptığı için bildirimOlustur'u true geçiyoruz kanka
        return new ResponseEntity<>(oduncService.kitapOduncVer(userId, kitapId, true), HttpStatus.CREATED);
    }

    // 2. ADMIN ÖZEL: Kitap İade Al
    @PutMapping("/iade/{oduncId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Odunc> kitapIadeAl(@PathVariable Long oduncId) {
        return ResponseEntity.ok(oduncService.kitapIadeAl(oduncId, true));
    }

    // 3. ÜYE ve PERSONEL: Kullanıcı kendi adına kitap ödünç talebi oluşturur
    @PostMapping("/kullanici-iste")
    @PreAuthorize("hasAnyRole('UYE', 'PERSONEL')")
    public ResponseEntity<Odunc> kullaniciKitapOduncIste(
            @RequestParam Long kitapId,
            @RequestParam Long userId) {
        // Kanka burada userId'nin token'daki kişiyle aynı olduğunu kontrol eden
        // bir mekanizma da eklenebilir ama şimdilik servise paslıyoruz.
        Odunc odunc = oduncService.kitapOduncVer(userId, kitapId, true);
        return new ResponseEntity<>(odunc, HttpStatus.CREATED);
    }

    // 4. ADMIN ÖZEL: Tüm aktif (iade edilmemiş) ödünçleri listele
    @GetMapping("/aktif")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Odunc>> aktifOduncleriGetir() {
        return ResponseEntity.ok(oduncService.tumAktifOduncleriGetir());
    }

    // 5. ÜYE ve PERSONEL: Giriş yapan kullanıcının kendi ödünçlerini getir
    @GetMapping("/kullanici/{userId}")
    public ResponseEntity<List<Odunc>> kullaniciOduncleriGetir(@PathVariable Long userId) {
        return ResponseEntity.ok(oduncService.kullaniciOduncleriGetir(userId));
    }

    // 7. ÜYE ve PERSONEL: Kullanıcı kendi kitabını iade eder
    @PutMapping("/kullanici-iade/{oduncId}")
    @PreAuthorize("hasAnyRole('UYE', 'PERSONEL')")
    public ResponseEntity<Odunc> kullaniciKitapIadeEt(@PathVariable Long oduncId) {
        return ResponseEntity.ok(oduncService.kitapIadeAl(oduncId, true));
    }

    // 6. ADMIN ÖZEL: Tüm geçmiş kayıtları getir (sadece ödünç işlemleri)
    @GetMapping("/tum-kayitlar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Odunc>> tumOduncleriGetir() {
        return ResponseEntity.ok(oduncService.tumOduncleriGetir());
    }

    // 7. ADMIN ÖZEL: Tüm işlem loglarını getir (ödünç + kitap işlemleri)
    @GetMapping("/tum-islem-loglari")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<IslemLogDTO>> tumIslemLoglariniGetir() {
        return ResponseEntity.ok(tumIslemLoglariService.tumIslemLoglariniGetir());
    }
}