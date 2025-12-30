package com.kutuphane.otomasyon.service;

import com.kutuphane.otomasyon.model.*;
import com.kutuphane.otomasyon.repository.*;
import com.kutuphane.otomasyon.exception.KutuphaneHatasi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.List;

@Service
public class OduncService {

    private final KitapRepository kitapRepository;
    private final KullaniciRepository kullaniciRepository;
    private final OduncRepository oduncRepository;
    private final BildirimService bildirimService;

    @PersistenceContext
    private EntityManager entityManager;

    public OduncService(KitapRepository kitapRepository, KullaniciRepository kullaniciRepository,
            OduncRepository oduncRepository, BildirimService bildirimService) {
        this.kitapRepository = kitapRepository;
        this.kullaniciRepository = kullaniciRepository;
        this.oduncRepository = oduncRepository;
        this.bildirimService = bildirimService;
    }

    @Transactional
    public Odunc kitapOduncVer(Long userId, Long kitapId) {
        return kitapOduncVer(userId, kitapId, false);
    }

    @Transactional
    public Odunc kitapOduncVer(Long userId, Long kitapId, boolean bildirimOlustur) {
        // 1. Kullanıcıyı ve Kitabı getir (Yoksa hatayı patlat)
        Kullanici kullanici = kullaniciRepository.findById(userId)
                .orElseThrow(() -> new KutuphaneHatasi("Kullanıcı bulunamadı!"));

        Kitap kitap = kitapRepository.findById(kitapId)
                .orElseThrow(() -> new KutuphaneHatasi("Kitap bulunamadı!"));

        // 2. İş Kuralları (Limit ve Stok Kontrolü)
        int limit = kullanici.oduncAlmaLimitiHesapla();
        List<Odunc> aktifOduncler = oduncRepository.findByKullaniciIdAndTeslimTarihiIsNull(userId);

        if (aktifOduncler.size() >= limit) {
            throw new KutuphaneHatasi("Ödünç alma limitiniz dolmuş! Limit: " + limit);
        }

        if (!kitap.isMevcut()) {
            throw new KutuphaneHatasi("Bu kitap şu an başkasında, mevcut değil.");
        }

        // 3. Kitabı güncelle ve Ödünç kaydını oluştur
        kitap.setMevcut(false);
        kitapRepository.save(kitap);

        Odunc yeniOdunc = new Odunc(kitap, kullanici);
        yeniOdunc = oduncRepository.save(yeniOdunc);

        // 4. Lazy loading'i force et (transaction içinde) - Response serialization için
        // EAGER fetch kullandığımız için artık gerekli değil ama yine de emin olmak
        // için
        yeniOdunc.getKullanici().getAdSoyad();
        yeniOdunc.getKitap().getBaslik();

        // 5. Tüm değişiklikleri flush et (transaction commit edilmeden önce)
        // Bu, entity'lerin veritabanına yazılmasını sağlar ve lazy loading sorunlarını
        // önler
        entityManager.flush();

        // 7. Bildirim oluştur (eğer isteniyorsa - transaction dışında, hata olsa bile
        // devam et)
        if (bildirimOlustur) {
            try {
                String kullaniciAdi = kullanici.getAdSoyad();
                String kitapAdi = kitap.getBaslik();
                String mesaj = kullaniciAdi + " kullanıcısı '" + kitapAdi + "' kitabını ödünç aldı.";
                bildirimService.bildirimOlustur(userId, kullaniciAdi, mesaj);
            } catch (Exception e) {
                // Bildirim oluşturma hatası ana işlemi etkilemesin
                System.err.println("Bildirim oluşturma hatası: " + e.getMessage());
            }
        }

        return yeniOdunc;
    }

    @Transactional
    public Odunc kitapIadeAl(Long oduncId) {
        return kitapIadeAl(oduncId, false);
    }

    @Transactional
    public Odunc kitapIadeAl(Long oduncId, boolean bildirimOlustur) {
        // 1. Kaydı bul ve kontrol et
        Odunc oduncKaydi = oduncRepository.findById(oduncId)
                .orElseThrow(() -> new KutuphaneHatasi("Böyle bir ödünç kaydı yok!"));

        if (oduncKaydi.getTeslimTarihi() != null) {
            throw new KutuphaneHatasi("Bu kitap zaten kütüphaneye dönmüş.");
        }

        // 2. Bildirim için bilgileri al (transaction içinde)
        String kullaniciAdi = null;
        Long userId = null;
        String kitapAdi = null;
        if (bildirimOlustur) {
            Kullanici kullanici = oduncKaydi.getKullanici();
            Kitap kitap = oduncKaydi.getKitap();
            kullaniciAdi = kullanici.getAdSoyad();
            userId = kullanici.getId();
            kitapAdi = kitap.getBaslik();
        }

        // 3. Kitabı tekrar boşa çıkar
        Kitap kitap = oduncKaydi.getKitap();
        kitap.setMevcut(true);
        kitapRepository.save(kitap);

        // 4. Tarihi set et ve kaydet
        oduncKaydi.setTeslimTarihi(LocalDate.now());
        oduncKaydi = oduncRepository.save(oduncKaydi);

        // 5. Lazy loading'i force et (transaction içinde) - Response serialization için
        // EAGER fetch kullandığımız için artık gerekli değil ama yine de emin olmak
        // için
        oduncKaydi.getKullanici().getAdSoyad();
        oduncKaydi.getKitap().getBaslik();

        // 6. Tüm değişiklikleri flush et (transaction commit edilmeden önce)
        // Bu, entity'lerin veritabanına yazılmasını sağlar ve lazy loading sorunlarını
        // önler
        entityManager.flush();

        // 8. Bildirim oluştur (eğer isteniyorsa - transaction dışında, hata olsa bile
        // devam et)
        if (bildirimOlustur) {
            try {
                String mesaj = kullaniciAdi + " kullanıcısı '" + kitapAdi + "' kitabını iade etti.";
                bildirimService.bildirimOlustur(userId, kullaniciAdi, mesaj);
            } catch (Exception e) {
                // Bildirim oluşturma hatası ana işlemi etkilemesin
                System.err.println("Bildirim oluşturma hatası: " + e.getMessage());
            }
        }

        return oduncKaydi;
    }

    // Tüm aktif ödünç kayıtlarını getir
    public List<Odunc> tumAktifOduncleriGetir() {
        return oduncRepository.findAllAktifOduncler();
    }

    // Tüm ödünç kayıtlarını getir
    public List<Odunc> tumOduncleriGetir() {
        return oduncRepository.findAllOrderByOduncTarihiDesc();
    }

    // Kullanıcının aktif ödünç kayıtlarını getir
    public List<Odunc> kullaniciOduncleriGetir(Long userId) {
        return oduncRepository.findByKullaniciIdAndTeslimTarihiIsNull(userId);
    }

    // Ödünç kaydını ID ile getir (bildirim için kullanıcı ve kitap bilgilerine
    // erişim için)
    public Odunc oduncKaydiBul(Long oduncId) {
        return oduncRepository.findById(oduncId)
                .orElseThrow(() -> new KutuphaneHatasi("Böyle bir ödünç kaydı yok!"));
    }
}