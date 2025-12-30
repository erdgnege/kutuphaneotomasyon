// API Base URL
const API_BASE_URL = 'http://localhost:8080/api';

// Global değişkenler
let selectedKitapId = null;
let oduncModal = null;
let loginModal = null;
let addBookModal = null;
let deleteModal = null;
let bookIdToDelete = null;
let authToken = null; // JWT token
let sessionTimer = null; // Session timer interval
let sessionExpiryTime = null; // Token expire zamanı

// Sayfa yüklendiğinde
document.addEventListener('DOMContentLoaded', function() {
    // Bootstrap modal instance oluştur
    oduncModal = new bootstrap.Modal(document.getElementById('oduncModal'));
    loginModal = new bootstrap.Modal(document.getElementById('loginModal'));
    addBookModal = new bootstrap.Modal(document.getElementById('addBookModal'));
    deleteModal = new bootstrap.Modal(document.getElementById('deleteBookModal'));
    
    // Login modal açıldığında blur efekti ekle
    const loginModalElement = document.getElementById('loginModal');
    loginModalElement.addEventListener('show.bs.modal', function() {
        document.body.classList.add('modal-open', 'login-modal-active');
        // Backdrop'u özelleştir
        setTimeout(() => {
            const backdrop = document.querySelector('.modal-backdrop');
            if (backdrop) {
                backdrop.style.backdropFilter = 'blur(5px)';
                backdrop.style.webkitBackdropFilter = 'blur(5px)';
            }
        }, 10);
    });
    
    // Login modal kapandığında blur efekti kaldır
    loginModalElement.addEventListener('hidden.bs.modal', function() {
        document.body.classList.remove('modal-open', 'login-modal-active');
    });
    
    // Add Book modal kapandığında blur efekti kaldır
    const addBookModalElement = document.getElementById('addBookModal');
    if (addBookModalElement) {
        addBookModalElement.addEventListener('hidden.bs.modal', function() {
            // Backdrop'u temizle
            const backdrop = document.querySelector('.modal-backdrop');
            if (backdrop && !document.querySelector('#loginModal.show')) {
                backdrop.remove();
                document.body.classList.remove('modal-open');
                document.body.style.overflow = '';
                document.body.style.paddingRight = '';
            }
        });
    }
    
    // "Admin değil misiniz?" linkine event listener ekle
    setTimeout(() => {
        const adminDegilLink = document.querySelector('[onclick*="switchToUserPage"]');
        if (adminDegilLink) {
            adminDegilLink.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                switchToUserPage(e);
            });
        }
    }, 500);
    
    // Kullanıcı sayfasından geliyorsa, kullanıcı oturumunu temizle
    // (Sadece admin sayfasından kullanıcı sayfasına geçiş yapıldığında)
    // Sayfa yenilendiğinde oturum korunmalı, bu yüzden burada temizleme yapmıyoruz
    
    // Kaydedilmiş token'ı kontrol et
    const savedToken = localStorage.getItem('adminToken');
    if (savedToken) {
        authToken = savedToken;
        // Token expire zamanını kontrol et
        if (isTokenExpired(authToken)) {
            // Token süresi dolmuş
            localStorage.removeItem('adminToken');
            authToken = null;
            // Modal instance'ın hazır olmasını bekle
            setTimeout(() => {
                showLoginModal();
            }, 100);
        } else {
            // Token geçerli, timer başlat
            startSessionTimer();
            // Token ile test isteği gönder
            testAuth();
        }
    } else {
        // Modal instance'ın hazır olmasını bekle
        setTimeout(() => {
            showLoginModal();
        }, 100);
    }
    
    // Tab değiştiğinde ilgili verileri yükle
    const loansTab = document.getElementById('loans-tab');
    if (loansTab) {
        loansTab.addEventListener('shown.bs.tab', function() {
            loadLoans();
        });
    }
    
    const usersTab = document.getElementById('users-tab');
    if (usersTab) {
        usersTab.addEventListener('shown.bs.tab', function() {
            loadAllUsers();
        });
    }
    
    const logsTab = document.getElementById('logs-tab');
    if (logsTab) {
        logsTab.addEventListener('shown.bs.tab', function() {
            loadAllLoans();
        });
    }
    
    const notificationsTab = document.getElementById('notifications-tab');
    if (notificationsTab) {
        notificationsTab.addEventListener('shown.bs.tab', function() {
            loadNotifications();
        });
    }
    
    // Periyodik olarak bildirim sayısını kontrol et (30 saniyede bir)
    setInterval(checkNotificationCount, 30000);
    checkNotificationCount(); // İlk yüklemede de kontrol et

    // Kitap silme onayı butonu dinleyicisi
    document.getElementById('confirmDeleteBtn').addEventListener('click', async function() {
        if (!bookIdToDelete) return;
        
        const btn = this;
        const originalText = btn.innerHTML;
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Siliniyor...';

        try {
            const response = await fetch(`${API_BASE_URL}/kitap/sil/${bookIdToDelete}`, {
                method: 'DELETE',
                headers: getAuthHeaders()
            });

            if (response.ok) {
                deleteModal.hide();
                loadBooks();
                showAlert('Kitap başarıyla silindi.', 'success');
            } else {
                let errorMessage = 'Silme işlemi başarısız oldu.';
                try {
                    const data = await response.json();
                    if (data.message) errorMessage = data.message;
                } catch (e) { /* JSON parse hatası olursa varsayılan mesaj kalsın */ }
                throw new Error(errorMessage);
            }
        } catch (error) {
            console.error('Silme hatası:', error);
            showAlert(error.message, 'danger');
        } finally {
            btn.disabled = false;
            btn.innerHTML = originalText;
            bookIdToDelete = null;
        }
    });
});

// Login modalını göster
function showLoginModal() {
    // mainContent'i gizle
    const mainContent = document.getElementById('mainContent');
    if (mainContent) {
        mainContent.style.display = 'none';
    }
    
    // Modal'ı göster (eğer modal instance hazırsa)
    if (loginModal) {
        try {
            loginModal.show();
        } catch (error) {
            console.error('Modal gösterilirken hata:', error);
            // Modal henüz hazır değilse, modal elementini direkt kullan
            const loginModalElement = document.getElementById('loginModal');
            if (loginModalElement) {
                const bsModal = new bootstrap.Modal(loginModalElement);
                loginModal = bsModal;
                bsModal.show();
            }
        }
    } else {
        // Modal instance yoksa oluştur
        const loginModalElement = document.getElementById('loginModal');
        if (loginModalElement) {
            loginModal = new bootstrap.Modal(loginModalElement);
            loginModal.show();
        }
    }
}

// Token'ı test et
async function testAuth() {
    try {
        const response = await fetch(`${API_BASE_URL}/kitap`, {
            method: 'GET',
            headers: getAuthHeaders()
        });
        
        if (response.ok) {
            // Token geçerli, içeriği göster
            // Önce modal'ı kapat
            if (loginModal) {
                try {
                    loginModal.hide();
                } catch (error) {
                    console.error('Modal kapatılırken hata:', error);
                }
            }
            
            // Ana içeriği göster
            const mainContent = document.getElementById('mainContent');
            if (mainContent) {
                mainContent.style.display = 'block';
            }
            
            // Verileri yükle
            loadBooks();
            loadUsers(); // Ödünç verme modalı için
            
            // Timer başlat (eğer başlamadıysa)
            if (!sessionTimer) {
                startSessionTimer();
            }
        } else if (response.status === 401) {
            // Token geçersiz veya süresi dolmuş, login modalını göster
            if (sessionTimer) {
                clearInterval(sessionTimer);
                sessionTimer = null;
            }
            const timerElement = document.getElementById('sessionTimer');
            if (timerElement) {
                timerElement.style.display = 'none';
            }
            localStorage.removeItem('adminToken');
            authToken = null;
            sessionExpiryTime = null;
            showLoginModal();
        }
    } catch (error) {
        console.error('Auth test hatası:', error);
        showLoginModal();
    }
}

