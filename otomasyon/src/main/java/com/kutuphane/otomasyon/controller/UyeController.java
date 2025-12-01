package com.kutuphane.otomasyon.controller;

import com.kutuphane.otomasyon.model.Uye;
import com.kutuphane.otomasyon.service.UyeService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/uyeler") // URL adresi: /api/uyeler
public class UyeController {

    private final UyeService uyeService;

    // Bağımlılık Enjeksiyonu
    public UyeController(UyeService uyeService) {
        this.uyeService = uyeService;
    }

    // 1. ÜYE EKLEME (POST Metodu)
    // URL: POST http://localhost:8080/api/uyeler
    @PostMapping
    public Uye uyeEkle(@RequestBody Uye uye) {
        return uyeService.uyeEkle(uye);
    }

    // 2. TÜM ÜYELERİ GETİRME (GET Metodu)
    // URL: GET http://localhost:8080/api/uyeler
    @GetMapping
    public List<Uye> tumUyeleriGetir() {
        return uyeService.tumUyeleriGetir();
    }

    // 3. ID İLE ÜYE GETİRME (GET Metodu)
    // URL: GET http://localhost:8080/api/uyeler/5
    @GetMapping("/{id}")
    public Uye uyeBul(@PathVariable Long id) {
        // Optional'dan Uye nesnesini güvenli bir şekilde çekeriz, yoksa hata veririz
        return uyeService.uyeBulById(id)
                .orElseThrow(() -> new RuntimeException("ID: " + id + " numaralı üye bulunamadı."));
    }

    @PutMapping("/{id}")
    public Uye uyeGuncelle(@PathVariable Long id, @RequestBody Uye newUye) {
        Optional<Uye> uye = uyeService.uyeBulById(id);
        if (uye.isPresent()) {
            Uye uyeBulundu = uye.get();
            uyeBulundu.setAdSoyad(newUye.getAdSoyad());
            uyeBulundu.setEmail(newUye.getEmail());
            return uyeService.uyeEkle(uyeBulundu);

        } else {
            throw new RuntimeException("ID: " + id + " numaralı üye bulunamadı.");
        }
    }

    // 4. ÜYE SİLME (DELETE Metodu)
    // URL: DELETE http://localhost:8080/api/uyeler/5
    @DeleteMapping("/{id}")
    public String uyeSil(@PathVariable Long id) {
        uyeService.uyeSil(id);
        return "Üye başarıyla silindi: ID " + id;
    }
}