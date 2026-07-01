const form = document.getElementById('registerForm');
const usernameInput = document.getElementById('username');
const emailInput = document.getElementById('email');
const passwordInput = document.getElementById('password');
const usernameError = document.getElementById('usernameError');
const emailError = document.getElementById('emailError');
const passwordError = document.getElementById('passwordError');
const submitBtn = document.getElementById('submitBtn');
const btnText = document.getElementById('btnText');
const btnLoader = document.getElementById('btnLoader');
const formMessage = document.getElementById('formMessage');
const togglePw = document.getElementById('togglePw');
const eyeIcon = document.getElementById('eyeIcon');

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

function setMessage(message, color = '#ef4444') {
  formMessage.textContent = message;
  formMessage.style.color = color;
}

togglePw.addEventListener('click', () => {
  const isHidden = passwordInput.type === 'password';
  passwordInput.type = isHidden ? 'text' : 'password';
  eyeIcon.className = isHidden ? 'fa-regular fa-eye-slash' : 'fa-regular fa-eye';
});

form.addEventListener('submit', async (event) => {
  event.preventDefault();

  let valid = true;
  setMessage('');

  if (!usernameInput.value.trim()) {
    setError(usernameInput, usernameError, 'Username is required.');
    valid = false;
  } else {
    clearError(usernameInput, usernameError);
  }

  if (!emailInput.value.trim()) {
    setError(emailInput, emailError, 'Email is required.');
    valid = false;
  } else if (!validateEmail(emailInput.value)) {
    setError(emailInput, emailError, 'Please enter a valid email.');
    valid = false;
  } else {
    clearError(emailInput, emailError);
  }

  if (!passwordInput.value) {
    setError(passwordInput, passwordError, 'Password is required.');
    valid = false;
  } else if (passwordInput.value.length < 6) {
    setError(passwordInput, passwordError, 'Password must be at least 6 characters.');
    valid = false;
  } else {
    clearError(passwordInput, passwordError);
  }

  if (!valid) {
    return;
  }

  submitBtn.disabled = true;
  btnText.classList.add('hidden');
  btnLoader.classList.remove('hidden');

  try {
    const response = await fetch('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username: usernameInput.value,
        email: emailInput.value,
        password: passwordInput.value,
      }),
    });

    const result = await response.text();

    if (response.ok) {
      setMessage('Registration successful! Redirecting to login...', '#22c55e');
      setTimeout(() => {
        window.location.href = '/login';
      }, 1400);
    } else {
      setMessage(result || 'Registration failed.');
    }
  } catch (error) {
    setMessage('Server error. Please try again later.');
  } finally {
    btnLoader.classList.add('hidden');
    btnText.classList.remove('hidden');
    submitBtn.disabled = false;
  }
});
