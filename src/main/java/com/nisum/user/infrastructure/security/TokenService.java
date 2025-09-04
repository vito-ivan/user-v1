package com.nisum.user.infrastructure.security;

import com.nisum.user.domain.User;

public interface TokenService {
    String createFor(User user);
}
