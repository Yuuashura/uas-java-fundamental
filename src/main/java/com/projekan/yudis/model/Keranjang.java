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

    // Relasi: Banyak keranjang dimiliki satu User
    @ManyToOne
    @JoinColumn(name = "id_user")
    private User user;

    // Relasi: Banyak item keranjang berisi satu jenis Produk
    @ManyToOne
    @JoinColumn(name = "id_product")
    private Produk produk;

    private Integer jumlah;

    // Harga per item saat dimasukkan (opsional, tapi bagus buat history)
    private Integer harga;

    // Mencatat kapan dimasukkan
    private LocalDateTime tanggalKeranjang = LocalDateTime.now();
}