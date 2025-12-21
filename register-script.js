// API Base URL
const API_BASE_URL = 'http://localhost:8080/api';

// Global değişkenler
let emailVerified = false;

// Form submit event
document.getElementById('registerForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    await registerUser();
});

// Üye kayıt işlemi
async function registerUser() {
    const adSoyad = document.getElementById('adSoyadInput').value.trim();
    const email = document.getElementById('emailInput').value.trim();
    const telefon = document.getElementById('telefonInput').value.trim();
    const uyeNo = document.getElementById('uyeNoInput').value.trim();
    const alertEl = document.getElementById('registerAlert');
    const registerBtn = document.getElementById('registerBtn');
    const spinner = document.getElementById('registerSpinner');
    
    // Validasyon
    if (!adSoyad) {
        showAlert('Lütfen ad soyad giriniz.', 'warning', alertEl);
        return;
    }
    
    if (!email) {
        showAlert('Lütfen e-posta adresinizi giriniz.', 'warning', alertEl);
        return;
    }
    
    // Email format kontrolü
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
        showAlert('Lütfen geçerli bir e-posta adresi giriniz.', 'warning', alertEl);
        return;
    }
    
    // Telefon validasyonu
    if (!telefon) {
        showAlert('Lütfen telefon numaranızı giriniz.', 'warning', alertEl);
        return;
    }
    
    // Telefon 11 haneli olmalı (sadece rakam)
    const telefonTemiz = telefon.replace(/\D/g, ''); // Sadece rakamları al
    if (telefonTemiz.length !== 11) {
        showAlert('Telefon numarası tam olarak 11 haneli olmalıdır (örn: 05551234567).', 'warning', alertEl);
        return;
    }
    
    // Üye numarası validasyonu
    if (!uyeNo) {
        showAlert('Lütfen üye numaranızı giriniz.', 'warning', alertEl);
        return;
    }
    
    // Üye numarası 6 haneli olmalı
    if (!/^\d{6}$/.test(uyeNo)) {
        showAlert('Üye numarası tam olarak 6 haneli olmalıdır (sadece rakam).', 'warning', alertEl);
        return;
    }
    
    // E-posta doğrulaması kontrolü
    if (!emailVerified) {
        showAlert('Lütfen önce e-posta adresinizi doğrulayın.', 'warning', alertEl);
        return;
    }
    
    // Loading göster
    registerBtn.disabled = true;
    spinner.classList.remove('d-none');
    alertEl.classList.add('d-none');
    
    try {
        // Üye verisi oluştur
        const uyeData = {
            dtype: 'UYE', // Jackson için tip bilgisi
            adSoyad: adSoyad,
            email: email,
            telefon: telefonTemiz, // Temizlenmiş telefon numarası (sadece rakamlar)
            uyeNo: uyeNo
        };
        
        // API'ye gönder
        const response = await fetch(`${API_BASE_URL}/kullanicilar/uye`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(uyeData)
        });
        
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: 'Bir hata oluştu' }));
            throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
        }
        
        const newUser = await response.json();
        
        // Başarılı kayıt
        showAlert('Kayıt başarılı! Giriş sayfasına yönlendiriliyorsunuz...', 'success', alertEl);
        
        // 2 saniye sonra giriş sayfasına yönlendir
        setTimeout(() => {
            window.location.href = 'user.html';
        }, 2000);
        
    } catch (error) {
        console.error('Kayıt hatası:', error);
        showAlert('Hata: ' + error.message, 'danger', alertEl);
    } finally {
        registerBtn.disabled = false;
        spinner.classList.add('d-none');
    }
}

