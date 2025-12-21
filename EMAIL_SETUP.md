# E-posta Sistemi Kurulum Rehberi

Bu rehber, kütüphane otomasyon sistemine gerçek e-posta entegrasyonu yapmak için gerekli adımları açıklar.

## 1. Gmail Kullanımı (Önerilen)

### Adımlar:

1. **Gmail Hesabınızda 2 Adımlı Doğrulamayı Aktif Edin**

   - Gmail hesabınıza giriş yapın
   - Hesap ayarları > Güvenlik > 2 Adımlı Doğrulama

2. **Uygulama Şifresi Oluşturun**

   - Google Hesap Ayarları > Güvenlik > 2 Adımlı Doğrulama > Uygulama şifreleri
   - "Uygulama" için "Mail" seçin
   - "Cihaz" için "Diğer (Özel ad)" yazın ve bir isim verin (örn: "Kütüphane Otomasyon")
   - Oluşturulan 16 haneli şifreyi kopyalayın

3. **application.properties Dosyasını Güncelleyin**

```properties
# E-posta servisini aktif et
app.email.enabled=true

# Gmail SMTP Ayarları
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-16-digit-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

**ÖNEMLİ:** `spring.mail.password` alanına normal Gmail şifrenizi DEĞİL, oluşturduğunuz 16 haneli uygulama şifresini girmelisiniz!

## 2. Outlook/Hotmail Kullanımı

```properties
app.email.enabled=true

spring.mail.host=smtp-mail.outlook.com
spring.mail.port=587
spring.mail.username=your-email@outlook.com
spring.mail.password=your-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

## 3. Yandex Mail Kullanımı

```properties
app.email.enabled=true

spring.mail.host=smtp.yandex.com
spring.mail.port=465
spring.mail.username=your-email@yandex.com
spring.mail.password=your-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.ssl.enable=true
```

## 4. Test Etme

1. `application.properties` dosyasını güncelleyin
2. Uygulamayı yeniden başlatın
3. Kayıt sayfasından bir e-posta adresi ile kod göndermeyi deneyin
4. E-postanızı kontrol edin

## 5. Sorun Giderme

### E-posta gönderilmiyor:

- `app.email.enabled=true` olduğundan emin olun
- SMTP ayarlarının doğru olduğunu kontrol edin
- Gmail kullanıyorsanız uygulama şifresi kullandığınızdan emin olun
- Firewall/antivirus programlarının SMTP portunu engellemediğini kontrol edin

### "Authentication failed" hatası:

- Gmail: Uygulama şifresi kullandığınızdan emin olun (normal şifre çalışmaz)
- Outlook: 2 adımlı doğrulama aktifse uygulama şifresi gerekebilir
- Şifrelerde özel karakterler varsa dikkatli olun

### E-postalar spam klasörüne düşüyor:

- Gönderen e-posta adresini güvenilir olarak işaretleyin
- SPF, DKIM kayıtlarını kontrol edin (profesyonel kullanım için)

## Notlar

- **Güvenlik:** `application.properties` dosyasını Git'e commit etmeyin (şifreler içerir)
- **Production:** Production ortamında environment variables kullanın
- **Rate Limiting:** E-posta servisleri genellikle günlük gönderim limiti koyar
- **Test Modu:** `app.email.enabled=false` yaparsanız e-postalar console'a yazdırılır
