package com.kutuphane.otomasyon.service;

import com.kutuphane.otomasyon.model.*;
import com.kutuphane.otomasyon.repository.KullaniciRepository;
import com.kutuphane.otomasyon.exception.KutuphaneHatasi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Random;

@Service
public class KullaniciService {

    private final KullaniciRepository kullaniciRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public KullaniciService(KullaniciRepository kullaniciRepository) {
        this.kullaniciRepository = kullaniciRepository;
    }

    // Polimorfizm: Üst sınıf (Kullanici) parametre olarak kullanılır (Uye veya Personel kabul edilir)
    public Kullanici kullaniciKaydet(Kullanici kullanici) {
        // E-posta kontrolü (sadece başka bir kullanıcı tarafından kullanılıyorsa hata ver)
        if (kullanici.getEmail() != null) {
            kullaniciRepository.findByEmail(kullanici.getEmail())
                    .ifPresent(existing -> {
                        // Eğer mevcut kullanıcının kendi email'i değilse hata ver
                        if (!existing.getId().equals(kullanici.getId())) {
                            throw new KutuphaneHatasi("Bu e-posta adresi zaten kullanılıyor: " + kullanici.getEmail());
                        }
                    });
        }

        // Telefon kontrolü (sadece başka bir kullanıcı tarafından kullanılıyorsa hata ver)
        if (kullanici.getTelefon() != null && !kullanici.getTelefon().trim().isEmpty()) {
            kullaniciRepository.findByTelefon(kullanici.getTelefon())
                    .ifPresent(existing -> {
                        // Eğer mevcut kullanıcının kendi telefonu değilse hata ver
                        if (!existing.getId().equals(kullanici.getId())) {
                            throw new KutuphaneHatasi("Bu telefon numarası zaten kullanılıyor: " + kullanici.getTelefon());
                        }
                    });
        }

        // Üye ise üye numarası kontrolü (sadece başka bir üye tarafından kullanılıyorsa hata ver)
        if (kullanici instanceof Uye) {
            Uye uye = (Uye) kullanici;
            if (uye.getUyeNo() != null) {
                kullaniciRepository.findByUyeNo(uye.getUyeNo())
                        .ifPresent(existing -> {
                            // Eğer mevcut üyenin kendi üye numarası değilse hata ver
                            if (!existing.getId().equals(uye.getId())) {
                                throw new KutuphaneHatasi("Bu üye numarası zaten kullanılıyor: " + uye.getUyeNo());
                            }
                        });
            }
        }

        return kullaniciRepository.save(kullanici);
    }

    // ID ile kullanıcı bulma (Özel hata fırlatmalı)
    public Kullanici kullaniciBulById(Long id) {
        return kullaniciRepository.findById(id)
                .orElseThrow(() -> new KutuphaneHatasi("Kullanıcı bulunamadı! ID: " + id));
    }

    // Email ile kullanıcı bulma
    public Kullanici kullaniciBulByEmail(String email) {
        return kullaniciRepository.findByEmail(email)
                .orElseThrow(() -> new KutuphaneHatasi("Kullanıcı bulunamadı! Email: " + email));
    }

    // Üye numarası ile üye bulma
    public Uye uyeBulByUyeNo(String uyeNo) {
        return kullaniciRepository.findByUyeNo(uyeNo)
                .orElseThrow(() -> new KutuphaneHatasi("Üye bulunamadı! Üye No: " + uyeNo));
    }

    // Listeler
    public List<Kullanici> tumKullanicilariGetir() {
        return kullaniciRepository.findAll();
    }

    public List<Uye> tumUyeleriGetir() {
        return kullaniciRepository.findAllUyeler();
    }

    public List<Personel> tumPersonelleriGetir() {
        return kullaniciRepository.findAllPersoneller();
    }

    // Kullanıcı silme
    public void kullaniciSil(Long id) {
        if (!kullaniciRepository.existsById(id)) {
            throw new KutuphaneHatasi("Sistemde böyle bir kullanıcı kayıtlı değil.");
        }
        kullaniciRepository.deleteById(id);
    }

    // Kullanıcı tipini değiştir (Üye <-> Personel)
    @Transactional
    public Kullanici kullaniciTipiniDegistir(Long id, String yeniTip) {
        Kullanici mevcutKullanici = kullaniciBulById(id);
        
        if (!yeniTip.equals("UYE") && !yeniTip.equals("PERSONEL")) {
            throw new KutuphaneHatasi("Geçersiz kullanıcı tipi! Sadece 'UYE' veya 'PERSONEL' olabilir.");
        }
        
        // Mevcut tip ile yeni tip aynıysa değişiklik yapma
        String mevcutTip = mevcutKullanici instanceof Uye ? "UYE" : "PERSONEL";
        if (mevcutTip.equals(yeniTip)) {
            return mevcutKullanici; // Değişiklik yok, mevcut kullanıcıyı döndür
        }
        
        // Uye -> Personel dönüşümü
        if (mevcutKullanici instanceof Uye && yeniTip.equals("PERSONEL")) {
            // Personel için gerekli alanlar
            // Sicil no oluştur (6 haneli rastgele sayı)
            Random random = new Random();
            String sicilNo;
            boolean sicilNoExists;
            do {
                sicilNo = String.format("%06d", random.nextInt(1000000));
                final String finalSicilNo = sicilNo;
                sicilNoExists = kullaniciRepository.findAllPersoneller().stream()
                        .anyMatch(p -> finalSicilNo.equals(p.getSicilNo()));
            } while (sicilNoExists);
            
            // Native query ile dtype, sicil_no, departman güncelle ve uye_no'yu temizle
            entityManager.createNativeQuery("UPDATE kullanicilar SET dtype = :yeniTip, sicil_no = :sicilNo, departman = :departman, uye_no = NULL WHERE id = :id")
                    .setParameter("yeniTip", yeniTip)
                    .setParameter("sicilNo", sicilNo)
                    .setParameter("departman", "Genel")
                    .setParameter("id", id)
                    .executeUpdate();
        } else {
            // Personel -> Uye dönüşümü
            // Üye no oluştur (6 haneli rastgele sayı)
            Random random = new Random();
            String uyeNo;
            boolean uyeNoExists;
            do {
                uyeNo = String.format("%06d", random.nextInt(1000000));
                final String finalUyeNo = uyeNo;
                uyeNoExists = kullaniciRepository.findAllUyeler().stream()
                        .anyMatch(u -> finalUyeNo.equals(u.getUyeNo()));
            } while (uyeNoExists);
            
            // Native query ile dtype, uye_no güncelle ve sicil_no, departman'ı temizle
            entityManager.createNativeQuery("UPDATE kullanicilar SET dtype = :yeniTip, uye_no = :uyeNo, sicil_no = NULL, departman = NULL WHERE id = :id")
                    .setParameter("yeniTip", yeniTip)
                    .setParameter("uyeNo", uyeNo)
                    .setParameter("id", id)
                    .executeUpdate();
        }
        
        // EntityManager cache'ini temizle ve entity'yi tekrar yükle
        entityManager.clear();
        return kullaniciBulById(id);
    }
}
