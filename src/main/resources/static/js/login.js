/* ── TaskPilot – main.js ──────────────────────────────── */

const form       = document.getElementById('loginForm');
const emailInput = document.getElementById('email');
const pwInput    = document.getElementById('password');
const emailError = document.getElementById('emailError');
const pwError    = document.getElementById('passwordError');
const togglePw   = document.getElementById('togglePw');
const eyeIcon    = document.getElementById('eyeIcon');
const submitBtn  = document.getElementById('submitBtn');
const btnText    = document.getElementById('btnText');
const btnLoader  = document.getElementById('btnLoader');

/* ── Password visibility toggle ───────────────────────── */

togglePw.addEventListener('click', () => {
  const isHidden = pwInput.type === 'password';
  pwInput.type   = isHidden ? 'text' : 'password';
  eyeIcon.className = isHidden ? 'fa-regular fa-eye-slash' : 'fa-regular fa-eye';
});

/* ── Validation helpers ───────────────────────────────── */

function setError(input, errorEl, msg) {
  errorEl.textContent = msg;
  input.classList.add('is-error');
}

function clearError(input, errorEl) {
  errorEl.textContent = '';
  input.classList.remove('is-error');
}

function validateEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

/* ── Real-time validation ─────────────────────────────── */

emailInput.addEventListener('input', () => {
  if (emailInput.value && !validateEmail(emailInput.value)) {
    setError(emailInput, emailError, 'Please enter a valid email.');
  } else {
    clearError(emailInput, emailError);
  }
});

pwInput.addEventListener('input', () => {
  if (pwInput.value && pwInput.value.length < 6) {
    setError(pwInput, pwError, 'Password must be at least 6 characters.');
  } else {
    clearError(pwInput, pwError);
  }
});

/* ── Login submit ─────────────────────────────────────── */

form.addEventListener('submit', async (e) => {
  e.preventDefault();

  let valid = true;

  // Email validation
  if (!emailInput.value.trim()) {
    setError(emailInput, emailError, 'Email is required.');
    valid = false;
  } else if (!validateEmail(emailInput.value)) {
    setError(emailInput, emailError, 'Invalid email format.');
    valid = false;
  } else {
    clearError(emailInput, emailError);
  }

  // Password validation
  if (!pwInput.value) {
    setError(pwInput, pwError, 'Password is required.');
    valid = false;
  } else if (pwInput.value.length < 6) {
    setError(pwInput, pwError, 'Password must be at least 6 characters.');
    valid = false;
  } else {
    clearError(pwInput, pwError);
  }

  if (!valid) return;

  // Loading state
  submitBtn.disabled = true;
  btnText.classList.add('hidden');
  btnLoader.classList.remove('hidden');

  try {
    const response = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        email:    emailInput.value,
        password: pwInput.value,
      }),
    });

    const result = await response.text();

    if (response.ok) {
      btnText.textContent        = '✓ Signed in!';
      submitBtn.style.background = '#22c55e';
      setTimeout(() => { window.location.href = '/dashboard'; }, 1000);
    } else {
      alert(result);
    }
  } catch (error) {
    console.error(error);
    alert('Server Error');
  } finally {
    btnLoader.classList.add('hidden');
    btnText.classList.remove('hidden');
    submitBtn.disabled = false;
    setTimeout(() => {
      btnText.textContent        = 'Sign In';
      submitBtn.style.background = '';
    }, 2000);
  }
});

/* ── Social button demo ───────────────────────────────── */

document.querySelectorAll('.btn-social').forEach(btn => {
  btn.addEventListener('click', () => {
    alert(btn.textContent.trim() + ' OAuth would open here');
  });
});