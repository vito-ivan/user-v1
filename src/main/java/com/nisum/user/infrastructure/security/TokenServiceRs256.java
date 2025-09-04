package com.nisum.user.infrastructure.security;

import com.nisum.user.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class TokenServiceRs256 implements TokenService {

    private final JwtEncoder encoder;
    private final Duration ttl;
    private final String issuer;

    public TokenServiceRs256(JwtEncoder encoder,
                             @Value("${jwt.ttl-minutes:1440}") long ttlMinutes,
                             @Value("${jwt.issuer:registration}") String issuer) {
        this.encoder = encoder;
        this.ttl = Duration.ofMinutes(ttlMinutes);
        this.issuer = issuer;
    }

    @Override
    public String createFor(User user) {
        var now = Instant.now();
        var claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                .subject(user.getId())
                .id(UUID.randomUUID().toString())
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .build();
        return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
