package com.kutuphane.otomasyon.model;

import java.time.LocalDateTime;

public class Bildirim {
    private Long id;
    private Long kullaniciId;
    private String kullaniciAdi;
    private String mesaj;
    private LocalDateTime olusturmaTarihi;
    private boolean okundu;

    public Bildirim() {
    }

    public Bildirim(Long kullaniciId, String kullaniciAdi, String mesaj) {
        this.kullaniciId = kullaniciId;
        this.kullaniciAdi = kullaniciAdi;
        this.mesaj = mesaj;
        this.olusturmaTarihi = LocalDateTime.now();
        this.okundu = false;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getKullaniciId() {
        return kullaniciId;
    }

    public void setKullaniciId(Long kullaniciId) {
        this.kullaniciId = kullaniciId;
    }

    public String getKullaniciAdi() {
        return kullaniciAdi;
    }

    public void setKullaniciAdi(String kullaniciAdi) {
        this.kullaniciAdi = kullaniciAdi;
    }

    public String getMesaj() {
        return mesaj;
    }

    public void setMesaj(String mesaj) {
        this.mesaj = mesaj;
    }

    public LocalDateTime getOlusturmaTarihi() {
        return olusturmaTarihi;
    }

    public void setOlusturmaTarihi(LocalDateTime olusturmaTarihi) {
        this.olusturmaTarihi = olusturmaTarihi;
    }

    public boolean isOkundu() {
        return okundu;
    }

    public void setOkundu(boolean okundu) {
        this.okundu = okundu;
    }
}
