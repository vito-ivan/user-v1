package com.nisum.user.infrastructure.entrypoints.rest;

import com.nisum.user.adapters.api.UsersApiDelegate;
import com.nisum.user.adapters.api.model.UserRequest;
import com.nisum.user.adapters.api.model.UserResponse;
import com.nisum.user.application.RegisterUserService;
import com.nisum.user.application.RegisterUserUseCase;
import com.nisum.user.infrastructure.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UsersApiDelegateImpl implements UsersApiDelegate {

    private final RegisterUserService registerUser;

    @Override
    public Mono<ResponseEntity<UserResponse>> registerUser(Mono<UserRequest> userRequest, ServerWebExchange exchange) {
        return userRequest
                .flatMap(req -> registerUser.execute(
                        new RegisterUserUseCase.Input(req.getName(), req.getEmail(), req.getPassword()),
                        req.getPhones())
                )
                .map(out -> new ResponseEntity<>(UserMapper.toResponse(out.created()), HttpStatus.CREATED));
    }
}

