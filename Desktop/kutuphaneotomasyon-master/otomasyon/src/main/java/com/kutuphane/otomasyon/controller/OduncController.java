package com.kutuphane.otomasyon.controller;

import com.kutuphane.otomasyon.model.Odunc;
import com.kutuphane.otomasyon.service.OduncService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Ödünç alma ve iade işlemleri ile ilgili HTTP isteklerini yöneten REST
 * denetleyicisi. Bu, Kitap ve Kullanıcı arasındaki ilişkiyi yönetir.
 */
@RestController
@RequestMapping("/api/odunc") // Bu denetleyiciye gelen tüm istekler "/api/odunc" yolu ile başlar.
public class OduncController {

    private final OduncService oduncService; // İş mantığı servisini tutan alan

    /**
     * Gerekli servisleri enjekte etmek için kullanılan kurucu metot (Constructor
     * Injection).
     */
    public OduncController(OduncService oduncService) {
        this.oduncService = oduncService;
    }

    /**
     * Bir kullanıcıya bir kitap ödünç verme işlemini gerçekleştirir.
     * HTTP Metodu: POST /api/odunc/ver?kitapId=...&userId=...
     * * @param kitapId Ödünç verilecek kitabın Query Parametresi ile alınan ID'si.
     * 
     * @param kullaniciId Kitabı ödünç alacak kullanıcının Query Parametresi ile
     *                    alınan ID'si.
     * @return Oluşturulan yeni Odunc kaydı ve HTTP 201 (Created) durum kodu.
     */
    @PostMapping("/ver")
    public ResponseEntity<Odunc> kitapOduncVer(
            @RequestParam Long kitapId, // URL'deki Query Parametresi 'kitapId' alınır
            @RequestParam("userId") Long kullaniciId) { // URL'deki Query Parametresi 'userId' alınır

        // Servis, iş kurallarını (limit kontrolü, stok azaltma) uygular
        Odunc yeniOdunc = oduncService.kitapOduncVer(kullaniciId, kitapId);
        return new ResponseEntity<>(yeniOdunc, HttpStatus.CREATED);
    }

    /**
     * Bir ödünç kaydını sonlandırarak kitabın iade edilmesini sağlar.
     * HTTP Metodu: PUT /api/odunc/iade/{oduncId}
     * * @param oduncId İade edilecek kaydın URL'den alınan (PathVariable) ID'si.
     * 
     * @return Güncellenmiş (iade tarihi eklenmiş) ödünç kaydı ve HTTP 200 (OK).
     */
    @PutMapping("/iade/{oduncId}")
    public ResponseEntity<Odunc> kitapIadeAl(@PathVariable Long oduncId) {
        // Servis, iade işlemini (stok arttırma, kayıt sonlandırma) uygular
        Odunc iadeEdilen = oduncService.kitapIadeAl(oduncId);
        return ResponseEntity.ok(iadeEdilen);
    }

}