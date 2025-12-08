package com.kutuphane.otomasyon.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

// Kullanici sınıfından Kalıtım (Inheritance) alır.
@Entity // Bu sınıfın bir JPA varlığı (Entity) olduğunu belirtir.
// SINGLE_TABLE kalıtım stratejisinde, bu değer veritabanındaki kayıt tipini
// ayırt etmek için kullanılır.
@DiscriminatorValue("PERSONEL")
public class Personel extends Kullanici {

    @Column(unique = true, nullable = true) // Benzersiz (UNIQUE) olabilir, ancak Uye kayıtları için NULL geçilebilir.
    private String sicilNo; // Personel için özgün bir numara (Opsiyonel)

    @Column(nullable = true) // Uye kayıtları için NULL geçilebilir.
    private String departman; // Hangi departmanda çalıştığı (Opsiyonel)

    // JPA/Hibernate'in veri çekerken nesne oluşturması için gerekli boş
    // constructor.
    public Personel() {
        super();
    }

    /**
     * Alanları ayarlayan parametreli constructor.
     * Bu constructor'da 'super()' çağrısı ile üst sınıfın zorunlu alanları
     * başlatılır.
     */
    public Personel(String adSoyad, String email, String sicilNo, String departman) {
        // Üst sınıfın constructor'ını (Kullanici(String, String)) çağırır.
        super(adSoyad, email);

        // DİKKAT: Üst sınıftan gelen alanlar için (adSoyad, email) setter çağırmak
        // yerine,
        // super() ile halletmek daha iyi bir JPA/OOP pratiğidir.

        // Kendi (alt sınıf) alanlarını setter üzerinden ayarlar.
        this.sicilNo = sicilNo;
        this.departman = departman;
    }

    // --- Polimorfizm Uygulaması ---

    /**
     * Polimorfizm için zorunlu metot.
     * Üst sınıftaki abstract metodu override ederek Personele özgü limiti tanımlar.
     */
    @Override
    public int oduncAlmaLimitiHesapla() {
        return 5; // Personelin limiti
    }

    // --- Personel Yetkisine Özgü Metotlar (İş Mantığı) ---

    public void sistemGuncellemesiYap() {
        System.out.println("Personel yetkisiyle sistem ayarları güncelleniyor.");
    }

    // --- Getter ve Setter Metotları (Kapsülleme) ---

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