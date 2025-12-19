package com.projekan.yudis.repository;

import com.projekan.yudis.model.Provinsi;
import com.projekan.yudis.model.Transaksi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransaksiRepository extends JpaRepository<Transaksi, Integer> {
    
    List<Transaksi> findByUser(com.projekan.yudis.model.User user);

    // === TAMBAHKAN INI ===
    List<Transaksi> findByProvinsi(Provinsi provinsi);
}