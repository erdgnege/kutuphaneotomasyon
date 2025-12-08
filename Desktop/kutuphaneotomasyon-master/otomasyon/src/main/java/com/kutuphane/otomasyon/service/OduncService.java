package com.kutuphane.otomasyon.service;

import com.kutuphane.otomasyon.model.Kitap;
import com.kutuphane.otomasyon.exception.IsKuraliException;
import com.kutuphane.otomasyon.exception.KaynakBulunamadiException;
import com.kutuphane.otomasyon.model.Odunc;
import com.kutuphane.otomasyon.model.Kullanici;
import com.kutuphane.otomasyon.repository.KitapRepository;
import com.kutuphane.otomasyon.repository.OduncRepository;
import com.kutuphane.otomasyon.repository.KullaniciRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Kitap ödünç alma ve iade etme ile ilgili iş mantığını yöneten servis sınıfı.
 */
@Service
public class OduncService {

    private static final Logger log = LoggerFactory.getLogger(OduncService.class);

    private static final String KULLANICI_BULUNAMADI_MESAJI = "Kullanıcı bulunamadı. ID: ";
    private static final String KITAP_BULUNAMADI_MESAJI = "Kitap bulunamadı. ID: ";
    private static final String ODUNC_KAYDI_BULUNAMADI_MESAJI = "Ödünç kaydı bulunamadı. ID: ";

    private final KitapRepository kitapRepository;
    private final KullaniciRepository kullaniciRepository;
    private final OduncRepository oduncRepository;

    /**
     * Gerekli repository'leri enjekte etmek için kullanılan kurucu metot.
     * 
     * @param kitapRepository     Kitap veritabanı işlemleri için.
     * @param kullaniciRepository Kullanıcı veritabanı işlemleri için.
     * @param oduncRepository     Ödünç kaydı veritabanı işlemleri için.
     */
    public OduncService(KitapRepository kitapRepository, KullaniciRepository kullaniciRepository,
            OduncRepository oduncRepository) {
        this.kitapRepository = kitapRepository;
        this.kullaniciRepository = kullaniciRepository;
        this.oduncRepository = oduncRepository;
    }

    /**
     * Bir kullanıcıya kitap ödünç verme işlemini yönetir.
     * Bu metot; kullanıcı ve kitap varlığını, ödünç alma limitini ve kitap stok
     * durumunu kontrol eder.
     * 
     * @param userId  Kitabı alacak kullanıcının ID'si.
     * @param kitapId Ödünç verilecek kitabın ID'si.
     * @return Oluşturulan yeni Odunc kaydı.
     */
    @Transactional // Bu metot içindeki veritabanı işlemlerinin bir bütün olarak çalışmasını
                   // sağlar.
    public Odunc kitapOduncVer(Long userId, Long kitapId) {

        // 1. Kullanıcıyı ve Kitabı bul
        Kullanici kullanici = kullaniciRepository.findById(userId)
                .orElseThrow(() -> new KaynakBulunamadiException(KULLANICI_BULUNAMADI_MESAJI + userId));

        Kitap kitap = kitapRepository.findById(kitapId)
                .orElseThrow(() -> new KaynakBulunamadiException(KITAP_BULUNAMADI_MESAJI + kitapId));

        // Polimorfizm: User nesnesi Uye veya Personel olabilir, doğru metot çalışır.
        int limit = kullanici.oduncAlmaLimitiHesapla();

        // 2. Limit Kontrolü (İş Mantığı)
        List<Odunc> uyeninOduncleri = oduncRepository.findByKullaniciIdAndTeslimTarihiIsNull(userId);
        if (uyeninOduncleri.size() >= limit) {
            throw new IsKuraliException("Ödünç alma limiti dolmuştur (" + limit + " kitap).");
        }

        // 3. Stok kontrolü
        if (!kitap.isMevcut()) {
            throw new IsKuraliException("Seçilen kitap stokta mevcut değil.");
        }

        // 4. İşlemleri yap (Veritabanını güncelle)
        kitap.setMevcut(false); // Kitabın durumunu "mevcut değil" yap
        kitapRepository.save(kitap); // Kitap Entity'sini DB'de güncelle

        // 6. Odunc tablosuna yeni kayıt ekle
        Odunc yeniOdunc = new Odunc();
        yeniOdunc.setKullanici(kullanici);
        yeniOdunc.setKitap(kitap);
        yeniOdunc.setOduncTarihi(LocalDate.now());
        // Teslim tarihi başlangıçta null olacak

        log.info("{} ({}) adlı kullanıcı, '{}' adlı kitabı ödünç aldı. Kullanıcı ID: {}, Kitap ID: {}",
                kullanici.getAdSoyad(), kullanici.getClass().getSimpleName(), kitap.getBaslik(), userId, kitapId);

        return oduncRepository.save(yeniOdunc);
    }

    /**
     * Bir ödünç kaydını sonlandırarak kitabın iade edilmesini sağlar.
     * Kitabın durumunu tekrar "mevcut" yapar ve ödünç kaydına teslim tarihini
     * işler.
     * 
     * @param oduncId İade edilecek işleme ait ödünç kaydının ID'si.
     * @return Güncellenmiş Odunc kaydı.
     */
    @Transactional // Bu metot içindeki veritabanı işlemlerinin bir bütün olarak çalışmasını
                   // sağlar.
    public Odunc kitapIadeAl(Long oduncId) {

        // 1. İade edilecek ödünç kaydını bul
        Odunc oduncKaydi = oduncRepository.findById(oduncId)
                .orElseThrow(() -> new KaynakBulunamadiException(ODUNC_KAYDI_BULUNAMADI_MESAJI + oduncId));

        if (oduncKaydi.getTeslimTarihi() != null) {
            throw new IsKuraliException("Bu kitap zaten iade edilmiş.");
        }

        // 2. Kitabın durumunu "mevcut" yap
        Kitap kitap = oduncKaydi.getKitap();
        kitap.setMevcut(true);
        kitapRepository.save(kitap);

        // 3. Ödünç kaydını güncelle (teslim tarihini ayarla)
        oduncKaydi.setTeslimTarihi(LocalDate.now());

        log.info("'{}' adlı kitap iade edildi. Ödünç ID: {}", kitap.getBaslik(), oduncId);

        return oduncRepository.save(oduncKaydi);
    }
}