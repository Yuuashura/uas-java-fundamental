document.addEventListener("DOMContentLoaded", function () {
    
    // CONFIG
    const totalPages = window.appConfig.totalPages;
    const keyword = window.appConfig.keyword;
    const kategori = window.appConfig.kategori; // <--- AMBIL VAR KATEGORI
    const sortBy = window.appConfig.sortBy;

    let currentPage = 1; 
    let isLoading = false;

    // ELEMENTS
    const loadingIndicator = document.getElementById('loading-indicator');
    const productContainer = document.getElementById('product-container');
    const endOfListElement = document.getElementById('end-of-list');

    // LOAD DATA FUNCTION
    async function loadMoreProducts() {
        if (isLoading || currentPage >= totalPages) return;

        isLoading = true;
        loadingIndicator.style.display = 'block';

        try {
            // Bangun URL dengan Kategori
            let url = `/api/products/load?page=${currentPage}`;
            if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`;
            if (kategori) url += `&kategori=${encodeURIComponent(kategori)}`; // <--- KIRIM KE API
            if (sortBy) url += `&sortBy=${encodeURIComponent(sortBy)}`;

            const response = await fetch(url);
            if (!response.ok) throw new Error("Gagal mengambil data");

            const products = await response.json();

            if (products.length > 0) {
                renderProducts(products);
                currentPage++;
            } else {
                showEndOfList();
            }

        } catch (error) {
            console.error("Error loading products:", error);
        } finally {
            isLoading = false;
            if (currentPage >= totalPages) {
                showEndOfList();
            } else {
                loadingIndicator.style.display = 'none';
            }
        }
    }
    
    // ... (Sisa fungsi renderProducts dan observer TETAP SAMA) ...
    // Pastikan copy-paste fungsi renderProducts yang sudah ada sebelumnya.
    
    function renderProducts(products) {
        // (Gunakan kode renderProducts yang saya berikan di jawaban sebelumnya)
        products.forEach(p => {
             // ... kode render HTML card ...
             // Agar tidak terlalu panjang di sini, pakai logika render yang sama persis
             // seperti di jawaban sebelumnya (bagian home-script.js).
             const col = document.createElement('div');
             col.className = 'col-6 col-md-3 mb-4';
             // ... dst ...
             
             // Handle Gambar
            const imgSrc = p.gambarProduct 
                ? `data:image/jpeg;base64,${p.gambarProduct}` 
                : null;
            
            let imgElement = '';
            if (imgSrc) {
                imgElement = `<img src="${imgSrc}" class="card-img-top" alt="${p.namaProduct}">`;
            } else {
                imgElement = `
                <div class="card-img-top d-flex align-items-center justify-content-center bg-dark bg-opacity-50">
                    <span class="text-white-50 small">Gambar Hilang</span>
                </div>`;
            }

            const cat = p.kategoriProduct || 'Item';

            col.innerHTML = `
                <a href="/produk/${p.idProduct}" class="text-decoration-none text-white d-block">
                    <div class="card card-product h-100">
                        <div class="card-img-wrapper">
                            ${imgElement}
                            <span class="position-absolute top-0 end-0 badge bg-dark bg-opacity-75 m-2 border border-secondary"
                                style="font-size: 0.65rem; backdrop-filter: blur(5px);">
                                ${cat}
                            </span>
                        </div>
                        <div class="card-body d-flex flex-column">
                            <small class="product-cat mb-1">Rarity ★★★★</small>
                            <h6 class="product-title">${p.namaProduct}</h6>
                            <div class="mt-auto pt-3 border-top border-light border-opacity-10 d-flex justify-content-between align-items-center">
                                <span class="price-tag">Rp ${p.harga}</span>
                                <span class="text-info opacity-75"><i class="bi bi-arrow-right-circle-fill fs-5"></i></span>
                            </div>
                        </div>
                    </div>
                </a>
            `;
            productContainer.appendChild(col);
        });
    }

    function showEndOfList() {
        loadingIndicator.style.display = 'block';
        loadingIndicator.innerHTML = "<p class='text-center text-white-50 mt-4 opacity-50'>--- Batas Wilayah Inazuma ---</p>";
        observer.disconnect();
    }

    const observer = new IntersectionObserver((entries) => {
        if (entries[0].isIntersecting) {
            loadMoreProducts();
        }
    }, { rootMargin: '100px' });

    if (currentPage < totalPages) {
        observer.observe(endOfListElement);
    } else {
        loadingIndicator.style.display = 'none';
    }
});