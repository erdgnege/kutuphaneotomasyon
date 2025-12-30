// API Base URL
const API_BASE_URL = 'http://localhost:8080/api';

// Global değişkenler
let currentUserId = null;
let currentUser = null;
let userLoginModal = null;
let updateUserInfoModal = null;
let forgotPasswordModal = null;
let pendingUserData = null; // Email doğrulaması için bekleyen kullanıcı verisi
let userToken = null; // JWT token
let forgotPasswordEmail = null; // Şifre sıfırlama için email

// Token'dan auth header'ı oluştur
function getAuthHeaders() {
    const headers = {
        'Content-Type': 'application/json'
    };
    
    if (userToken) {
        headers['Authorization'] = 'Bearer ' + userToken;
    }
    
    return headers;
}

// Sayfa yüklendiğinde
document.addEventListener('DOMContentLoaded', function() {
    // Admin sayfasından gelip gelmediğini kontrol et
    // sessionStorage kullanarak daha güvenilir bir kontrol yapıyoruz
    const fromAdminPage = sessionStorage.getItem('fromAdminPage');
    
    if (fromAdminPage === 'true') {
        // Admin sayfasından geliyorsa, admin oturumunu temizle
        localStorage.removeItem('adminAuth');
        localStorage.removeItem('adminToken');
        
        // Kullanıcı oturumunu da temizle (admin sayfasından geldiğinde)
        localStorage.removeItem('userToken');
        localStorage.removeItem('kullaniciId');
        localStorage.removeItem('kullaniciEmail');
        userToken = null;
        
        // Flag'i temizle
        sessionStorage.removeItem('fromAdminPage');
    }
    
    // Bootstrap modal instance oluştur
    const modalElement = document.getElementById('userLoginModal');
    if (modalElement) {
        userLoginModal = new bootstrap.Modal(modalElement, {
            backdrop: true,
            keyboard: true
        });
    }
    
    // Kullanıcı bilgilerini güncelleme modal instance oluştur
    const updateModalElement = document.getElementById('updateUserInfoModal');
    if (updateModalElement) {
        updateUserInfoModal = new bootstrap.Modal(updateModalElement);
    }
    
    // Şifre sıfırlama modal instance oluştur
    const forgotPasswordModalElement = document.getElementById('forgotPasswordModal');
    if (forgotPasswordModalElement) {
        forgotPasswordModal = new bootstrap.Modal(forgotPasswordModalElement);
    }
    
    // Başlangıçta kullanıcı bilgileri dropdown'ını gizle ve giriş butonunu göster
    const dropdown = document.getElementById('userInfoDropdown');
    if (dropdown) {
        dropdown.style.display = 'none';
        dropdown.style.setProperty('display', 'none', 'important'); // !important ile zorla gizle
        dropdown.classList.remove('d-inline-block'); // Bootstrap class'ını kaldır
    }
    const loginButton = document.getElementById('userLoginButton');
    if (loginButton) {
        loginButton.style.display = 'inline-block';
    }
    
    // Kaydedilmiş token'ı kontrol et
    const savedToken = localStorage.getItem('userToken');
    const savedEmail = localStorage.getItem('kullaniciEmail');
    
    if (savedToken && savedEmail) {
        userToken = savedToken;
        // Kaydedilmiş oturum var, doğrulama yap
        verifySavedSession(null, savedEmail);
    } else {
        // Oturum yok, direkt kitapları göster (giriş yapmadan)
        showContentWithoutLogin();
    }
    
    // Tab değiştiğinde ilgili verileri yükle
    const catalogTab = document.getElementById('catalog-tab');
    catalogTab.addEventListener('shown.bs.tab', function() {
        loadCatalog(); // Giriş yapmadan da kitapları göster
    });
    
    const myLoansTab = document.getElementById('my-loans-tab');
    myLoansTab.addEventListener('shown.bs.tab', function() {
        if (currentUserId) {
            loadMyLoans();
        }
    });
});

