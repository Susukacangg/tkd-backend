package com.tkd.iamservice.repository;

import com.tkd.iamservice.entity.IamUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDao extends JpaRepository<IamUserEntity, Long> {
    Optional<IamUserEntity> findByUsername(String username);

    Optional<IamUserEntity> findByEmail(String email);

    Optional<IamUserEntity> findByUsernameOrEmail(String username, String email);

    Optional<IamUserEntity> findByIdEquals(Long id);
}
