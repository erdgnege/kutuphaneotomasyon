package com.kutuphane.otomasyon.controller;

import com.kutuphane.otomasyon.model.Kitap;
import com.kutuphane.otomasyon.service.KitapService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/kitaplar")
public class KitapController {

    private final KitapService kitapService;

    public KitapController(KitapService kitapService) {
        this.kitapService = kitapService;
    }

    // POST: http://localhost:8080/api/kitaplar
    @PostMapping
    public ResponseEntity<Kitap> kitapEkle(@RequestBody Kitap kitap) {
        return new ResponseEntity<>(kitapService.kitapEkle(kitap), HttpStatus.CREATED);
    }

    // GET: http://localhost:8080/api/kitaplar
    @GetMapping
    public List<Kitap> tumKitaplariGetir() {
        return kitapService.tumKitaplariGetir();
    }

    /*
     * GET: http://localhost:8080/api/kitaplar/{id}
     * Örn: http://localhost:8080/api/kitaplar/5
     * Belirli bir kitabın detaylarını getirir.
     */
    @GetMapping("/{id}")
    public Kitap kitapGetir(@PathVariable Long id) {
        // Servis katmanında zaten hata kontrolü yaptığımız için burası tertemiz!
        return kitapService.kitapBulById(id);
    }

    // DELETE: http://localhost:8080/api/kitaplar/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> kitapSil(@PathVariable Long id) {
        kitapService.kitapSil(id);
        return ResponseEntity.noContent().build();
    }
}