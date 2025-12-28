package com.kutuphane.otomasyon.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "islem_loglari")
public class IslemLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IslemTipi islemTipi; // KITAP_EKLE, KITAP_SIL

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "kitap_id")
    private Kitap kitap; // Kitap bilgisi (silme durumunda null olabilir ama log'da bilgi tutuyoruz)

    @Column(nullable = false, length = 200)
    private String kitapBaslik; // Kitap başlığı (silme durumunda bile saklanması için)

    @Column(nullable = false, length = 100)
    private String kitapIsbn; // Kitap ISBN (silme durumunda bile saklanması için)

    @Column(nullable = false, length = 100)
    private String kitapYazar; // Kitap yazarı (silme durumunda bile saklanması için)

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime islemTarihi;

    // Boş constructor
    public IslemLog() {
    }

    // Constructor
    public IslemLog(IslemTipi islemTipi, Kitap kitap) {
        this.islemTipi = islemTipi;
        this.kitap = kitap;
        if (kitap != null) {
            this.kitapBaslik = kitap.getBaslik();
            this.kitapIsbn = kitap.getIsbn();
            this.kitapYazar = kitap.getYazar();
        }
    }

    // Enum: İşlem tipleri
    public enum IslemTipi {
        KITAP_EKLE,
        KITAP_SIL
    }

    // --- Getter ve Setterlar ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public IslemTipi getIslemTipi() {
        return islemTipi;
    }

    public void setIslemTipi(IslemTipi islemTipi) {
        this.islemTipi = islemTipi;
    }

    public Kitap getKitap() {
        return kitap;
    }

    public void setKitap(Kitap kitap) {
        this.kitap = kitap;
    }

    public String getKitapBaslik() {
        return kitapBaslik;
    }

    public void setKitapBaslik(String kitapBaslik) {
        this.kitapBaslik = kitapBaslik;
    }

    public String getKitapIsbn() {
        return kitapIsbn;
    }

    public void setKitapIsbn(String kitapIsbn) {
        this.kitapIsbn = kitapIsbn;
    }

    public String getKitapYazar() {
        return kitapYazar;
    }

    public void setKitapYazar(String kitapYazar) {
        this.kitapYazar = kitapYazar;
    }

    public LocalDateTime getIslemTarihi() {
        return islemTarihi;
    }

    public void setIslemTarihi(LocalDateTime islemTarihi) {
        this.islemTarihi = islemTarihi;
    }
}

