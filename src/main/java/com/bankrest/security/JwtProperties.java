package com.bankrest.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";
    private long expiration = 86400000;
}