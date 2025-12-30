package com.kutuphane.otomasyon.controller;

import com.kutuphane.otomasyon.model.Bildirim;
import com.kutuphane.otomasyon.service.BildirimService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bildirim")
@RequiredArgsConstructor
public class BildirimController {

    private final BildirimService bildirimService;

    // 1. GENEL: Tüm bildirimleri getir (Önce okunmamışlar ve en yeniler)
    @GetMapping
    public ResponseEntity<List<Bildirim>> getTumBildirimler() {
        return ResponseEntity.ok(bildirimService.tumBildirimleriGetir());
    }

    // 2. GENEL: Okunmamış bildirim sayısı (Badge için)
    @GetMapping("/okunmamis-sayisi")
    public ResponseEntity<Long> getOkunmamisSayisi() {
        return ResponseEntity.ok(bildirimService.okunmamisBildirimSayisi());
    }

    // 3. GENEL: Tek bir bildirimi okundu işaretle
    @PostMapping("/oku/{id}")
    public ResponseEntity<?> bildirimOku(@PathVariable Long id) {
        bildirimService.bildirimiOkunduIsaretle(id);
        return ResponseEntity.ok(Map.of("message", "Bildirim okundu."));
    }

    // 4. GENEL: Tüm bildirimleri okundu işaretle
    @PostMapping("/hepsini-oku")
    public ResponseEntity<?> tumunuOku() {
        bildirimService.tumBildirimleriOkunduIsaretle();
        return ResponseEntity.ok(Map.of("message", "Tüm bildirimler okundu olarak işaretlendi."));
    }
}