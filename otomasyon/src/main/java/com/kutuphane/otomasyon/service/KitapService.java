package com.kutuphane.otomasyon.service;

import com.kutuphane.otomasyon.model.Kitap;
import com.kutuphane.otomasyon.repository.KitapRepository;
import com.kutuphane.otomasyon.exception.KutuphaneHatasi;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.List;

@Service
public class KitapService {

    private final KitapRepository kitapRepository;

    public KitapService(KitapRepository kitapRepository) {
        this.kitapRepository = kitapRepository;
    }

    // Yeni kitap ekle
    public Kitap kitapEkle(Kitap kitap) {
        return kitapRepository.save(kitap);
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
    public void kitapSil(Long id) {
        // Silmeden önce var mı diye kontrol edelim ki patlamasın kanka
        if (!kitapRepository.existsById(id)) {
            throw new KutuphaneHatasi("Silinmek istenen kitap zaten yok!");
        }
        try {
            kitapRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new KutuphaneHatasi(
                    "Bu kitap ödünç verilmiş veya işlem görmüş olduğu için silinemez! Önce ilgili kayıtları kaldırmalısınız.");
        }
    }
}