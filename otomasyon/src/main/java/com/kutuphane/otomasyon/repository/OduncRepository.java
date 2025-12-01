package com.kutuphane.otomasyon.repository;

import com.kutuphane.otomasyon.model.Odunc;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OduncRepository extends JpaRepository<Odunc, Long> {
    // Odunc sınıfı için CRUD metotları hazır!
}