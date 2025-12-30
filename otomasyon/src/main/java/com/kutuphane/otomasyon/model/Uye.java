package com.kutuphane.otomasyon.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@DiscriminatorValue("UYE")
public class Uye extends Kullanici {

    @Column(unique = true, length = 6, nullable = false)
    private String uyeNo;

    // Boş constructor (JPA için gerekli)
    public Uye() {
        super();
    }

    // Parametreli constructor (kayıt işlemi için)
    public Uye(String adSoyad, String email, String sifre, String uyeNo) {
        super(adSoyad, email, sifre);
        this.uyeNo = uyeNo;
    }

    @Override
    public int oduncAlmaLimitiHesapla() {
        return 3;
    }
}