package com.projekan.yudis.model;

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

    // Barangnya apa?
    @ManyToOne
    @JoinColumn(name = "id_product")
    private Produk produk;

    private Integer jumlah;
    
    // PENTING: Harga disimpan di sini agar jika harga produk naik, data transaksi lama tidak berubah
    private Integer hargaSatuan; 
    
    private Integer subtotal; // jumlah * hargaSatuan
}