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

    // Relasi ke User (Bisa NULL jika user dihapus, karena data sudah dibackup di
    // snapshot)
    @ManyToOne
    @JoinColumn(name = "id_user", nullable = true)
    private User user;

    // === SNAPSHOT DATA PEMBELI (BARU) ===
    // Data ini diisi saat checkout. Jika User dihapus, data ini tetap tampil di
    // Invoice.
    @Column(name = "nama_pembeli_snapshot")
    private String namaPembeliSnapshot;

    @Column(name = "no_hp_pembeli_snapshot")
    private String noHpPembeliSnapshot;

    @Column(name = "username_pembeli_snapshot")
    private String usernamePembeliSnapshot;

    // Kirim ke mana? (Untuk ambil data ongkir)
    @ManyToOne
    @JoinColumn(name = "id_provinsi", nullable = true)
    private Provinsi provinsi;

    // === TAMBAHAN SNAPSHOT PROVINSI ===
    @Column(name = "nama_provinsi_snapshot")
    private String namaProvinsiSnapshot;

    private LocalDateTime tanggalTransaksi = LocalDateTime.now();

    private Integer totalHargaBarang; // Total belanjaan saja
    private Integer totalOngkir; // Biaya ongkir
    private Integer grandTotal; // Total + Ongkir

    @Column(columnDefinition = "TEXT")
    private String alamatPengiriman; // Alamat lengkap jalan/rt/rw

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