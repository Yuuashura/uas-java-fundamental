package com.projekan.yudis.repository;

import com.projekan.yudis.model.Produk;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdukRepository extends JpaRepository<Produk, Integer> {
    Page<Produk> findByNamaProductContainingIgnoreCase(String keyword, Pageable pageable);
    Page<Produk> findByKategoriProduct(String kategori, Pageable pageable);
    Page<Produk> findByNamaProductContainingIgnoreCaseAndKategoriProduct(String keyword, String kategori, Pageable pageable);
}