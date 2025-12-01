package com.kutuphane.otomasyon.repository;

import com.kutuphane.otomasyon.model.Uye;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UyeRepository extends JpaRepository<Uye, Long> {

    // Yine hiç kod yazmıyoruz. save(), findAll(), deleteById() metotları hazır
    // geldi!

}