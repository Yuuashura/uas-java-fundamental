/**
 * checkout.js
 * Logika perhitungan Grand Total (Subtotal + Ongkir) secara dinamis
 */

function hitungGrandTotal() {
    const select = document.getElementById('selectProvinsi');
    const selectedOption = select.options[select.selectedIndex];
    
    // Ambil data ongkir dari atribut HTML
    const ongkir = parseInt(selectedOption.getAttribute('data-ongkir')) || 0;
    
    // Ambil subtotal dari input hidden
    const subtotal = parseInt(document.getElementById('serverSubtotal').value) || 0;
    
    const grandTotal = subtotal + ongkir;

    // Helper: Format Rupiah
    const formatRupiah = (num) => 'Rp ' + num.toLocaleString('id-ID');

    // Update Tampilan
    document.getElementById('displayOngkir').innerText = formatRupiah(ongkir);
    document.getElementById('displayGrandTotal').innerText = formatRupiah(grandTotal);
}

// Jalankan saat halaman selesai dimuat
document.addEventListener('DOMContentLoaded', () => {
    hitungGrandTotal();
});