// Giriş yapmadan içeriği göster
function showContentWithoutLogin() {
    // İçeriği göster
    document.getElementById('mainUserContent').style.display = 'block';
    document.getElementById('userInfoCard').style.display = 'none';
    
    // Kullanıcı bilgileri dropdown'ını gizle
    const dropdown = document.getElementById('userInfoDropdown');
    if (dropdown) {
        dropdown.style.display = 'none';
        dropdown.style.setProperty('display', 'none', 'important'); // !important ile zorla gizle
        dropdown.classList.remove('d-inline-block'); // Bootstrap class'ını kaldır
    }
    
    // Giriş butonunu göster
    const loginButton = document.getElementById('userLoginButton');
    if (loginButton) {
        loginButton.style.display = 'inline-block';
    }
    
    // Katalog sekmesini aktif yap
    const catalogTab = document.getElementById('catalog-tab');
    const myLoansTab = document.getElementById('my-loans-tab');
    const catalogPane = document.getElementById('catalog');
    const myLoansPane = document.getElementById('my-loans');
    
    // "Ödünç Aldığım Kitaplar" sekmesini gizle (giriş yapılmadığı için)
    myLoansTab.style.display = 'none';
    myLoansTab.classList.remove('active');
    catalogTab.classList.add('active');
    catalogPane.classList.add('show', 'active');
    myLoansPane.classList.remove('show', 'active');
    
    // Kitapları yükle
    loadCatalog();
}

// Kullanıcı login modal'ını göster
function showUserLoginModal() {
    console.log('showUserLoginModal çağrıldı');
    
    const modalElement = document.getElementById('userLoginModal');
    if (!modalElement) {
        console.error('userLoginModal elementi bulunamadı!');
        return;
    }
    
    if (!userLoginModal) {
        // Modal instance yoksa oluştur
        console.log('Modal instance oluşturuluyor...');
        userLoginModal = new bootstrap.Modal(modalElement, {
            backdrop: true,
            keyboard: true
        });
    }
    
    // İçeriği gizleme (artık giriş yapmadan da içerik görünüyor)
    
    // Form'u sıfırla
    const uyeNoInput = document.getElementById('uyeNoInput');
    if (uyeNoInput) uyeNoInput.value = '';
    const userEmailOrPhoneInput = document.getElementById('userEmailOrPhoneInput');
    if (userEmailOrPhoneInput) userEmailOrPhoneInput.value = '';
    const alertEl = document.getElementById('userLoginAlert');
    if (alertEl) alertEl.classList.add('d-none');
    
    // Modal'ı göster
    console.log('Modal gösteriliyor...');
    try {
        userLoginModal.show();
        console.log('Modal show() çağrıldı');
    } catch (error) {
        console.error('Modal show() hatası:', error);
    }
}

// Kullanıcı girişi (Email veya Üye No + Şifre ile token al)
async function verifyUserLogin() {
    const emailOrUyeNo = document.getElementById('userEmailInput').value.trim();
    const password = document.getElementById('userPasswordInput').value;
    const alertEl = document.getElementById('userLoginAlert');
    const loginBtn = document.getElementById('loginBtn');
    const spinner = loginBtn.querySelector('.spinner-border');
    
    // Validasyon
    if (!emailOrUyeNo) {
        alertEl.className = 'alert alert-warning';
        alertEl.textContent = 'Lütfen e-posta adresinizi veya üye numaranızı giriniz.';
        alertEl.classList.remove('d-none');
        return;
    }
    
    if (!password) {
        alertEl.className = 'alert alert-warning';
        alertEl.textContent = 'Lütfen şifrenizi giriniz.';
        alertEl.classList.remove('d-none');
        return;
    }
    
    // Loading göster
    loginBtn.disabled = true;
    spinner.classList.add('active');
    alertEl.classList.add('d-none');
    
    try {
        // Token al (email alanına email veya üye no gönderilebilir)
        const loginResponse = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                email: emailOrUyeNo, // Backend'de email veya üye no olarak işlenecek
                password: password
            })
        });
        
        if (!loginResponse.ok) {
            if (loginResponse.status === 401 || loginResponse.status === 403) {
                alertEl.className = 'alert alert-danger';
                alertEl.textContent = 'E-posta/Üye No veya şifre hatalı!';
                alertEl.classList.remove('d-none');
                document.getElementById('userPasswordInput').value = '';
                document.getElementById('userPasswordInput').focus();
                return;
            }
            throw new Error('Giriş yapılamadı.');
        }
        
        const authData = await loginResponse.json();
        userToken = authData.token;
        
        // Kullanıcı bilgilerini al - önce email ile dene, sonra üye no ile
        let userData = null;
        let userResponse = null;
        
        // Email formatında mı kontrol et
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (emailRegex.test(emailOrUyeNo)) {
            // Email ile kullanıcı bilgilerini al
            userResponse = await fetch(`${API_BASE_URL}/kullanicilar/email/${emailOrUyeNo}`, {
                headers: getAuthHeaders()
            });
        } else {
            // Üye numarası ile kullanıcı bilgilerini al
            userResponse = await fetch(`${API_BASE_URL}/kullanicilar/uye-no/${emailOrUyeNo}`, {
                headers: getAuthHeaders()
            });
        }
        
        if (!userResponse.ok) {
            throw new Error('Kullanıcı bilgileri alınamadı.');
        }
        
        userData = await userResponse.json();
        
        // Doğrulama başarılı, kullanıcıyı sisteme al
        currentUser = userData;
        currentUserId = currentUser.id;
        
        // Token ve oturum bilgilerini kaydet
        localStorage.setItem('userToken', userToken);
        localStorage.setItem('kullaniciId', currentUserId.toString());
        localStorage.setItem('kullaniciEmail', currentUser.email);
        
        // Modal'ı kapat
        userLoginModal.hide();
        
        // İçeriği göster
        document.getElementById('mainUserContent').style.display = 'block';
        document.getElementById('userInfoCard').style.display = 'none';
        
        // Kullanıcı bilgilerini göster
        updateUserInfoDropdown();
        
        // "Ödünç Aldığım Kitaplar" sekmesini göster
        const myLoansTab = document.getElementById('my-loans-tab');
        if (myLoansTab) {
            myLoansTab.style.display = 'block';
        }
        
        // Verileri yükle
        loadMyLoans();
        loadCatalog();
        
        showAlert('Giriş başarılı! Hoş geldiniz ' + currentUser.adSoyad + '.', 'success');
        
    } catch (error) {
        console.error('Giriş hatası:', error);
        alertEl.className = 'alert alert-danger';
        alertEl.textContent = 'Hata: ' + error.message;
        alertEl.classList.remove('d-none');
    } finally {
        loginBtn.disabled = false;
        spinner.classList.remove('active');
    }
}

