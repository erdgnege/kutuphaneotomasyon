package com.kutuphane.otomasyon.service;

import java.util.Optional;
import java.util.List;
import com.kutuphane.otomasyon.model.Kitap;
import com.kutuphane.otomasyon.repository.KitapRepository; // Veri erişim katmanı
import org.springframework.stereotype.Service; // Bu sınıfın bir servis bileşeni olduğunu belirtir

/**
 * Kitap varlığı ile ilgili iş mantığı operasyonlarını yürüten servis sınıfı.
 * Controller'dan gelen istekleri Repository'ye iletir.
 */
@Service
public class KitapService {

    private final KitapRepository kitapRepository; // Repository bağımlılığı

    /**
     * Gerekli repository'yi enjekte etmek için kullanılan kurucu metot (Constructor
     * Injection).
     */
    public KitapService(KitapRepository kitapRepository) {
        this.kitapRepository = kitapRepository;
    }

    /**
     * Yeni bir kitabı veritabanına kaydeder (C - Create).
     * 
     * @param kitap Kaydedilecek Kitap nesnesi.
     * @return Kaydedilen Kitap nesnesi.
     */
    public Kitap kitapEkle(Kitap kitap) {
        // İş kuralı (business logic) gerekirse buraya yazılır, yoksa doğrudan
        // Repository çağrılır.
        return kitapRepository.save(kitap);
    }

    /**
     * Veritabanındaki tüm kitapları listeler (R - Read).
     * 
     * @return Kitap nesnelerinden oluşan bir liste.
     */
    public List<Kitap> tumKitaplariGetir() {
        return kitapRepository.findAll();
    }

    /**
     * Belirtilen ID'ye sahip kitabı veritabanından siler (D - Delete).
     * 
     * @param id Silinecek kitabın ID'si.
     */
    public void kitapSil(Long id) {
        kitapRepository.deleteById(id);
    }

    /**
     * Belirtilen ID'ye sahip kitabı bulur (R - Read).
     * 
     * @return Kitap bulunursa Optional içinde döner, bulunamazsa boş Optional
     *         döner.
     */
    public Optional<Kitap> kitapBulById(Long id) {
        return kitapRepository.findById(id);
    }
}