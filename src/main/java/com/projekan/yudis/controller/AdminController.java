package com.projekan.yudis.controller;

import com.projekan.yudis.model.Produk;
import com.projekan.yudis.model.User;
import com.projekan.yudis.service.ProdukService;
import com.projekan.yudis.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;

@Controller
@RequestMapping("/admin") // Semua URL diawali /admin
public class AdminController {

    @Autowired
    private ProdukService produkService;

    @Autowired
    private UserService userService;

    @Autowired
    private com.projekan.yudis.service.ProvinsiService provinsiService;

    // METHOD BANTUAN: Cek apakah user adalah ADMIN
    private boolean isAdmin(String token) {
        User user = userService.getUserFromToken(token);
        return user != null && user.getRole() == User.Role.ADMIN;
    }

  @GetMapping("/produk")
    public String listProduk(Model model, 
                             @CookieValue(value = "USER_TOKEN", required = false) String token,
                             @RequestParam(value = "keyword", required = false) String keyword,
                             @RequestParam(value = "sortBy", required = false) String sortBy,
                             @RequestParam(value = "page", defaultValue = "0") int page) { // <--- PARAMETER BARU (Default hal 0)
        
        if (!isAdmin(token)) return "redirect:/login";

        int pageSize = 12; // Tampilkan 12 produk per halaman

        // Panggil Service Pagination
        Page<Produk> pageProduk = produkService.getProdukPaged(keyword, sortBy, page, pageSize);

        model.addAttribute("listProduk", pageProduk); // Kirim Page object
        model.addAttribute("user", userService.getUserFromToken(token));
        
        // Balikin param biar navigasi tetap jalan
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageProduk.getTotalPages());
        
