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
    private TransaksiDetailRepository detailRepository;

    @Autowired
    private KeranjangRepository keranjangRepository;

    @Autowired
    private ProdukRepository produkRepository;

    @Autowired
    private ProvinsiRepository provinsiRepository;

    // FUNGSI UTAMA: PROSES CHECKOUT
    @Transactional // PENTING: Agar kalau error, semua perubahan dibatalkan (Rollback)
    public Transaksi buatTransaksi(User user, List<Integer> idKeranjangList, Integer idProvinsi, String alamat) {
        
        // 1. Ambil Data Ongkir
        Provinsi provinsi = provinsiRepository.findById(idProvinsi).orElseThrow();
        
        // 2. Siapkan Header Transaksi
        Transaksi transaksi = new Transaksi();
        transaksi.setUser(user);
        transaksi.setProvinsi(provinsi);
        transaksi.setAlamatPengiriman(alamat);
        transaksi.setTotalOngkir(provinsi.getHargaOngkir());
        transaksi.setStatus(Transaksi.Status.PENDING); // Status awal: Belum Bayar
        
        // Simpan dulu biar dapat ID Transaksi
        transaksi = transaksiRepository.save(transaksi);

        int totalHargaBarang = 0;

        // 3. Loop Barang yang dipilih
        for (Integer idCart : idKeranjangList) {
            Optional<Keranjang> cartOpt = keranjangRepository.findById(idCart);
            
            if (cartOpt.isPresent()) {
                Keranjang cart = cartOpt.get();
                Produk produk = cart.getProduk();

                // Cek Stok
                if (produk.getStock() < cart.getJumlah()) {
                    throw new RuntimeException("Stok habis untuk produk: " + produk.getNamaProduct());
                }

                // Kurangi Stok Produk
                produk.setStock(produk.getStock() - cart.getJumlah());
                produkRepository.save(produk);

                // Masukkan ke Transaksi Detail
                TransaksiDetail detail = new TransaksiDetail();
                detail.setTransaksi(transaksi);
                detail.setProduk(produk);
                detail.setJumlah(cart.getJumlah());
                detail.setHargaSatuan(produk.getHarga()); // Kunci harga saat beli
                detail.setSubtotal(produk.getHarga() * cart.getJumlah());
                detail.setNamaProductSnapshot(produk.getNamaProduct());
detail.setGambarSnapshot(produk.getGambarProduct()); // Salin Base64 Strin
                detailRepository.save(detail);

                // Hitung Total
                totalHargaBarang += detail.getSubtotal();

                // Hapus dari Keranjang
                keranjangRepository.delete(cart);
            }
        }

        // 4. Update Total Akhir di Header
        transaksi.setTotalHargaBarang(totalHargaBarang);
        transaksi.setGrandTotal(totalHargaBarang + transaksi.getTotalOngkir());
        
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

    
}