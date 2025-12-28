package com.kutuphane.otomasyon.repository;

import java.util.List;
import com.kutuphane.otomasyon.model.Odunc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OduncRepository extends JpaRepository<Odunc, Long> {

    // Kullanıcının henüz iade etmediği aktif ödünç kayıtlarını bulur
    @Query("SELECT o FROM Odunc o WHERE o.kullanici.id = :kullaniciId AND o.teslimTarihi IS NULL")
    List<Odunc> findByKullaniciIdAndTeslimTarihiIsNull(@Param("kullaniciId") Long kullaniciId);
    
    // Tüm aktif ödünç kayıtlarını getirir (iade edilmemiş)
    @Query("SELECT o FROM Odunc o WHERE o.teslimTarihi IS NULL ORDER BY o.oduncTarihi DESC")
    List<Odunc> findAllAktifOduncler();
    
    // Tüm ödünç kayıtlarını getirir
    @Query("SELECT o FROM Odunc o ORDER BY o.oduncTarihi DESC")
    List<Odunc> findAllOrderByOduncTarihiDesc();
    
    // Kitap ID'sine göre ödünç kayıtlarını bulur
    @Query("SELECT o FROM Odunc o WHERE o.kitap.id = :kitapId")
    List<Odunc> findByKitapId(@Param("kitapId") Long kitapId);
    
    // Kitap ID'sine göre ödünç kayıtlarını siler
    @Modifying
    @Query("DELETE FROM Odunc o WHERE o.kitap.id = :kitapId")
    void deleteByKitapId(@Param("kitapId") Long kitapId);
}