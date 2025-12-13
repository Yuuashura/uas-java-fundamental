package com.projekan.yudis.repository;

import com.projekan.yudis.model.Produk;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdukRepository extends JpaRepository<Produk, Integer> {
    
    // 1. Cari berdasarkan Nama (Sudah ada)
    Page<Produk> findByNamaProductContainingIgnoreCase(String keyword, Pageable pageable);

    // 2. Cari berdasarkan Kategori saja (BARU)
    Page<Produk> findByKategoriProduct(String kategori, Pageable pageable);

    // 3. Cari berdasarkan Nama DAN Kategori (BARU - Jika user mengetik nama sambil pilih kategori)
    Page<Produk> findByNamaProductContainingIgnoreCaseAndKategoriProduct(String keyword, String kategori, Pageable pageable);
}