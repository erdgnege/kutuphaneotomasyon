package com.kutuphane.otomasyon.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "uyeler") // Tablo adını 'uyeler' olarak belirledik
public class Uye {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String adSoyad;
    private String uyeNo; // Üye Numarası (Benzersiz olmasını isteyebiliriz)
    private String email;

    // Boş kurucu metot (JPA için gerekli)
    public Uye() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAdSoyad() {
        return adSoyad;
    }

    public void setAdSoyad(String adSoyad) {
        this.adSoyad = adSoyad;
    }

    public String getUyeNo() {
        return uyeNo;
    }

    public void setUyeNo(String uyeNo) {
        this.uyeNo = uyeNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}