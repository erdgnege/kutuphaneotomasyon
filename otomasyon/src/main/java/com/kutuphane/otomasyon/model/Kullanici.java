package com.kutuphane.otomasyon.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "kullanicilar")
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "dtype", visible = true, include = JsonTypeInfo.As.PROPERTY, defaultImpl = Uye.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Uye.class, name = "UYE"),
        @JsonSubTypes.Type(value = Personel.class, name = "PERSONEL")
})
// Kanka buraya UserDetails ekledik, Spring Security artık bu class'ın dilinden
// anlayacak
public abstract class Kullanici implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "İsim alanı boş bırakılamaz")
    @Column(name = "ad_soyad")
    private String adSoyad;

    @Email(message = "Geçerli bir e-posta giriniz")
    @NotBlank(message = "E-posta zorunludur")
    @Column(unique = true)
    private String email;

    // Kanka burası çok kritik, şifre alanı ekledik!
    @NotBlank(message = "Şifre zorunludur")
    private String sifre;

    @Column(unique = true, length = 11, nullable = true)
    private String telefon;

    @CreationTimestamp
    @Column(name = "kayit_tarihi", nullable = true, updatable = false)
    private LocalDateTime kayitTarihi;

    // --- Spring Security İçin Gerekli Metodlar ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Kanka burada dtype'a göre (UYE veya PERSONEL) yetki veriyoruz.
        // Personel ise ADMIN, Uye ise USER rolü verelim mesela.
        String role = this.getClass().getSimpleName().toUpperCase();
        // Eğer Personel ise "ROLE_PERSONEL" döner. SecurityConfig'de
        // hasRole("PERSONEL") diyebilirsin.
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return sifre; // Spring Security şifreyi buradan alacak
    }

    @Override
    public String getUsername() {
        return email; // Biz giriş yaparken email kullanacağız kanka
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // --- Standart Constructor, Getter ve Setterlar ---

    public Kullanici() {
    }

    public Kullanici(String adSoyad, String email, String sifre) {
        this.adSoyad = adSoyad;
        this.email = email;
        this.sifre = sifre;
    }

    public abstract int oduncAlmaLimitiHesapla();

    // Getter & Setter
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

    public String getSifre() {
        return sifre;
    }

    public void setSifre(String sifre) {
        this.sifre = sifre;
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