// E-posta doğrulama kodu gönder
async function sendVerificationCode() {
    const email = document.getElementById('emailInput').value.trim();
    const sendCodeBtn = document.getElementById('sendCodeBtn');
    const sendCodeSpinner = document.getElementById('sendCodeSpinner');
    const alertEl = document.getElementById('registerAlert');
    
    if (!email) {
        showAlert('Lütfen önce e-posta adresinizi giriniz.', 'warning', alertEl);
        return;
    }
    
    // Email format kontrolü
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
        showAlert('Lütfen geçerli bir e-posta adresi giriniz.', 'warning', alertEl);
        return;
    }
    
    // Loading göster
    sendCodeBtn.disabled = true;
    sendCodeSpinner.classList.remove('d-none');
    alertEl.classList.add('d-none');
    
    try {
        const response = await fetch(`${API_BASE_URL}/kullanicilar/email/kod-gonder`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email: email })
        });
        
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: 'Bir hata oluştu' }));
            throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
        }
        
        const result = await response.json();
        
        // Kod gönderme başarılı
        showAlert('Doğrulama kodu e-posta adresinize gönderildi. Lütfen e-postanızı kontrol ediniz.', 'success', alertEl);
        
        // Kod girme alanını göster
        document.getElementById('verificationCodeContainer').style.display = 'block';
        document.getElementById('verificationCodeInput').value = '';
        emailVerified = false;
        
        // Test için console'a kod yazdır (production'da kaldırılmalı)
        console.log('Test için doğrulama kodu:', result.code || 'Kod backend console\'da görüntüleniyor');
        
    } catch (error) {
        console.error('Kod gönderme hatası:', error);
        showAlert('Hata: ' + error.message, 'danger', alertEl);
    } finally {
        sendCodeBtn.disabled = false;
        sendCodeSpinner.classList.add('d-none');
    }
}

// E-posta doğrulama kodu doğrula
async function verifyCode() {
    const email = document.getElementById('emailInput').value.trim();
    const code = document.getElementById('verificationCodeInput').value.trim();
    const verifyCodeBtn = document.getElementById('verifyCodeBtn');
    const verifyCodeSpinner = document.getElementById('verifyCodeSpinner');
    const codeStatus = document.getElementById('codeStatus');
    const alertEl = document.getElementById('registerAlert');
    
    if (!email) {
        showAlert('Lütfen e-posta adresinizi giriniz.', 'warning', alertEl);
        return;
    }
    
    if (!code) {
        showAlert('Lütfen doğrulama kodunu giriniz.', 'warning', alertEl);
        return;
    }
    
    // Kod 6 haneli olmalı
    if (!/^\d{6}$/.test(code)) {
        showAlert('Doğrulama kodu 6 haneli olmalıdır.', 'warning', alertEl);
        return;
    }
    
    // Loading göster
    verifyCodeBtn.disabled = true;
    verifyCodeSpinner.classList.remove('d-none');
    codeStatus.innerHTML = '';
    alertEl.classList.add('d-none');
    
    try {
        const response = await fetch(`${API_BASE_URL}/kullanicilar/email/kod-dogrula`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email: email, code: code })
        });
        
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: 'Bir hata oluştu' }));
            throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
        }
        
        // Kod doğrulama başarılı
        emailVerified = true;
        codeStatus.innerHTML = '<div class="alert alert-success mb-0"><i class="bi bi-check-circle"></i> E-posta doğrulandı!</div>';
        document.getElementById('verificationCodeInput').disabled = true;
        verifyCodeBtn.disabled = true;
        verifyCodeBtn.innerHTML = '<i class="bi bi-check-circle"></i> Doğrulandı';
        
    } catch (error) {
        console.error('Kod doğrulama hatası:', error);
        codeStatus.innerHTML = '<div class="alert alert-danger mb-0"><i class="bi bi-x-circle"></i> ' + error.message + '</div>';
        emailVerified = false;
        // Hatalı girişte input'u tekrar aktif et
        document.getElementById('verificationCodeInput').disabled = false;
        document.getElementById('verificationCodeInput').value = '';
        document.getElementById('verificationCodeInput').focus();
        verifyCodeBtn.innerHTML = '<i class="bi bi-check-circle"></i> Doğrula';
    } finally {
        verifyCodeBtn.disabled = false;
        verifyCodeSpinner.classList.add('d-none');
    }
}

// Alert göster
function showAlert(message, type = 'info', alertEl) {
    alertEl.className = `alert alert-${type}`;
    alertEl.textContent = message;
    alertEl.classList.remove('d-none');
}

