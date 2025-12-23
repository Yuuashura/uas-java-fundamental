package com.projekan.yudis.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "keranjang")
public class Keranjang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idKeranjang;
    @ManyToOne
    @JoinColumn(name = "id_user")
    private User user;
    @ManyToOne
    @JoinColumn(name = "id_product")
    private Produk produk;

    private Integer jumlah;

    private Integer harga;

    private LocalDateTime tanggalKeranjang = LocalDateTime.now();
}