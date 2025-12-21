package com.projekan.yudis.controller;

import com.projekan.yudis.model.Produk;
import com.projekan.yudis.model.User;
import com.projekan.yudis.service.KeranjangService;
import com.projekan.yudis.service.ProdukService;
import com.projekan.yudis.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class MainController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProdukService produkService;

    @Autowired
    private KeranjangService keranjangService;

    // ==========================================
    // 1. HALAMAN UTAMA (INDEX) - Load Awal
    // ==========================================
    @GetMapping("/")
    public String index(Model model,
            @CookieValue(value = "USER_TOKEN", required = false) String token,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "kategori", required = false) String kategori, // <-- Parameter Kategori
            @RequestParam(value = "sortBy", required = false) String sortBy) {

        // A. Cek User Login
        User currentUser = userService.getUserFromToken(token);
        if (currentUser != null) {
            model.addAttribute("user", currentUser);
            int totalItem = keranjangService.hitungTotalItem(currentUser);
            model.addAttribute("cartCount", totalItem);
        }

        // B. Ambil Produk Halaman Pertama (Page 0, Size 20)
        int pageSize = 8;
        Page<Produk> pageProduk = produkService.getProdukPaged(keyword, kategori, sortBy, 0, pageSize);

        // Kirim Data ke HTML
        model.addAttribute("listProduk", pageProduk.getContent());

        // Kirim Parameter Filter Balik ke HTML (agar tidak hilang saat refresh/pindah
        // halaman)
        model.addAttribute("keyword", keyword);
        model.addAttribute("kategori", kategori);
        model.addAttribute("sortBy", sortBy);

        // Info untuk JavaScript (Infinite Scroll)
        model.addAttribute("totalPages", pageProduk.getTotalPages());

        return "index";
    }

    // ==========================================
    // 2. API LOAD MORE DATA (JSON)
    // Dipanggil oleh JavaScript saat Scroll
    // ==========================================
    @GetMapping("/api/products/load")
    @ResponseBody // Return JSON
    public List<Produk> loadMoreProduk(
            @RequestParam(value = "page") int page,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "kategori", required = false) String kategori, // <-- Parameter Kategori
            @RequestParam(value = "sortBy", required = false) String sortBy) {

        int pageSize = 20;

        // Ambil data halaman ke-X
        Page<Produk> pageProduk = produkService.getProdukPaged(keyword, kategori, sortBy, page, pageSize);

        // Jika data ada, kembalikan list-nya. Jika habis, kembalikan list kosong.
        if (pageProduk.hasContent()) {
            return pageProduk.getContent();
        } else {
            return new ArrayList<>();
        }
    }

    // ==========================================
    // 3. FITUR AUTH (LOGIN, REGISTER, LOGOUT)
    // ==========================================

    @GetMapping("/daftar")
    public String formDaftar(Model model, @CookieValue(value = "USER_TOKEN", required = false) String token) {
        User currentUser = userService.getUserFromToken(token);
        if (currentUser != null)
            return "redirect:/";
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/daftar")
    public String prosesDaftar(@Valid @ModelAttribute User user,
            BindingResult bindingResult,
            Model model) {

        // 1. Cek Validasi Form (Password pendek, dll)
        if (bindingResult.hasErrors()) {
            return "register";
        }

        // 2. Panggil Service Register (Return String)
        String hasil = userService.register(user);

        // 3. Cek Hasil
        if (hasil.equals("OK")) {
            // Jika sukses, lempar ke login
            return "redirect:/login";
        } else {
            // Jika gagal (Username/HP duplikat), balikin ke form dengan pesan error
            model.addAttribute("error", hasil);
            return "register";
        }
    }

    @GetMapping("/login")
    public String formLogin(@CookieValue(value = "USER_TOKEN", required = false) String token) {
        User currentUser = userService.getUserFromToken(token);
        if (currentUser != null)
            return "redirect:/";
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
            cookie.setMaxAge(24 * 60 * 60); // 1 Hari
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

    // ==========================================
    // 4. FITUR PROFIL USER
    // ==========================================

    @GetMapping("/profil")
    public String halamanProfil(Model model,
            @CookieValue(value = "USER_TOKEN", required = false) String token) {
        User user = userService.getUserFromToken(token);
        if (user == null)
            return "redirect:/login";

        model.addAttribute("user", user);
        return "profil";
    }

    @PostMapping("/profil/update")
    public String prosesUpdateProfil(@ModelAttribute User userForm,
            @RequestParam(required = false) String newPassword,
            @CookieValue(value = "USER_TOKEN", required = false) String token,
            Model model) {

        User userLogin = userService.getUserFromToken(token);
        if (userLogin == null)
            return "redirect:/login";

        String hasil = userService.updateDataUser(userLogin, userForm, newPassword);

        if (hasil.equals("OK")) {
            return "redirect:/profil?sukses=Data Berhasil Diupdate";
        } else {
            model.addAttribute("error", hasil);
            model.addAttribute("user", userLogin);
            return "profil";
        }
    }
}