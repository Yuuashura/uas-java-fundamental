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

    // 1. UNTUK MENGAMBIL DATA KERANJANG
    public List<Keranjang> getKeranjangByUser(User user) {
        return keranjangRepository.findByUser(user);
    }

    // 2. FUNGSI TAMBAH KE KERANJANG (VALIDASI KETAT)
    public void tambahKeKeranjang(User user, Integer idProduct, Integer jumlah) {
        Produk produk = produkRepository.findById(idProduct).orElse(null);

        if (produk != null) {
            if (produk.getStock() <= 0) {
                throw new RuntimeException("Stok habis");
            }

            List<Keranjang> existingItems = keranjangRepository.findByUser(user);
            Keranjang itemAda = null;

            for (Keranjang k : existingItems) {
                if (k.getProduk().getIdProduct().equals(idProduct)) {
                    itemAda = k;
                    break;
                }
            }

            if (itemAda != null) {
                // Hitung total jika ditambahkan
                int totalBaru = itemAda.getJumlah() + jumlah;

                // Cek apakah melebihi stok?
                if (totalBaru > produk.getStock()) {
                    // LEMPAR ERROR: Jangan simpan, biarkan Controller menanganinya
                    throw new RuntimeException("Total di keranjang melebihi stok tersedia!");
                }

                // Jika aman, update
                itemAda.setJumlah(totalBaru);
                keranjangRepository.save(itemAda);

            } else {
                // JIKA BELUM ADA
                if (jumlah > produk.getStock()) {
                    throw new RuntimeException("Jumlah melebihi stok tersedia!");
                }

                Keranjang k = new Keranjang();
                k.setUser(user);
                k.setProduk(produk);
                k.setJumlah(jumlah);
                k.setHarga(produk.getHarga());
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
        return list.stream().mapToInt(Keranjang::getJumlah).sum();
    }

    // FUNGSI UPDATE JUMLAH (+/-) DI HALAMAN KERANJANG
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

    public void setJumlahPasti(User user, Integer idKeranjang, int jumlahBaru) {
        Optional<Keranjang> cartOpt = keranjangRepository.findById(idKeranjang);

        if (cartOpt.isPresent()) {
            Keranjang cart = cartOpt.get();

            if (!cart.getUser().getIdUser().equals(user.getIdUser()))
                return; // yaa kosongg 
            int stokTersedia = cart.getProduk().getStock();
            if (jumlahBaru < 1)
                jumlahBaru = 1;
            if (jumlahBaru > stokTersedia)
                jumlahBaru = stokTersedia;
            cart.setJumlah(jumlahBaru);
            keranjangRepository.save(cart);
        }
    }
}