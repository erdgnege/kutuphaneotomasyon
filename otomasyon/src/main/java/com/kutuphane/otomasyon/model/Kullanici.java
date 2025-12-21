package com.kutuphane.otomasyon.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // Üye ve Personel aynı tabloda tutulsun diye
@Table(name = "kullanicilar")
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "dtype", visible = true, include = JsonTypeInfo.As.PROPERTY, defaultImpl = Uye.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Uye.class, name = "UYE"),
        @JsonSubTypes.Type(value = Personel.class, name = "PERSONEL")
})
public abstract class Kullanici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "İsim alanı boş bırakılamaz")
    @Column(name = "ad_soyad") // Veritabanındaki gerçek sütun adını buraya yazıyoruz
    private String adSoyad;

    @Email(message = "Geçerli bir e-posta giriniz")
    @NotBlank(message = "E-posta zorunludur")
    @Column(unique = true)
    private String email;

    @Column(unique = true, length = 11, nullable = true)
    private String telefon;

    @CreationTimestamp
    @Column(name = "kayit_tarihi", nullable = true, updatable = false)
    private LocalDateTime kayitTarihi;

    // Boş constructor
    public Kullanici() {
    }

    // Bilgileri hızlıca atamak için constructor
    public Kullanici(String adSoyad, String email) {
        this.adSoyad = adSoyad;
        this.email = email;
    }

    // Üye ve Personel tipleri için limit kuralı (Alt sınıflar dolduracak)
    public abstract int oduncAlmaLimitiHesapla();

    // --- Getter ve Setterlar ---

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

    public LocalDateTime getKayitTarihi() {
        return kayitTarihi;
    }

    public void setKayitTarihi(LocalDateTime kayitTarihi) {
        this.kayitTarihi = kayitTarihi;
    }
}