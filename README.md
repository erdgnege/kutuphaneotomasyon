# Kütüphane Otomasyon Sistemi

Bu proje, MSSQL veritabanı kullanan bir kütüphane otomasyon sistemidir. Spring Boot backend ve Bootstrap frontend ile geliştirilmiştir.

## 🚀 Özellikler

- **Kitap Yönetimi**: Kitapları listeleme, ekleme, silme
- **Ödünç Verme Sistemi**: Kitapları kullanıcılara ödünç verme ve iade alma
- **Kullanıcı Yönetimi**: Üye ve Personel yönetimi
- **MSSQL Entegrasyonu**: SQL Server veritabanı desteği
- **RESTful API**: Modern REST API yapısı
- **Responsive Frontend**: Bootstrap 5 ile modern ve mobil uyumlu arayüz

## 📋 Gereksinimler

- Java 17+
- Maven 3.6+
- Microsoft SQL Server
- Modern web tarayıcısı (Chrome, Firefox, Edge)

## 🗄️ Veritabanı Kurulumu

1. SQL Server Management Studio'yu açın
2. Yeni bir veritabanı oluşturun:

   ```sql
   CREATE DATABASE kutuphane;
   ```

3. `application.properties` dosyasındaki veritabanı bilgilerini kontrol edin:

   ```properties
   spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=kutuphane;trustServerCertificate=true;encrypt=true;
   spring.datasource.username=sa
   spring.datasource.password=e3206geMahmut.
   ```

4. Hibernate otomatik olarak tabloları oluşturacaktır (`ddl-auto=update`)

## 🔧 Backend Kurulumu

1. Proje dizinine gidin:

   ```bash
   cd otomasyon
   ```

2. Maven bağımlılıklarını yükleyin:

   ```bash
   mvn clean install
   ```

3. Uygulamayı çalıştırın:
   ```bash
   mvn spring-boot:run
   ```

Backend `http://localhost:8080` adresinde çalışacaktır.

## 🌐 Frontend Kullanımı

1. `index.html` dosyasını bir web tarayıcısında açın
2. Kitaplar otomatik olarak yüklenecektir
3. "Ödünç Ver" butonuna tıklayarak ödünç verme işlemi yapabilirsiniz

**Not**: Frontend'in çalışması için backend'in çalışıyor olması gerekmektedir.

## 📡 API Endpoint'leri

### Kitaplar

- `GET /api/kitaplar` - Tüm kitapları listele
- `GET /api/kitaplar/{id}` - Belirli bir kitabı getir
- `POST /api/kitaplar` - Yeni kitap ekle
- `DELETE /api/kitaplar/{id}` - Kitap sil

### Ödünç İşlemleri

- `POST /api/odunc/ver?kitapId={id}&userId={id}` - Kitap ödünç ver
- `PUT /api/odunc/iade/{oduncId}` - Kitap iade al

### Kullanıcılar

- `GET /api/kullanicilar` - Tüm kullanıcıları listele
- `GET /api/kullanicilar/uyeler` - Tüm üyeleri listele
- `GET /api/kullanicilar/personeller` - Tüm personelleri listele
- `GET /api/kullanicilar/{id}` - Belirli bir kullanıcıyı getir
- `POST /api/kullanicilar/uye` - Yeni üye ekle
- `POST /api/kullanicilar/personel` - Yeni personel ekle
- `PUT /api/kullanicilar/uye/{id}` - Üye güncelle
- `PUT /api/kullanicilar/personel/{id}` - Personel güncelle
- `DELETE /api/kullanicilar/{id}` - Kullanıcı sil

## 🔐 Güvenlik

- **Geliştirme Modu**: API endpoint'leri şu anda public (kimlik doğrulama gerektirmiyor)
- **Production**: Production ortamında SecurityConfig'te authentication aktif edilmelidir

## 📊 Veritabanı Şeması

Sistem aşağıdaki tabloları kullanır:

- **kullanicilar**: Kullanıcı bilgileri (SINGLE_TABLE inheritance ile Üye ve Personel)
- **kitaplar**: Kitap bilgileri
- **oduncler**: Ödünç verme kayıtları

## 🛠️ Teknolojiler

### Backend

- Spring Boot 4.0.0
- Spring Data JPA
- Spring Security
- MSSQL JDBC Driver
- Hibernate

### Frontend

- Bootstrap 5.3.2
- Bootstrap Icons
- Vanilla JavaScript

## 📝 Notlar

- Veritabanı tabloları Hibernate tarafından otomatik oluşturulur
- CORS yapılandırması frontend erişimi için yapılmıştır
- Hata yönetimi GlobalExceptionHandler ile merkezi olarak yapılmaktadır
- Ödünç verme limitleri: Üye 3 kitap, Personel 5 kitap

## 🐛 Sorun Giderme

### Backend başlamıyor

- SQL Server'ın çalıştığından emin olun
- Veritabanı bağlantı bilgilerini kontrol edin
- Port 8080'in kullanılabilir olduğundan emin olun

### Frontend API'ye bağlanamıyor

- Backend'in çalıştığından emin olun (`http://localhost:8080`)
- Tarayıcı konsolunda CORS hatalarını kontrol edin
- SecurityConfig'te CORS ayarlarını kontrol edin

### Veritabanı bağlantı hatası

- SQL Server Authentication'ın aktif olduğundan emin olun
- `trustServerCertificate=true` parametresini kontrol edin
- Firewall ayarlarını kontrol edin

## 📄 Lisans

Bu proje eğitim amaçlı geliştirilmiştir.
