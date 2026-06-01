package com.demo.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.List;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@Component
public class JwtUtils {
  private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

  @Value("${auth.app.jwtSecret}")
  private String jwtSecret;

  @Value("${auth.app.jwtExpirationMs}")
  private int jwtExpirationMs;

  private Key key() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
  }

  public String generateJwtToken(Authentication authentication) {
    Object principal = authentication.getPrincipal();
    String username = authentication.getName();

    // Extract user ID and roles from the principal
    String userId = null;
    if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
      // Try to get user ID from the principal if it has a getId method
      try {
        java.lang.reflect.Method getIdMethod = principal.getClass().getMethod("getId");
        Object id = getIdMethod.invoke(principal);
        userId = id != null ? id.toString() : null;
      } catch (Exception ignored) {
      }
    }

    return Jwts.builder().subject(username)
            .claim("userId", userId)
            .claim("roles", authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toArray()).issuedAt(new Date()).expiration(new Date((new Date()).getTime() + jwtExpirationMs))
        .signWith(key(), SignatureAlgorithm.HS256)
        .compact();
  }

  public String getUserNameFromJwtToken(String token) {
    return Jwts.parser().verifyWith((SecretKey) key()).build()
               .parseSignedClaims(token).getPayload().getSubject();
  }

  public Long getUserIdFromJwtToken(String token) {
    Claims claims = Jwts.parser().verifyWith((SecretKey) key()).build()
                       .parseSignedClaims(token).getPayload();
    return Long.valueOf(claims.get("userId", String.class));
  }

  @SuppressWarnings("unchecked")
  public List<String> getRolesFromJwtToken(String token) {
    Claims claims = Jwts.parser().verifyWith((SecretKey) key()).build()
                       .parseSignedClaims(token).getPayload();
    return (List<String>) claims.get("roles", List.class);
  }

  public boolean validateJwtToken(String authToken) {
    try {
      Jwts.parser().verifyWith((SecretKey) key()).build().parse(authToken);
      return true;
    } catch (MalformedJwtException e) {
      logger.error("Invalid JWT token: {}", e.getMessage());
    } catch (ExpiredJwtException e) {
      logger.error("JWT token is expired: {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      logger.error("JWT token is unsupported: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      logger.error("JWT claims string is empty: {}", e.getMessage());
    }

    return false;
  }
}
