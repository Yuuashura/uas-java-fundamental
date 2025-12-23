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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @Autowired
    private TransaksiDetailRepository transaksiDetailRepository;

    // 1. HALAMAN FORM CHECKOUT
    // Menerima list ID Keranjang dari checkbox
    @GetMapping("/checkout")
    public String halamanCheckout(@RequestParam(name = "idKeranjang") List<Integer> idKeranjangList,
            @CookieValue(value = "USER_TOKEN", required = false) String token,
            Model model) {

        User user = userService.getUserFromToken(token);
        if (user == null)
            return "redirect:/login";

        // Ambil data barang yang dipilih saja
        List<Keranjang> listBarangDipilih = keranjangRepository.findAllById(idKeranjangList);

        // Hitung subtotal sementara
        int subtotal = listBarangDipilih.stream().mapToInt(k -> k.getHarga() * k.getJumlah()).sum();

        model.addAttribute("user", user);
        model.addAttribute("listBarang", listBarangDipilih);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("listProvinsi", provinsiRepository.findAllByOrderByNamaProvinsiAsc()); // Untuk dropdown ongkir

        return "checkout"; // Ke file checkout.html
    }

    // 2. PROSES TOMBOL "BAYAR SEKARANG"
    @PostMapping("/transaksi/proses")
    public String prosesCheckout(@RequestParam(name = "idKeranjang") List<Integer> idKeranjangList,
            @RequestParam Integer idProvinsi,
            @RequestParam String kabupaten,
            @RequestParam String kecamatan,
            @RequestParam String detailAlamat,
            @CookieValue(value = "USER_TOKEN", required = false) String token) {

        User user = userService.getUserFromToken(token);

        // GABUNGKAN ALAMAT DI SINI
        String alamatLengkapGabungan = detailAlamat + ", Kec. " + kecamatan + ", Kab/Kota " + kabupaten;

        // Panggil Service dengan alamat yang sudah digabung
        Transaksi transaksi = transaksiService.buatTransaksi(user, idKeranjangList, idProvinsi, alamatLengkapGabungan);

        return "redirect:/transaksi/" + transaksi.getIdTransaksi();
    }

    // 3. HALAMAN DETAIL TRANSAKSI (Nota)
    @GetMapping("/transaksi/{id}")
    public String detailTransaksi(@PathVariable Integer id, Model model,
            @CookieValue(value = "USER_TOKEN", required = false) String token) {

        User user = userService.getUserFromToken(token);
        if (user == null)
            return "redirect:/login"; // Cek login dasar

        Transaksi transaksi = transaksiService.getTransaksiById(id);
        if (transaksi == null)
            return "redirect:/";

        // VALIDASI AKSES & LOGIKA TOMBOL KEMBALI
        String backUrl = "/riwayat"; // Default untuk User

        if (user.getRole() == User.Role.ADMIN) {
            // Jika ADMIN: Boleh lihat punya siapa saja & kembali ke dashboard admin
            backUrl = "/admin/transaksi";
        } else {
            // Jika USER BIASA: Hanya boleh lihat punya sendiri
            if (!transaksi.getUser().getIdUser().equals(user.getIdUser())) {
                return "/riwayat"; // Tendang jika coba intip punya orang
            }
        }

        // Kirim backUrl ke HTML
        model.addAttribute("backUrl", backUrl);

        // Ambil Detail Barang
        List<TransaksiDetail> details = transaksiDetailRepository.findByTransaksi(transaksi);
        model.addAttribute("detailTransaksi", details);

        model.addAttribute("transaksi", transaksi);
        model.addAttribute("user", user);

        return "detail-transaksi";
    }

    // 4. HALAMAN RIWAYAT BELANJA
    @GetMapping("/riwayat")
    public String riwayatBelanja(Model model,
            @CookieValue(value = "USER_TOKEN", required = false) String token) {

        User user = userService.getUserFromToken(token);
        if (user == null)
            return "redirect:/login";

        // Ambil list transaksi milik user ini
        List<Transaksi> listTransaksi = transaksiService.getTransaksiByUser(user);

        // Urutkan dari yang terbaru (ID terbesar di atas)
        listTransaksi.sort((t1, t2) -> t2.getIdTransaksi().compareTo(t1.getIdTransaksi()));

        model.addAttribute("listTransaksi", listTransaksi);
        model.addAttribute("user", user);

        return "riwayat";
    }

    @GetMapping("/riwayat/terima/{id}")
    public String selesaikanPesanan(@PathVariable("id") int idTransaksi,
            @CookieValue(value = "USER_TOKEN", required = false) String token,
            RedirectAttributes redirectAttributes) {
        User user = userService.getUserFromToken(token);
        if (user == null) {
            return "redirect:/login";
        }
        try {
            transaksiService.selesaikanPesanan(user, idTransaksi);
            redirectAttributes.addFlashAttribute("success", "Terima kasih! Pesanan telah dikonfirmasi diterima.");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Terjadi kesalahan saat menyelesaikan pesanan.");
        }
        return "redirect:/riwayat";
    }

    @GetMapping("/transaksi/batal/{id}")
    public String batalkanPesananUser(@PathVariable Integer id,
            @CookieValue(value = "USER_TOKEN", required = false) String token,
            RedirectAttributes redirectAttributes) {

        User user = userService.getUserFromToken(token);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            transaksiService.batalkanTransaksiOlehUser(id, user.getIdUser());
            redirectAttributes.addFlashAttribute("success", "Pesanan berhasil dibatalkan. Stok telah dikembalikan.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/riwayat";
    }

}