package com.projekan.yudis.repository;

import com.projekan.yudis.model.Keranjang;
import com.projekan.yudis.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KeranjangRepository extends JpaRepository<Keranjang, Integer> {
    List<Keranjang> findByUser(User user);
    List<Keranjang> findByProduk_IdProduct(Integer idProduk);
}