package com.nisum.user.infrastructure.persistence;

import com.nisum.user.domain.User;
import com.nisum.user.domain.exception.AppException;
import com.nisum.user.infrastructure.persistence.jpa.SpringUserJpaRepository;
import com.nisum.user.infrastructure.persistence.jpa.UserEntity;
import com.nisum.user.infrastructure.persistence.jpa.UserPhoneEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaUserRepositoryTest {

    @Mock
    private SpringUserJpaRepository springRepo;

    @InjectMocks
    private JpaUserRepository adapter;

    @Nested
    class FindByEmail {
        @Test
        @DisplayName("Returns User when Spring repo finds by email")
        void returnsUserWhenFound() {
            var entity = sampleEntity("john@doe.com");
            when(springRepo.findByEmailIgnoreCase("john@doe.com")).thenReturn(Optional.of(entity));

            StepVerifier.create(adapter.findByEmail("john@doe.com"))
                    .assertNext(u -> {
                        assertEquals(entity.getId(), u.getId());
                        assertEquals(entity.getEmail(), u.getEmail());
                        assertEquals(1, u.getPhones().size());
                        assertEquals(entity.getPhones().get(0).getNumber(), u.getPhones().get(0).getNumber());
                    })
                    .expectComplete()
                    .verify();
        }

        @Test
        @DisplayName("Returns empty when Spring repo returns empty")
        void returnsEmptyWhenNotFound() {
            when(springRepo.findByEmailIgnoreCase("none@doe.com")).thenReturn(Optional.empty());

            StepVerifier.create(adapter.findByEmail("none@doe.com"))
                    .expectComplete()
                    .verify();
        }

        @Test
        @DisplayName("Maps underlying exception to AppException.DB_ERROR on findByEmail")
        void mapsErrorOnFindByEmail() {
            when(springRepo.findByEmailIgnoreCase("john@doe.com")).thenThrow(new RuntimeException("DB down"));

            StepVerifier.create(adapter.findByEmail("john@doe.com"))
                    .expectErrorSatisfies(err -> {
                        assertEquals(AppException.class, err.getClass());
                        assertEquals(AppException.Code.DB_ERROR, ((AppException) err).getCode());
                    })
                    .verify();
        }
    }

    @Nested
    class Save {
        @Test
        @DisplayName("Returns mapped User when save succeeds")
        void returnsUserOnSave() {
            var user = User.newUser("John", "john@doe.com", "$2hash", List.of());
            var entity = sampleEntity(user.getEmail());
            entity.setId(user.getId());
            when(springRepo.save(any(UserEntity.class))).thenReturn(entity);

            StepVerifier.create(adapter.save(user))
                    .assertNext(saved -> {
                        assertEquals(user.getId(), saved.getId());
                        assertEquals(user.getEmail(), saved.getEmail());
                    })
                    .expectComplete()
                    .verify();
        }

        @Test
        @DisplayName("Maps underlying exception to AppException.DB_ERROR on save")
        void mapsErrorOnSave() {
            var user = User.newUser("John", "john@doe.com", "$2hash", List.of());
            when(springRepo.save(any(UserEntity.class))).thenThrow(new RuntimeException("Constraint violation"));

            StepVerifier.create(adapter.save(user))
                    .expectErrorSatisfies(err -> {
                        assertEquals(AppException.class, err.getClass());
                        assertEquals(AppException.Code.DB_ERROR, ((AppException) err).getCode());
                    })
                    .verify();
        }
    }

    private static UserEntity sampleEntity(String email) {
        var now = LocalDateTime.now();
        var entity = UserEntity.builder()
                .id(UUID.randomUUID().toString())
                .name("John")
                .email(email)
                .passwordHash("$2hash")
                .created(now)
                .modified(now)
                .lastLogin(now)
                .token("jwt-123")
                .isActive(true)
                .build();
        var phone = UserPhoneEntity.builder()
                .user(entity)
                .number("1234567")
                .cityCode("1")
                .countryCode("57")
                .build();
        entity.setPhones(List.of(phone));
        return entity;
    }
}