// Admin girişi (Sabit kullanıcı adı/şifre ile)
async function adminLogin() {
    const username = document.getElementById('usernameInput').value.trim();
    const password = document.getElementById('passwordInput').value;
    const alertEl = document.getElementById('loginAlert');
    const submitButton = document.querySelector('#loginModal .btn-primary');
    const spinner = submitButton.querySelector('.spinner-border');
    
    if (!username || !password) {
        alertEl.className = 'alert alert-danger';
        alertEl.textContent = 'Lütfen kullanıcı adı ve şifre giriniz.';
        alertEl.classList.remove('d-none');
        return;
    }
    
    // Loading göster
    submitButton.disabled = true;
    spinner.classList.add('active');
    alertEl.classList.add('d-none');
    
    try {
        // Admin login endpoint'ine istek gönder
        const response = await fetch(`${API_BASE_URL}/admin/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                username: username,
                password: password
            })
        });
        
        if (response.ok) {
            // Token'ı al
            const authResponse = await response.json();
            authToken = authResponse.token;
            
            // Token'ı localStorage'a kaydet
            localStorage.setItem('adminToken', authToken);
            
            // Token expire zamanını parse et ve timer başlat
            startSessionTimer();
            
            alertEl.className = 'alert alert-success';
            alertEl.textContent = 'Giriş başarılı!';
            alertEl.classList.remove('d-none');
            
            setTimeout(() => {
                loginModal.hide();
                document.getElementById('mainContent').style.display = 'block';
                loadBooks();
                loadUsers();
            }, 500);
        } else if (response.status === 401) {
            alertEl.className = 'alert alert-danger';
            alertEl.textContent = 'Kullanıcı adı veya şifre hatalı!';
            alertEl.classList.remove('d-none');
        } else {
            const errorData = await response.json().catch(() => ({ message: 'Giriş yapılamadı' }));
            throw new Error(errorData.message || 'Giriş yapılamadı');
        }
    } catch (error) {
        console.error('Login hatası:', error);
        alertEl.className = 'alert alert-danger';
        alertEl.textContent = 'Giriş yapılırken bir hata oluştu: ' + error.message;
        alertEl.classList.remove('d-none');
    } finally {
        submitButton.disabled = false;
        spinner.classList.remove('active');
    }
}

// Session timer'ı başlat
function startSessionTimer() {
    // Önce mevcut timer'ı temizle
    if (sessionTimer) {
        clearInterval(sessionTimer);
    }
    
    // Token'ı decode et (JWT base64 formatında)
    try {
        const tokenParts = authToken.split('.');
        if (tokenParts.length !== 3) {
            console.error('Geçersiz token formatı');
            return;
        }
        
        // Payload'ı decode et
        const payload = JSON.parse(atob(tokenParts[1]));
        
        // Expiration zamanını al (Unix timestamp - saniye cinsinden)
        const exp = payload.exp;
        sessionExpiryTime = exp * 1000; // JavaScript'te milisaniye cinsinden
        
        // Timer'ı göster
        const timerElement = document.getElementById('sessionTimer');
        if (timerElement) {
            timerElement.style.display = 'inline-block';
        }
        
        // Her saniye güncelle
        updateTimer();
        sessionTimer = setInterval(updateTimer, 1000);
        
    } catch (error) {
        console.error('Token parse hatası:', error);
    }
}

// Timer'ı güncelle
function updateTimer() {
    if (!sessionExpiryTime) return;
    
    const now = Date.now();
    const remaining = sessionExpiryTime - now;
    
    if (remaining <= 0) {
        // Süre doldu, otomatik logout
        clearInterval(sessionTimer);
        sessionTimer = null;
        autoLogout();
        return;
    }
    
    // Kalan süreyi dakika:saniye formatında göster
    const minutes = Math.floor(remaining / 60000);
    const seconds = Math.floor((remaining % 60000) / 1000);
    
    const timerText = document.getElementById('timerText');
    if (timerText) {
        timerText.textContent = `${minutes}:${seconds.toString().padStart(2, '0')}`;
    }
    
    // Son 30 saniyede uyarı göster (kırmızı renk)
    const timerElement = document.getElementById('sessionTimer');
    if (timerElement) {
        if (remaining <= 30000) {
            timerElement.className = 'badge bg-danger text-white me-2';
        } else {
            timerElement.className = 'badge bg-warning text-dark me-2';
        }
    }
}

// Token'ın expire olup olmadığını kontrol et
function isTokenExpired(token) {
    try {
        const tokenParts = token.split('.');
        if (tokenParts.length !== 3) {
            return true;
        }
        
        const payload = JSON.parse(atob(tokenParts[1]));
        const exp = payload.exp * 1000; // Milisaniye cinsinden
        
        return Date.now() >= exp;
    } catch (error) {
        console.error('Token parse hatası:', error);
        return true;
    }
}

// Otomatik logout
function autoLogout() {
    // Timer'ı gizle
    const timerElement = document.getElementById('sessionTimer');
    if (timerElement) {
        timerElement.style.display = 'none';
    }
    
    // Session bilgilerini temizle
    localStorage.removeItem('adminToken');
    authToken = null;
    sessionExpiryTime = null;
    
    // Ana içeriği gizle
    document.getElementById('mainContent').style.display = 'none';
    
    // Form alanlarını temizle
    document.getElementById('usernameInput').value = '';
    document.getElementById('passwordInput').value = '';
    
    // Uyarı göster
    showAlert('Oturum süreniz doldu. Lütfen tekrar giriş yapın.', 'warning');
    
    // Login modalını göster
    showLoginModal();
}

// Çıkış yap
function logout() {
    if (confirm('Çıkış yapmak istediğinize emin misiniz?')) {
        // Timer'ı temizle
        if (sessionTimer) {
            clearInterval(sessionTimer);
            sessionTimer = null;
        }
        
        // Timer'ı gizle
        const timerElement = document.getElementById('sessionTimer');
        if (timerElement) {
            timerElement.style.display = 'none';
        }
        
        localStorage.removeItem('adminToken');
        authToken = null;
        sessionExpiryTime = null;
        document.getElementById('mainContent').style.display = 'none';
        document.getElementById('usernameInput').value = '';
        document.getElementById('passwordInput').value = '';
        showLoginModal();
    }
}

// Şifre görünürlüğünü değiştir
function togglePasswordVisibility() {
    const passwordInput = document.getElementById('passwordInput');
    const passwordToggleIcon = document.getElementById('passwordToggleIcon');
    
    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        passwordToggleIcon.classList.remove('bi-eye');
        passwordToggleIcon.classList.add('bi-eye-slash');
    } else {
        passwordInput.type = 'password';
        passwordToggleIcon.classList.remove('bi-eye-slash');
        passwordToggleIcon.classList.add('bi-eye');
    }
}

// Kullanıcı sayfasına geç (admin oturumunu temizle)
function switchToUserPage(event) {
    if (event) {
        event.preventDefault();
        event.stopPropagation();
    }
    
    // Timer'ı temizle
    if (sessionTimer) {
        clearInterval(sessionTimer);
        sessionTimer = null;
    }
    
    // Timer'ı gizle
    const timerElement = document.getElementById('sessionTimer');
    if (timerElement) {
        timerElement.style.display = 'none';
    }
    
    // Modal açıksa kapat
    if (loginModal) {
        try {
            loginModal.hide();
        } catch (e) {
            console.log('Modal close error:', e);
        }
    }
    
    // Admin sayfasından kullanıcı sayfasına geçiş yapıldığını işaretle
    sessionStorage.setItem('fromAdminPage', 'true');
    
    // Admin oturumunu temizle
    localStorage.removeItem('adminToken');
    authToken = null;
    sessionExpiryTime = null;
    
    // Kullanıcı oturumunu da temizle
    localStorage.removeItem('kullaniciId');
    localStorage.removeItem('kullaniciEmail');
    localStorage.removeItem('userToken');
    
    // Kullanıcı sayfasına yönlendir
    window.location.href = 'user.html';
}

// Auth header'ı ekle (JWT Bearer Token)
function getAuthHeaders() {
    const headers = {
        'Content-Type': 'application/json'
    };
    
    if (authToken) {
        headers['Authorization'] = 'Bearer ' + authToken;
    }
    
    return headers;
}

// Kitapları yükle
async function loadBooks() {
    const loadingEl = document.getElementById('loadingBooks');
    const booksContainer = document.getElementById('booksContainer');
    const noBooksMessage = document.getElementById('noBooksMessage');
    
    // Loading göster
    loadingEl.style.display = 'block';
    booksContainer.style.display = 'none';
    noBooksMessage.style.display = 'none';
    
    try {
        const response = await fetch(`${API_BASE_URL}/kitap`, {
            headers: getAuthHeaders()
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const kitaplar = await response.json();
        
        // Loading gizle
        loadingEl.style.display = 'none';
        
        if (kitaplar.length === 0) {
            noBooksMessage.style.display = 'block';
            document.getElementById('kitapSayisi').textContent = '0 kitap';
        } else {
            booksContainer.style.display = 'flex';
            renderBooks(kitaplar);
            document.getElementById('kitapSayisi').textContent = `${kitaplar.length} kitap`;
        }
    } catch (error) {
        console.error('Kitaplar yüklenirken hata oluştu:', error);
        loadingEl.style.display = 'none';
        
        // Backend bağlantı hatası kontrolü
        if (error.message.includes('Failed to fetch') || error.message.includes('NetworkError')) {
            showAlert('Backend sunucusuna bağlanılamıyor. Lütfen backend\'in çalıştığından emin olun (http://localhost:8080)', 'danger');
        } else {
            showAlert('Kitaplar yüklenirken bir hata oluştu: ' + error.message, 'danger');
        }
    }
}

// Kitapları render et
function renderBooks(kitaplar) {
    const container = document.getElementById('booksContainer');
    container.innerHTML = '';
    
    kitaplar.forEach(kitap => {
        const bookCard = createBookCard(kitap);
        container.appendChild(bookCard);
    });
}

// Kitap kartı oluştur
function createBookCard(kitap) {
    const col = document.createElement('div');
    col.className = 'col-md-6 col-lg-4';
    
    const mevcutClass = kitap.mevcut ? 'success' : 'danger';
    const mevcutText = kitap.mevcut ? 'Mevcut' : 'Ödünçte';
    const oduncButtonDisabled = kitap.mevcut ? '' : 'disabled';
    
    const kapakGorsel = kitap.kapakUrl 
        ? `<div class="card-img-top d-flex align-items-center justify-content-center" style="height: 300px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 20px;">
            <img src="${escapeHtml(kitap.kapakUrl)}" alt="${escapeHtml(kitap.baslik)}" style="max-height: 100%; max-width: 100%; object-fit: contain; box-shadow: 0 4px 8px rgba(0,0,0,0.3); border-radius: 4px;">
           </div>`
        : `<div class="card-img-top d-flex align-items-center justify-content-center bg-light" style="height: 300px;">
            <i class="bi bi-book" style="font-size: 5rem; color: #ccc;"></i>
           </div>`;
    
    col.innerHTML = `
        <div class="card book-card h-100">
            ${kapakGorsel}
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-start mb-2">
                    <h5 class="card-title mb-0">${escapeHtml(kitap.baslik)}</h5>
                    <span class="badge bg-${mevcutClass} status-badge">${mevcutText}</span>
                </div>
                <p class="text-muted mb-2">
                    <i class="bi bi-person"></i> ${escapeHtml(kitap.yazar)}
                </p>
                <p class="text-muted mb-3">
                    <i class="bi bi-upc"></i> ISBN: ${escapeHtml(kitap.isbn)}
                </p>
                <div class="d-grid gap-2">
                    <button class="btn btn-primary btn-sm" onclick="openOduncModal(${kitap.id}, '${escapeHtml(kitap.baslik)}', '${escapeHtml(kitap.yazar)}')" ${oduncButtonDisabled}>
                        <i class="bi bi-bookmark-plus"></i> Ödünç Ver
                    </button>
                    <button class="btn btn-outline-danger btn-sm" onclick="showDeleteBookModal(${kitap.id})">
                        <i class="bi bi-trash"></i> Kaldır
                    </button>
                </div>
            </div>
        </div>
    `;
    
    return col;
}

// Kullanıcıları yükle
async function loadUsers() {
    try {
        const response = await fetch(`${API_BASE_URL}/kullanicilar/uyeler`, {
            headers: getAuthHeaders()
        });
        
        if (!response.ok) {
            console.warn('Kullanıcılar yüklenemedi, manuel ID girişi kullanılacak');
            return;
        }
        
        const uyeler = await response.json();
        const selectEl = document.getElementById('userIdSelect');
        
        // Dropdown'ı temizle
        selectEl.innerHTML = '<option value="">Kullanıcı seçiniz...</option>';
        
        // Kullanıcıları ekle
        uyeler.forEach(uye => {
            const option = document.createElement('option');
            option.value = uye.id;
            option.textContent = `${uye.adSoyad} (ID: ${uye.id})`;
            selectEl.appendChild(option);
        });
        
        // Dropdown değiştiğinde input'u güncelle
        selectEl.addEventListener('change', function() {
            if (this.value) {
                document.getElementById('userIdInput').value = this.value;
            }
        });
        
    } catch (error) {
        console.error('Kullanıcılar yüklenirken hata oluştu:', error);
    }
}

// Ödünç verme modalını aç
function openOduncModal(kitapId, baslik, yazar) {
    selectedKitapId = kitapId;
    document.getElementById('modalKitapBaslik').textContent = baslik;
    document.getElementById('modalKitapYazar').textContent = 'Yazar: ' + yazar;
    document.getElementById('userIdInput').value = '';
    document.getElementById('userIdSelect').value = '';
    document.getElementById('oduncAlert').classList.add('d-none');
    oduncModal.show();
}

// Ödünç ver
async function oduncVer() {
    const userIdSelect = document.getElementById('userIdSelect').value;
    const userIdInput = document.getElementById('userIdInput').value;
    const userId = userIdSelect || userIdInput;
    const alertEl = document.getElementById('oduncAlert');
    const submitButton = document.querySelector('#oduncModal .btn-primary');
    const spinner = submitButton.querySelector('.spinner-border');
    
    // Validasyon
    if (!userId || userId <= 0) {
        alertEl.className = 'alert alert-danger';
        alertEl.textContent = 'Lütfen bir kullanıcı seçiniz veya geçerli bir kullanıcı ID giriniz.';
        alertEl.classList.remove('d-none');
        return;
    }
    
    // Loading göster
    submitButton.disabled = true;
    spinner.classList.add('active');
    alertEl.classList.add('d-none');
    
    try {
        const response = await fetch(`${API_BASE_URL}/odunc/ver?kitapId=${selectedKitapId}&userId=${userId}`, {
            method: 'POST',
            headers: getAuthHeaders()
        });
        
        const responseData = await response.json();
        
        if (!response.ok) {
            // Backend'den gelen hata mesajını parse et
            let errorMessage = 'Bir hata oluştu';
            if (responseData.message) {
                errorMessage = responseData.message;
            } else if (typeof responseData === 'string') {
                errorMessage = responseData;
            }
            throw new Error(errorMessage);
        }
        
        // Başarı mesajı
        alertEl.className = 'alert alert-success';
        alertEl.textContent = 'Kitap başarıyla ödünç verildi!';
        alertEl.classList.remove('d-none');
        
        // Modal'ı kapat ve kitapları yenile
        setTimeout(() => {
            oduncModal.hide();
            loadBooks();
            // Eğer ödünçler sekmesi aktifse, onu da yenile
            const loansTab = document.getElementById('loans-tab');
            if (loansTab.classList.contains('active')) {
                loadLoans();
            }
        }, 1500);
        
    } catch (error) {
        console.error('Ödünç verme hatası:', error);
        alertEl.className = 'alert alert-danger';
        alertEl.textContent = 'Hata: ' + error.message;
        alertEl.classList.remove('d-none');
    } finally {
        // Loading gizle
        submitButton.disabled = false;
        spinner.classList.remove('active');
    }
}

// Alert göster
function showAlert(message, type = 'info') {
    const alertContainer = document.getElementById('alertContainer');
    const alertId = 'alert-' + Date.now();
    
    const alertHTML = `
        <div id="${alertId}" class="alert alert-${type} alert-dismissible fade show" role="alert">
            ${escapeHtml(message)}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    `;
    
    alertContainer.innerHTML = alertHTML;
    
    // 5 saniye sonra otomatik kapat
    setTimeout(() => {
        const alertEl = document.getElementById(alertId);
        if (alertEl) {
            const bsAlert = new bootstrap.Alert(alertEl);
            bsAlert.close();
        }
    }, 5000);
}

// Ödünç kayıtlarını yükle
async function loadLoans() {
    const loadingEl = document.getElementById('loadingLoans');
    const loansContainer = document.getElementById('loansContainer');
    const noLoansMessage = document.getElementById('noLoansMessage');
    
    // Loading göster
    loadingEl.style.display = 'block';
    loansContainer.style.display = 'none';
    noLoansMessage.style.display = 'none';
    
    try {
        const response = await fetch(`${API_BASE_URL}/odunc/aktif`, {
            headers: getAuthHeaders()
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const oduncler = await response.json();
        
        // Loading gizle
        loadingEl.style.display = 'none';
        
        if (oduncler.length === 0) {
            noLoansMessage.style.display = 'block';
            document.getElementById('oduncSayisi').textContent = '0 ödünç';
        } else {
            loansContainer.style.display = 'block';
            renderLoans(oduncler);
            document.getElementById('oduncSayisi').textContent = `${oduncler.length} ödünç`;
        }
    } catch (error) {
        console.error('Ödünç kayıtları yüklenirken hata oluştu:', error);
        loadingEl.style.display = 'none';
        
        if (error.message.includes('Failed to fetch') || error.message.includes('NetworkError')) {
            showAlert('Backend sunucusuna bağlanılamıyor. Lütfen backend\'in çalıştığından emin olun (http://localhost:8080)', 'danger');
        } else {
            showAlert('Ödünç kayıtları yüklenirken bir hata oluştu: ' + error.message, 'danger');
        }
    }
}

// Tüm işlem loglarını yükle (ödünç + kitap işlemleri)
async function loadAllLoans() {
    const loadingEl = document.getElementById('loadingLogs');
    const logsContainer = document.getElementById('logsContainer');
    const noLogsMessage = document.getElementById('noLogsMessage');
    
    // Loading göster
    loadingEl.style.display = 'block';
    logsContainer.style.display = 'none';
    noLogsMessage.style.display = 'none';
    
    try {
        const response = await fetch(`${API_BASE_URL}/odunc/tum-islem-loglari`, {
            headers: getAuthHeaders()
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const islemLoglari = await response.json();
        
        // Loading gizle
        loadingEl.style.display = 'none';
        
        if (islemLoglari.length === 0) {
            noLogsMessage.style.display = 'block';
            document.getElementById('totalLogsCount').textContent = '0 kayıt';
        } else {
            logsContainer.style.display = 'block';
            renderAllLogs(islemLoglari);
            document.getElementById('totalLogsCount').textContent = `${islemLoglari.length} kayıt`;
        }
    } catch (error) {
        console.error('İşlem logları yüklenirken hata oluştu:', error);
        loadingEl.style.display = 'none';
        
        if (error.message.includes('Failed to fetch') || error.message.includes('NetworkError')) {
            showAlert('Backend sunucusuna bağlanılamıyor. Lütfen backend\'in çalıştığından emin olun (http://localhost:8080)', 'danger');
        } else {
            showAlert('İşlem logları yüklenirken bir hata oluştu: ' + error.message, 'danger');
        }
    }
}

// Tüm işlem loglarını render et (ödünç + kitap işlemleri)
function renderAllLogs(islemLoglari) {
    const container = document.getElementById('logsContainer');
    container.innerHTML = '';
    
    const table = document.createElement('table');
    table.className = 'table table-hover';
    table.innerHTML = `
        <thead>
            <tr>
                <th>İşlem Tipi</th>
                <th>Kitap</th>
                <th>Yazar</th>
                <th>ISBN</th>
                <th>Kullanıcı</th>
                <th>Tarih</th>
                <th>Durum</th>
            </tr>
        </thead>
        <tbody id="logsTableBody">
        </tbody>
    `;
    
    container.appendChild(table);
    const tbody = document.getElementById('logsTableBody');
    
    islemLoglari.forEach(log => {
        const tr = document.createElement('tr');
        
        // İşlem tipi badge'i
        let islemTipiBadge = '';
        switch(log.islemTipi) {
            case 'ODUNC_VER':
                islemTipiBadge = '<span class="badge bg-primary"><i class="bi bi-bookmark-plus"></i> Ödünç Verildi</span>';
                break;
            case 'ODUNC_IADE':
                islemTipiBadge = '<span class="badge bg-success"><i class="bi bi-check-circle"></i> İade Edildi</span>';
                break;
            case 'KITAP_EKLE':
                islemTipiBadge = '<span class="badge bg-info"><i class="bi bi-plus-circle"></i> Kitap Eklendi</span>';
                break;
            case 'KITAP_SIL':
                islemTipiBadge = '<span class="badge bg-danger"><i class="bi bi-trash"></i> Kitap Silindi</span>';
                break;
            default:
                islemTipiBadge = '<span class="badge bg-secondary">' + escapeHtml(log.islemTipi) + '</span>';
        }
        
        // Tarih formatı
        let tarihText = '';
        if (log.islemTarihi) {
            const tarih = new Date(log.islemTarihi);
            tarihText = tarih.toLocaleString('tr-TR', { 
                year: 'numeric', 
                month: '2-digit', 
                day: '2-digit', 
                hour: '2-digit', 
                minute: '2-digit' 
            });
        }
        
        // Kullanıcı bilgisi (ödünç işlemleri için)
        let kullaniciText = '<span class="text-muted">-</span>';
        if (log.kullaniciAdSoyad) {
            kullaniciText = `${escapeHtml(log.kullaniciAdSoyad)}<br><small class="text-muted">${escapeHtml(log.kullaniciEmail)}</small>`;
        }
        
        // Durum (ödünç işlemleri için)
        let durumText = '<span class="text-muted">-</span>';
        if (log.islemTipi === 'ODUNC_VER') {
            durumText = '<span class="badge bg-warning">Aktif</span>';
        } else if (log.islemTipi === 'ODUNC_IADE') {
            durumText = '<span class="badge bg-success">Tamamlandı</span>';
        }
        
        tr.innerHTML = `
            <td>${islemTipiBadge}</td>
            <td><strong>${escapeHtml(log.kitapBaslik)}</strong></td>
            <td>${escapeHtml(log.kitapYazar)}</td>
            <td><code>${escapeHtml(log.kitapIsbn)}</code></td>
            <td>${kullaniciText}</td>
            <td>${tarihText}</td>
            <td>${durumText}</td>
        `;
        
        tbody.appendChild(tr);
    });
}

// Ödünç kayıtlarını render et
function renderLoans(oduncler) {
    const container = document.getElementById('loansContainer');
    container.innerHTML = '';
    
    const ODUNC_SURESI = 30; // Standart ödünç verme süresi (gün)
    
    const table = document.createElement('table');
    table.className = 'table table-hover';
    table.innerHTML = `
        <thead>
            <tr>
                <th>Kitap</th>
                <th>Yazar</th>
                <th>ISBN</th>
                <th>Kullanıcı</th>
                <th>Ödünç Tarihi</th>
                <th>İade Tarihi</th>
                <th>Kalan Süre</th>
                <th>İşlem</th>
            </tr>
        </thead>
        <tbody id="loansTableBody">
        </tbody>
    `;
    
    container.appendChild(table);
    const tbody = document.getElementById('loansTableBody');
    
    oduncler.forEach(odunc => {
        const tr = document.createElement('tr');
        
        const kitap = odunc.kitap;
        const kullanici = odunc.kullanici;
        const oduncTarihi = new Date(odunc.oduncTarihi + 'T00:00:00'); // Tarih parse için
        const bugun = new Date();
        bugun.setHours(0, 0, 0, 0); // Saat bilgisini sıfırla
        
        // Geçen gün sayısı
        const geçenGunSayisi = Math.floor((bugun - oduncTarihi) / (1000 * 60 * 60 * 24));
        
        // Kalan gün sayısı
        const kalanGunSayisi = ODUNC_SURESI - geçenGunSayisi;
        
        // İade tarihi hesapla
        const iadeTarihi = new Date(oduncTarihi);
        iadeTarihi.setDate(iadeTarihi.getDate() + ODUNC_SURESI);
        
        // Badge rengi ve metni belirle
        let badgeClass, badgeText;
        if (kalanGunSayisi < 0) {
            badgeClass = 'bg-danger';
            badgeText = `${Math.abs(kalanGunSayisi)} gün geçmiş`;
        } else if (kalanGunSayisi <= 7) {
            badgeClass = 'bg-danger';
            badgeText = `${kalanGunSayisi} gün kaldı`;
        } else if (kalanGunSayisi <= 14) {
            badgeClass = 'bg-warning';
            badgeText = `${kalanGunSayisi} gün kaldı`;
        } else {
            badgeClass = 'bg-success';
            badgeText = `${kalanGunSayisi} gün kaldı`;
        }
        
        tr.innerHTML = `
            <td><strong>${escapeHtml(kitap.baslik)}</strong></td>
            <td>${escapeHtml(kitap.yazar)}</td>
            <td><code>${escapeHtml(kitap.isbn)}</code></td>
            <td>
                <i class="bi bi-person"></i> ${escapeHtml(kullanici.adSoyad)}
                <br><small class="text-muted">${escapeHtml(kullanici.email)}</small>
            </td>
            <td>
                ${oduncTarihi.toLocaleDateString('tr-TR')}
            </td>
            <td>
                <strong>${iadeTarihi.toLocaleDateString('tr-TR')}</strong>
            </td>
            <td>
                <span class="badge ${badgeClass}">
                    ${badgeText}
                </span>
            </td>
            <td>
                <button class="btn btn-sm btn-success" onclick="iadeAl(${odunc.id})">
                    <i class="bi bi-check-circle"></i> İade Al
                </button>
            </td>
        `;
        
        tbody.appendChild(tr);
    });
}

// İade al
async function iadeAl(oduncId) {
    if (!confirm('Bu kitabı iade almak istediğinize emin misiniz?')) {
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/odunc/iade/${oduncId}`, {
            method: 'PUT',
            headers: getAuthHeaders()
        });
        
        const responseData = await response.json();
        
        if (!response.ok) {
            let errorMessage = 'Bir hata oluştu';
            if (responseData.message) {
                errorMessage = responseData.message;
            } else if (typeof responseData === 'string') {
                errorMessage = responseData;
            }
            throw new Error(errorMessage);
        }
        
        showAlert('Kitap başarıyla iade alındı!', 'success');
        
        // Ödünç listesini ve kitap listesini yenile
        loadLoans();
        loadBooks();
        
    } catch (error) {
        console.error('İade alma hatası:', error);
        showAlert('Hata: ' + error.message, 'danger');
    }
}

