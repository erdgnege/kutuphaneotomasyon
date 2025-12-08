package com.kutuphane.otomasyon.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Bir kitabın bir kullanıcı tarafından ödünç alınması işlemini temsil eden JPA
 * varlığı. Bu sınıf, Kitap ve Kullanici entity'leri arasında ManyToOne ilişkiyi
 * tanımlar.
 */
@Entity // Bu sınıfın bir JPA varlığı (Entity) olduğunu belirtir.
@Table(name = "oduncler") // Veritabanındaki tablo adını belirtir.
public class Odunc {

    @Id // Birincil anahtar (Primary Key) olduğunu belirtir.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID'nin DB tarafından otomatik artırılmasını sağlar.
    private Long id;

    // Bir ödünç kaydı sadece bir kitaba aittir (ManyToOne ilişki).
    @ManyToOne
    @JoinColumn(name = "kitap_id") // Bu tablodaki yabancı anahtar sütununun adı
    // OnDelete: İlişkili Kitap silindiğinde bu Odunc kaydının da silinmesini sağlar
    // (CASCADE).
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Kitap kitap;

    // Bir ödünç kaydı sadece bir kullanıcıya aittir (ManyToOne ilişki).
    @ManyToOne
    @JoinColumn(name = "kullanici_id") // Bu tablodaki yabancı anahtar sütununun adı
    // OnDelete: İlişkili Kullanici silindiğinde bu Odunc kaydının da silinmesini
    // sağlar (CASCADE).
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Kullanici kullanici;

    // Ödünç alma tarihi, kayıt oluşturulduğunda varsayılan olarak o günün tarihi
    // ayarlanır.
    private LocalDate oduncTarihi = LocalDate.now();
    private LocalDate teslimTarihi; // Kitabın ne zaman iade edildiği (NULL ise henüz iade edilmemiş demektir).

    // JPA/Hibernate'in veri çekerken nesne oluşturması için gerekli boş
    // constructor.
    public Odunc() {
    }

    // --- Getter ve Setter Metotları (Kapsülleme) ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Kitap getKitap() {
        return kitap;
    }

    public void setKitap(Kitap kitap) {
        this.kitap = kitap;
    }

    public Kullanici getKullanici() {
        return kullanici;
    }

    public void setKullanici(Kullanici kullanici) {
        this.kullanici = kullanici;
    }

    public LocalDate getOduncTarihi() {
        return oduncTarihi;
    }

    public void setOduncTarihi(LocalDate oduncTarihi) {
        this.oduncTarihi = oduncTarihi;
    }

    public LocalDate getTeslimTarihi() {
        return teslimTarihi;
    }

    public void setTeslimTarihi(LocalDate teslimTarihi) {
        this.teslimTarihi = teslimTarihi;
    }

}