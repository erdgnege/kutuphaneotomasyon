package com.kutuphane.otomasyon.service;

import com.kutuphane.otomasyon.model.IslemLog;
import com.kutuphane.otomasyon.model.Kitap;
import com.kutuphane.otomasyon.repository.IslemLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class IslemLogService {

    private final IslemLogRepository islemLogRepository;

    public IslemLogService(IslemLogRepository islemLogRepository) {
        this.islemLogRepository = islemLogRepository;
    }

    // Kitap ekleme logu kaydet
    @Transactional
    public void kitapEklemeLoguKaydet(Kitap kitap) {
        IslemLog log = new IslemLog(IslemLog.IslemTipi.KITAP_EKLE, kitap);
        islemLogRepository.save(log);
    }

    // Kitap silme logu kaydet (kitap bilgilerini saklamak için kitap objesi parametre olarak alınır)
    @Transactional
    public void kitapSilmeLoguKaydet(Kitap kitap) {
        IslemLog log = new IslemLog(IslemLog.IslemTipi.KITAP_SIL, kitap);
        // Kitap silindiği için null yapabiliriz ama bilgiler zaten kaydedildi
        log.setKitap(null); // İlişkiyi kaldır (kitap silindi)
        islemLogRepository.save(log);
    }

    // Tüm işlem loglarını getir
    public List<IslemLog> tumIslemLoglariniGetir() {
        return islemLogRepository.findAllOrderByIslemTarihiDesc();
    }
}