// Kaydedilmiş oturumu doğrula (Token ile)
async function verifySavedSession(userId, email) {
    try {
        // Token ile kullanıcı bilgilerini al (email ile)
        const response = await fetch(`${API_BASE_URL}/kullanicilar/email/${email}`, {
            headers: getAuthHeaders()
        });
        
        if (!response.ok) {
            if (response.status === 401 || response.status === 403) {
                // Token geçersiz, temizle
                localStorage.removeItem('userToken');
                localStorage.removeItem('kullaniciId');
                localStorage.removeItem('kullaniciEmail');
                userToken = null;
                throw new Error('Token geçersiz');
            }
            throw new Error('Kullanıcı bulunamadı');
        }
        
        const userData = await response.json();
        
        // Oturum geçerli
        currentUser = userData;
        currentUserId = userData.id;
        
        // İçeriği göster
        document.getElementById('mainUserContent').style.display = 'block';
        document.getElementById('userInfoCard').style.display = 'none';
        
        // Kullanıcı bilgilerini göster
        updateUserInfoDropdown();
        
        // "Ödünç Aldığım Kitaplar" sekmesini göster
        const myLoansTab = document.getElementById('my-loans-tab');
        if (myLoansTab) {
            myLoansTab.style.display = 'block';
        }
        
        // Verileri yükle
        loadMyLoans();
        loadCatalog();
        
    } catch (error) {
        console.error('Oturum doğrulama hatası:', error);
        // Oturum geçersiz, temizle ve içeriği göster (giriş yapmadan)
        localStorage.removeItem('userToken');
        localStorage.removeItem('kullaniciId');
        localStorage.removeItem('kullaniciEmail');
        userToken = null;
        showContentWithoutLogin();
    }
}

// Kullanıcı bilgilerini dropdown'a güncelle
function updateUserInfoDropdown() {
    if (!currentUser) return;
    
    const dropdown = document.getElementById('userInfoDropdown');
    const loginButton = document.getElementById('userLoginButton');
    
    if (dropdown) {
        dropdown.style.display = 'inline-block';
        dropdown.classList.add('d-inline-block'); // Bootstrap class'ını ekle
        
        // Dropdown içeriğini güncelle
        document.getElementById('dropdownUserName').textContent = currentUser.adSoyad;
        document.getElementById('dropdownUserEmail').textContent = currentUser.email;
        document.getElementById('dropdownUserPhone').textContent = currentUser.telefon || '-';
        
        // Limit bilgisini göster
        const limit = currentUser.dtype === 'UYE' ? 3 : 5;
        document.getElementById('dropdownUserLimit').textContent = `Ödünç alma limiti: ${limit} kitap`;
    }
    
    // Giriş butonunu gizle
    if (loginButton) {
        loginButton.style.display = 'none';
    }
}

