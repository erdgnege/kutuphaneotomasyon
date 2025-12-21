package com.kutuphane.otomasyon.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class EmailVerificationService {
    
    @Autowired
    private EmailService emailService;
    
    // Email -> VerificationCode (kod, oluşturulma zamanı)
    private final Map<String, VerificationCode> verificationCodes = new ConcurrentHashMap<>();
    private static final int CODE_EXPIRY_MINUTES = 10; // Kod 10 dakika geçerli
    
    // Kod bilgisi saklama sınıfı
    private static class VerificationCode {
        String code;
        LocalDateTime createdAt;
        
        VerificationCode(String code) {
            this.code = code;
            this.createdAt = LocalDateTime.now();
        }
        
        boolean isExpired() {
            return ChronoUnit.MINUTES.between(createdAt, LocalDateTime.now()) > CODE_EXPIRY_MINUTES;
        }
    }
    
    /**
     * E-posta için doğrulama kodu oluştur ve gönder
     */
    public String generateAndSendCode(String email) {
        // 6 haneli rastgele kod oluştur
        Random random = new Random();
        String code = String.format("%06d", random.nextInt(1000000));
        
        // Kodu sakla
        verificationCodes.put(email.toLowerCase(), new VerificationCode(code));
        
        // E-posta gönder
        emailService.sendVerificationCode(email, code);
        
        return code;
    }
    
    /**
     * E-posta ve kod doğrulaması yap
     */
    public boolean verifyCode(String email, String code) {
        String emailLower = email.toLowerCase();
        VerificationCode storedCode = verificationCodes.get(emailLower);
        
        if (storedCode == null) {
            return false; // Kod bulunamadı
        }
        
        if (storedCode.isExpired()) {
            verificationCodes.remove(emailLower); // Süresi dolmuş kodu temizle
            return false; // Kod süresi dolmuş
        }
        
        if (storedCode.code.equals(code)) {
            // Doğrulama başarılı, kodu silme (kayıt işlemi tamamlanana kadar tut)
            // Kod kayıt başarılı olduğunda silinecek
            return true;
        }
        
        return false; // Kod yanlış
    }
    
    /**
     * E-posta için kod var mı kontrol et
     */
    public boolean hasCode(String email) {
        String emailLower = email.toLowerCase();
        VerificationCode storedCode = verificationCodes.get(emailLower);
        return storedCode != null && !storedCode.isExpired();
    }
    
    /**
     * E-posta için doğrulanmış kodu temizle (kayıt başarılı olduğunda çağrılır)
     */
    public void clearCode(String email) {
        verificationCodes.remove(email.toLowerCase());
    }
    
    /**
     * Süresi dolmuş kodları temizle (periodic cleanup için)
     */
    public void cleanupExpiredCodes() {
        verificationCodes.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}

