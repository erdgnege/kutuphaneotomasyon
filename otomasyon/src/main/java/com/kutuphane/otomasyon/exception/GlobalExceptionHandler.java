package com.kutuphane.otomasyon.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // KutuphaneHatasi fırlatıldığında burası devreye girer
    @ExceptionHandler(KutuphaneHatasi.class)
    public ResponseEntity<Map<String, Object>> hataMesajiniGoster(KutuphaneHatasi ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", ex.getMessage());
        response.put("error", "KutuphaneHatasi");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Veritabanı constraint ihlalleri için özel handler
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> veritabaniHatasi(DataIntegrityViolationException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        
        String hataMesaji = ex.getMessage();
        String kullaniciMesaji;
        
        // Email unique constraint ihlali
        if (hataMesaji != null && hataMesaji.contains("email") && hataMesaji.contains("UNIQUE")) {
            kullaniciMesaji = "Bu e-posta adresi zaten kullanılıyor. Lütfen farklı bir e-posta adresi deneyin.";
        }
        // Telefon unique constraint ihlali
        else if (hataMesaji != null && hataMesaji.contains("telefon") && hataMesaji.contains("UNIQUE")) {
            kullaniciMesaji = "Bu telefon numarası zaten kullanılıyor. Lütfen farklı bir telefon numarası deneyin.";
        }
        // Üye numarası unique constraint ihlali
        else if (hataMesaji != null && (hataMesaji.contains("uyeNo") || hataMesaji.contains("uye_no")) && hataMesaji.contains("UNIQUE")) {
            kullaniciMesaji = "Bu üye numarası zaten kullanılıyor. Lütfen farklı bir üye numarası seçin.";
        }
        // Genel unique constraint ihlali
        else if (hataMesaji != null && hataMesaji.contains("UNIQUE")) {
            kullaniciMesaji = "Bu bilgi zaten kullanılıyor. Lütfen farklı bir değer deneyin.";
        }
        // Foreign key constraint ihlali
        else if (hataMesaji != null && hataMesaji.contains("FOREIGN KEY")) {
            kullaniciMesaji = "İlgili kayıt bulunamadı. Lütfen bilgilerinizi kontrol edin.";
        }
        // Genel veritabanı hatası
        else {
            kullaniciMesaji = "Veritabanı hatası: " + (hataMesaji != null ? hataMesaji : "Bilinmeyen hata");
        }
        
        response.put("message", kullaniciMesaji);
        response.put("error", "DataIntegrityViolationException");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // Genel exception handler
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> genelHata(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Bir hata oluştu: " + ex.getMessage());
        response.put("error", ex.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}