package com.kutuphane.otomasyon.exception;

/**
 * Veritabanında bir kaynak (kitap, kullanıcı vb.) arandığında bulunamaması
 * durumunda
 * fırlatılacak olan özel exception sınıfı.
 */
// RuntimeException'dan türetilmesi, bu istisnanın Unchecked (kontrolsüz)
// olmasını sağlar.
// Bu exception, GlobalExceptionHandler tarafından HTTP 404 olarak işlenir.
public class KaynakBulunamadiException extends RuntimeException {

    /**
     * Varsayılan Seri Kimliği (Serialization UID).
     */
    private static final long serialVersionUID = 1L;

    /**
     * Bu istisnayı, hatanın açıklayıcı mesajıyla birlikte oluşturur.
     * 
     * @param message Hata mesajı (Örn: "ID: 5 numaralı kitap bulunamadı.").
     */
    public KaynakBulunamadiException(String message) {
        super(message);
    }
}