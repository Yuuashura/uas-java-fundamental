// home-script.js - Optimized & Fixed
document.addEventListener('DOMContentLoaded', function () {
    // ==================== CONFIGURATION ====================
    const config = window.appConfig || {
        totalPages: 1,
        keyword: '',
        kategori: '',
        sortBy: '',
        currentPage: 1,
        totalProducts: 0
    };

    // ==================== STATE VARIABLES ====================
    let isLoading = false;
    let currentPage = config.currentPage || 1;
    const totalPages = config.totalPages || 1;

    console.log('Home Script Initialized:', config);

    // ==================== INITIALIZE ALL MODULES ====================
    initializeCommonFeatures();
    initializeDropdowns();
    initializeSearchForm();
    initializeInfiniteScroll(); // Hanya aktif jika totalPages > 1
    initializeAnimations();

    // ==================== COMMON FEATURES ====================
    function initializeCommonFeatures() {
        // 1. Back to Top Button
        const backToTop = document.getElementById('backToTop');
        if (backToTop) {
            window.addEventListener('scroll', () => {
                if (window.scrollY > 300) {
                    backToTop.classList.add('visible');
                } else {
                    backToTop.classList.remove('visible');
                }
            });

            backToTop.addEventListener('click', (e) => {
                e.preventDefault();
                window.scrollTo({ top: 0, behavior: 'smooth' });
            });
        }

        // 2. Mobile Menu (Safety Check)
        const menuToggle = document.getElementById('menuToggle');
        const navMenu = document.getElementById('navMenu');
        if (menuToggle && navMenu) {
            menuToggle.addEventListener('click', () => {
                navMenu.classList.toggle('active');
                menuToggle.innerHTML = navMenu.classList.contains('active')
                    ? '<span class="material-symbols-outlined">close</span>'
                    : '<span class="material-symbols-outlined">menu</span>';
            });
        }

        // 3. Initialize Toast Container (Create if missing)
        let toastContainer = document.getElementById('toastContainer');
        if (!toastContainer) {
            toastContainer = document.createElement('div');
            toastContainer.id = 'toastContainer';
            toastContainer.style.cssText = 'position: fixed; bottom: 20px; right: 20px; z-index: 9999; display: flex; flex-direction: column; gap: 10px;';
            document.body.appendChild(toastContainer);
        }
    }

    // ==================== DROPDOWNS ====================
    function initializeDropdowns() {
        // User Dropdown Logic
        const userDropdown = document.getElementById('userDropdown');
        if (userDropdown) {
            // Hapus onclick di HTML jika ada, kita handle via JS biar bersih
            const trigger = userDropdown.querySelector('.flex.items-center.gap-1');
            if (trigger) {
                trigger.removeAttribute('onclick'); // Bersihkan inline handler
                trigger.addEventListener('click', (e) => {
                    e.stopPropagation();
                    userDropdown.classList.toggle('active');
                });
            }
        }

        // Close dropdowns when clicking outside
        document.addEventListener('click', (event) => {
            const dropdowns = document.querySelectorAll('.dropdown.active');
            dropdowns.forEach(dropdown => {
                if (!dropdown.contains(event.target)) {
                    dropdown.classList.remove('active');
                }
            });
        });

        // Close on Escape key
        document.addEventListener('keydown', (event) => {
            if (event.key === 'Escape') {
                document.querySelectorAll('.dropdown.active').forEach(d => d.classList.remove('active'));
            }
        });
    }

    // ==================== SEARCH FORM ====================
    function initializeSearchForm() {
        const searchForm = document.getElementById('searchForm');
        if (!searchForm) return;

        searchForm.addEventListener('submit', function (e) {
            // Cari tombol submit dengan tipe submit (lebih aman daripada cari class)
            const submitBtn = this.querySelector('button[type="submit"]');
            
            if (submitBtn) {
                const originalContent = submitBtn.innerHTML;
                const originalWidth = submitBtn.offsetWidth; // Simpan lebar biar gak loncat
                
                submitBtn.style.width = `${originalWidth}px`;
                submitBtn.innerHTML = '<div class="spinner" style="width: 20px; height: 20px; border-width: 2px; margin: 0;"></div>';
                submitBtn.disabled = true;

                // Tampilkan loading overlay global
                showLoadingOverlay();

                // Safety fallback: kembalikan tombol jika server lambat (5 detik)
                setTimeout(() => {
                    if(submitBtn.disabled) {
                         submitBtn.innerHTML = originalContent;
                         submitBtn.disabled = false;
                         submitBtn.style.width = '';
                    }
                }, 5000);
            }
        });
    }

    // ==================== INFINITE SCROLL ====================
    function initializeInfiniteScroll() {
        // Cek apakah halaman > 1, jika tidak, fitur ini tidak perlu jalan
        if (totalPages <= 1) return;

        const endOfList = document.getElementById('end-of-list');
        const loadingIndicator = document.getElementById('loading-indicator');

        if (!endOfList || !loadingIndicator) return;

        const observer = new IntersectionObserver(async (entries) => {
            if (entries[0].isIntersecting && !isLoading && currentPage < totalPages) {
                await loadMoreProducts();
            }
        }, { rootMargin: '200px' }); // Load 200px sebelum sampai bawah

        observer.observe(endOfList);

        async function loadMoreProducts() {
            if (isLoading) return;

            isLoading = true;
            loadingIndicator.style.display = 'block';

            try {
                // Pastikan Anda memiliki Controller yang menangani URL ini dan mengembalikan JSON
                let url = `/api/products/load?page=${currentPage + 1}`;
                if (config.keyword) url += `&keyword=${encodeURIComponent(config.keyword)}`;
                if (config.kategori) url += `&kategori=${encodeURIComponent(config.kategori)}`;
                if (config.sortBy) url += `&sortBy=${encodeURIComponent(config.sortBy)}`;

                const response = await fetch(url, {
                    headers: { 'X-Requested-With': 'XMLHttpRequest' }
                });

                if (!response.ok) throw new Error('Gagal memuat data');

                const products = await response.json();
                console.log('Loaded products:', products);

                if (products && products.length > 0) {
                    renderProducts(products);
                    currentPage++;
                    
                    // Update counter produk
                    const countEl = document.querySelector('.product-count');
                    const totalNow = document.querySelectorAll('.product-card').length;
                    if(countEl) countEl.textContent = `${totalNow} produk ditampilkan`;

                    if (currentPage >= totalPages) {
                        showEndOfListMessage();
                        observer.disconnect();
                    }
                } else {
                    showEndOfListMessage();
                    observer.disconnect();
                }

            } catch (error) {
                console.error('Error:', error);
                // Jangan show error toast agar tidak mengganggu UX scroll, cukup log
            } finally {
                isLoading = false;
                loadingIndicator.style.display = 'none';
            }
        }

        function renderProducts(products) {
            const container = document.getElementById('product-container');
            
            products.forEach((p, index) => {
                const card = createProductCard(p);
                card.style.animationDelay = `${index * 0.1}s`; // Staggered animation
                container.appendChild(card);
            });
        }

        // FUNGSI PENTING: Membuat HTML yang PERSIS sama dengan index.html agar CSS masuk
        function createProductCard(p) {
            const card = document.createElement('div');
            card.className = 'product-card';
            
            // Logic Rarity
            let rarityLabel = 'Common';
            let rarityClass = 'common';
            if (p.harga > 1000000) { rarityLabel = 'Legendary'; rarityClass = 'legendary'; }
            else if (p.harga > 500000) { rarityLabel = 'Epic'; rarityClass = 'epic'; }
            else if (p.harga > 100000) { rarityLabel = 'Rare'; rarityClass = 'rare'; }
            
            card.setAttribute('data-rarity', rarityClass);

            // Logic Image
            let imgHTML = '';
            if (p.gambarProduct) {
                imgHTML = `<img src="data:image/jpeg;base64,${p.gambarProduct}" class="card-img-top" alt="${p.namaProduct}" loading="lazy">`;
            } else {
                imgHTML = `
                <div class="card-img-top flex justify-center items-center" style="background: linear-gradient(45deg, #2d3436, #636e72); color: #b2bec3;">
                    <span class="material-symbols-outlined" style="font-size: 3rem;">image_not_supported</span>
                </div>`;
            }

            // Format Harga Rupiah
            const hargaFormatted = new Intl.NumberFormat('id-ID').format(p.harga);

            card.innerHTML = `
                <a href="/produk/${p.idProduct}">
                    <div class="card-img-wrapper">
                        ${imgHTML}
                        <span class="card-badge">${p.kategoriProduct || 'Item'}</span>
                    </div>

                    <div class="card-body">
                        <div class="product-rarity">
                            <span class="material-symbols-outlined">star</span>
                            <span class="material-symbols-outlined">star</span>
                            <span class="material-symbols-outlined">star</span>
                            <span class="material-symbols-outlined">star</span>
                            <span>${rarityLabel}</span>
                        </div>

                        <h3 class="product-title">${p.namaProduct}</h3>

                        <div class="card-footer">
                            <span class="price">Rp ${hargaFormatted}</span>
                            <span class="material-symbols-outlined">arrow_circle_right</span>
                        </div>
                    </div>
                </a>
            `;
            return card;
        }

        function showEndOfListMessage() {
            const endDiv = document.getElementById('end-of-list');
            if (endDiv) {
                endDiv.innerHTML = `
                    <div style="text-align: center; color: var(--text-muted); margin-top: 20px;">
                        <span class="material-symbols-outlined">check_circle</span>
                        <p>Anda telah mencapai dasar Abyss (Semua produk dimuat)</p>
                    </div>
                `;
            }
        }
    }

    // ==================== ANIMATIONS ====================
    function initializeAnimations() {
        // Hapus logic mouseenter/mouseleave disini karena CSS (index.css) sudah menanganinya.
        // JS Animation hanya untuk initial load (fade in)
        
        const cards = document.querySelectorAll('.product-card');
        cards.forEach((card, index) => {
            // Gunakan requestAnimationFrame untuk performa lebih baik
            requestAnimationFrame(() => {
                card.style.animationDelay = `${index * 0.05}s`;
                card.style.opacity = '1'; // Pastikan opacity direset jika CSS gagal
            });
        });
    }

    // ==================== GLOBAL HELPERS ====================
    window.showToast = function (message, type = 'info') {
        const container = document.getElementById('toastContainer');
        if (!container) return;

        const toast = document.createElement('div');
        
        // Style toast secara dinamis agar tidak tergantung CSS eksternal jika belum ada
        const colors = {
            success: '#20c997',
            error: '#ff6b6b',
            warning: '#ffc107',
            info: '#00b4d8'
        };
        
        toast.style.cssText = `
            background: rgba(30, 20, 60, 0.95);
            border-left: 4px solid ${colors[type] || colors.info};
            color: #fff;
            padding: 15px 20px;
            border-radius: 8px;
            box-shadow: 0 5px 15px rgba(0,0,0,0.5);
            display: flex; align-items: center; gap: 10px;
            min-width: 300px;
            animation: slideInRight 0.3s ease-out;
            border: 1px solid rgba(255,255,255,0.1);
        `;

        const iconMap = { success: 'check_circle', error: 'error', warning: 'warning', info: 'info' };

        toast.innerHTML = `
            <span class="material-symbols-outlined" style="color: ${colors[type]}">${iconMap[type]}</span>
            <span style="flex: 1; font-size: 0.95rem;">${message}</span>
            <button onclick="this.parentElement.remove()" style="background:none; border:none; color:#aaa; cursor:pointer;">
                <span class="material-symbols-outlined">close</span>
            </button>
        `;

        container.appendChild(toast);

        // Auto remove
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(100%)';
            toast.style.transition = 'all 0.3s';
            setTimeout(() => toast.remove(), 300);
        }, 5000);
    };

    function showLoadingOverlay() {
        // Cek jika overlay sudah ada
        if(document.querySelector('.loading-overlay')) return;

        const overlay = document.createElement('div');
        overlay.className = 'loading-overlay';
        overlay.style.cssText = `
            position: fixed; inset: 0; background: rgba(10, 5, 20, 0.85);
            backdrop-filter: blur(5px); z-index: 9999;
            display: flex; flex-direction: column; align-items: center; justify-content: center;
        `;
        
        overlay.innerHTML = `
            <div class="spinner-large" style="
                width: 60px; height: 60px; 
                border: 4px solid rgba(162, 155, 254, 0.2); 
                border-top-color: #a29bfe; border-radius: 50%; 
                animation: spin 1s linear infinite;"></div>
            <p style="margin-top: 20px; color: #fff; font-family: 'Cinzel', serif;">Memproses Permintaan...</p>
        `;
        
        document.body.appendChild(overlay);

        // Auto remove safety
        setTimeout(() => {
            if(overlay.parentElement) overlay.remove();
        }, 8000);
    }
});