// Tüm kullanıcıları yükle
let allUsers = [];
let currentUserFilter = 'all';

async function loadAllUsers() {
    const loadingEl = document.getElementById('loadingUsers');
    const usersContainer = document.getElementById('usersContainer');
    const noUsersMessage = document.getElementById('noUsersMessage');
    
    loadingEl.style.display = 'block';
    usersContainer.style.display = 'none';
    noUsersMessage.style.display = 'none';
    
    try {
        // Kullanıcıları ve aktif ödünçleri paralel olarak yükle
        const [usersResponse, loansResponse] = await Promise.all([
            fetch(`${API_BASE_URL}/kullanicilar/hepsi`, { headers: getAuthHeaders() }),
            fetch(`${API_BASE_URL}/odunc/aktif`, { headers: getAuthHeaders() })
        ]);
        
        if (!usersResponse.ok) {
            throw new Error(`HTTP error! status: ${usersResponse.status}`);
        }
        
        allUsers = await usersResponse.json();
        
        // Aktif ödünçleri kullanıcı ID'lerine göre grupla
        let aktifOduncMap = {};
        if (loansResponse.ok) {
            const aktifOduncler = await loansResponse.json();
            aktifOduncMap = aktifOduncler.reduce((acc, odunc) => {
                const userId = odunc.kullanici?.id || odunc.kullaniciId;
                if (userId) {
                    acc[userId] = (acc[userId] || 0) + 1;
                }
                return acc;
            }, {});
        }
        
        // Her kullanıcıya aktif ödünç sayısını ekle
        allUsers.forEach(user => {
            user.aktifOduncSayisi = aktifOduncMap[user.id] || 0;
        });
        
        // Debug: Gelen veriyi konsola yazdır
        console.log('Gelen kullanıcılar:', allUsers);
        if (allUsers.length > 0) {
            console.log('İlk kullanıcı örneği:', allUsers[0]);
            console.log('dtype değeri:', allUsers[0].dtype, allUsers[0].DTYPE, allUsers[0].class);
        }
        
        loadingEl.style.display = 'none';
        
        if (allUsers.length === 0) {
            noUsersMessage.style.display = 'block';
            updateUserCounts();
        } else {
            usersContainer.style.display = 'block';
            renderUsers(allUsers);
            updateUserCounts();
        }
    } catch (error) {
        console.error('Kullanıcılar yüklenirken hata oluştu:', error);
        loadingEl.style.display = 'none';
        showAlert('Kullanıcılar yüklenirken bir hata oluştu: ' + error.message, 'danger');
    }
}

