package com.kutuphane.otomasyon.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final String fromEmail;
    private final boolean emailEnabled;
    
    // Constructor injection - eğer JavaMailSender bean'i yoksa null olabilir
    public EmailService(@Autowired(required = false) JavaMailSender mailSender,
                       @Value("${spring.mail.username:}") String fromEmail,
                       @Value("${app.email.enabled:false}") boolean emailEnabled) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail != null && !fromEmail.isEmpty() ? fromEmail : "noreply@kutuphane.com";
        this.emailEnabled = emailEnabled;
    }
    
    /**
     * Doğrulama kodu e-postası gönder
     */
    public void sendVerificationCode(String toEmail, String code) {
        // E-posta servisi aktif değilse veya mailSender yoksa console'a yazdır
        if (!emailEnabled) {
            System.out.println("========================================");
            System.out.println("E-POSTA DOĞRULAMA KODU (E-posta servisi aktif değil)");
            System.out.println("E-posta: " + toEmail);
            System.out.println("Doğrulama Kodu: " + code);
            System.out.println("Bu kod 10 dakika geçerlidir.");
            System.out.println("application.properties'te app.email.enabled=true yaparak e-posta göndermeyi aktif edebilirsiniz.");
            System.out.println("========================================");
            return;
        }
        
        if (mailSender == null) {
            System.out.println("========================================");
            System.out.println("E-POSTA DOĞRULAMA KODU (JavaMailSender bulunamadı)");
            System.out.println("E-posta: " + toEmail);
            System.out.println("Doğrulama Kodu: " + code);
            System.out.println("Bu kod 10 dakika geçerlidir.");
            System.out.println("SMTP ayarlarınızı kontrol edin.");
            System.out.println("========================================");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Kütüphane Otomasyon - E-posta Doğrulama Kodu");
            message.setText(
                "Merhaba,\n\n" +
                "Kütüphane otomasyon sistemine kayıt olmak için e-posta doğrulama kodunuz:\n\n" +
                "Doğrulama Kodu: " + code + "\n\n" +
                "Bu kod 10 dakika geçerlidir.\n\n" +
                "Eğer bu işlemi siz yapmadıysanız, bu e-postayı görmezden gelebilirsiniz.\n\n" +
                "Saygılarımızla,\n" +
                "Kütüphane Otomasyon Sistemi"
            );
            
            mailSender.send(message);
            System.out.println("Doğrulama kodu e-postası gönderildi: " + toEmail);
            
        } catch (Exception e) {
            System.err.println("E-posta gönderme hatası: " + e.getMessage());
            e.printStackTrace();
            // Hata durumunda console'a yazdır
            System.out.println("========================================");
            System.out.println("E-POSTA DOĞRULAMA KODU (E-posta gönderilemedi, hata oluştu)");
            System.out.println("E-posta: " + toEmail);
            System.out.println("Doğrulama Kodu: " + code);
            System.out.println("Bu kod 10 dakika geçerlidir.");
            System.out.println("Hata: " + e.getMessage());
            System.out.println("========================================");
        }
    }
    
    /**
     * Şifre sıfırlama kodu e-postası gönder
     */
    public void sendPasswordResetCode(String toEmail, String code) {
        // E-posta servisi aktif değilse veya mailSender yoksa console'a yazdır
        if (!emailEnabled) {
            System.out.println("========================================");
            System.out.println("ŞİFRE SIFIRLAMA KODU (E-posta servisi aktif değil)");
            System.out.println("E-posta: " + toEmail);
            System.out.println("Şifre Sıfırlama Kodu: " + code);
            System.out.println("Bu kod 15 dakika geçerlidir.");
            System.out.println("application.properties'te app.email.enabled=true yaparak e-posta göndermeyi aktif edebilirsiniz.");
            System.out.println("========================================");
            return;
        }
        
        if (mailSender == null) {
            System.out.println("========================================");
            System.out.println("ŞİFRE SIFIRLAMA KODU (JavaMailSender bulunamadı)");
            System.out.println("E-posta: " + toEmail);
            System.out.println("Şifre Sıfırlama Kodu: " + code);
            System.out.println("Bu kod 15 dakika geçerlidir.");
            System.out.println("SMTP ayarlarınızı kontrol edin.");
            System.out.println("========================================");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Kütüphane Otomasyon - Şifre Sıfırlama Kodu");
            message.setText(
                "Merhaba,\n\n" +
                "Şifre sıfırlama talebiniz için doğrulama kodunuz:\n\n" +
                "Şifre Sıfırlama Kodu: " + code + "\n\n" +
                "Bu kod 15 dakika geçerlidir.\n\n" +
                "Eğer bu işlemi siz yapmadıysanız, bu e-postayı görmezden gelebilirsiniz.\n\n" +
                "Saygılarımızla,\n" +
                "Kütüphane Otomasyon Sistemi"
            );
            
            mailSender.send(message);
            System.out.println("Şifre sıfırlama kodu e-postası gönderildi: " + toEmail);
            
        } catch (Exception e) {
            System.err.println("E-posta gönderme hatası: " + e.getMessage());
            e.printStackTrace();
            // Hata durumunda console'a yazdır
            System.out.println("========================================");
            System.out.println("ŞİFRE SIFIRLAMA KODU (E-posta gönderilemedi, hata oluştu)");
            System.out.println("E-posta: " + toEmail);
            System.out.println("Şifre Sıfırlama Kodu: " + code);
            System.out.println("Bu kod 15 dakika geçerlidir.");
            System.out.println("Hata: " + e.getMessage());
            System.out.println("========================================");
        }
    }
}

