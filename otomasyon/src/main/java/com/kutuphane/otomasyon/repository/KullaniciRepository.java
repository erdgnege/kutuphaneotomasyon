package com.kutuphane.otomasyon.repository;

import com.kutuphane.otomasyon.model.Kullanici; // Temel soyut sınıf
import com.kutuphane.otomasyon.model.Uye; // Alt sınıf
import com.kutuphane.otomasyon.model.Personel; // Alt sınıf
import org.springframework.data.jpa.repository.JpaRepository; // JPA veri erişimi için temel arayüz
import org.springframework.data.jpa.repository.Query; // Özel HQL/JPQL sorguları için
import org.springframework.stereotype.Repository; // Bu arayüzün bir Repository katmanı bileşeni olduğunu belirtir

import java.util.List;
import java.util.Optional;

/**
 * User (ve alt sınıfları Uye, Personel) varlığı için veritabanı işlemlerini
 * yürüten Spring Data JPA repository arayüzü.
 * JpaRepository<Kullanici, Long> ile tüm kullanıcı tiplerini yönetir.
 */
@Repository
public interface KullaniciRepository extends JpaRepository<Kullanici, Long> {

    /**
     * Verilen email adresine sahip kullanıcıyı bulur.
     * Spring Data JPA, metot adına göre sorgu üretir.
     * 
     * @param email Aranacak kullanıcının email adresi.
     * @return Bulunan kullanıcıyı içeren bir Optional. Kullanıcı, Uye veya Personel
     *         olabilir (Polimorfik).
     */
    Optional<Kullanici> findByEmail(String email);

    /**
     * Veritabanındaki sadece Uye tipindeki tüm kullanıcıları getirir.
     * 
     * @Query ile yazılan JPQL sorgusu, Kalıtım (Inheritance) yapısını kullanarak
     *        sadece Uye'leri filtreler.
     * @return Uye nesnelerinden oluşan bir liste.
     */
    @Query("SELECT u FROM Uye u")
    List<Uye> findAllUyeler();

    /**
     * Veritabanındaki sadece Personel tipindeki tüm kullanıcıları getirir.
     * 
     * @Query ile yazılan JPQL sorgusu, Kalıtım (Inheritance) yapısını kullanarak
     *        sadece Personel'leri filtreler.
     * @return Personel nesnelerinden oluşan bir liste.
     */
    @Query("SELECT p FROM Personel p")
    List<Personel> findAllPersoneller();
}