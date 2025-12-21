package com.kutuphane.otomasyon.repository;

import com.kutuphane.otomasyon.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface KullaniciRepository extends JpaRepository<Kullanici, Long> {

    // E-posta adresi üzerinden kullanıcı sorgulama
    Optional<Kullanici> findByEmail(String email);

    // Telefon numarası üzerinden kullanıcı sorgulama
    Optional<Kullanici> findByTelefon(String telefon);

    // Üye numarası üzerinden üye sorgulama
    @Query("SELECT u FROM Uye u WHERE u.uyeNo = :uyeNo")
    Optional<Uye> findByUyeNo(String uyeNo);

    // Kalıtım yapısını kullanarak sadece Üyeleri getiriyoruz
    @Query("SELECT u FROM Uye u")
    List<Uye> findAllUyeler();

    // Kalıtım yapısını kullanarak sadece Personelleri getiriyoruz
    @Query("SELECT p FROM Personel p")
    List<Personel> findAllPersoneller();
}