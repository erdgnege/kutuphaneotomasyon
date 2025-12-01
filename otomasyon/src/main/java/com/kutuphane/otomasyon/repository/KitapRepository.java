package com.kutuphane.otomasyon.repository; // Kendi paket adını kontrol et

import com.kutuphane.otomasyon.model.Kitap; // Az önce oluşturduğun Kitap sınıfını içeri aktar
import org.springframework.data.jpa.repository.JpaRepository;

// 👈 Önemli: Bu bir ARAYÜZ (interface)'dir, SINIF (class) DEĞİL!

// JpaRepository'den miras alarak hazır metotları elde ederiz.
// İki parametre alır:
// 1. Kitap: Hangi sınıf için Repository oluşturulduğu (Hangi tablo?)
// 2. Long: Kitap sınıfının birincil anahtarının (id) veri tipi
public interface KitapRepository extends JpaRepository<Kitap, Long> {

    // Buraya hiçbir metot yazmıyoruz!
    // save(), findAll(), findById() gibi metotlar JpaRepository sayesinde otomatik
    // olarak geldi bile.

}