package com.kutuphane.otomasyon.repository;

import com.kutuphane.otomasyon.model.Kitap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface KitapRepository extends JpaRepository<Kitap, Long> {

    // ISBN üzerinden kitap sorgulama (Benzersiz alan olduğu için önemli kanka)
    Optional<Kitap> findByIsbn(String isbn);
}