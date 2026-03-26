package com.dth.fashionshop.modules.identity.service;

import com.dth.fashionshop.modules.identity.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // 1. Hút các cấu hình từ file application.properties (và .env) vào biến
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * HÀM SỐ 1: CHUYÊN ĐÚC THẺ (Tạo Token cho Frontend)
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();

        String role = user.getRoles().iterator().next().getName();
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * HÀM SỐ 2: SOI THẺ (Lấy Email ra từ Token)
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * HÀM SỐ 3: KIỂM ĐỊNH THẺ (Xem thẻ còn hạn và có đúng của người này không)
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        // Hợp lệ nếu: Email trong thẻ trùng với Email người đang giữ thẻ VÀ thẻ chưa hết hạn
        return (email.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // =========================================================================
    // CÁC HÀM TIỆN ÍCH HỖ TRỢ BÊN DƯỚI (Dùng nội bộ trong class)
    // =========================================================================

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey()) // Dùng chìa khóa để mở Token
                .build()
                .parseClaimsJws(token)
                .getBody(); // Lấy ra phần ruột (Payload)
    }

    // Biến chuỗi Base64 dài ngoằng trong .env thành một Đối tượng Key chuẩn của Java Cryptography
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Hàm tạo token cho chức năng quên mật khẩu (token chỉ tồn tại 5 phút)
    public String generateResetToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("purpose", "RESET_PASSWORD"); // Đóng dấu mộc đặc biệt

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 5))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Hàm này dùng để soi xem Token được cấp với mục đích gì
    public String extractPurpose(String token) {
        return extractClaim(token, claims -> claims.get("purpose", String.class));
    }
}