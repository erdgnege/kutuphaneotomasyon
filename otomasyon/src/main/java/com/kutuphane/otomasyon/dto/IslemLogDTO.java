package com.kutuphane.otomasyon.dto;

import com.kutuphane.otomasyon.model.IslemLog;
import com.kutuphane.otomasyon.model.Odunc;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IslemLogDTO {
    private Long id;
    private String islemTipi; // "ODUNC_VER", "ODUNC_IADE", "KITAP_EKLE", "KITAP_SIL"
    private String kitapBaslik;
    private String kitapYazar;
    private String kitapIsbn;
    private String kullaniciAdSoyad; // Ödünç işlemleri için
    private String kullaniciEmail; // Ödünç işlemleri için
    private LocalDate oduncTarihi; // Ödünç işlemleri için
    private LocalDate iadeTarihi; // Ödünç işlemleri için
    private LocalDateTime islemTarihi; // Genel işlem tarihi
    private Boolean iadeEdildi; // Ödünç işlemleri için

    // Ödünç verme logu oluştur
    public static IslemLogDTO oduncVermeLogu(Odunc odunc) {
        return IslemLogDTO.builder()
                .id(odunc.getId() * 1000L) // ID çakışmasını önlemek için çarp
                .islemTipi("ODUNC_VER")
                .kitapBaslik(odunc.getKitap().getBaslik())
                .kitapYazar(odunc.getKitap().getYazar())
                .kitapIsbn(odunc.getKitap().getIsbn())
                .kullaniciAdSoyad(odunc.getKullanici().getAdSoyad())
                .kullaniciEmail(odunc.getKullanici().getEmail())
                .oduncTarihi(odunc.getOduncTarihi())
                .iadeTarihi(null)
                .islemTarihi(odunc.getOduncTarihi().atStartOfDay())
                .iadeEdildi(false)
                .build();
    }

    // İade logu oluştur
    public static IslemLogDTO iadeLogu(Odunc odunc) {
        return IslemLogDTO.builder()
                .id(odunc.getId() * 1000L + 1) // İade logu için +1
                .islemTipi("ODUNC_IADE")
                .kitapBaslik(odunc.getKitap().getBaslik())
                .kitapYazar(odunc.getKitap().getYazar())
                .kitapIsbn(odunc.getKitap().getIsbn())
                .kullaniciAdSoyad(odunc.getKullanici().getAdSoyad())
                .kullaniciEmail(odunc.getKullanici().getEmail())
                .oduncTarihi(odunc.getOduncTarihi())
                .iadeTarihi(odunc.getTeslimTarihi())
                .islemTarihi(odunc.getTeslimTarihi().atStartOfDay())
                .iadeEdildi(true)
                .build();
    }

    // İşlem log'undan DTO oluştur
    public static IslemLogDTO fromIslemLog(IslemLog log) {
        return IslemLogDTO.builder()
                .id(log.getId())
                .islemTipi(log.getIslemTipi().name())
                .kitapBaslik(log.getKitapBaslik())
                .kitapYazar(log.getKitapYazar())
                .kitapIsbn(log.getKitapIsbn())
                .islemTarihi(log.getIslemTarihi())
                .build();
    }
}