// Kullanıcı çıkışı
function userLogout() {
    if (confirm('Çıkış yapmak istediğinize emin misiniz?')) {
        localStorage.removeItem('userToken');
        localStorage.removeItem('kullaniciId');
        localStorage.removeItem('kullaniciEmail');
        userToken = null;
        currentUserId = null;
        currentUser = null;
        pendingUserData = null;
        
        // Dropdown'ı gizle
        const dropdown = document.getElementById('userInfoDropdown');
        if (dropdown) {
            dropdown.style.display = 'none';
        }
        
        // Giriş butonunu göster
        const loginButton = document.getElementById('userLoginButton');
        if (loginButton) {
            loginButton.style.display = 'inline-block';
        }
        
        // "Ödünç Aldığım Kitaplar" sekmesini gizle
        const myLoansTab = document.getElementById('my-loans-tab');
        const catalogTab = document.getElementById('catalog-tab');
        const catalogPane = document.getElementById('catalog');
        const myLoansPane = document.getElementById('my-loans');
        
        if (myLoansTab) {
            myLoansTab.style.display = 'none';
            myLoansTab.classList.remove('active');
        }
        if (catalogTab) {
            catalogTab.classList.add('active');
        }
        if (catalogPane) {
            catalogPane.classList.add('show', 'active');
        }
        if (myLoansPane) {
            myLoansPane.classList.remove('show', 'active');
        }
        
        // Kataloğu yenile
        loadCatalog();
        
        showAlert('Çıkış yapıldı.', 'info');
    }
}

