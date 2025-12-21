package com.kutuphane.otomasyon.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "kitaplar")
public class Kitap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Kitap adı boş olamaz")
    @Column(nullable = false)
    private String baslik;

    @NotBlank(message = "Yazar adı boş olamaz")
    @Column(nullable = false)
    private String yazar;

    @NotBlank(message = "ISBN alanı zorunludur")
    @Column(unique = true, nullable = false)
    private String isbn;

    @Column(name = "kapak_url", length = 500, nullable = true)
    private String kapakUrl; // Kitap kapağı URL'i (Google Books'tan)

    private boolean mevcut = true; // Kitap kütüphanede mi?

    // Boş constructor (JPA için gerekli)
    public Kitap() {
    }

    // Parametreli constructor (Kod yazarken işimizi kolaylaştırır aga)
    public Kitap(String baslik, String yazar, String isbn) {
        this.baslik = baslik;
        this.yazar = yazar;
        this.isbn = isbn;
    }

    // --- Getter ve Setterlar ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBaslik() {
        return baslik;
    }

    public void setBaslik(String baslik) {
        this.baslik = baslik;
    }

    public String getYazar() {
        return yazar;
    }

    public void setYazar(String yazar) {
        this.yazar = yazar;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public boolean isMevcut() {
        return mevcut;
    }

    public void setMevcut(boolean mevcut) {
        this.mevcut = mevcut;
    }

    public String getKapakUrl() {
        return kapakUrl;
    }

    public void setKapakUrl(String kapakUrl) {
        this.kapakUrl = kapakUrl;
    }
}