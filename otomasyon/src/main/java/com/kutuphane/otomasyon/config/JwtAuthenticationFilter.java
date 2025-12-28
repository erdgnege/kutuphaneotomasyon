package com.kutuphane.otomasyon.config;

import com.kutuphane.otomasyon.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 1. Header'dan "Authorization" kısmını çekiyoruz kanka
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 2. Token yoksa veya Bearer ile başlamıyorsa geç gitsin, bir sonrakine bak
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. "Bearer " kısmını atıp gerçek token'ı alıyoruz
        jwt = authHeader.substring(7);
        userEmail = jwtService.extractUsername(jwt); // JwtService'i burada kullanıyoruz adaş

        // 4. Username varsa ve kullanıcı henüz authenticate edilmemişse (SecurityContext boşsa)
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Admin kontrolü: Eğer username "admin" ise özel işlem yap
            if ("admin".equals(userEmail)) {
                // Admin için özel UserDetails oluştur
                UserDetails adminUser = User.builder()
                        .username("admin")
                        .password("")
                        .authorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .accountExpired(false)
                        .accountLocked(false)
                        .credentialsExpired(false)
                        .disabled(false)
                        .build();

                // Token geçerli mi kontrol et (admin için özel kontrol)
                // Admin token'ı AdminController'da oluşturulduğu için burada sadece kontrol ediyoruz
                if (jwtService.isTokenValid(jwt, adminUser)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            adminUser,
                            null,
                            adminUser.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } else {
                // Normal kullanıcı (UYE/PERSONEL) için işlem
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // 5. Token geçerli mi kontrol et
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities() // Roller (UYE/PERSONEL) burada atanıyor brom
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 6. Artık SecurityContext'e bu arkadaşı ekliyoruz, kapılar ona açılıyor
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }

        // İşlem bitince yolu açıyoruz, devam etsin
        filterChain.doFilter(request, response);
    }
}