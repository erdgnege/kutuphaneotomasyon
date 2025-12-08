package com.kutuphane.otomasyon.repository;

import com.kutuphane.otomasyon.model.Kitap;
import org.springframework.data.jpa.repository.JpaRepository; // JPA veri erişimi için temel arayüz
import org.springframework.stereotype.Repository; // Bu arayüzün bir Repository katmanı bileşeni olduğunu belirtir

import java.util.Optional; // Nesnenin var olup olmadığını güvenle kontrol etmek için

// Kitap Entity'si için Repository. ID tipi Long.
@Repository
public interface KitapRepository extends JpaRepository<Kitap, Long> {

    // JpaRepository'den temel CRUD (Create, Read, Update, Delete) metotları miras
    // alınır.

    /**
     * Spring Data JPA'nın otomatik üreteceği sorgu metodu.
     * Veritabanında ISBN alanına göre Kitap aramayı sağlar.
     * 
     * @param isbn Aranacak kitabın ISBN numarası.
     * @return Eğer kitap bulunursa Optional içinde Kitap, bulunamazsa boş Optional
     *         döner.
     */
    Optional<Kitap> findByIsbn(String isbn);
}