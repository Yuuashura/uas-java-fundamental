package com.projekan.yudis.service;

import com.projekan.yudis.model.*;
import com.projekan.yudis.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

@Service
public class TransaksiService {

    @Autowired
    private TransaksiRepository transaksiRepository;

    @Autowired
    private TransaksiDetailRepository transaksiDetailRepository;

    @Autowired
    private KeranjangRepository keranjangRepository;

    @Autowired
    private ProdukRepository produkRepository;

    @Autowired
    private ProvinsiRepository provinsiRepository;

    @Transactional
    public Transaksi  buatTransaksi(User user, List<Integer> idKeranjangList, Integer idProvinsi, String alamat) {
        // 1. Ambil Data Ongkir (Provinsi)
        Provinsi provinsi = provinsiRepository.findById(idProvinsi)
                .orElseThrow(() -> new RuntimeException("Provinsi tidak ditemukan"));
        // 2. Siapkan Header Transaksi
        Transaksi transaksi = new Transaksi();
        // --- A. DATA USER (Relasi & Snapshot) ---
        transaksi.setUser(user); // Relasi Asli
        // Snapshot: Simpan data teks user agar aman jika User dihapus
        transaksi.setNamaPembeliSnapshot(user.getNamaLengkap());
        transaksi.setNoHpPembeliSnapshot(user.getNoHp());
        transaksi.setUsernamePembeliSnapshot(user.getUsername());
        // --- B. DATA WILAYAH (Relasi & Snapshot) ---
        transaksi.setProvinsi(provinsi); // Relasi Asli
        // Snapshot: Simpan nama provinsi agar aman jika Master Provinsi dihapus
        transaksi.setNamaProvinsiSnapshot(provinsi.getNamaProvinsi());
        // --- C. DATA LAINNYA ---
        transaksi.setAlamatPengiriman(alamat);
        transaksi.setTotalOngkir(provinsi.getHargaOngkir()); // Snapshot Harga Ongkir
        transaksi.setStatus(Transaksi.Status.PENDING);
        transaksi.setTanggalTransaksi(java.time.LocalDateTime.now()); // Set Waktu Sekarang
        // Simpan Header dulu untuk mendapatkan ID Transaksi
        transaksi = transaksiRepository.save(transaksi);

        int totalHargaBarang = 0;

        // 3. Loop Barang yang dipilih dari Keranjang
        for (Integer idCart : idKeranjangList) {
            Optional<Keranjang> cartOpt = keranjangRepository.findById(idCart);

            if (cartOpt.isPresent()) {
                Keranjang cart = cartOpt.get();
                Produk produk = cart.getProduk();
                // Cek Stok
                if (produk.getStock() < cart.getJumlah()) {
                    throw new RuntimeException("Stok habis untuk produk: " + produk.getNamaProduct());
                }

                // Kurangi Stok Produk & Update
                produk.setStock(produk.getStock() - cart.getJumlah());
                produkRepository.save(produk);

                // Siapkan Detail Transaksi
                TransaksiDetail detail = new TransaksiDetail();
                detail.setTransaksi(transaksi);
                detail.setProduk(produk); // Relasi Asli
                detail.setJumlah(cart.getJumlah());

                // Snapshot Harga & Subtotal (Kunci harga saat ini)
                detail.setHargaSatuan(produk.getHarga());
                detail.setSubtotal(produk.getHarga() * cart.getJumlah());

                // --- D. SNAPSHOT PRODUK (Agar aman jika Produk dihapus/diedit) ---
                detail.setNamaProductSnapshot(produk.getNamaProduct());
                detail.setGambarSnapshot(produk.getGambarProduct());
                detail.setDeskripsiProductSnapshot(produk.getDeskripsiProduct());

                // Simpan Detail ke Database
                transaksiDetailRepository.save(detail);

                // Hitung Total Belanja
                totalHargaBarang += detail.getSubtotal();

                // Hapus item dari Keranjang karena sudah dibeli
                keranjangRepository.delete(cart);
            }
        }

        // 4. Update Total Akhir di Header Transaksi
        transaksi.setTotalHargaBarang(totalHargaBarang);
        transaksi.setGrandTotal(totalHargaBarang + transaksi.getTotalOngkir());

        // Simpan perubahan final (Total Harga) ke database
        return transaksiRepository.save(transaksi);
    }

    
    // Ambil Riwayat Transaksi User
    public List<Transaksi> getTransaksiByUser(User user) {
        return transaksiRepository.findByUser(user);
    }

    // Ambil Detail Transaksi
    public Transaksi getTransaksiById(Integer id) {
        return transaksiRepository.findById(id).orElse(null);
    }

    // 1. ADMIN: Ambil Semua Transaksi (Urutkan yang terbaru di atas)
    public List<Transaksi> getAllTransaksi() {
        return transaksiRepository.findAll(Sort.by(Sort.Direction.DESC, "idTransaksi"));
    }

    // 2. ADMIN: Update Status Pesanan
    public void updateStatus(Integer idTransaksi, Transaksi.Status statusBaru) {
        Transaksi t = transaksiRepository.findById(idTransaksi).orElse(null);
        if (t != null) {
            t.setStatus(statusBaru);
            transaksiRepository.save(t);
        }
    }

    // 3. USER: Konfirmasi Pesanan Diterima
    public void selesaikanPesanan(User user, Integer idTransaksi) {
        Transaksi t = transaksiRepository.findById(idTransaksi).orElse(null);

        // Validasi:
        // 1. Transaksi ada
        // 2. Milik user yang login
        // 3. Statusnya harus SENT (Sedang Dikirim)
        if (t != null
                && t.getUser().getIdUser().equals(user.getIdUser())
                && t.getStatus() == Transaksi.Status.SENT) {

            t.setStatus(Transaksi.Status.DONE);
            transaksiRepository.save(t);
        }
    }

    // ... import TransaksiDetail, Produk ...
    @Transactional
    public void batalkanTransaksiOlehUser(Integer idTransaksi, Integer idUser) {
        // 1. Cari Transaksi
        Transaksi transaksi = transaksiRepository.findById(idTransaksi)
                .orElseThrow(() -> new RuntimeException("Transaksi tidak ditemukan"));

        // 2. Validasi Kepemilikan (Cegah user A membatalkan pesanan user B)
        if (transaksi.getUser() == null || !transaksi.getUser().getIdUser().equals(idUser)) {
            throw new RuntimeException("Anda tidak memiliki akses ke pesanan ini.");
        }

        // 3. Validasi Status (Hanya boleh batal jika masih PENDING)
        if (transaksi.getStatus() != Transaksi.Status.PENDING) {
            throw new RuntimeException("Pesanan tidak bisa dibatalkan karena sudah diproses atau selesai.");
        }

        // 4. KEMBALIKAN STOK BARANG (RESTOCK)
        // Loop semua barang di detail transaksi, balikin jumlahnya ke produk asli
        for (TransaksiDetail detail : transaksi.getDetails()) {
            Produk produk = detail.getProduk();
            if (produk != null) {
                produk.setStock(produk.getStock() + detail.getJumlah());
                produkRepository.save(produk);
            }
        }
        // 5. Ubah Status
        transaksi.setStatus(Transaksi.Status.CANCEL);
        transaksiRepository.save(transaksi);
    }

}