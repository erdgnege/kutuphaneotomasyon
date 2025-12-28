package com.kutuphane.otomasyon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    // İstersen buraya "role", "userId" gibi alanlar da ekleyebiliriz ileride
}
