package com.projekan.yudis.service;

import com.projekan.yudis.model.Keranjang;
import com.projekan.yudis.model.Transaksi;
import com.projekan.yudis.model.User;
import com.projekan.yudis.repository.KeranjangRepository;
import com.projekan.yudis.repository.TransaksiRepository;
import com.projekan.yudis.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KeranjangRepository keranjangRepository; // Wajib ada

    @Autowired
    private TransaksiRepository transaksiRepository; // Wajib ada

    // REGISTER
    public String register(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return "Username sudah terdaftar! Silakan gunakan yang lain.";
        }
        if (userRepository.findByNoHp(user.getNoHp()).isPresent()) {
            return "Nomor HP sudah terdaftar! Gunakan nomor lain.";
        }
        if (user.getRole() == null) {
            user.setRole(User.Role.USER);
        }
        userRepository.save(user);
        return "OK";
    }

    public String login(String username, String password) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null && user.getPassword().equals(password)) {
            String token = UUID.randomUUID().toString();
            user.setAuthToken(token);
            userRepository.save(user);
            return token;
        }
        return null;
    }

    public User getUserFromToken(String token) {
        if (token == null || token.isEmpty())
            return null;
        return userRepository.findByAuthToken(token).orElse(null);
    }

    public void logout(String token) {
        User user = getUserFromToken(token);
        if (user != null) {
            user.setAuthToken(null);
            userRepository.save(user);
        }
    }

    // === UPDATE DATA USER (DIPERBAIKI) ===
    public String updateDataUser(User userLogin, User dataBaru, String passwordBaru) {

        // 1. Validasi Username (Jika berubah)
        if (!userLogin.getUsername().equals(dataBaru.getUsername())) {
            if (userRepository.findByUsername(dataBaru.getUsername()).isPresent()) {
                return "Username sudah terpakai!";
            }
        }

        // 2. Validasi Keunikan No HP (Jika berubah)
        if (!userLogin.getNoHp().equals(dataBaru.getNoHp())) {
            if (userRepository.findByNoHp(dataBaru.getNoHp()).isPresent()) {
                return "Nomor HP sudah digunakan user lain!";
            }
        }

        // 3. (BARU) Validasi Format Nomor HP (Mencegah Error 500)
        String noHpBaru = dataBaru.getNoHp();
        if (noHpBaru == null || !noHpBaru.matches("[0-9]+")) {
            return "Nomor HP harus berupa angka!";
        }
        if (noHpBaru.length() < 10 || noHpBaru.length() > 15) {
            return "Nomor HP harus antara 10 sampai 15 digit!";
        }

        // 4. Update Data Dasar
        userLogin.setNamaLengkap(dataBaru.getNamaLengkap());
        userLogin.setUsername(dataBaru.getUsername());
        userLogin.setNoHp(noHpBaru);

        // 5. Update Password (Hanya jika diisi)
        if (passwordBaru != null && !passwordBaru.isBlank()) {
            if (passwordBaru.length() < 6) {
                return "Password minimal 6 karakter!";
            }
            userLogin.setPassword(passwordBaru);
        }

        // 6. Simpan
        try {
            userRepository.save(userLogin);
        } catch (Exception e) {
            return "Gagal menyimpan data: " + e.getMessage();
        }

        return "OK";
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // === HAPUS USER (TAPI RIWAYAT TETAP ADA) ===
    public void deleteUser(Integer idUser) {
        User user = userRepository.findById(idUser).orElse(null);

        if (user != null) {
            // 1. HAPUS KERANJANG (Wajib bersih)
            List<Keranjang> keranjangItems = keranjangRepository.findByUser(user);
            if (!keranjangItems.isEmpty()) {
                keranjangRepository.deleteAll(keranjangItems);
            }

            // 2. PUTUSKAN HUBUNGAN TRANSAKSI (JANGAN DIHAPUS)
            // Ubah kolom id_user menjadi NULL, data aman karena ada snapshot
            List<Transaksi> transaksiList = transaksiRepository.findByUser(user);
            for (Transaksi trx : transaksiList) {
                trx.setUser(null);
                transaksiRepository.save(trx); // Update transaksi
            }

            // 3. BARU HAPUS USER
            userRepository.delete(user);
        }
    }

   

  }