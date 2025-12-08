package com.kutuphane.otomasyon.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

// Kullanici sınıfından Kalıtım (Inheritance) alır.
@Entity // Bu sınıfın bir JPA varlığı (Entity) olduğunu belirtir.
// SINGLE_TABLE kalıtım stratejisinde, bu değer veritabanındaki kayıt tipini
// ayırt etmek için kullanılır.
@DiscriminatorValue("UYE")
public class Uye extends Kullanici {

    @Column(unique = true, nullable = true) // Benzersiz (UNIQUE) olabilir, ancak Personel kayıtları için NULL
                                            // geçilebilir.
    private String uyeNo; // Üye için özgün bir numara (Opsiyonel)

    // JPA/Hibernate'in veri çekerken nesne oluşturması için gerekli boş
    // constructor.
    public Uye() {
        super();
    }

    /**
     * Alanları ayarlayan parametreli constructor.
     * 
     * @param uyeNo Uye sınıfına özgü alan.
     */
    public Uye(String adSoyad, String email, String uyeNo) {
        // Üst sınıfın zorunlu alanlarını (adSoyad, email) atamak için super() çağrısı
        // yapılır.
        super(adSoyad, email);

        // DİKKAT: Üst sınıftan gelen alanlara (adSoyad, email) tekrar setter çağrılması
        // genellikle gereksizdir
        // ve üst sınıfın constructor'ının işini tekrarlar. Sadece super() yeterlidir.

        // Kendi (alt sınıf) alanına atama.
        this.uyeNo = uyeNo;
    }

    // --- Polimorfizm Uygulaması ---

    /**
     * Polimorfizm için zorunlu metot.
     * Üst sınıftaki abstract metodu override ederek Üyeye özgü limiti tanımlar.
     */
    @Override
    public int oduncAlmaLimitiHesapla() {
        return 3; // Üyelerin limiti
    }

    // --- Getter ve Setter Metotları (Kapsülleme) ---

    public String getUyeNo() {
        return uyeNo;
    }

    public void setUyeNo(String uyeNo) {
        this.uyeNo = uyeNo;
    }
}