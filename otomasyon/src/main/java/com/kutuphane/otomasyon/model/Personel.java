package com.kutuphane.otomasyon.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@DiscriminatorValue("PERSONEL")
public class Personel extends Kullanici {

    @Column(unique = true)
    private String sicilNo;

    private String departman;

    // Boş constructor (JPA için gerekli)
    public Personel() {
        super();
    }

    // Parametreli constructor (şifre super class'a gönderilir)
    public Personel(String adSoyad, String email, String sifre, String sicilNo, String departman) {
        super(adSoyad, email, sifre);
        this.sicilNo = sicilNo;
        this.departman = departman;
    }

    // Personel için ödünç limiti
    @Override
    public int oduncAlmaLimitiHesapla() {
        return 10; // Personel ödünç alma limiti
    }

}