// Kullanıcı sayılarını güncelle
function updateUserCounts() {
    const uyeler = allUsers.filter(u => {
        const dtype = (u.dtype || u.DTYPE || '').toUpperCase();
        // Üye: dtype UYE
        return dtype === 'UYE';
    });
    const personeller = allUsers.filter(u => {
        const dtype = (u.dtype || u.DTYPE || '').toUpperCase();
        // Personel: dtype PERSONEL
        return dtype === 'PERSONEL';
    });
    
    document.getElementById('totalUsersCount').textContent = `${allUsers.length} kullanıcı`;
    document.getElementById('membersCount').textContent = `${uyeler.length} üye`;
    document.getElementById('personnelCount').textContent = `${personeller.length} personel`;
}

// Kullanıcıları render et
function renderUsers(users) {
    const container = document.getElementById('usersContainer');
    container.innerHTML = '';
    
    const filteredUsers = filterUsersList(users);
    
    if (filteredUsers.length === 0) {
        container.innerHTML = '<div class="alert alert-info">Bu kategoride kullanıcı bulunamadı.</div>';
        return;
    }
    
    const table = document.createElement('table');
    table.className = 'table table-hover';
    table.innerHTML = `
        <thead>
            <tr>
                <th>ID</th>
                <th>Ad Soyad</th>
                <th>E-posta</th>
                <th>Telefon</th>
                <th>Tip</th>
                <th>Kalan Ödünç Limiti</th>
            </tr>
        </thead>
        <tbody id="usersTableBody">
        </tbody>
    `;
    
    container.appendChild(table);
    const tbody = document.getElementById('usersTableBody');
    
        filteredUsers.forEach(user => {
        const tr = document.createElement('tr');
        
        // Tip belirleme: dtype'a bak
        const dtype = (user.dtype || user.DTYPE || '').toUpperCase();
        const hasSicilNo = user.sicilNo !== null && user.sicilNo !== undefined && user.sicilNo !== '';
        
        // Tip belirleme mantığı
        let userType;
        if (dtype === 'UYE') {
            userType = 'UYE';
        } else if (dtype === 'PERSONEL') {
            userType = 'PERSONEL';
        } else {
            // Varsayılan olarak personel kabul et (eğer hiçbir bilgi yoksa)
            userType = 'PERSONEL';
        }
        
        const tipBadge = userType === 'UYE' ? '<span class="badge bg-success">Üye</span>' : '<span class="badge bg-info">Personel</span>';
        const maxLimit = userType === 'UYE' ? 3 : 10; // Personel limiti 10 olmalı (Personel.java'da 10 olarak tanımlı)
        // Personeller için departman bilgisini tip badge'inin yanına ekle
        const tipWithDept = userType === 'PERSONEL' && user.departman 
            ? `${tipBadge}<br><small class="text-muted">${escapeHtml(user.departman)}</small>` 
            : tipBadge;
        
        // Kalan limiti hesapla (async olarak aktif ödünç sayısını al)
        const kalanLimit = user.aktifOduncSayisi !== undefined 
            ? Math.max(0, maxLimit - (user.aktifOduncSayisi || 0))
            : maxLimit; // Eğer aktif ödünç sayısı yoksa, toplam limiti göster
        
        // Kitap ikonu rengi: kalan limit varsa yeşil, yoksa kırmızı
        const iconColorClass = kalanLimit > 0 ? 'book-icon-success' : 'book-icon-danger';
        const iconSize = 'fs-4'; // Daha büyük ikon
        const limitDisplay = kalanLimit > 0 
            ? `<span class="fs-5 fw-bold me-2">${kalanLimit}</span><i class="bi bi-book ${iconColorClass} ${iconSize}"></i>`
            : `<i class="bi bi-book ${iconColorClass} ${iconSize}"></i>`;
        
        // Son 24 saat içinde kayıt olan kullanıcıları kontrol et
        let yeniBadge = '';
        if (user.kayitTarihi) {
            const kayitTarihi = new Date(user.kayitTarihi);
            const simdi = new Date();
            const farkSaat = (simdi - kayitTarihi) / (1000 * 60 * 60); // Saat cinsinden fark
            
            if (farkSaat <= 24) {
                yeniBadge = ' <span class="badge bg-danger">Yeni</span>';
            }
        }
        
        // Tip değiştirme butonları
        let tipChangeButtons = '';
        if (userType === 'UYE') {
            tipChangeButtons = `<button class="btn btn-sm btn-outline-info" onclick="changeUserType(${user.id}, 'PERSONEL')" title="Personel yap">
                <i class="bi bi-person-badge"></i> Personel Yap
            </button>`;
        } else {
            tipChangeButtons = `<button class="btn btn-sm btn-outline-success" onclick="changeUserType(${user.id}, 'UYE')" title="Üye yap">
                <i class="bi bi-person"></i> Üye Yap
            </button>`;
        }
        
        tr.innerHTML = `
            <td><strong>#${user.id}</strong></td>
            <td>${escapeHtml(user.adSoyad)}${yeniBadge}</td>
            <td>${escapeHtml(user.email)}</td>
            <td>${escapeHtml(user.telefon || '-')}</td>
            <td>${tipWithDept}<br>${tipChangeButtons}</td>
            <td>${limitDisplay}</td>
        `;
        
        tbody.appendChild(tr);
    });
}

