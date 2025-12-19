package com.projekan.yudis.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "transaksi_detail")
public class TransaksiDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDetail;

    // Nempel ke Nota mana?
    @ManyToOne
    @JoinColumn(name = "id_transaksi")
    private Transaksi transaksi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_product", nullable = true)
    // ANOTASI UTAMA: Jika Produk dihapus (Action), set kolom FK menjadi NULL
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Produk produk;

    private Integer jumlah;

    // PENTING: Harga disimpan di sini agar jika harga produk naik, data transaksi
    // lama tidak berubah
    private Integer hargaSatuan;

    private Integer subtotal; // jumlah * hargaSatuan

    // --- SNAPSHOT DENGAN TIPE STRING (BASE64) ---
    @Column(nullable = true)
    private String namaProductSnapshot; // Nama produk saat dibeli

    // Gunakan Tipe STRING dan definisikan sebagai LONGTEXT untuk menyimpan Base64
    // yang panjang
    @Column(columnDefinition = "LONGTEXT", nullable = true)
    private String gambarSnapshot; // Gambar Base64 (String) saat dibeli
    @Column(name = "deskripsi_product_snapshot", columnDefinition = "TEXT")
    private String deskripsiProductSnapshot;

}