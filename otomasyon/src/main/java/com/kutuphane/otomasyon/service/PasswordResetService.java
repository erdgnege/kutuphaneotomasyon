package com.kutuphane.otomasyon.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class PasswordResetService {

    @Autowired
    private EmailService emailService;

    // Email -> ResetCode (kod, oluşturulma zamanı)
    private final Map<String, ResetCode> resetCodes = new ConcurrentHashMap<>();
    private static final int CODE_EXPIRY_MINUTES = 15; // Kod 15 dakika geçerli

    // Kod bilgisi saklama sınıfı
    private static class ResetCode {
        String code;
        LocalDateTime createdAt;

        ResetCode(String code) {
            this.code = code;
            this.createdAt = LocalDateTime.now();
        }

        boolean isExpired() {
            return ChronoUnit.MINUTES.between(createdAt, LocalDateTime.now()) > CODE_EXPIRY_MINUTES;
        }
    }

    /**
     * Şifre sıfırlama kodu oluştur ve gönder
     */
    public String generateAndSendResetCode(String email) {
        // 6 haneli rastgele kod oluştur
        Random random = new Random();
        String code = String.format("%06d", random.nextInt(1000000));

        // Kodu sakla (varsa eski kodu üzerine yaz)
        resetCodes.put(email.toLowerCase(), new ResetCode(code));

        // E-posta gönder
        emailService.sendPasswordResetCode(email, code);

        return code;
    }

    /**
     * Şifre sıfırlama kodu doğrulaması yap
     */
    public boolean verifyResetCode(String email, String code) {
        String emailLower = email.toLowerCase();
        ResetCode storedCode = resetCodes.get(emailLower);

        if (storedCode == null) {
            return false; // Kod bulunamadı
        }

        if (storedCode.isExpired()) {
            resetCodes.remove(emailLower); // Süresi dolmuş kodu temizle
            return false; // Kod süresi dolmuş
        }

        if (storedCode.code.equals(code)) {
            // Doğrulama başarılı, kodu sil
            resetCodes.remove(emailLower);
            return true;
        }

        return false; // Kod yanlış
    }

    /**
     * Şifre sıfırlama kodu var mı kontrol et
     */
    public boolean hasResetCode(String email) {
        String emailLower = email.toLowerCase();
        ResetCode storedCode = resetCodes.get(emailLower);
        return storedCode != null && !storedCode.isExpired();
    }

    /**
     * Süresi dolmuş kodları temizle
     */
    public void cleanupExpiredCodes() {
        resetCodes.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}
