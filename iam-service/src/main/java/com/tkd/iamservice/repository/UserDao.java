package com.tkd.iamservice.repository;

import com.tkd.iamservice.entity.IamUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDao extends JpaRepository<IamUser, Long> {
    Optional<IamUser> findByUsername(String username);

    Optional<IamUser> findByEmail(String email);

    Optional<IamUser> findByUsernameOrEmail(String username, String email);
}
