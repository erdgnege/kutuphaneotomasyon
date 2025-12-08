package com.kutuphane.otomasyon.controller;

import com.kutuphane.otomasyon.model.Kitap;
import com.kutuphane.otomasyon.exception.KaynakBulunamadiException;
import com.kutuphane.otomasyon.service.KitapService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Kitap varlıkları ile ilgili HTTP isteklerini yöneten REST denetleyicisi.
 * Bu sınıf, kitap ekleme, listeleme ve silme işlemleri için API endpoint'leri
 * sağlar.
 */
@RestController
@RequestMapping("/api/kitaplar") // Bu denetleyiciye gelen tüm istekler "/api/kitaplar" yolu ile başlar.
public class KitapController {

    private final KitapService kitapService; // İş mantığı servisini tutan final alan

    /**
     * Gerekli servisleri enjekte etmek için kullanılan kurucu metot (Constructor
     * Injection). Spring, KitapService bean'ini otomatik sağlar.
     */
    public KitapController(KitapService kitapService) {
        this.kitapService = kitapService;
    }

    /**
     * Sisteme yeni bir kitap ekler.
     * HTTP Metodu: POST /api/kitaplar
     * 
     * @param kitap HTTP isteğinin gövdesinden (RequestBody) gelen Kitap nesnesi.
     * @return Oluşturulan kitap ve HTTP 201 (Created) durum kodu ile yanıt
     *         döndürülür.
     */
    @PostMapping
    public ResponseEntity<Kitap> kitapEkle(@RequestBody Kitap kitap) {
        Kitap yeniKitap = kitapService.kitapEkle(kitap); // Servis katmanında kaydı gerçekleştirir
        return new ResponseEntity<>(yeniKitap, HttpStatus.CREATED);
    }

    /**
     * Sistemdeki tüm kitapları listeler.
     * HTTP Metodu: GET /api/kitaplar
     * 
     * @return Kitap listesi ve HTTP 200 (OK) durum kodu ile yanıt döndürülür.
     */
    @GetMapping
    public ResponseEntity<List<Kitap>> tumKitaplariGetir() {
        List<Kitap> kitaplar = kitapService.tumKitaplariGetir(); // Tüm kitap verisini servisten çeker
        return ResponseEntity.ok(kitaplar); // HTTP 200 OK ile listeyi döndürür
    }

    /**
     * Belirtilen ID'ye sahip kitabı sistemden siler.
     * HTTP Metodu: DELETE /api/kitaplar/{id}
     * 
     * @param id Silinecek kitabın URL'den alınan (PathVariable) ID'si.
     * @return İçerik olmadığını belirten HTTP 204 (No Content) durum kodu.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> kitapSil(@PathVariable Long id) {
        // Silme işleminden önce kaynağın var olup olmadığını kontrol eder, yoksa hata
        // fırlatır.
        kitapService.kitapBulById(id)
                .orElseThrow(() -> new KaynakBulunamadiException("ID: " + id + " numaralı kitap bulunamadı."));
        kitapService.kitapSil(id); // Servis üzerinden silme işlemini çağırır
        return ResponseEntity.noContent().build(); // HTTP 204 No Content yanıtı döner
    }
}