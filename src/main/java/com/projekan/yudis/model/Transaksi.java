package com.projekan.yudis.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "transaksi")
public class Transaksi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idTransaksi;

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = true)
    private User user;

    @Column(name = "nama_pembeli_snapshot")
    private String namaPembeliSnapshot;

    @Column(name = "no_hp_pembeli_snapshot")
    private String noHpPembeliSnapshot;

    @Column(name = "username_pembeli_snapshot")
    private String usernamePembeliSnapshot;

    @ManyToOne
    @JoinColumn(name = "id_provinsi", nullable = true)
    private Provinsi provinsi;

    @Column(name = "nama_provinsi_snapshot")
    private String namaProvinsiSnapshot;

    private LocalDateTime tanggalTransaksi = LocalDateTime.now();

    private Integer totalHargaBarang; // Total belanjaan saja
    private Integer totalOngkir; // Biaya ongkir
    private Integer grandTotal; // Total + Ongkir

    @Column(columnDefinition = "TEXT")
    private String alamatPengiriman; // Alamat lengkap

    @Enumerated(EnumType.STRING)
    private Status status;

    // Enum Status Transaksi
    public enum Status {
        PENDING, // Menunggu Bayar
        PAID, // Sudah Bayar (Dikemas)
        SENT, // Sedang Dikirim
        CANCEL, // Batal
        DONE // Selesai (Diterima User)
    }

    @OneToMany(mappedBy = "transaksi", fetch = FetchType.LAZY)
    @ToString.Exclude // Wajib ada biar gak error looping
    private List<TransaksiDetail> details;

}