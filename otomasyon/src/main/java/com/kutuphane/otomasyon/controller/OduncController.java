package com.kutuphane.otomasyon.controller;

import com.kutuphane.otomasyon.model.Odunc;
import com.kutuphane.otomasyon.service.OduncService;
import org.springframework.web.bind.annotation.*;

// Not: Bu sınıf için ayrı bir DTO (Data Transfer Object) kullanmak daha profesyonel olsa da,
// basitlik için şimdilik ID'leri parametre olarak alıyoruz.
@RestController
@RequestMapping("/api/odunc") // URL adresi: /api/odunc
public class OduncController {

    private final OduncService oduncService;

    // Bağımlılık Enjeksiyonu
    public OduncController(OduncService oduncService) {
        this.oduncService = oduncService;
    }

    // 1. KİTAP ÖDÜNÇ VERME (POST Metodu)
    // URL: POST http://localhost:8080/api/odunc/ver?kitapId=1&uyeId=2
    @PostMapping("/ver")
    public Odunc kitapOduncVer(
            @RequestParam Long kitapId, // URL'den kitapId'yi alır
            @RequestParam Long uyeId) { // URL'den uyeId'yi alır

        return oduncService.kitapOduncVer(kitapId, uyeId);
    }

    // (Opsiyonel) TÜM ÖDÜNÇ KAYITLARINI GETİRME
    // Bu metodu OduncService'e ekleyip buraya da GetMapping yapabilirsin.
}