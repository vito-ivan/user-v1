package com.nisum.user.infrastructure.entrypoints.rest;

import com.nisum.user.adapters.api.model.Phone;
import com.nisum.user.adapters.api.model.UserRequest;
import com.nisum.user.application.RegisterUserService;
import com.nisum.user.application.RegisterUserUseCase;
import com.nisum.user.domain.User;
import com.nisum.user.domain.exception.AppException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsersApiDelegateImplTest {

    @Mock
    private RegisterUserService registerUserService;

    @Mock
    private ServerWebExchange exchange;

    @InjectMocks
    private UsersApiDelegateImpl controller;

    @Test
    @DisplayName("Returns HTTP 201 Created with mapped user body on success")
    void registerUser_success() {
        var req = new UserRequest()
                .name("John")
                .email("john@doe.com")
                .password("Secret22!")
                .phones(List.of(new Phone().number("1234567").cityCode("1").countryCode("57")));

        var domainUser = User.builder()
                .id(UUID.randomUUID().toString())
                .name("John")
                .email("john@doe.com")
                .passwordHash("$2hash")
                .phones(List.of(new com.nisum.user.domain.Phone("1234567", "1", "57")))
                .token("jwt-123")
                .created(LocalDateTime.now())
                .modified(LocalDateTime.now())
                .lastLogin(LocalDateTime.now())
                .isActive(true)
                .build();

        when(registerUserService.execute(any(RegisterUserUseCase.Input.class), any(List.class)))
                .thenReturn(Mono.just(new RegisterUserUseCase.Output(domainUser)));

        var responseMono = controller.registerUser(Mono.just(req), exchange);

        StepVerifier.create(responseMono)
                .assertNext(resp -> {
                    assertEquals(HttpStatus.CREATED, resp.getStatusCode());
                    var body = resp.getBody();
                    assertEquals(domainUser.getId(), body.getId().toString());
                    assertEquals("John", body.getName());
                    assertEquals("john@doe.com", body.getEmail());
                    assertEquals("jwt-123", body.getToken());
                    assertEquals(true, body.getIsActive());
                    assertEquals(1, body.getPhones().size());
                    assertEquals("1234567", body.getPhones().get(0).getNumber());
                })
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Propagates AppException from service on failure")
    void registerUser_error() {
        var req = new UserRequest()
                .name("John")
                .email("john@doe.com")
                .password("Secret22!")
                .phones(List.of(new Phone().number("1234567").cityCode("1").countryCode("57")));

        var ex = AppException.of(AppException.Code.CONFLICT, "User with email already exists");
        when(registerUserService.execute(any(RegisterUserUseCase.Input.class), any(List.class)))
                .thenReturn(Mono.error(ex));

        var responseMono = controller.registerUser(Mono.just(req), exchange);

        StepVerifier.create(responseMono)
                .expectErrorSatisfies(err -> {
                    assertEquals(AppException.class, err.getClass());
                    assertEquals("User with email already exists", err.getMessage());
                })
                .verify();
    }
}