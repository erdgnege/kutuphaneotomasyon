package com.kutuphane.otomasyon.controller;

import com.kutuphane.otomasyon.model.*;
import com.kutuphane.otomasyon.service.KullaniciService;
import com.kutuphane.otomasyon.service.EmailVerificationService;
import com.kutuphane.otomasyon.exception.KutuphaneHatasi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kullanicilar")
public class KullaniciController {

    private final KullaniciService kullaniciService;
    private final EmailVerificationService emailVerificationService;
    private final com.kutuphane.otomasyon.service.BildirimService bildirimService;

    public KullaniciController(KullaniciService kullaniciService,
            EmailVerificationService emailVerificationService,
            com.kutuphane.otomasyon.service.BildirimService bildirimService) {
        this.kullaniciService = kullaniciService;
        this.emailVerificationService = emailVerificationService;
        this.bildirimService = bildirimService;
    }

    // --- LİSTELEME İŞLEMLERİ ---

    @GetMapping
    public List<Kullanici> tumKullanicilariGetir() {
        return kullaniciService.tumKullanicilariGetir();
    }

    /*
     * GET: http://localhost:8080/api/kullanicilar/uyeler
     * Sadece Üye (Uye) tipindeki kullanıcıları listeler.
     */
    @GetMapping("/uyeler")
    public List<Uye> tumUyeleriGetir() {
        return kullaniciService.tumUyeleriGetir();
    }

    /*
     * GET: http://localhost:8080/api/kullanicilar/personeller
     * Sadece Personel (Personel) tipindeki kullanıcıları listeler.
     */
    @GetMapping("/personeller")
    public List<Personel> tumPersonelleriGetir() {
        return kullaniciService.tumPersonelleriGetir();
    }

    @GetMapping("/{id}")
    public Kullanici kullaniciBul(@PathVariable Long id) {
        return kullaniciService.kullaniciBulById(id);
    }

    @GetMapping("/uye-no/{uyeNo}")
    public Uye uyeBulByUyeNo(@PathVariable String uyeNo) {
        return kullaniciService.uyeBulByUyeNo(uyeNo);
    }

    // --- E-POSTA DOĞRULAMA İŞLEMLERİ ---

    @PostMapping("/email/kod-gonder")
    public ResponseEntity<Map<String, String>> kodGonder(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                throw new KutuphaneHatasi("E-posta adresi gereklidir.");
            }