// Ödünç aldığım kitapları yükle
async function loadMyLoans() {
    if (!currentUserId) {
        document.getElementById('noMyLoansMessage').style.display = 'block';
        document.getElementById('myLoansContainer').style.display = 'none';
        return;
    }
    
    const loadingEl = document.getElementById('loadingMyLoans');
    const loansContainer = document.getElementById('myLoansContainer');
    const noLoansMessage = document.getElementById('noMyLoansMessage');
    
    loadingEl.style.display = 'block';
    loansContainer.style.display = 'none';
    noLoansMessage.style.display = 'none';
    
    try {
        const response = await fetch(`${API_BASE_URL}/odunc/kullanici/${currentUserId}`, {
            headers: getAuthHeaders()
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const oduncler = await response.json();
        
        loadingEl.style.display = 'none';
        
        if (oduncler.length === 0) {
            noLoansMessage.style.display = 'block';
            document.getElementById('myLoansCount').textContent = '0 kitap';
        } else {
            loansContainer.style.display = 'block';
            renderMyLoans(oduncler);
            document.getElementById('myLoansCount').textContent = `${oduncler.length} kitap`;
        }
    } catch (error) {
        console.error('Ödünç kayıtları yüklenirken hata:', error);
        loadingEl.style.display = 'none';
        showAlert('Ödünç kayıtları yüklenirken bir hata oluştu: ' + error.message, 'danger');
    }
}

// Ödünç aldığım kitapları render et
function renderMyLoans(oduncler) {
    const container = document.getElementById('myLoansContainer');
    container.innerHTML = '';
    
    const ODUNC_SURESI = 30; // Standart ödünç verme süresi (gün)
    
    oduncler.forEach(odunc => {
        const card = document.createElement('div');
        card.className = 'card mb-3';
        
        const kitap = odunc.kitap;
        const oduncTarihi = new Date(odunc.oduncTarihi + 'T00:00:00');
        const bugun = new Date();
        bugun.setHours(0, 0, 0, 0);
        
        const geçenGunSayisi = Math.floor((bugun - oduncTarihi) / (1000 * 60 * 60 * 24));
        const kalanGunSayisi = ODUNC_SURESI - geçenGunSayisi;
        
        const iadeTarihi = new Date(oduncTarihi);
        iadeTarihi.setDate(iadeTarihi.getDate() + ODUNC_SURESI);
        
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
        
        card.innerHTML = `
            <div class="card-body">
                <div class="row align-items-center">
                    <div class="col-md-8">
                        <h5 class="card-title mb-2">
                            <i class="bi bi-book"></i> ${escapeHtml(kitap.baslik)}
                        </h5>
                        <p class="text-muted mb-2">
                            <i class="bi bi-person"></i> ${escapeHtml(kitap.yazar)}
                        </p>
                        <p class="text-muted mb-2">
                            <i class="bi bi-upc"></i> ISBN: ${escapeHtml(kitap.isbn)}
                        </p>
                        <button class="btn btn-danger btn-sm" onclick="iadeEt(${odunc.id}, '${escapeHtml(kitap.baslik).replace(/'/g, "\\'")}')">
                            <i class="bi bi-arrow-return-left"></i> İade Et
                        </button>
                    </div>
                    <div class="col-md-4 text-end">
                        <div class="mb-2">
                            <small class="text-muted d-block">Ödünç Tarihi:</small>
                            <strong>${oduncTarihi.toLocaleDateString('tr-TR')}</strong>
                        </div>
                        <div class="mb-2">
                            <small class="text-muted d-block">İade Tarihi:</small>
                            <strong>${iadeTarihi.toLocaleDateString('tr-TR')}</strong>
                        </div>
                        <div>
                            <span class="badge ${badgeClass} fs-6">
                                ${badgeText}
                            </span>
                        </div>
                    </div>
                </div>
            </div>
        `;
        
        container.appendChild(card);
    });
}

// Kütüphane kataloğunu yükle
async function loadCatalog() {
    const loadingEl = document.getElementById('loadingCatalog');
    const catalogContainer = document.getElementById('catalogContainer');
    const noCatalogMessage = document.getElementById('noCatalogMessage');
    
    loadingEl.style.display = 'block';
    catalogContainer.style.display = 'none';
    noCatalogMessage.style.display = 'none';
    
    try {
        const response = await fetch(`${API_BASE_URL}/kitap`);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const kitaplar = await response.json();
        
        loadingEl.style.display = 'none';
        
        if (kitaplar.length === 0) {
            noCatalogMessage.style.display = 'block';
            document.getElementById('catalogCount').textContent = '0 kitap';
        } else {
            catalogContainer.style.display = 'flex';
            renderCatalog(kitaplar);
            document.getElementById('catalogCount').textContent = `${kitaplar.length} kitap`;
        }
    } catch (error) {
        console.error('Kitaplar yüklenirken hata:', error);
        loadingEl.style.display = 'none';
        
        if (error.message.includes('Failed to fetch') || error.message.includes('NetworkError')) {
            showAlert('Backend sunucusuna bağlanılamıyor. Lütfen backend\'in çalıştığından emin olun (http://localhost:8080)', 'danger');
        } else {
            showAlert('Kitaplar yüklenirken bir hata oluştu: ' + error.message, 'danger');
        }
    }
}

// Kataloğu render et
function renderCatalog(kitaplar) {
    const container = document.getElementById('catalogContainer');
    container.innerHTML = '';
    
    kitaplar.forEach(kitap => {
        const col = document.createElement('div');
        col.className = 'col-md-6 col-lg-4';
        
        const mevcutClass = kitap.mevcut ? 'success' : 'danger';
        const mevcutText = kitap.mevcut ? 'Mevcut' : 'Ödünçte';
        // Ödünçte olan kitaplar için koyu stil ekle
        const oduncClass = kitap.mevcut ? '' : 'odunc-te';
        const textColorClass = kitap.mevcut ? 'text-muted' : 'text-white';
        // Ödünç alma butonu sadece mevcut kitaplar için göster
        let oduncButton = '';
        if (kitap.mevcut && currentUserId) {
            const baslikEscaped = escapeHtml(kitap.baslik).replace(/'/g, "\\'");
            oduncButton = `<button class="btn btn-primary btn-sm w-100 mt-2" onclick="oduncAl(${kitap.id}, '${baslikEscaped}')">
                <i class="bi bi-book-half"></i> Ödünç Al
               </button>`;
        }
        
        const kapakGorsel = kitap.kapakUrl 
            ? `<div class="card-img-top d-flex align-items-center justify-content-center" style="height: 300px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 20px;">
                <img src="${escapeHtml(kitap.kapakUrl)}" alt="${escapeHtml(kitap.baslik)}" style="max-height: 100%; max-width: 100%; object-fit: contain; box-shadow: 0 4px 8px rgba(0,0,0,0.3); border-radius: 4px;">
               </div>`
            : `<div class="card-img-top d-flex align-items-center justify-content-center bg-light" style="height: 300px;">
                <i class="bi bi-book" style="font-size: 5rem; color: #ccc;"></i>
               </div>`;
        
        col.innerHTML = `
            <div class="card book-card h-100 ${oduncClass}">
                ${kapakGorsel}
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-start mb-2">
                        <h5 class="card-title mb-0">${escapeHtml(kitap.baslik)}</h5>
                        <span class="badge bg-${mevcutClass} status-badge">${mevcutText}</span>
                    </div>
                    <p class="${textColorClass} mb-2">
                        <i class="bi bi-person"></i> ${escapeHtml(kitap.yazar)}
                    </p>
                    <p class="${textColorClass} mb-2">
                        <i class="bi bi-upc"></i> ISBN: ${escapeHtml(kitap.isbn)}
                    </p>
                    ${oduncButton}
                </div>
            </div>
        `;
        
        container.appendChild(col);
    });
}

// Mevcut sayfayı yenile
function refreshCurrentPage() {
    if (!currentUserId) {
        showUserLoginModal();
        return;
    }
    
    const myLoansTab = document.getElementById('my-loans-tab');
    const catalogTab = document.getElementById('catalog-tab');
    
    if (myLoansTab.classList.contains('active')) {
        loadMyLoans();
    } else if (catalogTab.classList.contains('active')) {
        loadCatalog();
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
    
    setTimeout(() => {
        const alertEl = document.getElementById(alertId);
        if (alertEl) {
            const bsAlert = new bootstrap.Alert(alertEl);
            bsAlert.close();
        }
    }, 5000);
}

// Kitap iade et
async function iadeEt(oduncId, kitapBaslik) {
    if (!currentUserId) {
        showAlert('Lütfen önce giriş yapınız.', 'warning');
        return;
    }
    
    if (!confirm(`${kitapBaslik} kitabını iade etmek istediğinize emin misiniz?`)) {
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/odunc/kullanici-iade/${oduncId}`, {
            method: 'PUT',
            headers: getAuthHeaders()
        });
        
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: 'Bir hata oluştu' }));
            throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
        }
        
        const odunc = await response.json();
        showAlert(`${kitapBaslik} kitabı başarıyla iade edildi!`, 'success');
        
        // Kataloğu ve ödünç listesini yenile
        loadCatalog();
        loadMyLoans();
        
    } catch (error) {
        console.error('İade hatası:', error);
        showAlert('Hata: ' + error.message, 'danger');
    }
}

// Kitap ödünç al
async function oduncAl(kitapId, kitapBaslik) {
    if (!currentUserId) {
        showAlert('Lütfen önce giriş yapınız.', 'warning');
        return;
    }
    
    if (!confirm(`${kitapBaslik} kitabını ödünç almak istediğinize emin misiniz?`)) {
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/odunc/kullanici-iste?kitapId=${kitapId}&userId=${currentUserId}`, {
            method: 'POST',
            headers: getAuthHeaders()
        });
        
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: 'Bir hata oluştu' }));
            throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
        }
        
        const odunc = await response.json();
        showAlert(`${kitapBaslik} kitabı başarıyla ödünç alındı!`, 'success');
        
        // Kataloğu ve ödünç listesini yenile
        loadCatalog();
        loadMyLoans();
        
    } catch (error) {
        console.error('Ödünç alma hatası:', error);
        showAlert('Hata: ' + error.message, 'danger');
    }
}

