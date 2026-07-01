const sidebar = document.getElementById('dashboardSidebar');
const overlay = document.getElementById('sidebarOverlay');
const sidebarToggle = document.getElementById('sidebarToggle');
const createButton = document.getElementById('createButton');
const createMenu = document.getElementById('createMenu');
const avatarButton = document.getElementById('avatarButton');
const avatarMenu = document.getElementById('avatarMenu');

function closeDropdowns() {
  createMenu?.classList.remove('active');
  avatarMenu?.classList.remove('active');
  createButton?.setAttribute('aria-expanded', 'false');
  avatarButton?.setAttribute('aria-expanded', 'false');
}

sidebarToggle?.addEventListener('click', () => {
  document.body.classList.toggle('sidebar-open');
});

overlay?.addEventListener('click', () => {
  document.body.classList.remove('sidebar-open');
  closeDropdowns();
});

createButton?.addEventListener('click', (event) => {
  event.stopPropagation();
  createMenu?.classList.toggle('active');
  const isActive = createMenu?.classList.contains('active');
  createButton?.setAttribute('aria-expanded', isActive ? 'true' : 'false');
  avatarMenu?.classList.remove('active');
});

avatarButton?.addEventListener('click', (event) => {
  event.stopPropagation();
  avatarMenu?.classList.toggle('active');
  const isActive = avatarMenu?.classList.contains('active');
  avatarButton?.setAttribute('aria-expanded', isActive ? 'true' : 'false');
  createMenu?.classList.remove('active');
});

document.addEventListener('click', (event) => {
  const target = event.target;
  if (!createButton?.contains(target) && !createMenu?.contains(target)) {
    createMenu?.classList.remove('active');
    createButton?.setAttribute('aria-expanded', 'false');
  }
  if (!avatarButton?.contains(target) && !avatarMenu?.contains(target)) {
    avatarMenu?.classList.remove('active');
    avatarButton?.setAttribute('aria-expanded', 'false');
  }
});

window.addEventListener('resize', () => {
  if (window.innerWidth > 900) {
    document.body.classList.remove('sidebar-open');
  }
});
