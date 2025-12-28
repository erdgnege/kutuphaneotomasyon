package com.kutuphane.otomasyon.controller;

import com.kutuphane.otomasyon.model.Kitap;
import com.kutuphane.otomasyon.service.KitapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kitap")
@RequiredArgsConstructor // Kanka constructor injection'ı Lombok ile hallediyoruz
public class KitapController {

    private final KitapService kitapService;

    // 1. Herkese Açık: Tüm kitapları listele
    @GetMapping
    public ResponseEntity<List<Kitap>> tumKitaplariGetir() {
        return ResponseEntity.ok(kitapService.tumKitaplariGetir());
    }

    // 2. Herkese Açık: ID ile kitap detayını getir
    @GetMapping("/{id}")
    public ResponseEntity<Kitap> kitapBul(@PathVariable Long id) {
        return ResponseEntity.ok(kitapService.kitapBulById(id));
    }

    // 3. SADECE ADMIN: Yeni kitap ekle
    @PostMapping("/ekle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Kitap> kitapEkle(@Valid @RequestBody Kitap kitap) {
        // Eğer kapakUrl boş gelirse varsayılan bir görsel set edebilirsin kanka
        if (kitap.getKapakUrl() == null || kitap.getKapakUrl().isEmpty()) {
            kitap.setKapakUrl("https://via.placeholder.com/150?text=Kitap+Kapagi");
        }
        return ResponseEntity.ok(kitapService.kitapEkle(kitap));
    }

    // 4. SADECE ADMIN: Kitap sil
    @DeleteMapping("/sil/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> kitapSil(@PathVariable Long id) {
        kitapService.kitapSil(id);
        return ResponseEntity.ok(Map.of("message", "Kitap başarıyla silindi kanka."));
    }

    // 5. SADECE ADMIN: Kitap bilgilerini güncelle
    @PutMapping("/guncelle/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Kitap> kitapGuncelle(@PathVariable Long id, @RequestBody Kitap yeniKitap) {
        Kitap mevcutKitap = kitapService.kitapBulById(id);

        mevcutKitap.setBaslik(yeniKitap.getBaslik());
        mevcutKitap.setYazar(yeniKitap.getYazar());
        mevcutKitap.setIsbn(yeniKitap.getIsbn());
        mevcutKitap.setKapakUrl(yeniKitap.getKapakUrl());
        // Mevcut durumunu (ödünçte mi değil mi) elle değiştirtmiyoruz,
        // onu OduncService hallediyor kanka.

        return ResponseEntity.ok(kitapService.kitapEkle(mevcutKitap));
    }
}