// XSS koruması için HTML escape
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Kullanıcı bilgilerini güncelleme modal'ını göster
function showUpdateUserInfoModal() {
    if (!currentUser) {
        showAlert('Lütfen önce giriş yapınız.', 'warning');
        return;
    }
    
    // Form alanlarını doldur
    document.getElementById('updateAdSoyad').value = currentUser.adSoyad || '';
    document.getElementById('updateEmail').value = currentUser.email || '';
    document.getElementById('updateTelefon').value = currentUser.telefon || '';
    
    // Üye ise üye numarası alanını göster (sadece görüntüleme için, değiştirilemez)
    const uyeNoContainer = document.getElementById('updateUyeNoContainer');
    const uyeNoDisplay = document.getElementById('updateUyeNoDisplay');
    if (currentUser.dtype === 'UYE' && currentUser.uyeNo) {
        uyeNoContainer.style.display = 'block';
        uyeNoDisplay.value = currentUser.uyeNo;
    } else {
        uyeNoContainer.style.display = 'none';
    }
    
    // Alert'i temizle
    const alertEl = document.getElementById('updateUserInfoAlert');
    if (alertEl) {
        alertEl.classList.add('d-none');
        alertEl.textContent = '';
    }
    
    // Modal'ı göster
    if (updateUserInfoModal) {
        updateUserInfoModal.show();
    }
}

