package com.kutuphane.otomasyon.service;

import com.kutuphane.otomasyon.model.Kitap;
import com.kutuphane.otomasyon.model.Odunc;
import com.kutuphane.otomasyon.model.Uye;
import com.kutuphane.otomasyon.repository.OduncRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class OduncService {

    private final OduncRepository oduncRepository;
    private final KitapService kitapService; // Kitap durumunu güncellemek için gerekli
    private final UyeService uyeService; // Üye kontrolü için gerekli

    // Tüm Service ve Repository'leri enjekte ediyoruz
    public OduncService(OduncRepository oduncRepository, KitapService kitapService, UyeService uyeService) {
        this.oduncRepository = oduncRepository;
        this.kitapService = kitapService;
        this.uyeService = uyeService;
    }

    // --- ÖDÜNÇ VERME İŞ MANTIĞI ---
    public Odunc kitapOduncVer(Long kitapId, Long uyeId) {
        // 1. Kitap ve Üye Nesnelerini Bul
        Optional<Kitap> kitapOpt = kitapService.kitapBulById(kitapId);
        Optional<Uye> uyeOpt = uyeService.uyeBulById(uyeId);

        if (!kitapOpt.isPresent() || !uyeOpt.isPresent()) {
            throw new RuntimeException("Kitap veya Üye bulunamadı!");
        }

        Kitap kitap = kitapOpt.get();
        Uye uye = uyeOpt.get();

        // 2. İş Kuralı Kontrolü (Kitap müsait mi?)
        if (!kitap.isMevcut()) {
            throw new RuntimeException("Bu kitap şu anda ödünç verilmiş durumdadır.");
        }

        // 3. Kitabın Durumunu Güncelle (mevcut: false)
        kitap.setMevcut(false);
        kitapService.kitapEkle(kitap); // Kitap bilgisini veritabanında günceller

        // 4. Yeni Ödünç Kaydını Oluştur
        Odunc yeniOdunc = new Odunc();
        yeniOdunc.setKitap(kitap);
        yeniOdunc.setUye(uye);
        // Teslim tarihi 14 gün sonrası olsun:
        yeniOdunc.setTeslimTarihi(LocalDate.now().plusDays(14));

        return oduncRepository.save(yeniOdunc);
    }

    // --- Kalan metotları daha sonra eklersin: iadeAl(), tumOduncleriGetir() ---
}