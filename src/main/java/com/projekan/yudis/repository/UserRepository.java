package com.projekan.yudis.repository;

import com.projekan.yudis.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Optional<User> findByAuthToken(String authToken);
    Optional<User> findByNoHp(String noHp);
}