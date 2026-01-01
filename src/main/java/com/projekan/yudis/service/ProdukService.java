package com.projekan.yudis.service;

import com.projekan.yudis.model.Keranjang;
import com.projekan.yudis.model.Produk;
import com.projekan.yudis.repository.KeranjangRepository;
import com.projekan.yudis.repository.ProdukRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Base64;
import java.util.List;



@Service
public class ProdukService {

    @Autowired
    private ProdukRepository produkRepository;

    @Autowired
    private KeranjangRepository keranjangRepository;

    // FUNGSI UTAMA: MENCARI DATA DENGAN FILTER (KEYWORD, KATEGORI, SORT, PAGE)
    public Page<Produk> getProdukPaged(String keyword, String kategori, String sortBy, int page, int size) {

        // 1. Tentukan Logika Sorting
        Sort sort = Sort.by(Sort.Direction.ASC, "idProduct"); // Default urut ID

        if (sortBy != null) {
            switch (sortBy) {
                case "termurah":
                    sort = Sort.by(Sort.Direction.ASC, "harga");
                    break;
                case "termahal":
                    sort = Sort.by(Sort.Direction.DESC, "harga");
                    break;
                case "nama":
                    sort = Sort.by(Sort.Direction.ASC, "namaProduct");
                    break;
                case "stok":
                    sort = Sort.by(Sort.Direction.DESC, "stock");
                    break;
            }
        }

        // 2. Buat Pageable
        Pageable pageable = PageRequest.of(page, size, sort);

        // 3. Cek Parameter Filter yang Ada
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasKategori = kategori != null && !kategori.isBlank();

        // 4. Eksekusi Query Sesuai Kondisi
        if (hasKeyword && hasKategori) {
            // Jika User mencari Nama DAN memilih Kategori
            return produkRepository.findByNamaProductContainingIgnoreCaseAndKategoriProduct(keyword, kategori,
                    pageable);
        } else if (hasKategori) {
            // Jika User hanya memilih Kategori
            return produkRepository.findByKategoriProduct(kategori, pageable);
        } else if (hasKeyword) {
            // Jika User hanya mencari Nama
            return produkRepository.findByNamaProductContainingIgnoreCase(keyword, pageable);
        } else {
            // Jika tidak ada filter (Tampilkan Semua)
            return produkRepository.findAll(pageable);
        }
    }

    // --- Method Standar Lainnya ---

    public List<Produk> getAllProduk() {
        return produkRepository.findAll();
    }

    public Produk getProdukById(Integer id) {
        return produkRepository.findById(id).orElse(null);
    }

    public void saveProduk(Produk produk, MultipartFile file) {
        try {
            if (!file.isEmpty()) {
                String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
                produk.setGambarProduct(base64Image);
            }
            produkRepository.save(produk);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateProduk(Produk produkBaru, MultipartFile file) {
        try {
            Produk produkLama = produkRepository.findById(produkBaru.getIdProduct()).orElse(null);
            if (produkLama != null) {
                if (!file.isEmpty()) {
                    String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
                    produkBaru.setGambarProduct(base64Image);
                } else {
                    produkBaru.setGambarProduct(produkLama.getGambarProduct());
                }
                produkRepository.save(produkBaru);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteProduk(Integer idProduk) {
        // Hapus item di keranjang yang terkait produk ini dulu agar tidak error FK
        List<Keranjang> cartItems = keranjangRepository.findByProduk_IdProduct(idProduk);
        if (cartItems != null && !cartItems.isEmpty()) {
            keranjangRepository.deleteAll(cartItems);
        }
        produkRepository.deleteById(idProduk);
    }
}