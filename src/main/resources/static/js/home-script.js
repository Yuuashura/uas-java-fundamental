// home-script.js - ALL JavaScript functionality for home page
document.addEventListener('DOMContentLoaded', function() {
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
    
    console.log('Home Script Initialized:', { 
        currentPage, 
        totalPages, 
        keyword: config.keyword,
        kategori: config.kategori 
    });
    
    // ==================== INITIALIZE ALL MODULES ====================
    initializeCommonFeatures();
    initializeInfiniteScroll();
    initializeCardAnimations();
    initializeSearchForm();
    initializeDropdowns();
    
    // ==================== COMMON FEATURES ====================
    function initializeCommonFeatures() {
        // Back to Top Button
        const backToTop = document.getElementById('backToTop');
        if (backToTop) {
            window.addEventListener('scroll', () => {
                if (window.scrollY > 300) {
                    backToTop.classList.add('visible');
                } else {
                    backToTop.classList.remove('visible');
                }
            });
            
            backToTop.addEventListener('click', () => {
                window.scrollTo({
                    top: 0,
                    behavior: 'smooth'
                });
            });
        }
        
        // Mobile Menu Toggle
        const menuToggle = document.getElementById('menuToggle');
        const navMenu = document.getElementById('navMenu');
        
        if (menuToggle && navMenu) {
            menuToggle.addEventListener('click', () => {
                navMenu.classList.toggle('active');
                menuToggle.innerHTML = navMenu.classList.contains('active') 
                    ? '<span class="material-symbols-outlined">close</span>' 
                    : '<span class="material-symbols-outlined">menu</span>';
            });
            
            // Close menu when clicking outside
            document.addEventListener('click', (e) => {
                if (!navMenu.contains(e.target) && !menuToggle.contains(e.target)) {
                    navMenu.classList.remove('active');
                    menuToggle.innerHTML = '<span class="material-symbols-outlined">menu</span>';
                }
            });
        }
        
        // Mobile Search Button
        const searchMobileBtn = document.getElementById('searchMobileBtn');
        const searchForm = document.getElementById('searchForm');
        
        if (searchMobileBtn && searchForm) {
            searchMobileBtn.addEventListener('click', () => {
                searchForm.scrollIntoView({ behavior: 'smooth' });
                const input = searchForm.querySelector('input[name="keyword"]');
                if (input) {
                    input.focus();
                }
            });
        }
        
        // Quick Actions
        document.querySelectorAll('.quick-action').forEach(btn => {
            btn.addEventListener('click', (e) => {
                if (btn.getAttribute('onclick')) return; // Skip if already has onclick
                showToast('Aksi berhasil!', 'success');
            });
        });
        
        // Toast System
        initializeToastSystem();
    }
    
    // ==================== DROPDOWNS ====================
    function initializeDropdowns() {
        // User dropdown toggle
        const userDropdown = document.getElementById('userDropdown');
        if (userDropdown) {
            const dropdownToggle = userDropdown.querySelector('.flex.items-center.gap-1');
            if (dropdownToggle) {
                dropdownToggle.addEventListener('click', (e) => {
                    e.stopPropagation();
                    userDropdown.classList.toggle('active');
                });
            }
        }
        
        // Close dropdowns when clicking outside
        document.addEventListener('click', function(event) {
            const dropdowns = document.querySelectorAll('.dropdown');
            dropdowns.forEach(dropdown => {
                if (!dropdown.contains(event.target)) {
                    dropdown.classList.remove('active');
                }
            });
        });
        
        // Close dropdown on escape key
        document.addEventListener('keydown', function(event) {
            if (event.key === 'Escape') {
                document.querySelectorAll('.dropdown').forEach(dropdown => {
                    dropdown.classList.remove('active');
                });
            }
        });
    }
    
    // ==================== SEARCH FORM ====================
    function initializeSearchForm() {
        const searchForm = document.getElementById('searchForm');
        if (!searchForm) return;
        
        // Form submission animation
        searchForm.addEventListener('submit', function(e) {
            const submitBtn = this.querySelector('.search-btn');
            if (submitBtn) {
                const originalHTML = submitBtn.innerHTML;
                submitBtn.innerHTML = '<span class="material-symbols-outlined">search</span> Mencari...';
                submitBtn.disabled = true;
                
                // Show loading overlay
                showLoadingOverlay();
                
                // Restore button after 3 seconds (fallback)
                setTimeout(() => {
                    submitBtn.innerHTML = originalHTML;
                    submitBtn.disabled = false;
                }, 3000);
            }
        });
        
        // Auto-submit on filter changes (optional)
        const filterSelects = searchForm.querySelectorAll('select');
        filterSelects.forEach(select => {
            select.addEventListener('change', function() {
                // Add visual feedback
                this.style.boxShadow = '0 0 0 3px rgba(162, 155, 254, 0.3)';
                setTimeout(() => {
                    this.style.boxShadow = '';
                }, 500);
            });
        });
    }
    
    // ==================== INFINITE SCROLL ====================
    function initializeInfiniteScroll() {
        // Only setup if there are more pages
        if (totalPages <= 1) {
            console.log('Infinite scroll disabled: only one page');
            return;
        }
        
        const endOfList = document.getElementById('end-of-list');
        const loadingIndicator = document.getElementById('loading-indicator');
        
        if (!endOfList || !loadingIndicator) {
            console.warn('Required elements for infinite scroll not found');
            return;
        }
        
        const observer = new IntersectionObserver(async (entries) => {
            if (entries[0].isIntersecting && !isLoading && currentPage < totalPages) {
                console.log('Loading more products, page:', currentPage + 1);
                await loadMoreProducts();
            }
        }, {
            root: null,
            rootMargin: '100px',
            threshold: 0.1
        });
        
        observer.observe(endOfList);
        
        // Load more products function
        async function loadMoreProducts() {
            if (isLoading) return;
            
            isLoading = true;
            loadingIndicator.style.display = 'block';
            
            try {
                // Build URL with current parameters
                let url = `/api/products/load?page=${currentPage + 1}`;
                
                if (config.keyword) url += `&keyword=${encodeURIComponent(config.keyword)}`;
                if (config.kategori) url += `&kategori=${encodeURIComponent(config.kategori)}`;
                if (config.sortBy) url += `&sortBy=${encodeURIComponent(config.sortBy)}`;
                
                console.log('Fetching from:', url);
                
                const response = await fetch(url, {
                    headers: {
                        'Accept': 'application/json',
                        'X-Requested-With': 'XMLHttpRequest'
                    }
                });
                
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                
                const products = await response.json();
                
                if (products && products.length > 0) {
                    console.log(`Loaded ${products.length} more products`);
                    renderProducts(products);
                    currentPage++;
                    
                    // Update product count
                    updateProductCount();
                    
                    // Check if we've reached the end
                    if (currentPage >= totalPages) {
                        showEndOfListMessage();
                    }
                } else {
                    console.log('No more products to load');
                    showEndOfListMessage();
                }
                
            } catch (error) {
                console.error('Error loading products:', error);
                showErrorMessage(error.message);
                
                // Re-enable loading after error
                currentPage--;
            } finally {
                isLoading = false;
                loadingIndicator.style.display = 'none';
            }
        }
        
        // Render new products to the grid
        function renderProducts(products) {
            const productContainer = document.getElementById('product-container');
            
            // Remove empty state if it exists
            const emptyState = productContainer.querySelector('.empty-state');
            if (emptyState && products.length > 0) {
                emptyState.remove();
            }
            
            products.forEach((product, index) => {
                const productCard = createProductCard(product);
                
                // Add animation delay
                productCard.style.animationDelay = `${index * 0.1}s`;
                productCard.classList.add('fade-in');
                
                productContainer.appendChild(productCard);
            });
        }
        
        // Create product card HTML element
        function createProductCard(product) {
            const card = document.createElement('div');
            card.className = 'product-card';
            
            // Format price
            const formattedPrice = new Intl.NumberFormat('id-ID').format(product.harga || 0);
            
            // Determine rarity
            const rarity = product.harga > 1000000 ? 'Legendary' :
                          product.harga > 500000 ? 'Epic' :
                          product.harga > 100000 ? 'Rare' : 'Common';
            
            // Image HTML
            let imageHtml;
            if (product.gambarProduct) {
                imageHtml = `
                    <img src="data:image/jpeg;base64,${product.gambarProduct}" 
                         class="card-img-top" 
                         alt="${product.namaProduct}"
                         loading="lazy">
                `;
            } else {
                imageHtml = `
                    <div class="card-img-top placeholder-image">
                        <span class="material-symbols-outlined">image_not_supported</span>
                    </div>
                `;
            }
            
            // Card HTML
            card.innerHTML = `
                <a href="/produk/${product.idProduct}" class="product-link">
                    <div class="card-img-wrapper">
                        ${imageHtml}
                        <span class="card-badge">${product.kategoriProduct || 'Uncategorized'}</span>
                    </div>
                    <div class="card-body">
                        <div class="product-rarity">
                            <span class="material-symbols-outlined star-icon">star</span>
                            <span class="material-symbols-outlined star-icon">star</span>
                            <span class="material-symbols-outlined star-icon">star</span>
                            <span class="material-symbols-outlined star-icon">star</span>
                            <span class="rarity-label">${rarity}</span>
                        </div>
                        <h3 class="product-title">${product.namaProduct}</h3>
                        <div class="card-footer">
                            <span class="price-formatted">${formattedPrice}</span>
                            <span class="material-symbols-outlined arrow-icon">arrow_circle_right</span>
                        </div>
                    </div>
                </a>
            `;
            
            return card;
        }
        
        // Update product count display
        function updateProductCount() {
            const productCount = document.querySelectorAll('.product-card').length;
            const countElement = document.querySelector('.product-count');
            if (countElement) {
                countElement.textContent = `${productCount} produk ditemukan`;
            }
        }
        
        // Show end of list message
        function showEndOfListMessage() {
            const endOfList = document.getElementById('end-of-list');
            if (endOfList) {
                endOfList.innerHTML = `
                    <div class="end-message">
                        <div class="end-icon">
                            <span class="material-symbols-outlined">check_circle</span>
                        </div>
                        <h4>Semua Barang Telah Dimuat</h4>
                        <p>Anda telah melihat semua ${document.querySelectorAll('.product-card').length} barang</p>
                    </div>
                `;
            }
        }
        
        // Show error message
        function showErrorMessage(message) {
            const endOfList = document.getElementById('end-of-list');
            if (endOfList) {
                endOfList.innerHTML = `
                    <div class="error-message">
                        <div class="error-icon">
                            <span class="material-symbols-outlined">error</span>
                        </div>
                        <h4>Gagal Memuat Data</h4>
                        <p>${message || 'Terjadi kesalahan saat memuat data'}</p>
                        <button onclick="window.location.reload()" class="btn btn-outline">
                            <span class="material-symbols-outlined">refresh</span> Coba Lagi
                        </button>
                    </div>
                `;
            }
        }
    }
    
    // ==================== CARD ANIMATIONS ====================
    function initializeCardAnimations() {
        // Initial animation for existing cards
        const cards = document.querySelectorAll('.product-card');
        cards.forEach((card, index) => {
            card.style.animationDelay = `${index * 0.05}s`;
            card.classList.add('fade-in');
            
            // Add hover effects
            card.addEventListener('mouseenter', function() {
                const arrow = this.querySelector('.arrow-icon');
                if (arrow) {
                    arrow.style.transform = 'translateX(8px)';
                }
            });
            
            card.addEventListener('mouseleave', function() {
                const arrow = this.querySelector('.arrow-icon');
                if (arrow) {
                    arrow.style.transform = 'translateX(0)';
                }
            });
        });
        
        // Button hover effects
        document.querySelectorAll('.btn').forEach(btn => {
            btn.addEventListener('mouseenter', function() {
                this.style.transform = 'translateY(-3px)';
            });
            
            btn.addEventListener('mouseleave', function() {
                this.style.transform = 'translateY(0)';
            });
        });
    }
    
    // ==================== TOAST SYSTEM ====================
    function initializeToastSystem() {
        window.showToast = function(message, type = 'info', duration = 5000) {
            const container = document.getElementById('toastContainer');
            if (!container) {
                console.warn('Toast container not found');
                return;
            }
            
            const toast = document.createElement('div');
            toast.className = `toast ${type}`;
            
            // Icon based on type
            const icons = {
                success: 'check_circle',
                error: 'error',
                warning: 'warning',
                info: 'info'
            };
            
            toast.innerHTML = `
                <span class="material-symbols-outlined">${icons[type] || 'info'}</span>
                <span class="toast-message">${message}</span>
                <button class="toast-close" onclick="this.parentElement.remove()">
                    <span class="material-symbols-outlined">close</span>
                </button>
            `;
            
            container.appendChild(toast);
            
            // Auto remove after duration
            setTimeout(() => {
                if (toast.parentNode) {
                    toast.style.animation = 'slideOut 0.3s forwards';
                    setTimeout(() => toast.remove(), 300);
                }
            }, duration);
        };
    }
    
    // ==================== HELPER FUNCTIONS ====================
    function showLoadingOverlay() {
        const loadingOverlay = document.createElement('div');
        loadingOverlay.className = 'loading-overlay';
        loadingOverlay.innerHTML = `
            <div class="spinner-large"></div>
            <p>Memproses pencarian...</p>
        `;
        document.body.appendChild(loadingOverlay);
        
        // Auto remove after 5 seconds (safety)
        setTimeout(() => {
            if (loadingOverlay.parentNode) {
                loadingOverlay.remove();
            }
        }, 5000);
    }
    
    // Expose showToast globally for onclick handlers
    window.showToast = showToast;
});