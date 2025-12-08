package com.kutuphane.otomasyon.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// ABSTRACT (Soyut): Bu sınıfın kendi başına bir tablosu olmayacak, 
// alt sınıflar (Uye, Personel) bunu kullanacak.
@Entity // Bu sınıfın bir JPA varlığı (Entity) olduğunu belirtir.
// Kalıtım Stratejisi: SINGLE_TABLE (Tek Tablo) kullanılır. Tüm alt sınıfların
// verileri tek tabloda tutulur.
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "kullanicilar") // Tüm kullanıcı tiplerinin verilerinin tutulduğu ortak tablo
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Kullanici {

    @Id // Birincil anahtar (Primary Key)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID'nin DB tarafından otomatik artırılmasını sağlar.
    private Long id;

    @Column(nullable = false) // Veritabanı seviyesinde zorunlu (NOT NULL)
    @NotBlank(message = "Ad Soyad boş olamaz") // API validasyonu
    private String adSoyad;

    @Column(unique = true, nullable = false) // Benzersiz (UNIQUE) ve zorunlu (NOT NULL)
    @Email(message = "Geçerli bir email adresi girilmelidir") // Email format validasyonu
    @NotBlank(message = "Mail adresi boş olamaz")
    private String email;

    @Column(nullable = true) // Veritabanında boş (NULL) geçilebilir (Opsiyonel alan)
    private String telefon;

    // --- OOP ve İş Mantığı Alanları ---

    // Gerçek uygulamada burası @OneToMany ilişkisi ile Kitap tablosuna
    // bağlanmalıdır.
    // List<Kitap> oduncAlinanlar;

    // JPA/Hibernate'in veritabanından veri çekerken nesne oluşturması için gerekli
    // boş constructor.
    public Kullanici() {
    }

    /**
     * Alt sınıfların (Uye/Personel) üst sınıfın zorunlu alanlarını başlatması için
     * parametreli constructor.
     * Bu, constructor zincirini kurar ve zorunlu alanları set metotları üzerinden
     * (validasyonlu) atar.
     */
    public Kullanici(String adSoyad, String email) {
        // Setter metotları çağrılarak Kapsülleme (Encapsulation) sağlanır ve
        // validasyonlar tetiklenir.
        this.setAdSoyad(adSoyad);
        this.setEmail(email);
    }

    // --- Kalıtım ve Polimorfizm için Abstract Metot ---

    /**
     * Polimorfizm için zorunlu soyut metot.
     * Alt sınıflar kendi ödünç alma limit kurallarını uygulamak (Override etmek)
     * zorundadır.
     */
    public abstract int oduncAlmaLimitiHesapla();

    // --- Getter ve Setter Metotları (Kapsülleme) ---

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }
}