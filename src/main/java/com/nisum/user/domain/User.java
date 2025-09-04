package com.nisum.user.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {
    @EqualsAndHashCode.Include
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    private String name;
    private String email;
    private String passwordHash;
    @Builder.Default
    private List<Phone> phones = new ArrayList<>();
    @Builder.Default
    private LocalDateTime created = LocalDateTime.now();
    @Builder.Default
    private LocalDateTime modified = LocalDateTime.now();
    @Builder.Default
    private LocalDateTime lastLogin = LocalDateTime.now();
    private String token;
    @Builder.Default
    private boolean isActive = true;

    public static User newUser(String name, String email, String passwordHash, List<Phone> phones) {
        var user = new User();
        user.id = UUID.randomUUID().toString();
        user.name = name;
        user.email = email;
        user.passwordHash = passwordHash;
        if (phones != null) user.phones = phones;
        var now = LocalDateTime.now();
        user.created = now;
        user.modified = now;
        user.lastLogin = now;
        user.isActive = true;
        return user;
    }

    public void setToken(String token) {
        this.token = token;
        var now = LocalDateTime.now();
        this.modified = now;
        this.lastLogin = now;
    }
}
