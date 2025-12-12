package com.projekan.yudis.controller;

import com.projekan.yudis.model.User;
import com.projekan.yudis.service.KeranjangService;
import com.projekan.yudis.service.ProdukService;
import com.projekan.yudis.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

@Controller
public class MainController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProdukService produkService;

    @Autowired
    private KeranjangService keranjangService;

    @GetMapping("/")
    public String index(Model model,
            @CookieValue(value = "USER_TOKEN", required = false) String token,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "sortBy", required = false) String sortBy) {

        // A. Cek User Login
        User currentUser = userService.getUserFromToken(token);
        if (currentUser != null) {
            model.addAttribute("user", currentUser);

            // 2. TAMBAHKAN INI (Hitung Keranjang)
            int totalItem = keranjangService.hitungTotalItem(currentUser);
            model.addAttribute("cartCount", totalItem);
        }

        // B. Ambil Produk (Panggil method baru yang ada filternya)
        model.addAttribute("listProduk", produkService.getProdukWithFilter(keyword, sortBy));

        // C. Balikin nilai filter ke HTML biar gak hilang setelah reload
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortBy", sortBy);

        return "index";
    }

    // ==========================================
    // 2. FITUR REGISTRASI
    // ==========================================
    @GetMapping("/daftar")
    public String formDaftar(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/daftar")
    public String prosesDaftar(@Valid @ModelAttribute User user,
            BindingResult bindingResult,
            Model model) {

        // 1. Cek apakah ada error validasi (Misal: Password kependekan)
        if (bindingResult.hasErrors()) {
            // Jika ada error, kembalikan ke halaman register biar user benerin
            return "register";
        }

        // 2. Cek apakah Username sudah terpakai (Logika Bisnis)
        boolean sukses = userService.register(user);

        if (!sukses) {
            model.addAttribute("error", "Username sudah terdaftar! Ganti yang lain.");
            return "register";
        }

        return "redirect:/login";
    }

    // ==========================================
    // 3. FITUR LOGIN
    // ==========================================
    @GetMapping("/login")
    public String formLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String prosesLogin(@RequestParam String username,
            @RequestParam String password,
            HttpServletResponse response,
            Model model) {

        String token = userService.login(username, password);

        if (token != null) {
            Cookie cookie = new Cookie("USER_TOKEN", token);
            cookie.setMaxAge(24 * 60 * 60);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);

            User user = userService.getUserFromToken(token);
            if (user.getRole() == User.Role.ADMIN) {
                return "redirect:/admin/produk";
            } else {
                return "redirect:/";
            }
        } else {
            model.addAttribute("error", "Username atau Password salah bro!");
            return "login";
        }
    }

    // ==========================================
    // 4. FITUR LOGOUT
    // ==========================================
    @GetMapping("/logout")
    public String logout(HttpServletResponse response,
            @CookieValue(value = "USER_TOKEN", required = false) String token) {
        userService.logout(token);
        Cookie cookie = new Cookie("USER_TOKEN", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return "redirect:/";
    }
    // 5. HALAMAN EDIT PROFIL
    @GetMapping("/profil")
    public String halamanProfil(Model model,
                                @CookieValue(value = "USER_TOKEN", required = false) String token) {
        User user = userService.getUserFromToken(token);
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        return "profil";
    }

    // 6. PROSES UPDATE PROFIL
    @PostMapping("/profil/update")
    public String prosesUpdateProfil(@ModelAttribute User userForm,
                                     @RequestParam(required = false) String newPassword,
                                     @CookieValue(value = "USER_TOKEN", required = false) String token,
                                     Model model) {
        
        User userLogin = userService.getUserFromToken(token);
        if (userLogin == null) return "redirect:/login";

        // Panggil Service Update
        String hasil = userService.updateDataUser(userLogin, userForm, newPassword);

        if (hasil.equals("OK")) {
            return "redirect:/profil?sukses=Data Berhasil Diupdate";
        } else {
            // Jika Gagal (misal username duplikat), balikin ke form dengan error
            model.addAttribute("error", hasil);
            model.addAttribute("user", userLogin); // Balikin data lama biar gak kosong
            return "profil";
        }
    }
}