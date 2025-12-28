package com.kutuphane.otomasyon.repository;

import com.kutuphane.otomasyon.model.Kullanici;
import com.kutuphane.otomasyon.model.Personel;
import com.kutuphane.otomasyon.model.Uye;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KullaniciRepository extends JpaRepository<Kullanici, Long> {
    Optional<Kullanici> findByEmail(String email);

    Optional<Kullanici> findByTelefon(String telefon);

    @Query("SELECT u FROM Uye u WHERE u.uyeNo = :uyeNo")
    Optional<Uye> findByUyeNo(String uyeNo);

    @Query("SELECT u FROM Uye u")
    List<Uye> findAllUyeler();

    @Query("SELECT p FROM Personel p")
    List<Personel> findAllPersoneller();
}