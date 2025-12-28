package com.kutuphane.otomasyon.service;

import com.kutuphane.otomasyon.model.Kitap;
import com.kutuphane.otomasyon.repository.KitapRepository;
import com.kutuphane.otomasyon.repository.OduncRepository;
import com.kutuphane.otomasyon.exception.KutuphaneHatasi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.List;

@Service
public class KitapService {

    private final KitapRepository kitapRepository;
    private final OduncRepository oduncRepository;
    private final IslemLogService islemLogService;

    public KitapService(KitapRepository kitapRepository, OduncRepository oduncRepository, IslemLogService islemLogService) {
        this.kitapRepository = kitapRepository;
        this.oduncRepository = oduncRepository;
        this.islemLogService = islemLogService;
    }

    // Yeni kitap ekle
    @Transactional
    public Kitap kitapEkle(Kitap kitap) {
        Kitap kaydedilenKitap = kitapRepository.save(kitap);
        // İşlem logu kaydet
        islemLogService.kitapEklemeLoguKaydet(kaydedilenKitap);
        return kaydedilenKitap;
    }

    // Tüm kitapları getir
    public List<Kitap> tumKitaplariGetir() {
        return kitapRepository.findAll();
    }

    // Kitabı ID ile bul (Hata fırlatmalı - Modern öğrenci işi)
    public Kitap kitapBulById(Long id) {
        return kitapRepository.findById(id)
                .orElseThrow(() -> new KutuphaneHatasi("Kitap bulunamadı! ID: " + id));
    }

    // Kitabı sil
    @Transactional
    public void kitapSil(Long id) {
        // Silmeden önce var mı diye kontrol edelim ki patlamasın kanka
        if (!kitapRepository.existsById(id)) {
            throw new KutuphaneHatasi("Silinmek istenen kitap zaten yok!");
        }
        
        // Kitap bilgilerini log için al (silmeden önce)
        Kitap silinecekKitap = kitapRepository.findById(id)
                .orElseThrow(() -> new KutuphaneHatasi("Kitap bulunamadı! ID: " + id));
        
        // Kitap ile ilişkili tüm ödünç kayıtlarını sil
        oduncRepository.deleteByKitapId(id);
        
        // Şimdi kitabı güvenle silebiliriz
        try {
            kitapRepository.deleteById(id);
            // İşlem logu kaydet (kitap silindi, bilgileri log'da saklıyoruz)
            islemLogService.kitapSilmeLoguKaydet(silinecekKitap);
        } catch (DataIntegrityViolationException e) {
            throw new KutuphaneHatasi(
                    "Kitap silinirken bir hata oluştu: " + e.getMessage());
        }
    }
}