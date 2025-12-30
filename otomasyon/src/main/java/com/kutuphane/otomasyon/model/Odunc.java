package com.kutuphane.otomasyon.model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "oduncler")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Odunc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "kitap_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_odunc_kitap"))
    @OnDelete(action = OnDeleteAction.CASCADE) // Kitap silinirse kayıt da silinsin
    private Kitap kitap;

    @ManyToOne(fetch = FetchType.EAGER) // Lazy loading sorununu önlemek için EAGER yapıyoruz
    @JoinColumn(name = "kullanici_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    @OnDelete(action = OnDeleteAction.CASCADE) // Kullanıcı silinirse kayıt da silinsin
    private Kullanici kullanici;

    private LocalDate oduncTarihi = LocalDate.now();
    private LocalDate teslimTarihi;

    // Boş Constructor
    public Odunc() {
    }

    // Kolaylık olması için parametreli constructor
    public Odunc(Kitap kitap, Kullanici kullanici) {
        this.kitap = kitap;
        this.kullanici = kullanici;
    }

}