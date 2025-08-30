package com.nisum.user.domain;

import reactor.core.publisher.Mono;

public interface UserRepository {
    Mono<User> findByEmail(String email);

    Mono<User> save(User user);
}
