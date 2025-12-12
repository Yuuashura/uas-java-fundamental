package com.projekan.yudis.service;

import com.projekan.yudis.model.Produk;
import com.projekan.yudis.repository.ProdukRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
// Import tambahan
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Service
public class ProdukService {

    @Autowired
    private ProdukRepository produkRepository;

    // === MODIFIKASI: GET PRODUK DENGAN FILTER & SORT ===
    public List<Produk> getProdukWithFilter(String keyword, String sortBy) {

        // 1. Tentukan Logika Sorting
        Sort sort = Sort.by("idProduct").ascending(); // Default urut ID (Produk terlama)

        if ("termurah".equals(sortBy)) {
            sort = Sort.by("harga").ascending();
        } else if ("termahal".equals(sortBy)) {
            sort = Sort.by("harga").descending();
        } else if ("nama".equals(sortBy)) {
            sort = Sort.by("namaProduct").ascending();
        } else if ("stok".equals(sortBy)) {
            sort = Sort.by("stock").descending();
        }

        // 2. Eksekusi Query
        if (keyword != null && !keyword.isBlank()) {
            // Jika ada keyword pencarian
            return produkRepository.findByNamaProductContainingIgnoreCase(keyword, sort);
        } else {
            // Jika tidak ada keyword (hanya sorting)
            return produkRepository.findAll(sort);
        }
    }

    // FUNGSI CARI + SORT + PAGINATION (Halaman)
    public Page<Produk> getProdukPaged(String keyword, String sortBy, int page, int size) {
        // 1. Tentukan Urutan (Sorting)
        Sort sort = Sort.by(Sort.Direction.ASC, "idProduct"); // Default ID
        
        if (sortBy != null) {
            switch (sortBy) {
                case "termurah": sort = Sort.by(Sort.Direction.ASC, "harga"); break;
                case "termahal": sort = Sort.by(Sort.Direction.DESC, "harga"); break;
                case "stok": sort = Sort.by(Sort.Direction.DESC, "stock"); break;
                case "nama": sort = Sort.by(Sort.Direction.ASC, "namaProduct"); break;
            }
        }

        // 2. Buat Pageable (Halaman ke-berapa, Berapa biji, Urutan apa)
        Pageable pageable = PageRequest.of(page, size, sort);

        // 3. Ambil Data
        if (keyword != null && !keyword.isBlank()) {
            return produkRepository.findByNamaProductContainingIgnoreCase(keyword, pageable);
        } else {
            return produkRepository.findAll(pageable);
        }
    }



    // Method untuk Admin: Ambil Semua (Tanpa filter aneh-aneh)
    public List<Produk> getAllProduk() {
        return produkRepository.findAll();
    }

    // Method Simpan Produk (Admin)
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

    // Method Hapus Produk (Admin)
    public void deleteProduk(Integer id) {
        produkRepository.deleteById(id);
    }
    // ... method lain ...

    // METHOD UPDATE PRODUK
    public void updateProduk(Produk produkBaru, MultipartFile file) {
        try {
            // Ambil data lama dari database
            Produk produkLama = produkRepository.findById(produkBaru.getIdProduct()).orElse(null);

            if (produkLama != null) {
                // 1. Cek apakah ada file gambar baru?
                if (!file.isEmpty()) {
                    // Kalau ada, convert jadi Base64
                    String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
                    produkBaru.setGambarProduct(base64Image);
                } else {
                    // Kalau tidak ada, pakai gambar lama
                    produkBaru.setGambarProduct(produkLama.getGambarProduct());
                }

                // Simpan perubahan
                produkRepository.save(produkBaru);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method cari berdasarkan ID (untuk menampilkan data di form edit)
    public Produk getProdukById(Integer id) {
        return produkRepository.findById(id).orElse(null);
    }
}