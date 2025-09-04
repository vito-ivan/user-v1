package com.nisum.user.application;

import com.nisum.user.domain.Phone;
import com.nisum.user.domain.User;
import com.nisum.user.domain.UserRepository;
import com.nisum.user.domain.exception.AppException;
import com.nisum.user.infrastructure.security.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepository repo;
    private final TokenService tokenService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Mono<Output> execute(Input input, List<com.nisum.user.adapters.api.model.Phone> phones) {
        return repo.findByEmail(input.email())
                .flatMap(existing -> Mono.<Output>error(AppException.of(AppException.Code.CONFLICT,
                        "User with email already exists")
                ))
                .switchIfEmpty(Mono.defer(() -> {

                    var phoneList = (phones == null ? List.<com.nisum.user.adapters.api.model.Phone>of() : phones)
                            .stream()
                            .map(phone -> new Phone(phone.getNumber(), phone.getCityCode(),
                                    phone.getCountryCode()))
                            .toList();

                    var user = User.newUser(
                            input.name(),
                            input.email(),
                            passwordEncoder.encode(input.rawPassword()),
                            phoneList
                    );
                    var token = tokenService.createFor(user);
                    user.setToken(token);

                    return repo.save(user).map(Output::new);
                }));
    }
}
