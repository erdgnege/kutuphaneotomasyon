package com.kutuphane.otomasyon.service;

import com.kutuphane.otomasyon.model.Personel;
import com.kutuphane.otomasyon.model.Kullanici; // Temel soyut sınıf
import com.kutuphane.otomasyon.model.Uye; // Alt sınıf
import com.kutuphane.otomasyon.repository.KullaniciRepository; // Veri erişim katmanı
import org.springframework.stereotype.Service; // Bu sınıfın bir servis bileşeni olduğunu belirtir
import java.util.List;
import java.util.Optional;

@Service
public class KullaniciService {

    private final KullaniciRepository kullaniciRepository; // Repository bağımlılığı

    /**
     * Repository'yi enjekte etmek için kullanılan kurucu metot (Constructor
     * Injection).
     */
    public KullaniciService(KullaniciRepository kullaniciRepository) {
        this.kullaniciRepository = kullaniciRepository;
    }

    // --- TEMEL İŞ MANTIKLARI ---

    /**
     * 1. Yeni Kullanıcı Ekleme veya Mevcut Kullanıcıyı Güncelleme.
     * Generics (<T extends Kullanici>) ve Polimorfizm sayesinde hem Uye hem de
     * Personel kaydedilebilir.
     */
    public <T extends Kullanici> T kullaniciEkle(T kullanici) {
        // İş kuralı (Örn: Emailin daha önce kaydedilip kaydedilmediği) gerekirse buraya
        // yazılır.
        return kullaniciRepository.save(kullanici);
    }

    /**
     * 2. ID ile Kullanıcı Bulma.
     * 
     * @return Bulunan kullanıcıyı (Uye veya Personel olabilir) içeren Optional.
     */
    public Optional<Kullanici> kullaniciBulById(Long id) {
        return kullaniciRepository.findById(id);
    }

    /**
     * Veritabanındaki tüm kullanıcıları (Uye ve Personel dahil) getirir.
     * 
     * @return Kullanici tipinde Polimorfik bir liste.
     */
    public List<Kullanici> tumKullanicilariGetir() {
        return kullaniciRepository.findAll();
    }

    /**
     * 3. Tüm Üyeleri Getirme.
     * Repository'deki özel JPQL sorgusu (@Query) kullanılarak sadece Uye tipleri
     * filtrelenir.
     */
    public List<Uye> tumUyeleriGetir() {
        return kullaniciRepository.findAllUyeler();
    }

    /**
     * Tüm personelleri Getirme.
     * Repository'deki özel JPQL sorgusu kullanılarak sadece Personel tipleri
     * filtrelenir.
     */
    public List<Personel> tumPersonelleriGetir() {
        return kullaniciRepository.findAllPersoneller();
    }

    /**
     * 4. Kullanıcıyı ID ile silme.
     */
    public void kullaniciSil(Long id) {
        kullaniciRepository.deleteById(id);
    }
}