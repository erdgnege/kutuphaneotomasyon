package com.kutuphane.otomasyon.service;

import com.kutuphane.otomasyon.model.*;
import com.kutuphane.otomasyon.repository.KullaniciRepository;
import com.kutuphane.otomasyon.exception.KutuphaneHatasi;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class KullaniciService {

    private final KullaniciRepository kullaniciRepository;

    public KullaniciService(KullaniciRepository kullaniciRepository) {
        this.kullaniciRepository = kullaniciRepository;
    }

    // Polimorfizm budur kanka: Parametre olarak üst sınıfı (Kullanici) alırız,
    // içine Uye de gelse Personel de gelse Java bunu kabul eder.
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
}