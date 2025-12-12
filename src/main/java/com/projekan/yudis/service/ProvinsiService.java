package com.projekan.yudis.service;

import com.projekan.yudis.model.Provinsi;
import com.projekan.yudis.repository.ProvinsiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProvinsiService {

    @Autowired
    private ProvinsiRepository provinsiRepository;

    // 1. AMBIL SEMUA DATA (READ)
    public List<Provinsi> getAllProvinsi() {
        return provinsiRepository.findAll();
    }

    // 2. AMBIL SATU DATA BY ID (Untuk Form Edit)
    public Provinsi getProvinsiById(Integer id) {
        return provinsiRepository.findById(id).orElse(null);
    }

    // 3. SIMPAN / UPDATE DATA (CREATE & UPDATE)
    public void saveProvinsi(Provinsi provinsi) {
        provinsiRepository.save(provinsi);
    }

    // 4. HAPUS DATA (DELETE)
    public void deleteProvinsi(Integer id) {
        provinsiRepository.deleteById(id);
    }
}