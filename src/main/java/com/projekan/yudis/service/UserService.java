package com.projekan.yudis.service;

import com.projekan.yudis.model.User;
import com.projekan.yudis.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

public boolean register(User user) {
        // 1. Cek apakah username sudah ada di Database?
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return false; // GAGAL: Username sudah terpakai
        }

        // 2. Jika belum ada, lanjut simpan
        if (user.getRole() == null) {
            user.setRole(User.Role.USER);
        }
        userRepository.save(user);
        return true; // SUKSES
    }

    // FUNGSI 2: LOGIN (Generate Token)
    public String login(String username, String password) {
        User user = userRepository.findByUsername(username).orElse(null);

        // Cek apakah user ada & password cocok
        if (user != null && user.getPassword().equals(password)) {
            // Buat token acak baru
            String token = UUID.randomUUID().toString();
            
            // Simpan token ke database
            user.setAuthToken(token);
            userRepository.save(user);

            return token; // Kembalikan token untuk dikirim ke Controller
        }
        return null; // Login gagal
    }

    // FUNGSI 3: CEK USER DARI TOKEN (Pengganti Session)
    public User getUserFromToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        return userRepository.findByAuthToken(token).orElse(null);
    }

    // FUNGSI 4: LOGOUT
    public void logout(String token) {
        User user = getUserFromToken(token);
        if (user != null) {
            // Hapus token di database
            user.setAuthToken(null);
            userRepository.save(user);
        }
    }

    // FUNGSI UPDATE DATA USER
    public String updateDataUser(User userLogin, User dataBaru, String passwordBaru) {
        
        // 1. Validasi Username (Jika berubah, cek apakah sudah dipakai orang lain)
        if (!userLogin.getUsername().equals(dataBaru.getUsername())) {
            if (userRepository.findByUsername(dataBaru.getUsername()).isPresent()) {
                return "Username sudah terpakai!";
            }
        }

        // 2. Update Data Dasar
        userLogin.setNamaLengkap(dataBaru.getNamaLengkap());
        userLogin.setUsername(dataBaru.getUsername());
        userLogin.setNoHp(dataBaru.getNoHp());
 
        // 3. Update Password (Hanya jika diisi)
        if (passwordBaru != null && !passwordBaru.isBlank()) {
            if (passwordBaru.length() < 6) {
                return "Password minimal 6 karakter!";
            }
            userLogin.setPassword(passwordBaru); // Di real project, ini harus di-hash dulu
        }

        // 4. Simpan
        userRepository.save(userLogin);
        return "OK";
    }

    // ... import List ...

    // AMBIL SEMUA USER (Kecuali yang sedang login/admin utama jika mau, tapi ambil semua dulu)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // HAPUS USER
    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }

    // UBAH ROLE JADI ADMIN (Promosi)
    public void promoteToAdmin(Integer id) {
        User u = userRepository.findById(id).orElse(null);
        if (u != null) {
            u.setRole(User.Role.ADMIN); // Pastikan Enum Role.ADMIN ada di Model User
            userRepository.save(u);
        }
    }
    
    // UBAH JADI USER BIASA (Demosi)
    public void demoteToUser(Integer id) {
        User u = userRepository.findById(id).orElse(null);
        if (u != null) {
            u.setRole(User.Role.USER);
            userRepository.save(u);
        }
    }
    
}