// Kullanıcı tipini değiştir
async function changeUserType(userId, newType) {
    if (!confirm(`Bu kullanıcının tipini ${newType === 'UYE' ? 'Üye' : 'Personel'} olarak değiştirmek istediğinize emin misiniz?`)) {
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/kullanicilar/tip-degistir/${userId}?yeniTip=${newType}`, {
            method: 'PUT',
            headers: getAuthHeaders()
        });
        
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: 'Bir hata oluştu' }));
            throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
        }
        
        const updatedUser = await response.json();
        showAlert(`Kullanıcı tipi başarıyla ${newType === 'UYE' ? 'Üye' : 'Personel'} olarak değiştirildi!`, 'success');
        
        // Kullanıcı listesini yenile
        loadAllUsers();
        
    } catch (error) {
        console.error('Kullanıcı tipi değiştirme hatası:', error);
        showAlert('Hata: ' + error.message, 'danger');
    }
}

// Kullanıcı listesini filtrele
function filterUsersList(users) {
    if (currentUserFilter === 'all') {
        return users;
    } else if (currentUserFilter === 'uye') {
        return users.filter(u => {
            const dtype = (u.dtype || u.DTYPE || '').toUpperCase();
            // Üye: dtype UYE
            return dtype === 'UYE';
        });
    } else if (currentUserFilter === 'personel') {
        return users.filter(u => {
            const dtype = (u.dtype || u.DTYPE || '').toUpperCase();
            // Personel: dtype PERSONEL
            return dtype === 'PERSONEL';
        });
    }
    return users;
}

// Filtre butonlarını güncelle
function filterUsers(filter) {
    currentUserFilter = filter;
    
    // Buton aktif durumlarını güncelle
    document.getElementById('filterAll').classList.remove('active');
    document.getElementById('filterUye').classList.remove('active');
    document.getElementById('filterPersonel').classList.remove('active');
    
    document.getElementById('filterAll').classList.remove('btn-primary');
    document.getElementById('filterUye').classList.remove('btn-success');
    document.getElementById('filterPersonel').classList.remove('btn-info');
    
    document.getElementById('filterAll').classList.add('btn-outline-primary');
    document.getElementById('filterUye').classList.add('btn-outline-success');
    document.getElementById('filterPersonel').classList.add('btn-outline-info');
    
    if (filter === 'all') {
        document.getElementById('filterAll').classList.add('active', 'btn-primary');
        document.getElementById('filterAll').classList.remove('btn-outline-primary');
    } else if (filter === 'uye') {
        document.getElementById('filterUye').classList.add('active', 'btn-success');
        document.getElementById('filterUye').classList.remove('btn-outline-success');
    } else if (filter === 'personel') {
        document.getElementById('filterPersonel').classList.add('active', 'btn-info');
        document.getElementById('filterPersonel').classList.remove('btn-outline-info');
    }
    
    // Listeyi yeniden render et
    renderUsers(allUsers);
}

// Mevcut sayfayı yenile
function refreshCurrentPage() {
    const booksTab = document.getElementById('books-tab');
    const loansTab = document.getElementById('loans-tab');
    const usersTab = document.getElementById('users-tab');
    
    if (booksTab.classList.contains('active')) {
        loadBooks();
    } else     if (loansTab.classList.contains('active')) {
        loadLoans();
    } else if (logsTab && logsTab.classList.contains('active')) {
        loadAllLoans();
    } else if (usersTab.classList.contains('active')) {
        loadAllUsers();
    }
}

// Bildirimleri yükle
async function loadNotifications() {
    const loadingEl = document.getElementById('loadingNotifications');
    const notificationsContainer = document.getElementById('notificationsContainer');
    const noNotificationsMessage = document.getElementById('noNotificationsMessage');
    
    loadingEl.style.display = 'block';
    notificationsContainer.style.display = 'none';
    noNotificationsMessage.style.display = 'none';
    
    try {
        const response = await fetch(`${API_BASE_URL}/bildirim`, {
            headers: getAuthHeaders()
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const bildirimler = await response.json();
        
        loadingEl.style.display = 'none';
        
        if (bildirimler.length === 0) {
            noNotificationsMessage.style.display = 'block';
        } else {
            notificationsContainer.style.display = 'block';
            renderNotifications(bildirimler);
        }
    } catch (error) {
        console.error('Bildirimler yüklenirken hata oluştu:', error);
        loadingEl.style.display = 'none';
        showAlert('Bildirimler yüklenirken bir hata oluştu: ' + error.message, 'danger');
    }
}

// Bildirimleri render et
function renderNotifications(bildirimler) {
    const container = document.getElementById('notificationsContainer');
    container.innerHTML = '';
    
    bildirimler.forEach(bildirim => {
        const card = document.createElement('div');
        card.className = `card mb-3 ${bildirim.okundu ? '' : 'border-primary'}`;
        
        const tarih = new Date(bildirim.olusturmaTarihi);
        const tarihStr = tarih.toLocaleString('tr-TR', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
        
        const okunduBadge = bildirim.okundu 
            ? '<span class="badge bg-secondary">Okundu</span>' 
            : '<span class="badge bg-primary">Yeni</span>';
        
        card.innerHTML = `
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-start">
                    <div class="flex-grow-1">
                        <h6 class="card-title mb-2">
                            ${okunduBadge}
                            <i class="bi bi-person-circle"></i> ${escapeHtml(bildirim.kullaniciAdi)}
                        </h6>
                        <p class="card-text mb-2">${escapeHtml(bildirim.mesaj)}</p>
                        <small class="text-muted">
                            <i class="bi bi-clock"></i> ${tarihStr}
                        </small>
                    </div>
                    ${!bildirim.okundu ? `
                        <button class="btn btn-sm btn-outline-primary ms-2" onclick="markNotificationAsRead(${bildirim.id})">
                            <i class="bi bi-check"></i> Okundu
                        </button>
                    ` : ''}
                </div>
            </div>
        `;
        
        container.appendChild(card);
    });
}

// Okunmamış bildirim sayısını kontrol et
async function checkNotificationCount() {
    try {
        const response = await fetch(`${API_BASE_URL}/bildirim/okunmamis-sayisi`, {
            headers: getAuthHeaders()
        });
        
        if (response.ok) {
            const data = await response.json();
            const badge = document.getElementById('notificationBadge');
            if (badge) {
                if (data.sayi > 0) {
                    badge.textContent = data.sayi;
                    badge.style.display = 'inline-block';
                } else {
                    badge.style.display = 'none';
                }
            }
        }
    } catch (error) {
        console.error('Bildirim sayısı kontrol edilirken hata:', error);
    }
}

// Bildirimi okundu olarak işaretle
async function markNotificationAsRead(bildirimId) {
    try {
        const response = await fetch(`${API_BASE_URL}/bildirim/oku/${bildirimId}`, {
            method: 'POST',
            headers: getAuthHeaders()
        });
        
        if (response.ok) {
            // Bildirimleri yenile
            loadNotifications();
            checkNotificationCount();
        }
    } catch (error) {
        console.error('Bildirim okundu işaretlenirken hata:', error);
    }
}

// Tüm bildirimleri okundu olarak işaretle
async function markAllNotificationsAsRead() {
    try {
        const response = await fetch(`${API_BASE_URL}/bildirim/hepsini-oku`, {
            method: 'POST',
            headers: getAuthHeaders()
        });
        
        if (response.ok) {
            // Bildirimleri yenile
            loadNotifications();
            checkNotificationCount();
            showAlert('Tüm bildirimler okundu olarak işaretlendi.', 'success');
        }
    } catch (error) {
        console.error('Bildirimler okundu işaretlenirken hata:', error);
        showAlert('Hata: ' + error.message, 'danger');
    }
}

// Google Books API'den kitap ara
async function searchGoogleBooks() {
    const searchInput = document.getElementById('googleBookSearch');
    const searchTerm = searchInput.value.trim();
    const resultsDiv = document.getElementById('googleBooksResults');
    
    if (!searchTerm) {
        resultsDiv.innerHTML = '<div class="alert alert-warning mb-0">Lütfen arama terimi giriniz.</div>';
        resultsDiv.style.display = 'block';
        return;
    }
    
    resultsDiv.innerHTML = '<div class="text-center"><div class="spinner-border spinner-border-sm" role="status"></div> <span class="ms-2">Aranıyor...</span></div>';
    resultsDiv.style.display = 'block';
    
    try {
        // Google Books API - API key gerektirmiyor
        const apiUrl = `https://www.googleapis.com/books/v1/volumes?q=${encodeURIComponent(searchTerm)}&maxResults=5&langRestrict=tr`;
        
        const response = await fetch(apiUrl);
        
        if (!response.ok) {
            throw new Error('Google Books API hatası');
        }
        
        const data = await response.json();
        
        if (!data.items || data.items.length === 0) {
            resultsDiv.innerHTML = '<div class="alert alert-info mb-0">Kitap bulunamadı. Lütfen farklı bir arama terimi deneyin.</div>';
            return;
        }
        
        // Sonuçları göster
        const resultsContainer = document.createElement('div');
        resultsContainer.className = 'list-group';
        
        data.items.forEach((item, index) => {
            const volumeInfo = item.volumeInfo;
            const title = volumeInfo.title || 'Bilinmeyen';
            const authors = volumeInfo.authors ? volumeInfo.authors.join(', ') : 'Bilinmeyen Yazar';
            const isbn = volumeInfo.industryIdentifiers 
                ? volumeInfo.industryIdentifiers.find(id => id.type === 'ISBN_13' || id.type === 'ISBN_10')
                : null;
            const isbnValue = isbn ? isbn.identifier : 'ISBN bulunamadı';
            const thumbnail = volumeInfo.imageLinks ? volumeInfo.imageLinks.thumbnail : null;
            
            const itemDiv = document.createElement('div');
            itemDiv.className = 'list-group-item list-group-item-action';
            itemDiv.style.cursor = 'pointer';
            itemDiv.onclick = () => selectGoogleBook(title, authors, isbnValue, thumbnail);
            
            let itemHTML = '<div class="d-flex">';
            if (thumbnail) {
                itemHTML += `<img src="${escapeHtml(thumbnail)}" alt="Kitap kapağı" style="width: 50px; height: 75px; object-fit: cover; margin-right: 10px;">`;
            }
            itemHTML += `
                <div class="flex-grow-1">
                    <h6 class="mb-1">${escapeHtml(title)}</h6>
                    <p class="mb-1 text-muted"><small>${escapeHtml(authors)}</small></p>
                    <p class="mb-0"><small class="text-muted">ISBN: ${escapeHtml(isbnValue)}</small></p>
                </div>
            </div>`;
            
            itemDiv.innerHTML = itemHTML;
            resultsContainer.appendChild(itemDiv);
        });
        
        resultsDiv.innerHTML = '';
        resultsDiv.appendChild(resultsContainer);
        
    } catch (error) {
        console.error('Google Books arama hatası:', error);
        resultsDiv.innerHTML = '<div class="alert alert-danger mb-0">Arama sırasında bir hata oluştu. Lütfen tekrar deneyin.</div>';
    }
}

