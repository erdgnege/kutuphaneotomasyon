package com.kutuphane.otomasyon.repository;

import com.kutuphane.otomasyon.model.IslemLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IslemLogRepository extends JpaRepository<IslemLog, Long> {
    
    // Tüm işlem loglarını tarihe göre sıralı getir
    @Query("SELECT il FROM IslemLog il ORDER BY il.islemTarihi DESC")
    List<IslemLog> findAllOrderByIslemTarihiDesc();
}

