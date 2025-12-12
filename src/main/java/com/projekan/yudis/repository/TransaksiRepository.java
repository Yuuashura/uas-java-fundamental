package com.projekan.yudis.repository;

import com.projekan.yudis.model.Transaksi;
import com.projekan.yudis.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransaksiRepository extends JpaRepository<Transaksi, Integer> {
    List<Transaksi> findByUser(User user); // Riwayat belanja user
}