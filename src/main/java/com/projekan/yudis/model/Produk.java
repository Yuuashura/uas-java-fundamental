package com.projekan.yudis.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "produk")
public class Produk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idProduct;

    @Column(nullable = false)
    private String namaProduct;

    private String kategoriProduct;

    private Integer harga;

    private Integer stock;

    @Lob // Untuk menyimpan Base64 String yang panjang
    @Column(columnDefinition = "LONGTEXT")
    private String gambarProduct;

    @Column(columnDefinition = "TEXT")
    private String deskripsiProduct;
}