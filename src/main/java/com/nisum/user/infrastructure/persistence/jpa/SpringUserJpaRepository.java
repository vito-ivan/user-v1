package com.nisum.user.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringUserJpaRepository extends JpaRepository<UserEntity, String> {
  Optional<UserEntity> findByEmailIgnoreCase(String email);
}
