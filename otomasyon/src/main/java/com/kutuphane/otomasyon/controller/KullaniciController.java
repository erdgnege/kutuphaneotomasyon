package com.kutuphane.otomasyon.controller;

import com.kutuphane.otomasyon.exception.KaynakBulunamadiException;
import com.kutuphane.otomasyon.model.Personel;
import com.kutuphane.otomasyon.model.Kullanici; // Temel soyut sınıf
import com.kutuphane.otomasyon.model.Uye;
import com.kutuphane.otomasyon.service.KullaniciService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Kullanıcı (Üye ve Personel) varlıkları ile ilgili HTTP isteklerini yöneten
 * REST denetleyicisi. Polimorfizm prensibini kullanır.
 */
@RestController
@RequestMapping("/api/kullanicilar") // Tüm isteklere ön ek olarak eklenir
public class KullaniciController {

    private final KullaniciService kullaniciService; // İş mantığı servisini tutan alan

    /**
     * Gerekli servisleri enjekte etmek için kullanılan kurucu metot (Constructor
     * Injection).
     */
    public KullaniciController(KullaniciService kullaniciService) {
        this.kullaniciService = kullaniciService;
    }

    /**
     * Sisteme yeni bir üye ekler. POST /api/kullanicilar/uye
     * 
     * @param uye Gövdeden gelen Uye nesnesi.
     * @return Oluşturulan üye ve HTTP 201 (Created).
     */
    @PostMapping("/uye")
    public ResponseEntity<Uye> uyeEkle(@RequestBody Uye uye) {
        Uye yeniUye = kullaniciService.kullaniciEkle(uye); // Servis, Uye'yi Kullanici olarak kaydeder (Polimorfizm)
        return new ResponseEntity<>(yeniUye, HttpStatus.CREATED);
    }

    /**
     * Sisteme yeni bir personel ekler. POST /api/kullanicilar/personel
     * 
     * @param personel Gövdeden gelen Personel nesnesi.
     * @return Oluşturulan personel ve HTTP 201 (Created).
     */
    @PostMapping("/personel")
    public ResponseEntity<Personel> personelEkle(@RequestBody Personel personel) {
        Personel yeniPersonel = kullaniciService.kullaniciEkle(personel); // Servis, Personel'i Kullanici olarak
                                                                          // kaydeder
        return new ResponseEntity<>(yeniPersonel, HttpStatus.CREATED);
    }

    /**
     * Sistemdeki tüm kullanıcıları (Üye ve Personel) listeler. GET
     * /api/kullanicilar
     * 
     * @return Tüm Kullanici tiplerinin listesi (Polimorfik dönüş).
     */
    @GetMapping
    public ResponseEntity<List<Kullanici>> tumKullanicilariGetir() {
        List<Kullanici> kullanicilar = kullaniciService.tumKullanicilariGetir();
        return ResponseEntity.ok(kullanicilar);
    }

    /**
     * Sistemdeki sadece üyeleri listeler. GET /api/kullanicilar/uyeler
     * 
     * @return Tüm Uye nesnelerinin listesi.
     */
    @GetMapping("/uyeler")
    public ResponseEntity<List<Uye>> tumUyeleriGetir() {
        List<Uye> uyeler = kullaniciService.tumUyeleriGetir();
        return ResponseEntity.ok(uyeler);
    }

    /**
     * Sistemdeki sadece personelleri listeler. GET /api/kullanicilar/personeller
     * 
     * @return Tüm Personel nesnelerinin listesi.
     */
    @GetMapping("/personeller")
    public ResponseEntity<List<Personel>> tumPersonelleriGetir() {
        List<Personel> personeller = kullaniciService.tumPersonelleriGetir();
        return ResponseEntity.ok(personeller);
    }

    /**
     * Belirtilen ID'ye sahip kullanıcıyı getirir. GET /api/kullanicilar/{id}
     * 
     * @param id Aranacak kullanıcının ID'si.
     * @return Bulunan Kullanici nesnesi (Uye veya Personel olabilir).
     */
    @GetMapping("/{id}")
    public ResponseEntity<Kullanici> kullaniciBul(@PathVariable Long id) {
        // Servis ile ID'ye göre arama yapılır, bulunamazsa 404 fırlatılır.
        Kullanici kullanici = kullaniciService.kullaniciBulById(id)
                .orElseThrow(() -> new KaynakBulunamadiException("ID: " + id + " numaralı kullanıcı bulunamadı."));
        return ResponseEntity.ok(kullanici);
    }

