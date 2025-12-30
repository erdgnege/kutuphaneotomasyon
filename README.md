📚 Kütüphane Otomasyon Sistemi (Library Management System)
Bu proje, modern bir kütüphanenin ihtiyaç duyabileceği tüm temel işlemleri (kitap yönetimi, ödünç alma/iade, kullanıcı yetkilendirme ve loglama) kapsayan, Spring Boot tabanlı bir kurumsal otomasyon çözümüdür.
🛠️ Kullanılan Teknolojiler
Backend
•	Java 17 & Spring Boot 3.4.1: Projenin ana iskeleti.
•	Spring Security & JWT: Stateless (durumsuz) kimlik doğrulama ve rol tabanlı (ADMIN, UYE, PERSONEL) yetkilendirme.
•	Spring Data JPA & Hibernate: Veritabanı yönetim ve ORM (Object-Relational Mapping) işlemleri.
•	MSSQL Server: Kurumsal seviyede ilişkisel veritabanı.
•	Lombok: Gereksiz kod kalabalığını (boilerplate) önlemek için kullanılan yardımcı kütüphane.
•	Spring Mail: Şifre sıfırlama ve doğrulama işlemleri için e-posta entegrasyonu.
Frontend
•	Bootstrap 5: Modern ve responsive (mobil uyumlu) kullanıcı arayüzü.
•	Vanilla JavaScript: Dinamik frontend işlemleri ve REST API tüketimi.
🚀 Öne Çıkan Özellikler
•	🛡️ Güvenli Kimlik Doğrulama: JWT (JSON Web Token) ile güvenli oturum yönetimi.
•	👥 Rol Tabanlı Yetkilendirme:
o	Admin: Kitap ekleme/silme, kullanıcı yönetimi, tüm ödünç kayıtlarını ve işlem loglarını görebilme.
o	Üye/Personel: Kitap ödünç alma, iade etme ve kendi geçmişini görüntüleme.
•	📈 Dinamik Ödünç Limiti: Üyeler için 3, Personeller için 5 kitap sınırı.
•	📜 İşlem Logları (Audit Log): Sistemde yapılan her kritik işlemin (kitap ödünç verme, iade vb.) tarih ve kullanıcı bazlı kaydedilmesi.
•	📧 E-Posta Servisi: Şifre sıfırlama ve kayıt doğrulama süreçlerinde kullanılan entegre e-posta motoru.
•	🛑 Merkezi Hata Yönetimi: GlobalExceptionHandler ile kullanıcıya dönen anlamlı hata mesajları.
🗄️ Veritabanı Şeması
Sistem Single Table Inheritance (Tek Tablo Kalıtımı) stratejisini kullanarak kullanıcıları yönetir:
•	kullanicilar: UYE ve PERSONEL verilerini tek tabloda, dtype ayrımıyla tutar.
•	kitaplar: Kitap detaylarını ve stok (mevcutluk) durumunu tutar.
•	oduncler: Aktif ve geçmiş ödünç verme kayıtlarını saklar.
•	islem_loglari: Sistemdeki tüm hareketleri denetim (audit) amacıyla kaydeder.
⚙️ Kurulum ve Çalıştırma
1. Veritabanı Ayarı
SQL Server üzerinde kutuphane isimli bir veritabanı oluşturun ve src/main/resources/application.properties dosyasını kendi bilgilerinizle güncelleyin:
Properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=kutuphane;trustServerCertificate=true;encrypt=true;
spring.datasource.username=KULLANICI_ADINIZ
spring.datasource.password=SIFRENIZ
2. E-Posta Ayarı (Opsiyonel)
Sistemin mail gönderebilmesi için app.email.enabled=true yapıp SMTP ayarlarınızı girin. Detaylar için EMAIL_SETUP.md dosyasına bakabilirsiniz.
3. Uygulamayı Başlatma
Proje dizininde aşağıdaki komutu çalıştırın:
Bash
mvn spring-boot:run
📡 API Uç Noktaları (Endpoints)
Metot	Endpoint	Açıklama	Yetki
POST	/api/auth/login	Giriş yap ve JWT Token al	Herkese Açık
POST	/api/kitap/ekle	Yeni kitap ekle	ADMIN
POST	/api/odunc/ver	Kitap ödünç ver	ADMIN
GET	/api/kitap	Tüm kitapları listele	Herkese Açık
DELETE	/api/kullanici/sil/{id}	Kullanıcı sil	ADMIN

