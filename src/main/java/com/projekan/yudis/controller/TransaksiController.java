package com.projekan.yudis.controller;

import com.projekan.yudis.model.*;
import com.projekan.yudis.repository.KeranjangRepository;
import com.projekan.yudis.repository.ProvinsiRepository;
import com.projekan.yudis.repository.TransaksiDetailRepository;
import com.projekan.yudis.service.TransaksiService;
import com.projekan.yudis.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class TransaksiController {

    @Autowired
    private UserService userService;

    @Autowired
    private KeranjangRepository keranjangRepository;

    @Autowired
    private ProvinsiRepository provinsiRepository;

    @Autowired
    private TransaksiService transaksiService;

    

    // 1. HALAMAN FORM CHECKOUT
    // Menerima list ID Keranjang dari checkbox
    @GetMapping("/checkout")
    public String halamanCheckout(@RequestParam(name = "idKeranjang") List<Integer> idKeranjangList,
                                  @CookieValue(value = "USER_TOKEN", required = false) String token,
                                  Model model) {
        
        User user = userService.getUserFromToken(token);
        if (user == null) return "redirect:/login";

        // Ambil data barang yang dipilih saja
        List<Keranjang> listBarangDipilih = keranjangRepository.findAllById(idKeranjangList);
        
        // Hitung subtotal sementara
        int subtotal = listBarangDipilih.stream().mapToInt(k -> k.getHarga() * k.getJumlah()).sum();

        model.addAttribute("user", user);
        model.addAttribute("listBarang", listBarangDipilih);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("listProvinsi", provinsiRepository.findAll()); // Untuk dropdown ongkir
        
        return "checkout"; // Ke file checkout.html
    }

    // 2. PROSES TOMBOL "BAYAR SEKARANG"
    @PostMapping("/transaksi/proses")
    public String prosesCheckout(@RequestParam(name = "idKeranjang") List<Integer> idKeranjangList,
                                 @RequestParam Integer idProvinsi,
                                 @RequestParam String alamatLengkap,
                                 @CookieValue(value = "USER_TOKEN", required = false) String token) {
        
        User user = userService.getUserFromToken(token);
        
        // Panggil Service Sakti kita
        Transaksi transaksi = transaksiService.buatTransaksi(user, idKeranjangList, idProvinsi, alamatLengkap);

        // Redirect ke halaman detail transaksi (Nota)
        return "redirect:/transaksi/" + transaksi.getIdTransaksi();
    }

 // Import repository detail
    @Autowired
    private TransaksiDetailRepository transaksiDetailRepository; // <-- Tambahkan ini di atas

    // ...

    @GetMapping("/transaksi/{id}")
    public String detailTransaksi(@PathVariable Integer id, Model model,
                                  @CookieValue(value = "USER_TOKEN", required = false) String token) {
        
        User user = userService.getUserFromToken(token);
        Transaksi transaksi = transaksiService.getTransaksiById(id);

        // Validasi: Hanya pemilik transaksi yang boleh lihat
        if (transaksi == null || !transaksi.getUser().getIdUser().equals(user.getIdUser())) {
            return "redirect:/";
        }

        // === TAMBAHAN BARU: AMBIL LIST DETAIL BARANG ===
        List<TransaksiDetail> details = transaksiDetailRepository.findByTransaksi(transaksi);
        model.addAttribute("detailTransaksi", details);
        // ===============================================

        model.addAttribute("transaksi", transaksi);
        model.addAttribute("user", user);
        return "detail-transaksi";
    }

    @GetMapping("/riwayat")
    public String riwayatBelanja(Model model, 
                                 @CookieValue(value = "USER_TOKEN", required = false) String token) {
        
        User user = userService.getUserFromToken(token);
        if (user == null) return "redirect:/login";

        // Ambil list transaksi milik user ini
        List<Transaksi> listTransaksi = transaksiService.getTransaksiByUser(user);
        
        // (Opsional) Urutkan dari yang terbaru (ID terbesar di atas)
        listTransaksi.sort((t1, t2) -> t2.getIdTransaksi().compareTo(t1.getIdTransaksi()));

        model.addAttribute("listTransaksi", listTransaksi);
        model.addAttribute("user", user);
        
        return "riwayat"; // Mengarah ke file riwayat.html
    }
    // 5. PROSES KONFIRMASI DITERIMA
    @GetMapping("/transaksi/terima/{id}")
    public String terimaPesanan(@PathVariable Integer id,
                                @CookieValue(value = "USER_TOKEN", required = false) String token) {
        User user = userService.getUserFromToken(token);
        if (user != null) {
            transaksiService.selesaikanPesanan(user, id);
        }
        return "redirect:/riwayat";
    }
}