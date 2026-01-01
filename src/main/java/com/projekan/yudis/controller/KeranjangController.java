package com.projekan.yudis.controller;

import com.projekan.yudis.model.Produk;
import com.projekan.yudis.model.User;
import com.projekan.yudis.repository.ProdukRepository;
import com.projekan.yudis.service.KeranjangService;
import com.projekan.yudis.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class KeranjangController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProdukRepository produkRepository;

    @Autowired
    private KeranjangService keranjangService;

    // 1. HALAMAN DETAIL PRODUK
    @GetMapping("/produk/{id}")
    public String detailProduk(@PathVariable Integer id, Model model,
            @CookieValue(value = "USER_TOKEN", required = false) String token) {

        Produk produk = produkRepository.findById(id).orElse(null);
        if (produk == null)
            return "redirect:/";

        User user = userService.getUserFromToken(token);
        if (user != null)
            model.addAttribute("user", user);

        model.addAttribute("produk", produk);
        return "detail-product"; // Mengarah ke template detail-product.html
    }

    // 2. PROSES TAMBAH KE KERANJANG
    @PostMapping("/keranjang/tambah")
    public String tambahKeranjang(@RequestParam Integer idProduct,
            @RequestParam Integer jumlah,
            @CookieValue(value = "USER_TOKEN", required = false) String token) {

        User user = userService.getUserFromToken(token);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            // Coba tambahkan
            keranjangService.tambahKeKeranjang(user, idProduct, jumlah);

            // Jika sukses
            return "redirect:/produk/" + idProduct + "?sukses=true";

        } catch (RuntimeException e) {
            // Jika GAGAL (Stok tidak cukup)
            // Redirect kembali dengan pesan error
            return "redirect:/produk/" + idProduct + "?error=stock";
        }
    }

    // 3. HALAMAN LIHAT KERANJANG
    @GetMapping("/keranjang")
    public String lihatKeranjang(Model model,
            @CookieValue(value = "USER_TOKEN", required = false) String token) {

        User user = userService.getUserFromToken(token);
        if (user == null)
            return "redirect:/login";

        model.addAttribute("user", user);
        model.addAttribute("listKeranjang", keranjangService.getKeranjangByUser(user));

        // Hitung Total Belanjaan Sementara
        int totalBelanja = keranjangService.getKeranjangByUser(user).stream()
                .mapToInt(k -> k.getHarga() * k.getJumlah())
                .sum();
        model.addAttribute("totalBelanja", totalBelanja);

        return "keranjang"; // Mengarah ke template keranjang.html
    }

    // 4. HAPUS ITEM DARI KERANJANG
    @GetMapping("/keranjang/hapus/{id}")
    public String hapusKeranjang(@PathVariable Integer id,
            @CookieValue(value = "USER_TOKEN", required = false) String token) {
        User user = userService.getUserFromToken(token);
        if (user != null) {
            keranjangService.hapusItem(id);
        }
        return "redirect:/keranjang";
    }

    // 5. UBAH JUMLAH ITEM (PLUS / MINUS)
    @GetMapping("/keranjang/ubah/{id}")
    public String ubahJumlah(@PathVariable Integer id,
            @RequestParam Integer change, // +1 atau -1
            @CookieValue(value = "USER_TOKEN", required = false) String token) {

        User user = userService.getUserFromToken(token);
        if (user != null) {
            keranjangService.updateJumlah(user, id, change);
        }
        return "redirect:/keranjang";
    }

    // 6. SET JUMLAH MANUAL (INPUT KEYBOARD)
    @GetMapping("/keranjang/set/{id}")
    public String setJumlah(@PathVariable Integer id,
            @RequestParam Integer jumlah,
            @CookieValue(value = "USER_TOKEN", required = false) String token) {

        User user = userService.getUserFromToken(token);
        if (user != null) {
            keranjangService.setJumlahPasti(user, id, jumlah);
        }
        return "redirect:/keranjang";
    }
}