package com.projekan.yudis.repository;

import com.projekan.yudis.model.Provinsi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProvinsiRepository extends JpaRepository<Provinsi, Integer> {
    // Kita pakai fungsi bawaan JpaRepository saja sudah cukup untuk CRUD standar
}