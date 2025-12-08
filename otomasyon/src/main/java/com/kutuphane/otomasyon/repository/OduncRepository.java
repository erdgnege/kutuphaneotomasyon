package com.kutuphane.otomasyon.repository;

import java.util.List;
import com.kutuphane.otomasyon.model.Odunc;
import org.springframework.data.jpa.repository.JpaRepository; // JPA veri erişimi için temel arayüz
import org.springframework.stereotype.Repository; // Bu arayüzün bir Repository katmanı bileşeni olduğunu belirtir

@Repository
public interface OduncRepository extends JpaRepository<Odunc, Long> {

    // JpaRepository'den temel CRUD (Create, Read, Update, Delete) metotları miras
    // alınır.

    /**
     * Spring Data JPA'nın otomatik üreteceği sorgu metodu (Query Method).
     * Belirli bir kullanıcının (kullaniciId) henüz teslim etmediği (TeslimTarihi
     * NULL) tüm kayıtları getirir.
     * Bu, kullanıcının o an kaç kitap ödünç aldığını bulmak için kullanılır.
     * 
     * @param kullaniciId Kitapları ödünç alan kullanıcının ID'si.
     * @return Henüz iade edilmemiş ödünç kayıtlarının listesi.
     */
    List<Odunc> findByKullaniciIdAndTeslimTarihiIsNull(Long kullaniciId);

    // Odunc sınıfı için CRUD metotları hazır!
}