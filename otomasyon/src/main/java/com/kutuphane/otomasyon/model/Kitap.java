package com.kutuphane.otomasyon.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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

    // Parametreli constructor
    public Kitap(String baslik, String yazar, String isbn) {
        this.baslik = baslik;
        this.yazar = yazar;
        this.isbn = isbn;
    }

}