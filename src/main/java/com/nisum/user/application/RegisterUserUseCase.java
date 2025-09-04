package com.nisum.user.application;

import com.nisum.user.domain.User;

public interface RegisterUserUseCase {
    record Input(String name, String email, String rawPassword) {
    }

    record Output(User created) {
    }
}
