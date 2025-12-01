package com.kutuphane.otomasyon.service;

import com.kutuphane.otomasyon.model.Uye;
import com.kutuphane.otomasyon.repository.UyeRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional; // findById'dan gelen sonucu yakalamak için gerekli

@Service
public class UyeService {

    private final UyeRepository uyeRepository;

    // Bağımlılık Enjeksiyonu
    public UyeService(UyeRepository uyeRepository) {
        this.uyeRepository = uyeRepository;
    }

    // --- TEMEL İŞ MANTIKLARI ---

    // 1. Yeni Üye Ekleme
    public Uye uyeEkle(Uye uye) {
        return uyeRepository.save(uye);
    }

    // 2. ID ile Üye Bulma
    public Optional<Uye> uyeBulById(Long id) {
        return uyeRepository.findById(id);
    }

    // 3. Tüm Üyeleri Getirme
    public List<Uye> tumUyeleriGetir() {
        return uyeRepository.findAll();
    }

    // 4. Üye Silme
    public void uyeSil(Long id) {
        uyeRepository.deleteById(id);
    }
}