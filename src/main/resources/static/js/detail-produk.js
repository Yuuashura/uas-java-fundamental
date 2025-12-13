/**
 * detail-produk.js
 * Logic untuk halaman Detail Produk (Validasi Stok & Spinner)
 */

// Ambil elemen input
const input = document.getElementById('inputJumlah');

// Helper: Ambil Stok Maksimal dari atribut HTML 'data-max-stock'
function getMaxStock() {
    return parseInt(input.getAttribute('data-max-stock')) || 0;
}

// ==========================================
// 1. FUNGSI UTAMA: VALIDASI SAAT FORM DI-SUBMIT
// ==========================================
function validateStock() {
    let val = parseInt(input.value) || 1;
    const maxStock = getMaxStock();

    if (val > maxStock) {
        let setMax = confirm("Stok hanya tersisa " + maxStock + ". Ambil semua?");

        if (setMax) {
            // Jika pilih OK, set ke max dan lanjut submit
            input.value = maxStock;
            return true;
        } else {
            // Jika Cancel, reset ke 1 dan batalkan submit
            input.value = 1;
            alert("Jumlah pesanan direset ke 1. Silakan coba lagi.");
            return false; // Mencegah form submit
        }
    }
    return true;
}

// ==========================================
// 2. FUNGSI INTERAKSI (TOMBOL +/- DAN KETIK)
// ==========================================

function ubahQty(change) {
    let val = parseInt(input.value) || 1;
    val += change;
    checkBounds(val);
}

function validasiManual() {
    let val = parseInt(input.value);
    if (isNaN(val)) val = 1;
    checkBounds(val);
}

function checkBounds(val) {
    const maxStock = getMaxStock();

    if (val < 1) val = 1;

    // Batasi input secara visual agar tidak melebihi stok
    if (val > maxStock) {
        val = maxStock;
    }
    input.value = val;
}

// ==========================================
// 3. INISIALISASI SAAT LOAD
// ==========================================
document.addEventListener('DOMContentLoaded', () => {
    if(input) {
        checkBounds(parseInt(input.value));
    }
});