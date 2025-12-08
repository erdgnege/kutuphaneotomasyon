package com.kutuphane.otomasyon.exception;

/**
 * İş mantığı kurallarının (limit aşımı, stok durumu vb.) ihlal edilmesi
 * durumunda fırlatılacak özel exception.
 */
// RuntimeException'dan türetilmesi, bu istisnanın Unchecked (kontrolsüz)
// olmasını sağlar.
// Yani metot imzalarında 'throws' belirtme zorunluluğu yoktur.
public class IsKuraliException extends RuntimeException {

    /**
     * Varsayılan Seri Kimliği (Serialization UID).
     */
    private static final long serialVersionUID = 1L;

    /**
     * Bu istisnayı, hatanın açıklayıcı mesajıyla birlikte oluşturur.
     * 
     * @param message Hata mesajı (Örn: "Üye ödünç limitini aştı.").
     */
    public IsKuraliException(String message) {
        super(message);
    }
}