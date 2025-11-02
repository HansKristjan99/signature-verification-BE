package com.vericode.data.UserSession;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vericode.data.User.UserEntity;

@Repository
public interface UserSessionRepository extends CrudRepository<UserSessionEntity, Long> {
    Optional<UserSessionEntity> findBySessionToken(String sessionToken);

    void deleteByUser(UserEntity user);

    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
