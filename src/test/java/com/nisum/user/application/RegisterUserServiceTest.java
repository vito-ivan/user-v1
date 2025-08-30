package com.nisum.user.application;

import com.nisum.user.adapters.api.model.Phone;
import com.nisum.user.domain.User;
import com.nisum.user.domain.UserRepository;
import com.nisum.user.domain.exception.AppException;
import com.nisum.user.infrastructure.security.TokenService;
import org.json.JSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import com.nisum.user.TestUtils;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    @Mock
    private UserRepository repo;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private RegisterUserService service;

    @Test
    @DisplayName("Emits CONFLICT when the email already exists")
    void conflictWhenEmailExists() {
        var input = new RegisterUserUseCase.Input("John", "john@doe.com", "Secret22!");
        var existing = User.builder().id("u1").email(input.email()).build();

        when(repo.findByEmail(input.email())).thenReturn(Mono.just(existing));

        var result = service.execute(input, List.of());

        StepVerifier.create(result)
                .expectErrorSatisfies(ex -> {
                    assert ex instanceof AppException;
                    assert ((AppException) ex).getCode() == AppException.Code.CONFLICT;
                })
                .verify();
    }

    @Test
    @DisplayName("Creates user with token when the email does not exist")
    void createsUserWhenEmailNotExists() throws JSONException {
        var input = new RegisterUserUseCase.Input("John", "john@doe.com", "Secret22!");
        var phones = List.of(new Phone().number("1234567").cityCode("1").countryCode("57"));

        when(repo.findByEmail(input.email())).thenReturn(Mono.empty());
        when(tokenService.createFor(any(User.class))).thenReturn("jwt-123");

        when(repo.save(any(User.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        var result = service.execute(input, phones);
        var expectedJson = TestUtils.readResource("/expected/register-success.json");
        var actualJsonRef = new AtomicReference<String>();

        StepVerifier.create(result)
                .assertNext(out -> {
                    var u = out.created();

                    var actualMap = Map.of(
                            "email", u.getEmail(),
                            "token", u.getToken(),
                            "phones", u.getPhones() == null ? List.of() : u.getPhones().stream()
                                    .map(p -> Map.of(
                                            "number", p.getNumber(),
                                            "cityCode", p.getCityCode(),
                                            "countryCode", p.getCountryCode()
                                    ))
                                    .collect(Collectors.toList())
                    );
                    var actualJson = TestUtils.toJson(actualMap);
                    actualJsonRef.set(actualJson);

                    assert !u.getPasswordHash().equals(input.rawPassword());
                    assert u.getPasswordHash().startsWith("$2");
                })
                .expectComplete()
                .verify();

        JSONAssert.assertEquals(expectedJson, actualJsonRef.get(), JSONCompareMode.STRICT);
    }

    @Test
    @DisplayName("Accepts null phones as an empty list")
    void handlesNullPhonesAsEmptyList() throws JSONException {
        var input = new RegisterUserUseCase.Input("John", "john@doe.com", "Secret22!");

        when(repo.findByEmail(input.email())).thenReturn(Mono.empty());
        when(tokenService.createFor(any(User.class))).thenReturn("jwt-xyz");
        when(repo.save(any(User.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        var result = service.execute(input, null);
        var expectedJson = TestUtils.readResource("/expected/register-success-empty-phones.json");
        var actualJsonRef = new AtomicReference<String>();

        StepVerifier.create(result)
                .assertNext(out -> {
                    var u = out.created();

                    var actualMap = Map.of(
                            "email", u.getEmail(),
                            "token", u.getToken(),
                            "phones", List.of()
                    );
                    var actualJson = TestUtils.toJson(actualMap);
                    actualJsonRef.set(actualJson);
                })
                .expectComplete()
                .verify();

        JSONAssert.assertEquals(expectedJson, actualJsonRef.get(), JSONCompareMode.STRICT);

    }

}