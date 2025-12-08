package com.kutuphane.otomasyon;

import com.kutuphane.otomasyon.exception.IsKuraliException;
import com.kutuphane.otomasyon.exception.KaynakBulunamadiException;
import com.kutuphane.otomasyon.model.Kitap;
import com.kutuphane.otomasyon.model.Odunc;
import com.kutuphane.otomasyon.model.Uye;
import com.kutuphane.otomasyon.repository.KitapRepository;
import com.kutuphane.otomasyon.repository.KullaniciRepository;
import com.kutuphane.otomasyon.repository.OduncRepository;
import com.kutuphane.otomasyon.service.OduncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Bu, OduncService'in iş mantığını test eden bir unit test sınıfıdır.
// MockitoExtension, @Mock ve @InjectMocks anotasyonlarını etkinleştirir.
@ExtendWith(MockitoExtension.class)
class OtomasyonApplicationTests {

	@Mock
	private KitapRepository kitapRepository;

	@Mock
	private KullaniciRepository kullaniciRepository;

	@Mock
	private OduncRepository oduncRepository;

	@InjectMocks
	private OduncService oduncService; // Test edilecek servis sınıfı

	private Uye testUye;
	private Kitap testKitap;
	private final Long uyeId = 1L;
	private final Long kitapId = 10L;
	private final Long oduncId = 100L;

	/**
	 * Her testten önce çalışacak hazırlık metodu.
	 */
	@BeforeEach
	void setUp() {
		// Test Üye ve Kitap nesnelerini hazırla
		testUye = new Uye("Test Uye", "test@mail.com", "U001");
		testUye.setId(uyeId);

		testKitap = new Kitap();
		testKitap.setId(kitapId);
		testKitap.setBaslik("Test Kitabı");
		testKitap.setMevcut(true); // Varsayılan olarak mevcut
	}

	/* ------------------- KITAP ODUNC VERME (LEND) TESTLERİ ------------------- */

	@Test
	@DisplayName("Başarılı Kitap Ödünç Verme Testi")
	void kitapOduncVer_Basarili() {
		// Hazırlık: Kullanıcı, Kitap bulundu, şu an ödünç yok.
		when(kullaniciRepository.findById(uyeId)).thenReturn(Optional.of(testUye));
		when(kitapRepository.findById(kitapId)).thenReturn(Optional.of(testKitap));
		when(oduncRepository.findByKullaniciIdAndTeslimTarihiIsNull(uyeId)).thenReturn(Collections.emptyList());

		Odunc mockOdunc = new Odunc();
		mockOdunc.setId(oduncId);
		mockOdunc.setKitap(testKitap); // Hata düzeltmesi: Kitap nesnesi atanmalı.
		mockOdunc.setKullanici(testUye); // Hata düzeltmesi: Kullanıcı nesnesi atanmalı.
		when(oduncRepository.save(any(Odunc.class))).thenReturn(mockOdunc);

		// Aksiyon
		Odunc sonuc = oduncService.kitapOduncVer(uyeId, kitapId);

		// Doğrulama
		assertNotNull(sonuc);
		assertEquals(kitapId, sonuc.getKitap().getId());

		// Kitabın mevcut durumu güncellendi mi?
		assertFalse(testKitap.isMevcut());
		verify(kitapRepository, times(1)).save(testKitap);
		verify(oduncRepository, times(1)).save(any(Odunc.class));
	}

	@Test
	@DisplayName("Kullanıcı Ödünç Alma Limiti Dolu Testi")
	void kitapOduncVer_LimitDoldu_HataFirlat() {
		// Hazırlık: Üye limiti 3 (Uye.java). 3 kayıt döndür.
		List<Odunc> doluOduncler = List.of(new Odunc(), new Odunc(), new Odunc());

		when(kullaniciRepository.findById(uyeId)).thenReturn(Optional.of(testUye));
		when(kitapRepository.findById(kitapId)).thenReturn(Optional.of(testKitap)); // Hata düzeltmesi: Bu mock tanımı
																					// eksikti.
		when(oduncRepository.findByKullaniciIdAndTeslimTarihiIsNull(uyeId)).thenReturn(doluOduncler);

		// Aksiyon ve Doğrulama: IsKuraliException fırlatılmalı
		assertThrows(IsKuraliException.class, () -> {
			oduncService.kitapOduncVer(uyeId, kitapId);
		}, "Ödünç alma limiti dolmuştur (3 kitap).");

		// Doğrulama: Save metotları çağrılmadı
		verify(oduncRepository, never()).save(any(Odunc.class));
	}

