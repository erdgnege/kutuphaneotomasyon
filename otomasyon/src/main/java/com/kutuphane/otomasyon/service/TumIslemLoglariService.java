package com.kutuphane.otomasyon.service;

import com.kutuphane.otomasyon.dto.IslemLogDTO;
import com.kutuphane.otomasyon.model.IslemLog;
import com.kutuphane.otomasyon.model.Odunc;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TumIslemLoglariService {

    private final OduncService oduncService;
    private final IslemLogService islemLogService;

    public TumIslemLoglariService(OduncService oduncService, IslemLogService islemLogService) {
        this.oduncService = oduncService;
        this.islemLogService = islemLogService;
    }

    // Tüm işlem loglarını getir (ödünç işlemleri + kitap işlemleri, tarihe göre
    // sıralı)
    public List<IslemLogDTO> tumIslemLoglariniGetir() {
        // Ödünç kayıtlarını DTO'ya çevir
        List<Odunc> oduncler = oduncService.tumOduncleriGetir();
        List<IslemLogDTO> oduncLoglari = new ArrayList<>();

        for (Odunc odunc : oduncler) {
            // Ödünç verme logu
            oduncLoglari.add(IslemLogDTO.oduncVermeLogu(odunc));

            // İade edildiyse, iade logu da ekle
            if (odunc.getTeslimTarihi() != null) {
                oduncLoglari.add(IslemLogDTO.iadeLogu(odunc));
            }
        }

        // İşlem loglarını DTO'ya çevir (kitap ekleme/silme)
        List<IslemLog> islemLoglari = islemLogService.tumIslemLoglariniGetir();
        List<IslemLogDTO> kitapIslemLoglari = islemLoglari.stream()
                .map(log -> {
                    IslemLogDTO dto = IslemLogDTO.fromIslemLog(log);
                    // ID'yi kitap logları için farklı aralıkta tut (ödünç logları ile çakışmasın)
                    dto.setId(1000000L + log.getId()); // 1M'den başlat
                    return dto;
                })
                .collect(Collectors.toList());

        // Tüm logları birleştir
        List<IslemLogDTO> tumLoglar = new ArrayList<>();
        tumLoglar.addAll(oduncLoglari);
        tumLoglar.addAll(kitapIslemLoglari);

        // Tarihe göre sırala (en yeni önce)
        tumLoglar.sort(Comparator.comparing(IslemLogDTO::getIslemTarihi).reversed());

        return tumLoglar;
    }
}