// Kullanıcı bilgilerini güncelle
async function updateUserInfo() {
    if (!currentUser || !currentUserId) {
        showAlert('Lütfen önce giriş yapınız.', 'warning');
        return;
    }
    
    const adSoyad = document.getElementById('updateAdSoyad').value.trim();
    const email = document.getElementById('updateEmail').value.trim();
    const telefon = document.getElementById('updateTelefon').value.trim();
    const alertEl = document.getElementById('updateUserInfoAlert');
    const updateBtn = document.getElementById('updateUserInfoBtn');
    const spinner = updateBtn.querySelector('.spinner-border');
    
    // Validasyon
    if (!adSoyad) {
        alertEl.className = 'alert alert-warning';
        alertEl.textContent = 'Lütfen ad soyad giriniz.';
        alertEl.classList.remove('d-none');
        return;
    }
    
    if (!email) {
        alertEl.className = 'alert alert-warning';
        alertEl.textContent = 'Lütfen e-posta adresi giriniz.';
        alertEl.classList.remove('d-none');
        return;
    }
    
    // Email format kontrolü
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
        alertEl.className = 'alert alert-warning';
        alertEl.textContent = 'Lütfen geçerli bir e-posta adresi giriniz.';
        alertEl.classList.remove('d-none');
        return;
    }
    
    // Telefon format kontrolü (11 haneli olmalı)
    if (telefon && telefon.length > 0 && telefon.length !== 11) {
        alertEl.className = 'alert alert-warning';
        alertEl.textContent = 'Telefon numarası 11 haneli olmalıdır (05XXXXXXXXX).';
        alertEl.classList.remove('d-none');
        return;
    }
    
    // Loading göster
    updateBtn.disabled = true;
    spinner.classList.add('active');
    alertEl.classList.add('d-none');
    
    try {
        // Güncelleme için veri hazırla (üye numarası gönderilmez, değiştirilemez)
        const updateData = {
            adSoyad: adSoyad,
            email: email,
            telefon: telefon || null
        };
        
        const response = await fetch(`${API_BASE_URL}/kullanicilar/guncelle/${currentUserId}`, {
            method: 'PUT',
            headers: getAuthHeaders(),
            body: JSON.stringify(updateData)
        });
        
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: 'Bir hata oluştu' }));
            throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
        }
        
        const updatedUser = await response.json();
        
        // Kullanıcı bilgilerini güncelle
        currentUser = updatedUser;
        localStorage.setItem('kullaniciEmail', updatedUser.email);
        
        // Dropdown'ı güncelle
        updateUserInfoDropdown();
        
        // Modal'ı kapat
        if (updateUserInfoModal) {
            updateUserInfoModal.hide();
        }
        
        showAlert('Bilgileriniz başarıyla güncellendi!', 'success');
        
    } catch (error) {
        console.error('Güncelleme hatası:', error);
        alertEl.className = 'alert alert-danger';
        alertEl.textContent = 'Hata: ' + error.message;
        alertEl.classList.remove('d-none');
    } finally {
        updateBtn.disabled = false;
        spinner.classList.remove('active');
    }
}

// Şifre sıfırlama modalını göster
function showForgotPasswordModal(event) {
    if (event) {
        event.preventDefault();
    }
    
    // Login modalını kapat
    if (userLoginModal) {
        userLoginModal.hide();
    }
    
    // Formu sıfırla
    resetForgotPasswordForm();
    
    // Şifre sıfırlama modalını göster
    if (forgotPasswordModal) {
        forgotPasswordModal.show();
    } else {
        console.error('forgotPasswordModal bulunamadı!');
    }
}

// Şifre sıfırlama kodunu gönder
async function sendPasswordResetCode() {
    const email = document.getElementById('forgotPasswordEmailInput').value.trim();
    const alertEl = document.getElementById('forgotPasswordAlert');
    const sendBtn = document.getElementById('sendResetCodeBtn');
    const spinner = sendBtn.querySelector('.spinner-border');
    
    if (!email) {
        alertEl.className = 'alert alert-warning';
        alertEl.textContent = 'Lütfen e-posta adresinizi giriniz.';
        alertEl.classList.remove('d-none');
        return;
    }
    
    sendBtn.disabled = true;
    spinner.classList.add('active');
    alertEl.classList.add('d-none');
    
    try {
        const response = await fetch(`${API_BASE_URL}/auth/forgot-password?email=${encodeURIComponent(email)}`, {
            method: 'POST'
        });
        
        if (response.ok) {
            forgotPasswordEmail = email;
            // Step 2'ye geç
            document.getElementById('forgotPasswordStep1').style.display = 'none';
            document.getElementById('forgotPasswordStep2').style.display = 'block';
            document.getElementById('forgotPasswordButtons1').style.display = 'none';
            document.getElementById('forgotPasswordButtons2').style.display = 'block';
        } else {
            const errorData = await response.json().catch(() => ({ message: 'Bir hata oluştu' }));
            throw new Error(errorData.message || 'Kod gönderilemedi');
        }
    } catch (error) {
        console.error('Şifre sıfırlama hatası:', error);
        alertEl.className = 'alert alert-danger';
        alertEl.textContent = 'Hata: ' + error.message;
        alertEl.classList.remove('d-none');
    } finally {
        sendBtn.disabled = false;
        spinner.classList.remove('active');
    }
}

