package com.kutuphane.otomasyon.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Bu hata fırlatıldığında HTTP 404 (Not Found) döner
@ResponseStatus(HttpStatus.NOT_FOUND)
public class KutuphaneHatasi extends RuntimeException {

    public KutuphaneHatasi(String mesaj) {
        super(mesaj);
    }
}