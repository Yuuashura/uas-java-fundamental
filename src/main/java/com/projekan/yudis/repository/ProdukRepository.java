package com.projekan.yudis.repository;

import com.projekan.yudis.model.Produk;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdukRepository extends JpaRepository<Produk, Integer> {
    // Cari berdasarkan nama dengan fitur Pageable
    Page<Produk> findByNamaProductContainingIgnoreCase(String keyword, Pageable pageable);
        List<Produk> findByNamaProductContainingIgnoreCase(String keyword, Sort sort);

}