	@Test
	@DisplayName("Kitap Stokta Mevcut Değil Testi")
	void kitapOduncVer_StoktaYok_HataFirlat() {
		// Hazırlık: Kitabı mevcut değil olarak ayarla.
		testKitap.setMevcut(false);

		when(kullaniciRepository.findById(uyeId)).thenReturn(Optional.of(testUye));
		when(kitapRepository.findById(kitapId)).thenReturn(Optional.of(testKitap));
		when(oduncRepository.findByKullaniciIdAndTeslimTarihiIsNull(uyeId)).thenReturn(Collections.emptyList());

		// Aksiyon ve Doğrulama: IsKuraliException fırlatılmalı
		assertThrows(IsKuraliException.class, () -> {
			oduncService.kitapOduncVer(uyeId, kitapId);
		}, "Seçilen kitap stokta mevcut değil.");

		// Doğrulama: Save metotları çağrılmadı
		verify(oduncRepository, never()).save(any(Odunc.class));
	}

	/* ------------------- KITAP IADE ALMA (RETURN) TESTLERİ ------------------- */

	@Test
	@DisplayName("Başarılı Kitap İade Alma Testi")
	void kitapIadeAl_Basarili() {
		// Hazırlık: İade edilecek Odunc kaydı oluştur.
		Odunc oduncKaydi = new Odunc();
		oduncKaydi.setId(oduncId);
		oduncKaydi.setKitap(testKitap);
		testKitap.setMevcut(false); // Kitap mevcut değil olmalı ki iade sonrası true olsun.

		when(oduncRepository.findById(oduncId)).thenReturn(Optional.of(oduncKaydi));
		when(oduncRepository.save(any(Odunc.class))).thenReturn(oduncKaydi);

		// Aksiyon
		Odunc sonuc = oduncService.kitapIadeAl(oduncId);

		// Doğrulama
		assertNotNull(sonuc.getTeslimTarihi());
		assertTrue(testKitap.isMevcut()); // Kitabın mevcut durumu true oldu mu?
		verify(kitapRepository, times(1)).save(testKitap);
		verify(oduncRepository, times(1)).save(oduncKaydi);
	}

	@Test
	@DisplayName("Zaten İade Edilmiş Kitabı İade Etme Testi")
	void kitapIadeAl_ZatenIadeEdilmis_HataFirlat() {
		// Hazırlık: Teslim tarihi dolu olan Odunc kaydı oluştur.
		Odunc oduncKaydi = new Odunc();
		oduncKaydi.setId(oduncId);
		oduncKaydi.setTeslimTarihi(LocalDate.now().minusDays(5)); // Zaten iade edilmiş

		when(oduncRepository.findById(oduncId)).thenReturn(Optional.of(oduncKaydi));

		// Aksiyon ve Doğrulama: IsKuraliException fırlatılmalı
		assertThrows(IsKuraliException.class, () -> {
			oduncService.kitapIadeAl(oduncId);
		}, "Bu kitap zaten iade edilmiş.");

		// Doğrulama: Save metodu çağrılmadı
		verify(oduncRepository, never()).save(any(Odunc.class));
	}

	@Test
	@DisplayName("Geçersiz Ödünç ID'si ile İade Testi")
	void kitapIadeAl_GecersizOduncId_HataFirlat() {
		// Hazırlık: Odunc kaydı bulunamadı.
		when(oduncRepository.findById(anyLong())).thenReturn(Optional.empty());

		// Aksiyon ve Doğrulama: KaynakBulunamadiException fırlatılmalı
		assertThrows(KaynakBulunamadiException.class, () -> {
			oduncService.kitapIadeAl(999L);
		}, "Ödünç kaydı bulunamadı.");

		verify(oduncRepository, never()).save(any(Odunc.class));
	}
}