            // E-posta format kontrolü
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new KutuphaneHatasi("Geçerli bir e-posta adresi giriniz.");
            }

            // Kod oluştur ve gönder
            emailVerificationService.generateAndSendCode(email);

            return ResponseEntity.ok(Map.of(
                    "message", "Doğrulama kodu e-posta adresinize gönderildi. Lütfen e-postanızı kontrol ediniz.",
                    "email", email));
        } catch (Exception e) {
            // E-posta gönderme hatası olsa bile kod oluşturuldu, kullanıcıya bilgi ver
            System.err.println("E-posta gönderme hatası (kod yine de oluşturuldu): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                    "message",
                    "Doğrulama kodu oluşturuldu. E-posta gönderilemediyse backend console'unu kontrol ediniz.",
                    "email", request.get("email")));
        }
    }

    @PostMapping("/email/kod-dogrula")
    public ResponseEntity<Map<String, String>> kodDogrula(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        if (email == null || email.trim().isEmpty()) {
            throw new KutuphaneHatasi("E-posta adresi gereklidir.");
        }

        if (code == null || code.trim().isEmpty()) {
            throw new KutuphaneHatasi("Doğrulama kodu gereklidir.");
        }

        boolean isValid = emailVerificationService.verifyCode(email, code);

        if (isValid) {
            return ResponseEntity.ok(Map.of("message", "E-posta doğrulandı.", "verified", "true"));
        } else {
            throw new KutuphaneHatasi("Doğrulama kodu geçersiz veya süresi dolmuş.");
        }
    }

    // --- KAYIT İŞLEMLERİ ---

    @PostMapping("/uye")
    public ResponseEntity<Kullanici> uyeEkle(@RequestBody Uye uye) {
        // E-posta doğrulaması kontrolü
        if (!emailVerificationService.hasCode(uye.getEmail())) {
            throw new KutuphaneHatasi("Lütfen önce e-posta adresinizi doğrulayın.");
        }

        // Kayıt işlemi
        Kullanici kaydedilenKullanici = kullaniciService.kullaniciKaydet(uye);

        // Kayıt başarılı oldu, doğrulama kodunu temizle
        emailVerificationService.clearCode(uye.getEmail());

        return new ResponseEntity<>(kaydedilenKullanici, HttpStatus.CREATED);
    }

    @PostMapping("/personel")
    public ResponseEntity<Kullanici> personelEkle(@RequestBody Personel personel) {
        return new ResponseEntity<>(kullaniciService.kullaniciKaydet(personel), HttpStatus.CREATED);
    }

    // --- GÜNCELLEME İŞLEMLERİ (instanceof Kullanımı) ---

    @PutMapping("/uye/{id}")
    public Kullanici uyeGuncelle(@PathVariable Long id, @RequestBody Uye guncelUye) {
        Kullanici k = kullaniciService.kullaniciBulById(id);

        if (!(k instanceof Uye)) {
            throw new KutuphaneHatasi("Hata: Belirtilen ID bir Üye kaydına ait değil!");
        }

        Uye mevcutUye = (Uye) k;
        mevcutUye.setAdSoyad(guncelUye.getAdSoyad());
        mevcutUye.setEmail(guncelUye.getEmail());
        mevcutUye.setTelefon(guncelUye.getTelefon());
        mevcutUye.setUyeNo(guncelUye.getUyeNo());

        return kullaniciService.kullaniciKaydet(mevcutUye);
    }

    @PutMapping("/personel/{id}")
    public Kullanici personelGuncelle(@PathVariable Long id, @RequestBody Personel guncelPersonel) {
        Kullanici k = kullaniciService.kullaniciBulById(id);

        if (!(k instanceof Personel)) {
            throw new KutuphaneHatasi("Hata: Belirtilen ID bir Personel kaydına ait değil!");
        }

        Personel mevcutPersonel = (Personel) k;
        mevcutPersonel.setAdSoyad(guncelPersonel.getAdSoyad());
        mevcutPersonel.setSicilNo(guncelPersonel.getSicilNo());
        mevcutPersonel.setDepartman(guncelPersonel.getDepartman());

        return kullaniciService.kullaniciKaydet(mevcutPersonel);
    }

    // Kullanıcının kendi bilgilerini güncellemesi için endpoint
    @PutMapping("/kendi-bilgilerim/{id}")
    public ResponseEntity<Kullanici> kendiBilgilerimiGuncelle(@PathVariable Long id,
            @RequestBody Kullanici guncelBilgiler) {
        Kullanici mevcutKullanici = kullaniciService.kullaniciBulById(id);

        // Değişiklikleri takip et
        StringBuilder degisiklikler = new StringBuilder();
        boolean degisiklikVar = false;

        if (!mevcutKullanici.getAdSoyad().equals(guncelBilgiler.getAdSoyad())) {
            degisiklikler.append("Ad Soyad: '").append(mevcutKullanici.getAdSoyad())
                    .append("' → '").append(guncelBilgiler.getAdSoyad()).append("'");
            degisiklikVar = true;
        }

        if (!mevcutKullanici.getEmail().equals(guncelBilgiler.getEmail())) {
            if (degisiklikVar)
                degisiklikler.append(", ");
            degisiklikler.append("E-posta: '").append(mevcutKullanici.getEmail())
                    .append("' → '").append(guncelBilgiler.getEmail()).append("'");
            degisiklikVar = true;
        }

        String eskiTelefon = mevcutKullanici.getTelefon() != null ? mevcutKullanici.getTelefon() : "";
        String yeniTelefon = guncelBilgiler.getTelefon() != null ? guncelBilgiler.getTelefon() : "";
        if (!eskiTelefon.equals(yeniTelefon)) {
            if (degisiklikVar)
                degisiklikler.append(", ");
            degisiklikler.append("Telefon: '").append(eskiTelefon.isEmpty() ? "Yok" : eskiTelefon)
                    .append("' → '").append(yeniTelefon.isEmpty() ? "Yok" : yeniTelefon).append("'");
            degisiklikVar = true;
        }

        // Sadece ad, email ve telefon güncellenebilir (üye numarası değiştirilemez)
        mevcutKullanici.setAdSoyad(guncelBilgiler.getAdSoyad());
        mevcutKullanici.setEmail(guncelBilgiler.getEmail());
        mevcutKullanici.setTelefon(guncelBilgiler.getTelefon());

        // Üye numarası değiştirilemez - sabit kalır

        Kullanici guncellenmisKullanici = kullaniciService.kullaniciKaydet(mevcutKullanici);

        // Eğer değişiklik varsa bildirim oluştur
        if (degisiklikVar) {
            String mesaj = mevcutKullanici.getAdSoyad() + " kullanıcısı bilgilerini güncelledi: "
                    + degisiklikler.toString();
            bildirimService.bildirimOlustur(id, mevcutKullanici.getAdSoyad(), mesaj);
        }

        return new ResponseEntity<>(guncellenmisKullanici, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> kullaniciSil(@PathVariable Long id) {
        kullaniciService.kullaniciSil(id);
        return ResponseEntity.noContent().build();
    }

    // --- BİLDİRİM İŞLEMLERİ ---

    @GetMapping("/bildirimler")
    public List<com.kutuphane.otomasyon.model.Bildirim> tumBildirimleriGetir() {
        return bildirimService.tumBildirimleriGetir();
    }

    @GetMapping("/bildirimler/okunmamis-sayisi")
    public ResponseEntity<Map<String, Long>> okunmamisBildirimSayisi() {
        return ResponseEntity.ok(Map.of("sayi", bildirimService.okunmamisBildirimSayisi()));
    }

    @PutMapping("/bildirimler/{id}/okundu")
    public ResponseEntity<Map<String, String>> bildirimiOkunduIsaretle(@PathVariable Long id) {
        bildirimService.bildirimiOkunduIsaretle(id);
        return ResponseEntity.ok(Map.of("message", "Bildirim okundu olarak işaretlendi."));
    }

    @PutMapping("/bildirimler/tumunu-okundu")
    public ResponseEntity<Map<String, String>> tumBildirimleriOkunduIsaretle() {
        bildirimService.tumBildirimleriOkunduIsaretle();
        return ResponseEntity.ok(Map.of("message", "Tüm bildirimler okundu olarak işaretlendi."));
    }
}