        return "admin/list_produk"; 
    }

    // 2. FORM TAMBAH PRODUK
    @GetMapping("/produk/tambah")
    public String formTambah(Model model,
            @CookieValue(value = "USER_TOKEN", required = false) String token) {
        if (!isAdmin(token))
            return "redirect:/login";

        model.addAttribute("produk", new Produk());
        return "admin/form_produk";
    }

    // 3. PROSES SIMPAN
    @PostMapping("/produk/simpan")
    public String simpanProduk(@ModelAttribute Produk produk,
            @RequestParam("fileGambar") MultipartFile file,
            @CookieValue(value = "USER_TOKEN", required = false) String token) {
        if (!isAdmin(token))
            return "redirect:/login";

        produkService.saveProduk(produk, file);
        return "redirect:/admin/produk";
    }

    // 4. HAPUS PRODUK
    @GetMapping("/produk/hapus/{id}")
    public String hapusProduk(@PathVariable Integer id,
            @CookieValue(value = "USER_TOKEN", required = false) String token) {
        if (!isAdmin(token))
            return "redirect:/login";

        produkService.deleteProduk(id);
        return "redirect:/admin/produk";
    }

    // Jangan lupa @Autowired TransaksiService di atas (biasanya sudah ada di
    // TransaksiController, tapi ini AdminController)
    @Autowired
    private com.projekan.yudis.service.TransaksiService transaksiService;

    // 5. HALAMAN KELOLA TRANSAKSI
    @GetMapping("/transaksi")
    public String listTransaksi(Model model,
            @CookieValue(value = "USER_TOKEN", required = false) String token) {
        if (!isAdmin(token))
            return "redirect:/login";

        model.addAttribute("listTransaksi", transaksiService.getAllTransaksi());
        model.addAttribute("user", userService.getUserFromToken(token));

        return "admin/list_transaksi"; // File HTML baru
    }

    // 6. PROSES UBAH STATUS
    @GetMapping("/transaksi/status/{id}")
    public String ubahStatus(@PathVariable Integer id,
            @RequestParam String status, // PAID, SENT, CANCEL
            @CookieValue(value = "USER_TOKEN", required = false) String token) {
        if (!isAdmin(token))
            return "redirect:/login";

        // Konversi String ke Enum
        try {
            com.projekan.yudis.model.Transaksi.Status statEnum = com.projekan.yudis.model.Transaksi.Status
                    .valueOf(status);

            transaksiService.updateStatus(id, statEnum);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        return "redirect:/admin/transaksi";
    }

    // 7. TAMPILKAN FORM EDIT
    @GetMapping("/produk/edit/{id}")
    public String formEdit(@PathVariable Integer id, Model model,
            @CookieValue(value = "USER_TOKEN", required = false) String token) {
        // Cek Admin (Wajib)
        // ... (asumsi logika isAdmin ada di method helper atau copy dari atas) ...

        Produk produk = produkService.getProdukById(id);
        model.addAttribute("produk", produk);
        return "admin/form_produk"; // Kita pakai ulang form yang sama
    }

    // 8. PROSES UPDATE
    @PostMapping("/produk/update")
    public String prosesUpdate(@ModelAttribute Produk produk,
            @RequestParam("fileGambar") MultipartFile file,
            @CookieValue(value = "USER_TOKEN", required = false) String token) {
        // Cek Admin...

        produkService.updateProduk(produk, file);
        return "redirect:/admin/produk";
    }

    // ... autowired userService, provinsiService ...

    // ================= MANAJEMEN USER =================

    @GetMapping("/users")
    public String listUsers(Model model, @CookieValue(value = "USER_TOKEN", required = false) String token) {
        if (!isAdmin(token)) return "redirect:/login";
        
        model.addAttribute("listUser", userService.getAllUsers());
        return "admin/list_user";
    }

    @GetMapping("/users/hapus/{id}")
    public String hapusUser(@PathVariable Integer id, @CookieValue(value = "USER_TOKEN", required = false) String token) {
        if (!isAdmin(token)) return "redirect:/login";
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/users/promote/{id}")
    public String promoteUser(@PathVariable Integer id, @CookieValue(value = "USER_TOKEN", required = false) String token) {
        if (!isAdmin(token)) return "redirect:/login";
        userService.promoteToAdmin(id);
        return "redirect:/admin/users";
    }
    
    @GetMapping("/users/demote/{id}")
    public String demoteUser(@PathVariable Integer id, @CookieValue(value = "USER_TOKEN", required = false) String token) {
        if (!isAdmin(token)) return "redirect:/login";
        userService.demoteToUser(id);
        return "redirect:/admin/users";
    }

    // ================= MANAJEMEN PROVINSI =================

    @GetMapping("/provinsi")
    public String listProvinsi(Model model, @CookieValue(value = "USER_TOKEN", required = false) String token) {
        if (!isAdmin(token)) return "redirect:/login";
        
        model.addAttribute("listProvinsi", provinsiService.getAllProvinsi());
        return "admin/list_provinsi";
    }

    @GetMapping("/provinsi/tambah")
    public String formTambahProvinsi(Model model, @CookieValue(value = "USER_TOKEN", required = false) String token) {
        if (!isAdmin(token)) return "redirect:/login";
        
        model.addAttribute("provinsi", new com.projekan.yudis.model.Provinsi());
        return "admin/form_provinsi";
    }

    @GetMapping("/provinsi/edit/{id}")
    public String formEditProvinsi(@PathVariable Integer id, Model model, @CookieValue(value = "USER_TOKEN", required = false) String token) {
        if (!isAdmin(token)) return "redirect:/login";
        
        model.addAttribute("provinsi", provinsiService.getProvinsiById(id));
        return "admin/form_provinsi";
    }

    @PostMapping("/provinsi/simpan")
    public String simpanProvinsi(@ModelAttribute com.projekan.yudis.model.Provinsi provinsi, @CookieValue(value = "USER_TOKEN", required = false) String token) {
        if (!isAdmin(token)) return "redirect:/login";
        
        provinsiService.saveProvinsi(provinsi);
        return "redirect:/admin/provinsi";
    }

    @GetMapping("/provinsi/hapus/{id}")
    public String hapusProvinsi(@PathVariable Integer id, @CookieValue(value = "USER_TOKEN", required = false) String token) {
        if (!isAdmin(token)) return "redirect:/login";
        
        provinsiService.deleteProvinsi(id);
        return "redirect:/admin/provinsi";
    }
}