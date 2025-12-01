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

@Entity
@Table(name = "oduncler")
public class Odunc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "kitap_id")
    private Kitap kitap;

    @ManyToOne
    @JoinColumn(name = "uye_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Uye uye;

    private LocalDate oduncTarihi = LocalDate.now();
    private LocalDate teslimTarihi;

    public Odunc() {
    }

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

    public Uye getUye() {
        return uye;
    }

    public void setUye(Uye uye) {
        this.uye = uye;
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