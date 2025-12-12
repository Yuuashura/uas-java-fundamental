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

    // Siapa yang beli?
    @ManyToOne
    @JoinColumn(name = "id_user")
    private User user;

    // Kirim ke mana? (Untuk ambil data ongkir)
    @ManyToOne
    @JoinColumn(name = "id_provinsi")
    private Provinsi provinsi;

    private LocalDateTime tanggalTransaksi = LocalDateTime.now();

    private Integer totalHargaBarang; // Total belanjaan saja
    private Integer totalOngkir;      // Biaya ongkir
    private Integer grandTotal;       // Total + Ongkir

    @Column(columnDefinition = "TEXT")
    private String alamatPengiriman; // Alamat lengkap jalan/rt/rw

    // Bukti Transfer (Base64 Image)
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String buktiBayar;

    @Enumerated(EnumType.STRING)
    private Status status;

    // Enum Status Transaksi
public enum Status {
        PENDING, // Menunggu Bayar
        PAID,    // Sudah Bayar (Dikemas)
        SENT,    // Sedang Dikirim
        CANCEL,  // Batal
        DONE     // Selesai (Diterima User) <--- BARU
    }
    
    @OneToMany(mappedBy = "transaksi", fetch = FetchType.LAZY)
    @ToString.Exclude // Wajib ada biar gak error looping
    private List<TransaksiDetail> details;

}