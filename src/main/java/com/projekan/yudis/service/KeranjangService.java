package com.projekan.yudis.service;

import com.projekan.yudis.model.Keranjang;
import com.projekan.yudis.model.Produk;
import com.projekan.yudis.model.User;
import com.projekan.yudis.repository.KeranjangRepository;
import com.projekan.yudis.repository.ProdukRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class KeranjangService {

    @Autowired
    private KeranjangRepository keranjangRepository;

    @Autowired
    private ProdukRepository produkRepository;


    public List<Keranjang> getKeranjangByUser(User user) {
        return keranjangRepository.findByUser(user);
    }

    // 2. Tambah ke Keranjang
    public void tambahKeKeranjang(User user, Integer idProduct, Integer jumlah) {
        Produk produk = produkRepository.findById(idProduct).orElse(null);
        
        if (produk != null) {
            // Cek dulu: Apakah barang ini sudah ada di keranjang user?
            List<Keranjang> existingItems = keranjangRepository.findByUser(user);
            Keranjang itemAda = null;

            for (Keranjang k : existingItems) {
                if (k.getProduk().getIdProduct().equals(idProduct)) {
                    itemAda = k;
                    break;
                }
            }

            if (itemAda != null) {
                // JIKA SUDAH ADA: Update jumlahnya saja
                itemAda.setJumlah(itemAda.getJumlah() + jumlah);
                keranjangRepository.save(itemAda);
            } else {
                // JIKA BELUM ADA: Buat baru
                Keranjang k = new Keranjang();
                k.setUser(user);
                k.setProduk(produk);
                k.setJumlah(jumlah);
                k.setHarga(produk.getHarga()); // Simpan harga saat masuk keranjang
                k.setTanggalKeranjang(LocalDateTime.now());
                keranjangRepository.save(k);
            }
        }
    }

    // 3. Hapus Item Keranjang
    public void hapusItem(Integer idKeranjang) {
        keranjangRepository.deleteById(idKeranjang);
    }

    public int hitungTotalItem(User user) {
        List<Keranjang> list = keranjangRepository.findByUser(user);
        // Menjumlahkan kolom 'jumlah' dari semua item
        return list.stream().mapToInt(Keranjang::getJumlah).sum();
    }
    // ... method lain ...

    // FUNGSI UPDATE JUMLAH (+/-)
    public void updateJumlah(User user, Integer idKeranjang, int perubahan) {
        Optional<Keranjang> cartOpt = keranjangRepository.findById(idKeranjang);
        
        if (cartOpt.isPresent()) {
            Keranjang cart = cartOpt.get();
            
            // Validasi Keamanan: Pastikan ini keranjang milik user yang login
            if (!cart.getUser().getIdUser().equals(user.getIdUser())) {
                return;
            }

            int stokTersedia = cart.getProduk().getStock();
            int jumlahBaru = cart.getJumlah() + perubahan;

            // Aturan: Minimal 1, Maksimal sebanyak Stok
            if (jumlahBaru >= 1 && jumlahBaru <= stokTersedia) {
                cart.setJumlah(jumlahBaru);
                keranjangRepository.save(cart);
            }
        }
    }

    // FUNGSI SET JUMLAH MANUAL (Input Ketik)
    public void setJumlahPasti(User user, Integer idKeranjang, int jumlahBaru) {
        Optional<Keranjang> cartOpt = keranjangRepository.findById(idKeranjang);
        
        if (cartOpt.isPresent()) {
            Keranjang cart = cartOpt.get();
            
            // Validasi Pemilik
            if (!cart.getUser().getIdUser().equals(user.getIdUser())) return;

            int stokTersedia = cart.getProduk().getStock();

            // Validasi: Kalau input < 1, paksa jadi 1. Kalau > stok, mentok di stok.
            if (jumlahBaru < 1) jumlahBaru = 1;
            if (jumlahBaru > stokTersedia) jumlahBaru = stokTersedia;

            cart.setJumlah(jumlahBaru);
            keranjangRepository.save(cart);
        }
    }

}
