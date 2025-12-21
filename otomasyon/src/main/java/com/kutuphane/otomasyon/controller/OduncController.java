package com.kutuphane.otomasyon.controller;

import com.kutuphane.otomasyon.model.Odunc;
import com.kutuphane.otomasyon.service.OduncService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/odunc")
public class OduncController {

    private final OduncService oduncService;

    public OduncController(OduncService oduncService) {
        this.oduncService = oduncService;
    }

    /*
     * POST: http://localhost:8080/api/odunc/ver?kitapId=1&userId=2
     * Bu işlem RequestBody değil, RequestParam kullanır kanka.
     * Postman'de 'Params' kısmına kitapId ve userId ekleyeceksin.
     */
    @PostMapping("/ver")
    public ResponseEntity<Odunc> kitapOduncVer(
            @RequestParam Long kitapId,
            @RequestParam Long userId) {

        return new ResponseEntity<>(oduncService.kitapOduncVer(userId, kitapId), HttpStatus.CREATED);
    }

    /*
     * PUT: http://localhost:8080/api/odunc/iade/1
     * {id} yerine ödünç işleminin ID'sini yazıyorsun brom.
     */
    @PutMapping("/iade/{oduncId}")
    public ResponseEntity<Odunc> kitapIadeAl(@PathVariable Long oduncId) {
        return ResponseEntity.ok(oduncService.kitapIadeAl(oduncId));
    }

    /*
     * GET: http://localhost:8080/api/odunc/aktif
     * Tüm aktif (iade edilmemiş) ödünç kayıtlarını getirir.
     */
    @GetMapping("/aktif")
    public List<Odunc> aktifOduncleriGetir() {
        return oduncService.tumAktifOduncleriGetir();
    }

    /*
     * GET: http://localhost:8080/api/odunc
     * Tüm ödünç kayıtlarını getirir (aktif ve iade edilmiş).
     */
    @GetMapping
    public List<Odunc> tumOduncleriGetir() {
        return oduncService.tumOduncleriGetir();
    }

    /*
     * GET: http://localhost:8080/api/odunc/kullanici/{userId}
     * Belirli bir kullanıcının aktif ödünç kayıtlarını getirir.
     */
    @GetMapping("/kullanici/{userId}")
    public List<Odunc> kullaniciOduncleriGetir(@PathVariable Long userId) {
        return oduncService.kullaniciOduncleriGetir(userId);
    }

    /*
     * POST: http://localhost:8080/api/odunc/kullanici-iste?kitapId=1&userId=2
     * Kullanıcıların kendi adına kitap ödünç istemesi için endpoint.
     */
    @PostMapping("/kullanici-iste")
    public ResponseEntity<Odunc> kullaniciKitapOduncIste(
            @RequestParam Long kitapId,
            @RequestParam Long userId) {
        // Bildirim oluşturma işlemi OduncService içinde yapılıyor (transaction içinde)
        Odunc odunc = oduncService.kitapOduncVer(userId, kitapId, true);
        return new ResponseEntity<>(odunc, HttpStatus.CREATED);
    }

    /*
     * PUT: http://localhost:8080/api/odunc/kullanici-iade/{oduncId}
     * Kullanıcıların kendi ödünçlerini iade etmesi için endpoint.
     */
    @PutMapping("/kullanici-iade/{oduncId}")
    public ResponseEntity<Odunc> kullaniciKitapIadeEt(@PathVariable Long oduncId) {
        // Bildirim oluşturma işlemi OduncService içinde yapılıyor (transaction içinde)
        Odunc oduncKaydi = oduncService.kitapIadeAl(oduncId, true);
        return ResponseEntity.ok(oduncKaydi);
    }
}