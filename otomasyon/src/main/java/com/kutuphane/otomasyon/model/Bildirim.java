package com.kutuphane.otomasyon.model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
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

}