    // --- KULLANICI GÜNCELLEME VE SİLME ---

    /**
     * Ortak alanları günceller. PUT /api/kullanicilar/{id}
     * Not: Özel alt sınıf alanlarını güncellemek için DTO veya özel endpoint
     * gerekir.
     */
    // otomasyon/controller/KullaniciController.java - Eklenecek Metot

    /**
     * Bir üyeyi ID'si ile bulup, sadece Üye alanlarını günceller.
     * PUT /api/kullanicilar/uye/{id}
     */
    @PutMapping("/uye/{id}")
    public ResponseEntity<Uye> uyeGuncelle(@PathVariable Long id, @RequestBody Uye guncelUye) {
        Uye uye = kullaniciService.kullaniciBulById(id)
                .map(mevcutUser -> {
                    if (!(mevcutUser instanceof Uye)) {
                        // Eğer bulunan kullanıcı Üye değilse, hata fırlat.
                        throw new KaynakBulunamadiException("ID: " + id + " numaralı kayıt bir Üye değil.");
                    }

                    Uye mevcutUye = (Uye) mevcutUser;
                    // Ortak alanları güncelle (Kullanici.java'dan miras)
                    mevcutUye.setAdSoyad(guncelUye.getAdSoyad());
                    mevcutUye.setEmail(guncelUye.getEmail());
                    mevcutUye.setTelefon(guncelUye.getTelefon());

                    // Uye'ye özgü alanları güncelle (Uye.java'dan)
                    mevcutUye.setUyeNo(guncelUye.getUyeNo());

                    return kullaniciService.kullaniciEkle(mevcutUye);
                })
                .orElseThrow(() -> new KaynakBulunamadiException("ID: " + id + " numaralı kullanıcı bulunamadı."));

        return ResponseEntity.ok((Uye) uye);
    }
    // otomasyon/controller/KullaniciController.java - Eklenecek Metot

    /**
     * Bir personeli ID'si ile bulup, sadece Personel alanlarını günceller.
     * PUT /api/kullanicilar/personel/{id}
     */
    @PutMapping("/personel/{id}")
    public ResponseEntity<Personel> personelGuncelle(@PathVariable Long id, @RequestBody Personel guncelPersonel) {
        Personel personel = kullaniciService.kullaniciBulById(id)
                .map(mevcutUser -> {
                    if (!(mevcutUser instanceof Personel)) {
                        // Eğer bulunan kullanıcı Personel değilse, hata fırlat.
                        throw new KaynakBulunamadiException("ID: " + id + " numaralı kayıt bir Personel değil.");
                    }

                    Personel mevcutPersonel = (Personel) mevcutUser;
                    // Ortak alanları güncelle (Kullanici.java'dan miras)
                    mevcutPersonel.setAdSoyad(guncelPersonel.getAdSoyad());
                    mevcutPersonel.setEmail(guncelPersonel.getEmail());
                    mevcutPersonel.setTelefon(guncelPersonel.getTelefon());

                    // Personel'e özgü alanları güncelle (Personel.java'dan)
                    mevcutPersonel.setSicilNo(guncelPersonel.getSicilNo());
                    mevcutPersonel.setDepartman(guncelPersonel.getDepartman());

                    return kullaniciService.kullaniciEkle(mevcutPersonel);
                })
                .orElseThrow(() -> new KaynakBulunamadiException("ID: " + id + " numaralı kullanıcı bulunamadı."));

        return ResponseEntity.ok((Personel) personel);
    }

    /**
     * Belirtilen ID'ye sahip kullanıcıyı sistemden siler. DELETE
     * /api/kullanicilar/{id}
     * 
     * @param id Silinecek kullanıcının ID'si.
     * @return HTTP 204 (No Content) durum kodu.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> kullaniciSil(@PathVariable Long id) {
        // Silme işleminden önce varlık kontrolü yapılır.
        kullaniciService.kullaniciBulById(id)
                .orElseThrow(() -> new KaynakBulunamadiException("ID: " + id + " numaralı kullanıcı bulunamadı."));
        kullaniciService.kullaniciSil(id);
        return ResponseEntity.noContent().build();
    }
}