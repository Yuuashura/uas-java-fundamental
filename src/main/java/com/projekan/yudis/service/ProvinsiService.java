package com.projekan.yudis.service;

import com.projekan.yudis.model.Provinsi;
import com.projekan.yudis.model.Transaksi;
import com.projekan.yudis.repository.ProvinsiRepository;
import com.projekan.yudis.repository.TransaksiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProvinsiService {

    @Autowired
    private ProvinsiRepository provinsiRepository;

    @Autowired
    private TransaksiRepository transaksiRepository;

    public List<Provinsi> getAllProvinsi() {
        return provinsiRepository.findAll();
    }

    // === TAMBAHKAN INI (Untuk Edit di AdminController) ===
    public Provinsi getProvinsiById(Integer id) {
        // Cari by ID, jika tidak ada kembalikan null
        return provinsiRepository.findById(id).orElse(null);
    }

    public void saveProvinsi(Provinsi provinsi) {
        provinsiRepository.save(provinsi);
    }

    @Transactional
    public void deleteProvinsi(Integer id) {
        Provinsi provinsi = provinsiRepository.findById(id).orElse(null);
        if (provinsi != null) {
            List<Transaksi> listTrx = transaksiRepository.findByProvinsi(provinsi);
            for (Transaksi t : listTrx) {
                t.setProvinsi(null);
                transaksiRepository.save(t);
            }
            provinsiRepository.deleteById(id);
        }
    }
}