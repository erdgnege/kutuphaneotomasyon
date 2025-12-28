package com.kutuphane.otomasyon.model;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("UYE")
public class Uye extends Kullanici {

    @Column(unique = true, length = 6, nullable = false)
    private String uyeNo;

    // Boş constructor kanka, JPA'nın içi rahat etsin
    public Uye() {
        super();
    }

    // Kanka buraya da 'sifre' ekledik, kayıt olurken lazım olacak
    public Uye(String adSoyad, String email, String sifre, String uyeNo) {
        super(adSoyad, email, sifre);
        this.uyeNo = uyeNo;
    }

    @Override
    public int oduncAlmaLimitiHesapla() {
        return 3;
    }

    public String getUyeNo() {
        return uyeNo;
    }

    public void setUyeNo(String uyeNo) {
        this.uyeNo = uyeNo;
    }
}