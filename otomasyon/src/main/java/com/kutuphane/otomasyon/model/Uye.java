package com.kutuphane.otomasyon.model;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("UYE")
public class Uye extends Kullanici {

    @Column(unique = true, length = 6, nullable = false)
    private String uyeNo;

    // Boş constructor
    public Uye() {
        super();
    }

    // Bilgileri hızlıca girmek için constructor
    public Uye(String adSoyad, String email, String uyeNo) {
        super(adSoyad, email);
        this.uyeNo = uyeNo;
    }

    // Üye için ödünç limiti (Polimorfizm)
    @Override
    public int oduncAlmaLimitiHesapla() {
        return 3;
    }

    // --- Getter ve Setter ---

    public String getUyeNo() {
        return uyeNo;
    }

    public void setUyeNo(String uyeNo) {
        this.uyeNo = uyeNo;
    }
}