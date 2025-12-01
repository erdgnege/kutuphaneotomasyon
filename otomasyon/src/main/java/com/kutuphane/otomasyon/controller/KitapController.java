package com.kutuphane.otomasyon.controller;

import com.kutuphane.otomasyon.model.Kitap;
import com.kutuphane.otomasyon.service.KitapService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// 👈 Bu etiketi koyarak Spring Boot'a bu sınıfın API isteklerini yöneteceğini söylüyoruz.
@RestController
// 👈 Bu etiketi koyarak tüm metotların /api/kitaplar adresiyle başlayacağını
// söylüyoruz.
@RequestMapping("/api/kitaplar")
public class KitapController {

    // Service'i buraya dahil ediyoruz (Bağımlılık Enjeksiyonu)
    private final KitapService kitapService;

    // Kurucu metot: Spring Boot Service'i hazır verir.
    public KitapController(KitapService kitapService) {
        this.kitapService = kitapService;
    }

    // --- TEMEL API METOTLARI ---

    // 1. KİTAP EKLEME (POST Metodu)
    // URL: POST http://localhost:8080/api/kitaplar
    @PostMapping
    public Kitap kitapEkle(@RequestBody Kitap kitap) {
        // Gelen JSON verisini Kitap nesnesine dönüştürür
        return kitapService.kitapEkle(kitap);
    }

    // 2. TÜM KİTAPLARI GETİRME (GET Metodu)
    // URL: GET http://localhost:8080/api/kitaplar
    @GetMapping
    public List<Kitap> tumKitaplariGetir() {
        return kitapService.tumKitaplariGetir();
    }

    // 3. KİTAP SİLME (DELETE Metodu)
    // URL: DELETE http://localhost:8080/api/kitaplar/3 (3 numaralı kitabı siler)
    @DeleteMapping("/{id}")
    public String kitapSil(@PathVariable Long id) {
        kitapService.kitapSil(id);
        return "Kitap başarıyla silindi: ID " + id;
    }
}