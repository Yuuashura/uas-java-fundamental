# 📦 Genshin Vision Goods - Merchant Management System

[![Java Version](https://img.shields.io/badge/Java-17%2B-orange)](https://www.oracle.com/java/)
[![Theme](https://img.shields.io/badge/Theme-Genshin--Impact-blue)](https://genshin.hoyoverse.com/)

Program berbasis Java CLI ini dirancang khusus untuk mengelola inventaris dan transaksi pada merchant merchandise **Genshin Impact**, mulai dari penjualan *Action Figures*, *Clothing (Apparel)*, hingga aksesoris koleksi lainnya.

---

## 🌟 Deskripsi Proyek
Proyek ini dibuat sebagai pemenuhan tugas **UAS Java Fundamental**. Aplikasi ini mensimulasikan sistem manajemen toko merchandise yang memungkinkan admin untuk mengelola stok barang dan memproses transaksi pelanggan dengan tema dunia Teyvat.

## 🛠️ Fitur Utama
* **Inventory Management**: Menambah, melihat, dan memperbarui stok merchandise (Figure, Baju, Gantungan Kunci).
* **Transaction System**: Menghitung total belanjaan berdasarkan kategori barang.
* **Categorization**: Pengelompokan barang berdasarkan region (Mondstadt, Liyue, Inazuma, dll - *Opsional*).
* **Receipt Generator**: Mencetak struk pembelian sederhana di terminal.

## 💻 Konsep Java yang Diterapkan
Untuk memenuhi standar **Java Fundamental**, program ini menggunakan:
1.  **Object-Oriented Programming (OOP)**:
    * `Merchandise` (Superclass)
    * `Clothing`, `Figure` (Subclasses - Inheritance)
2.  **Collection/Array**: Menyimpan daftar barang yang tersedia.
    * `ArrayList` untuk dinamis stok.
3.  **Exception Handling**: Validasi input (misalnya input angka untuk harga/stok).
4.  **Control Flow**: Perulangan untuk menu utama dan percabangan untuk pemilihan kategori.

## 📁 Struktur Class (Rencana)
* `Main.java`: Entry point aplikasi dan logika menu.
* `Item.java`: Class induk untuk atribut umum (Nama, Harga, Stok).
* `Figure.java`: Class khusus figure (Atribut tambahan: Skala, Karakter).
* `Clothing.java`: Class khusus baju (Atribut tambahan: Ukuran, Bahan).
* `Transaction.java`: Logika perhitungan total harga.

## 🚀 Cara Menjalankan
1.  **Clone Repository**
    ```bash
    git clone [https://github.com/Yuuashura/uas-java-fundamental.git](https://github.com/Yuuashura/uas-java-fundamental.git)
    ```
2.  **Compile**
    ```bash
    javac src/*.java -d bin
    ```
3.  **Run**
    ```bash
    java -cp bin Main
    ```

---

## 👤 Identitas Mahasiswa
* **Nama**: Yudistira Syaputra
* **NIM**: 02032411037
* **Program Studi**: Teknik Informatika
* **Kampus**: Universitas Nasional Pasim
* **Organisasi**: Pemberdayaam Umat Berkelanjutan (PUB)

---
*"Ad Astra Abyssosque!"* – Dibuat dengan ❤️ oleh [Yuuashura](https://github.com/Yuuashura)
