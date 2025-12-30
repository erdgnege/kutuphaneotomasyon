package com.kutuphane.otomasyon.repository;

import com.kutuphane.otomasyon.model.Kitap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface KitapRepository extends JpaRepository<Kitap, Long> {

    // ISBN ile kitap sorgulama (Benzersiz alan)
    Optional<Kitap> findByIsbn(String isbn);
}