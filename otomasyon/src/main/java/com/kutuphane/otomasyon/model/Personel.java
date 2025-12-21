package com.kutuphane.otomasyon.model;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("PERSONEL")
public class Personel extends Kullanici {

    @Column(unique = true)
    private String sicilNo;

    private String departman;

    // Boş constructor
    public Personel() {
        super();
    }

    // Bilgileri doldurmak için constructor
    public Personel(String adSoyad, String email, String sicilNo, String departman) {
        super(adSoyad, email);
        this.sicilNo = sicilNo;
        this.departman = departman;
    }

    // Personel için ödünç limiti (Override)
    @Override
    public int oduncAlmaLimitiHesapla() {
        return 5;
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