// Google Books'tan seçilen kitabı forma doldur
function selectGoogleBook(title, author, isbn, kapakUrl) {
    // Google sekmesindeki form alanlarını doldur
    document.getElementById('bookBaslikGoogle').value = title;
    document.getElementById('bookYazarGoogle').value = author;
    document.getElementById('bookIsbnGoogle').value = isbn;
    document.getElementById('bookKapakUrlGoogle').value = kapakUrl || '';
    
    // Arama sonuçlarını gizle
    document.getElementById('googleBooksResults').style.display = 'none';
    document.getElementById('googleBookSearch').value = '';
    
    // Form alanlarına odaklan
    document.getElementById('bookBaslikGoogle').focus();
}

// XSS koruması için HTML escape
function escapeHtml(text) {
    if (text == null) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Kitap ekleme modal'ını göster
function showAddBookModal() {
    // Formları temizle
    document.getElementById('addBookForm').reset();
    document.getElementById('addBookFormGoogle').reset();
    document.getElementById('googleBookSearch').value = '';
    document.getElementById('googleBooksResults').style.display = 'none';
    document.getElementById('googleBooksResults').innerHTML = '';
    document.getElementById('addBookAlert').classList.add('d-none');
    
    // İlk sekmeye geç (Manuel Ekleme)
    const manualTab = document.getElementById('manual-tab');
    if (manualTab) {
        const tab = new bootstrap.Tab(manualTab);
        tab.show();
    }

    addBookModal.show();
}

// Kitap ekle
async function addBook() {
    const alertEl = document.getElementById('addBookAlert');
    const addBookBtn = document.getElementById('addBookBtn');
    const spinner = document.getElementById('addBookSpinner');
    
    // Hangi sekme aktif mi kontrol et
    const manualTab = document.getElementById('manual-tab');
    const isManualTab = manualTab && manualTab.classList.contains('active');
    
    let baslik, yazar, isbn, kapakUrl;
    
    if (isManualTab) {
        // Manuel ekleme sekmesi
        baslik = document.getElementById('bookBaslik').value.trim();
        yazar = document.getElementById('bookYazar').value.trim();
        isbn = document.getElementById('bookIsbn').value.trim();
        kapakUrl = document.getElementById('bookKapakUrl').value.trim();
    } else {
        // Google Books sekmesi
        baslik = document.getElementById('bookBaslikGoogle').value.trim();
        yazar = document.getElementById('bookYazarGoogle').value.trim();
        isbn = document.getElementById('bookIsbnGoogle').value.trim();
        kapakUrl = document.getElementById('bookKapakUrlGoogle').value.trim();
    }
    
    // Validasyon
    if (!baslik) {
        showBookAlert('Lütfen kitap adını giriniz.', 'warning', alertEl);
        return;
    }
    
    if (!yazar) {
        showBookAlert('Lütfen yazar adını giriniz.', 'warning', alertEl);
        return;
    }
    
    if (!isbn) {
        showBookAlert('Lütfen ISBN numarasını giriniz.', 'warning', alertEl);
        return;
    }
    
    // Loading göster
    addBookBtn.disabled = true;
    spinner.classList.remove('d-none');
    alertEl.classList.add('d-none');
    
    try {
        const kitapData = {
            baslik: baslik,
            yazar: yazar,
            isbn: isbn,
            kapakUrl: kapakUrl || null,
            mevcut: true
        };
        
        const response = await fetch(`${API_BASE_URL}/kitap/ekle`, {
            method: 'POST',
            headers: {
                ...getAuthHeaders(),
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(kitapData)
        });
        
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: 'Bir hata oluştu' }));
            throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
        }
        
        const newBook = await response.json();
        
        // Başarılı
        showBookAlert('Kitap başarıyla eklendi!', 'success', alertEl);
        
        // Modal'ı kapat ve listeyi yenile
        setTimeout(() => {
            addBookModal.hide();
            // Blur efektini kaldırmak için body class'ını ve backdrop'u temizle
            setTimeout(() => {
                document.body.classList.remove('modal-open');
                const backdrop = document.querySelector('.modal-backdrop');
                if (backdrop) {
                    backdrop.remove();
                }
                document.body.style.overflow = '';
                document.body.style.paddingRight = '';
            }, 300);
            loadBooks(); // Kitap listesini yenile
        }, 1000);
        
    } catch (error) {
        console.error('Kitap ekleme hatası:', error);
        showBookAlert('Hata: ' + error.message, 'danger', alertEl);
    } finally {
        addBookBtn.disabled = false;
        spinner.classList.add('d-none');
    }
}

// Kitap ekleme için alert göster
function showBookAlert(message, type = 'info', alertEl) {
    alertEl.className = `alert alert-${type}`;
    alertEl.textContent = message;
    alertEl.classList.remove('d-none');
}

// Silme modalını göster
function showDeleteBookModal(id) {
    bookIdToDelete = id;
    deleteModal.show();
}