// Şifreyi sıfırla
async function resetPassword() {
    const code = document.getElementById('resetCodeInput').value.trim();
    const newPassword = document.getElementById('newPasswordInput').value;
    const newPasswordConfirm = document.getElementById('newPasswordConfirmInput').value;
    const alertEl = document.getElementById('resetPasswordAlert');
    const resetBtn = document.getElementById('resetPasswordBtn');
    const spinner = resetBtn.querySelector('.spinner-border');
    
    if (!code) {
        alertEl.className = 'alert alert-warning';
        alertEl.textContent = 'Lütfen şifre sıfırlama kodunu giriniz.';
        alertEl.classList.remove('d-none');
        return;
    }
    
    if (!newPassword) {
        alertEl.className = 'alert alert-warning';
        alertEl.textContent = 'Lütfen yeni şifrenizi giriniz.';
        alertEl.classList.remove('d-none');
        return;
    }
    
    if (newPassword.length < 6) {
        alertEl.className = 'alert alert-warning';
        alertEl.textContent = 'Şifre en az 6 karakter olmalıdır.';
        alertEl.classList.remove('d-none');
        return;
    }
    
    if (newPassword !== newPasswordConfirm) {
        alertEl.className = 'alert alert-warning';
        alertEl.textContent = 'Şifreler eşleşmiyor.';
        alertEl.classList.remove('d-none');
        return;
    }
    
    resetBtn.disabled = true;
    spinner.classList.add('active');
    alertEl.classList.add('d-none');
    
    try {
        const response = await fetch(`${API_BASE_URL}/auth/reset-password`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: `email=${encodeURIComponent(forgotPasswordEmail)}&code=${encodeURIComponent(code)}&newPassword=${encodeURIComponent(newPassword)}`
        });
        
        if (response.ok) {
            alertEl.className = 'alert alert-success';
            alertEl.textContent = 'Şifreniz başarıyla sıfırlandı! Giriş yapabilirsiniz.';
            alertEl.classList.remove('d-none');
            
            // 2 saniye sonra modalı kapat ve login modalını göster
            setTimeout(() => {
                if (forgotPasswordModal) {
                    forgotPasswordModal.hide();
                }
                resetForgotPasswordForm();
                if (userLoginModal) {
                    userLoginModal.show();
                }
            }, 2000);
        } else {
            const errorData = await response.json().catch(() => ({ message: 'Bir hata oluştu' }));
            throw new Error(errorData.message || 'Şifre sıfırlanamadı');
        }
    } catch (error) {
        console.error('Şifre sıfırlama hatası:', error);
        alertEl.className = 'alert alert-danger';
        alertEl.textContent = 'Hata: ' + error.message;
        alertEl.classList.remove('d-none');
    } finally {
        resetBtn.disabled = false;
        spinner.classList.remove('active');
    }
}

// Şifre sıfırlama formunu sıfırla
function resetForgotPasswordForm() {
    document.getElementById('forgotPasswordEmailInput').value = '';
    document.getElementById('resetCodeInput').value = '';
    document.getElementById('newPasswordInput').value = '';
    document.getElementById('newPasswordConfirmInput').value = '';
    document.getElementById('forgotPasswordAlert').classList.add('d-none');
    document.getElementById('resetPasswordAlert').classList.add('d-none');
    forgotPasswordEmail = null;
    backToForgotPasswordStep1();
}

// Şifre sıfırlama Step 1'e geri dön
function backToForgotPasswordStep1() {
    document.getElementById('forgotPasswordStep1').style.display = 'block';
    document.getElementById('forgotPasswordStep2').style.display = 'none';
    document.getElementById('forgotPasswordButtons1').style.display = 'block';
    document.getElementById('forgotPasswordButtons2').style.display = 'none';
}

// Admin sayfasına geç (kullanıcı oturumunu temizle)
function switchToAdminPage(event) {
    if (event) {
        event.preventDefault();
    }
    
    // Modal açıksa kapat
    if (userLoginModal) {
        userLoginModal.hide();
    }
    
    // Kullanıcı oturumunu temizle
    localStorage.removeItem('kullaniciId');
    localStorage.removeItem('kullaniciEmail');
    currentUserId = null;
    currentUser = null;
    
    // Admin sayfasına yönlendir
    window.location.href = 'index.html';
}
