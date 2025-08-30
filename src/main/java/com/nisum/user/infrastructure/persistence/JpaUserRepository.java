package com.nisum.user.infrastructure.persistence;

import com.nisum.user.domain.Phone;
import com.nisum.user.domain.User;
import com.nisum.user.domain.UserRepository;
import com.nisum.user.domain.exception.AppException;
import com.nisum.user.infrastructure.persistence.jpa.SpringUserJpaRepository;
import com.nisum.user.infrastructure.persistence.jpa.UserEntity;
import com.nisum.user.infrastructure.persistence.jpa.UserPhoneEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;

@Repository
@Slf4j
@RequiredArgsConstructor
public class JpaUserRepository implements UserRepository {

    private final SpringUserJpaRepository jpa;

    @Override
    public Mono<User> findByEmail(final String email) {
        return Mono.fromCallable(() -> jpa.findByEmailIgnoreCase(email)
                        .map(this::toDomain)
                        .orElse(null))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(ex -> log.error("DB error finding user by email={}",
                        email, ex))
                .onErrorMap(ex -> AppException.of(
                        AppException.Code.DB_ERROR, "Database error while finding user", ex))
                .flatMap(user -> user == null ? Mono.empty() : Mono.just(user));
    }

    @Override
    public Mono<User> save(User user) {
        return Mono.fromCallable(() -> jpa.save(toEntity(user)))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(ex -> log.error("DB error saving user id={} email={}",
                        user.getId(), user.getEmail(), ex))
                .onErrorMap(ex -> AppException.of(
                        AppException.Code.DB_ERROR, "Database error while saving user", ex))
                .map(this::toDomain);
    }

    private User toDomain(UserEntity userEntity) {

        var phones = Optional.ofNullable(userEntity.getPhones())
                .map(phones0 -> phones0.stream()
                        .map(p -> new Phone(p.getNumber(), p.getCityCode(), p.getCountryCode()))
                        .toList()
                )
                .orElse(null);

        return User.builder()
                .id(userEntity.getId())
                .name(userEntity.getName())
                .email(userEntity.getEmail())
                .passwordHash(userEntity.getPasswordHash())
                .created(userEntity.getCreated())
                .modified(userEntity.getModified())
                .lastLogin(userEntity.getLastLogin())
                .token(userEntity.getToken())
                .isActive(userEntity.isActive())
                .phones(phones)
                .build();
    }

    private UserEntity toEntity(final User user) {

        var userEntityBuilder = UserEntity.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .created(user.getCreated())
                .modified(user.getModified())
                .lastLogin(user.getLastLogin())
                .token(user.getToken())
                .isActive(user.isActive());

        var userEntity = userEntityBuilder.build();

        var phoneEntityList = Optional.ofNullable(user.getPhones())
                .map(phones -> phones.stream()
                        .map(phone -> UserPhoneEntity.builder()
                                .user(userEntity)
                                .number(phone.getNumber())
                                .cityCode(phone.getCityCode())
                                .countryCode(phone.getCountryCode())
                                .build()
                        )
                        .toList())
                .orElse(null);

        userEntity.setPhones(phoneEntityList);
        return userEntity;
    }
}
