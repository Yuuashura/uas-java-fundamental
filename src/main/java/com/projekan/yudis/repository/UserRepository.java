package com.projekan.yudis.repository;

import com.projekan.yudis.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    // Cari user berdasarkan username (saat login)
    Optional<User> findByUsername(String username);

    // Cari user berdasarkan token (saat cek cookie)
    Optional<User> findByAuthToken(String authToken);
}