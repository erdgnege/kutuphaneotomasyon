package com.kutuphane.otomasyon.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "kitaplar")
public class Kitap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String baslik;
    private String yazar;
    private String isbn;
    private boolean mevcut; // True: Rafta, False: Ödünç Verilmiş

    public Kitap() {
    }

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

    public boolean isMevcut() { // Boolean için genellikle 'is' ile başlar
        return mevcut;
    }

    public void setMevcut(boolean mevcut) {
        this.mevcut = mevcut;
    }
}