package com.kutuphane.otomasyon.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Uygulama genelindeki istisnaları yakalayıp, istemciye standart bir formatta
 * JSON yanıtı dönmek için kullanılan merkezi hata yöneticisi.
 */
@RestControllerAdvice // Uygulama genelindeki tüm controller'ları dinler.
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class); // Hataları konsola
                                                                                             // yazdırmak için Logger

    /**
     * Kaynak (Entity) bulunamadığında fırlatılan istisnayı yakalar.
     * 
     * @return HTTP 404 NOT_FOUND yanıtı döndürülür.
     */
    @ExceptionHandler(KaynakBulunamadiException.class)
    public ResponseEntity<Object> handleKaynakBulunamadiException(KaynakBulunamadiException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, request);
    }

    /**
     * İş mantığı (Business Logic) hatalarını yakalar (Örn: Limit aşımı, Stok yok).
     * 
     * @return HTTP 400 BAD_REQUEST yanıtı döndürülür.
     */
    @ExceptionHandler(IsKuraliException.class)
    public ResponseEntity<Object> handleIsKuraliException(IsKuraliException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Uygulama genelinde beklenmedik tüm diğer istisnaları (NullPointer vs.)
     * yakalar.
     * 
     * @return HTTP 500 INTERNAL_SERVER_ERROR yanıtı döndürülür.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest request) {
        log.error("Beklenmedik bir hata oluştu: ", ex); // Beklenmedik hataları detaylı logla
        return buildErrorResponse(ex, "Beklenmedik bir sunucu hatası oluştu.", HttpStatus.INTERNAL_SERVER_ERROR,
                request);
    }

    // İstisna objesini kullanarak hata mesajını alan yardımcı metot
    private ResponseEntity<Object> buildErrorResponse(Exception ex, HttpStatus status, WebRequest request) {
        return buildErrorResponse(ex, ex.getMessage(), status, request);
    }

    /**
     * Hata yanıtı için standart JSON gövdesini oluşturan temel yardımcı metot.
     * 
     * @return JSON gövdesi ve belirtilen HTTP durumu (status) ile yanıt döner.
     */
    private ResponseEntity<Object> buildErrorResponse(Exception ex, String message, HttpStatus status,
            WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now()); // Hatanın oluştuğu zaman
        body.put("status", status.value()); // HTTP durum kodu (Örn: 404, 500)
        body.put("error", status.getReasonPhrase()); // HTTP durum açıklaması (Örn: Not Found)
        body.put("message", message); // İstisnanın mesajı
        body.put("path", request.getDescription(false).replace("uri=", "")); // Hatanın oluştuğu API yolu

        return new ResponseEntity<>(body, status);
    }
}