package com.kutuphane.otomasyon.model;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("PERSONEL")
public class Personel extends Kullanici {

    @Column(unique = true)
    private String sicilNo;

    private String departman;

    // Boş constructor kanka, JPA için şart
    public Personel() {
        super();
    }

    // Kanka buraya 'sifre' parametresini ekledik ve super'e gönderdik
    public Personel(String adSoyad, String email, String sifre, String sicilNo, String departman) {
        super(adSoyad, email, sifre);
        this.sicilNo = sicilNo;
        this.departman = departman;
    }

    // Personel için ödünç limiti
    @Override
    public int oduncAlmaLimitiHesapla() {
        return 10; // Kanka istersen personelin limitini biraz artırabilirsin :)
    }

    // --- Getter ve Setterlar ---

    public String getSicilNo() {
        return sicilNo;
    }

    public void setSicilNo(String sicilNo) {
        this.sicilNo = sicilNo;
    }

    public String getDepartman() {
        return departman;
    }

    public void setDepartman(String departman) {
        this.departman = departman;
    }
}