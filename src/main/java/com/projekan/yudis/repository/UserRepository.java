package com.projekan.yudis.repository;

import com.projekan.yudis.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    
    // Cari user berdasarkan username
    Optional<User> findByUsername(String username);

    // Cari user berdasarkan token
    Optional<User> findByAuthToken(String authToken);

    // === BARU: Cari user berdasarkan No HP ===
    Optional<User> findByNoHp(String noHp);
}