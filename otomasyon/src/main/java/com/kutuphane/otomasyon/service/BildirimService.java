package com.kutuphane.otomasyon.service;

import com.kutuphane.otomasyon.model.Bildirim;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class BildirimService {

    // In-memory bildirim listesi (basit implementasyon)
    private final ConcurrentLinkedQueue<Bildirim> bildirimler = new ConcurrentLinkedQueue<>();
    private long bildirimIdCounter = 1;

    /**
     * Yeni bildirim oluştur
     */
    public Bildirim bildirimOlustur(Long kullaniciId, String kullaniciAdi, String mesaj) {
        Bildirim bildirim = new Bildirim(kullaniciId, kullaniciAdi, mesaj);
        bildirim.setId(bildirimIdCounter++);
        bildirimler.add(bildirim);

        // Eski bildirimleri temizle (30 günden eski)
        temizleEskiBildirimler();

        return bildirim;
    }

    /**
     * Tüm bildirimleri getir (okunmamışlar önce)
     */
    public List<Bildirim> tumBildirimleriGetir() {
        return bildirimler.stream()
                .sorted((a, b) -> {
                    // Okunmamışlar önce
                    if (a.isOkundu() != b.isOkundu()) {
                        return a.isOkundu() ? 1 : -1;
                    }
                    // Son eklenenler önce
                    return b.getOlusturmaTarihi().compareTo(a.getOlusturmaTarihi());
                })
                .collect(Collectors.toList());
    }

    /**
     * Okunmamış bildirim sayısını getir
     */
    public long okunmamisBildirimSayisi() {
        return bildirimler.stream()
                .filter(b -> !b.isOkundu())
                .count();
    }

    /**
     * Bildirimi okundu olarak işaretle
     */
    public void bildirimiOkunduIsaretle(Long bildirimId) {
        bildirimler.stream()
                .filter(b -> b.getId().equals(bildirimId))
                .findFirst()
                .ifPresent(b -> b.setOkundu(true));
    }

    /**
     * Tüm bildirimleri okundu olarak işaretle
     */
    public void tumBildirimleriOkunduIsaretle() {
        bildirimler.forEach(b -> b.setOkundu(true));
    }

    /**
     * 30 günden eski bildirimleri temizle
     */
    private void temizleEskiBildirimler() {
        LocalDateTime otuzGunOnce = LocalDateTime.now().minus(30, ChronoUnit.DAYS);
        bildirimler.removeIf(b -> b.getOlusturmaTarihi().isBefore(otuzGunOnce));
    }
}
