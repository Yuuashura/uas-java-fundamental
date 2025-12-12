package com.projekan.yudis.repository;

import com.projekan.yudis.model.Transaksi;
import com.projekan.yudis.model.TransaksiDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransaksiDetailRepository extends JpaRepository<TransaksiDetail, Integer> {
    // Cari detail berdasarkan Transaksi Induknya
    List<TransaksiDetail> findByTransaksi(Transaksi transaksi);
}