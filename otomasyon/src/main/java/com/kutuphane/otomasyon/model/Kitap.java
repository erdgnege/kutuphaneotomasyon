package com.kutuphane.otomasyon.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity // Bu sınıfın bir JPA varlığı (Entity) olduğunu belirtir.
@Table(name = "kitaplar") // Veritabanındaki tablo adını belirtir.
public class Kitap {

    @Id // Birincil anahtar (Primary Key) olduğunu belirtir.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID'nin veritabanı tarafından otomatik artırılmasını sağlar.
    private Long id;

    @NotBlank(message = "Başlık boş olamaz") // API çağrılarında (JSON) alanın boş bırakılamayacağını kontrol eder
                                             // (Validasyon).
    @Column(nullable = false) // Veritabanı seviyesinde de zorunlu (NOT NULL) olduğunu belirtir.
    private String baslik;

    @NotBlank(message = "Yazar boş olamaz")
    @Column(nullable = false) // NOT NULL
    private String yazar;

    @NotBlank(message = "ISBN boş olamaz")
    @Column(unique = true, nullable = false) // ISBN'nin hem benzersiz (UNIQUE) hem de zorunlu (NOT NULL) olduğunu
                                             // belirtir.
    private String isbn;

    // primitive 'boolean' olduğu için zaten null olamaz. JPA bunu "BIT" veya
    // "BOOLEAN" olarak saklar.
    private boolean mevcut = true; // Kitabın rafta olup olmadığını tutar. Varsayılan değer: true

    // JPA/Hibernate'in veri çekerken nesne oluşturması için gerekli boş
    // constructor.
    public Kitap() {
    }

    // --- Getter ve Setter Metotları (Kapsülleme) ---

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

    public boolean isMevcut() { // Boolean için 'is' önekli getter metodu (Java kuralı)
        return mevcut;
    }

    public void setMevcut(boolean mevcut) {
        this.mevcut = mevcut;
    }
}