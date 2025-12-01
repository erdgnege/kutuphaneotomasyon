package com.kutuphane.otomasyon.service;

import java.util.Optional;
import java.util.List;
import com.kutuphane.otomasyon.model.Kitap;
import com.kutuphane.otomasyon.repository.KitapRepository;
import org.springframework.stereotype.Service;

// 👈 Bu etiketi koyarak Spring Boot'a bu sınıfın iş mantığını yönettiğini söylüyoruz.
@Service
public class KitapService {

    // Repository'yi buraya dahil ediyoruz (Bağımlılık Enjeksiyonu)
    private final KitapRepository kitapRepository;

    // 👈 KURUCU METOT: Spring Boot bu metodu gördüğünde, KitapRepository'yi
    // otomatik olarak oluşturur ve bize hazır verir.
    public KitapService(KitapRepository kitapRepository) {
        this.kitapRepository = kitapRepository;
    }

    // --- TEMEL İŞ MANTIKLARI ---

    // 1. Kitap Ekleme Metodu
    public Kitap kitapEkle(Kitap kitap) {
        // İstersen burada iş kuralı uygulayabilirsin. Örneğin:
        // if (kitap.getYazar() == null) throw new RuntimeException("Yazar boş
        // olamaz!");

        // Repository'nin hazır save() metodunu kullanarak veriyi kaydediyoruz!
        return kitapRepository.save(kitap);
    }

    // 2. Tüm Kitapları Listeleme Metodu
    public List<Kitap> tumKitaplariGetir() {
        return kitapRepository.findAll(); // Hazır findAll() metodunu kullandık.
    }

    // 3. Kitap Silme Metodu
    public void kitapSil(Long id) {
        kitapRepository.deleteById(id); // Hazır deleteById() metodunu kullandık.
    }

    public Optional<Kitap> kitapBulById(Long id) {
        return kitapRepository.findById(id);
    }

    // NOT: List sınıfını kullanabilmen için yukarıya 'import java.util.List;'
    // eklemen gerekebilir.
}