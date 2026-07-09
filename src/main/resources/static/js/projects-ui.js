// Projects CRUD Management
const projectModal = document.getElementById('projectModal');
const detailModal = document.getElementById('detailModal');
const projectForm = document.getElementById('projectForm');
const projectsContainer = document.getElementById('projectsContainer');
const projectMessage = document.getElementById('projectMessage');
const newProjectBtn = document.getElementById('newProjectBtn');
const closeModal = document.getElementById('closeModal');
const cancelBtn = document.getElementById('cancelBtn');
const closeDetail = document.getElementById('closeDetail');
const closeDetail2 = document.getElementById('closeDetail2');
const projectSearch = document.getElementById('projectSearch');

let allProjects = [];
let currentProjectId = null;

// Modal Management
newProjectBtn.addEventListener('click', () => openCreateModal());
closeModal.addEventListener('click', () => closeProjectModal());
cancelBtn.addEventListener('click', () => closeProjectModal());
closeDetail.addEventListener('click', () => closeDetailModal());
closeDetail2.addEventListener('click', () => closeDetailModal());

projectModal.addEventListener('click', (e) => {
  if (e.target === projectModal) closeProjectModal();
});

detailModal.addEventListener('click', (e) => {
  if (e.target === detailModal) closeDetailModal();
});

// Form Submit
projectForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  const projectId = document.getElementById('projectId').value;
  const name = document.getElementById('projectName').value.trim();
  const description = document.getElementById('projectDescription').value.trim();

  if (!name) {
    showFormError('nameError', 'Project name is required');
    return;
  }

  const method = projectId ? 'PUT' : 'POST';
  const url = projectId ? `/api/projects/${projectId}` : '/api/projects';

  try {
    const response = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, description }),
    });

    const result = await response.json().catch(() => ({}));

    if (!response.ok) {
      showMessage(result.message || `Unable to ${projectId ? 'update' : 'create'} project.`, 'error');
      return;
    }

    showMessage(
      projectId ? 'Project updated successfully.' : 'Project created successfully.',
      'success'
    );
    closeProjectModal();
    fetchProjects();
  } catch (error) {
    showMessage(`${projectId ? 'Update' : 'Create'} project failed. Please try again.`, 'error');
  }
});

// Search/Filter
projectSearch.addEventListener('input', (e) => {
  const query = e.target.value.toLowerCase();
  renderProjects(
    allProjects.filter(
      (p) =>
        p.name.toLowerCase().includes(query) ||
        (p.description && p.description.toLowerCase().includes(query))
    )
  );
});

// Fetch Projects
async function fetchProjects() {
  try {
    const response = await fetch('/api/projects');
    if (!response.ok) throw new Error('Failed to fetch projects');

    allProjects = await response.json();
    renderProjects(allProjects);
  } catch (error) {
    showMessage('Unable to load projects. Please refresh.', 'error');
  }
}

// Render Projects
function renderProjects(projects) {
  if (projects.length === 0) {
    projectsContainer.innerHTML = `
      <div class="empty-state" style="grid-column: 1/-1;">
        <div class="empty-state-icon"><i class="fas fa-layer-group"></i></div>
        <h3>No projects yet</h3>
        <p>Create your first project to get started.</p>
      </div>
    `;
    return;
  }

  projectsContainer.innerHTML = projects
    .map(
      (project) => `
    <article class="project-card" data-id="${project.id}">
      <div class="project-card-header">
        <div>
          <h3>${escapeHtml(project.name)}</h3>
          <p class="text-muted">Created by ${escapeHtml(project.createdBy)}</p>
        </div>
      </div>
      <p class="project-description">${escapeHtml(project.description || 'No description added yet.')}</p>
      <div class="project-members">
        <h4>Members (${project.members?.length || 0})</h4>
        <ul>
          ${
            project.members && project.members.length > 0
              ? project.members
                  .map(
                    (member) =>
                      `<li>${escapeHtml(member.username)} <span class="role-tag">${member.role}</span></li>`
                  )
                  .join('')
              : '<li style="color: var(--muted);">No members yet</li>'
          }
        </ul>
      </div>
      <div class="project-footer">
        <button type="button" class="text-button" data-action="view" data-id="${project.id}">View</button>
        <button type="button" class="text-button" data-action="edit" data-id="${project.id}">Edit</button>
        <button type="button" class="text-button text-danger" data-action="delete" data-id="${project.id}">Delete</button>
      </div>
    </article>
  `
    )
    .join('');

  attachCardListeners();
}

// Card Action Listeners
function attachCardListeners() {
  document.querySelectorAll('.project-card .text-button').forEach((btn) => {
    btn.addEventListener('click', (e) => {
      const action = e.target.getAttribute('data-action');
      const id = e.target.getAttribute('data-id');

      if (action === 'view') viewProject(id);
      else if (action === 'edit') editProject(id);
      else if (action === 'delete') deleteProject(id);
    });
  });
}

// Actions
function openCreateModal() {
  document.getElementById('modalTitle').textContent = 'Create New Project';
  projectForm.reset();
  document.getElementById('projectId').value = '';
  document.getElementById('submitBtn').textContent = 'Create Project';
  clearFormErrors();
  projectModal.style.display = 'flex';
}

function editProject(id) {
  const project = allProjects.find((p) => p.id == id);
  if (!project) return;

  document.getElementById('modalTitle').textContent = 'Edit Project';
  document.getElementById('projectId').value = id;
  document.getElementById('projectName').value = project.name;
  document.getElementById('projectDescription').value = project.description || '';
  document.getElementById('submitBtn').textContent = 'Save Changes';
  clearFormErrors();
  projectModal.style.display = 'flex';
}

async function deleteProject(id) {
  if (!confirm('Are you sure you want to delete this project? This action cannot be undone.')) {
    return;
  }

  try {
    const response = await fetch(`/api/projects/${id}`, { method: 'DELETE' });

    if (!response.ok) {
      const result = await response.json().catch(() => ({}));
      showMessage(result.message || 'Unable to delete project.', 'error');
      return;
    }

    showMessage('Project deleted successfully.', 'success');
    fetchProjects();
  } catch (error) {
    showMessage('Delete failed. Please try again.', 'error');
  }
}

async function viewProject(id) {
  try {
    const response = await fetch(`/api/projects/${id}`);
    if (!response.ok) throw new Error('Failed to fetch project');

    const project = await response.json();
    currentProjectId = id;

    document.getElementById('detailTitle').textContent = escapeHtml(project.name);
    document.getElementById('detailName').textContent = escapeHtml(project.name);
    document.getElementById('detailDescription').textContent = escapeHtml(
      project.description || 'No description'
    );
    document.getElementById('detailCreatedBy').textContent = escapeHtml(project.createdBy);

    const membersList = document.getElementById('teamMembersList');
    if (project.members && project.members.length > 0) {
      membersList.innerHTML = project.members
        .map(
          (member) => `
        <div class="team-member-item">
          <div class="team-member-info">
            <div class="member-avatar">${member.username.charAt(0).toUpperCase()}</div>
            <div>
              <div>${escapeHtml(member.username)}</div>
              <small style="color: var(--muted);">${escapeHtml(member.email)}</small>
            </div>
          </div>
          <span class="role-tag">${member.role}</span>
        </div>
      `
        )
        .join('');
    } else {
      membersList.innerHTML = '<p style="color: var(--muted); text-align: center;">No members yet</p>';
    }

    detailModal.style.display = 'flex';
  } catch (error) {
    showMessage('Unable to load project details.', 'error');
  }
}

// Invite Member
document.getElementById('inviteBtn').addEventListener('click', async () => {
  const email = document.getElementById('inviteEmail').value.trim();

  if (!email) {
    alert('Please enter an email address');
    return;
  }

  try {
    const response = await fetch(`/api/projects/${currentProjectId}/invite`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email }),
    });

    const result = await response.json().catch(() => ({}));

    if (!response.ok) {
      alert(result.message || 'Unable to invite member');
      return;
    }

    alert(`Invited ${email} successfully!`);
    document.getElementById('inviteEmail').value = '';
    viewProject(currentProjectId);
    fetchProjects();
  } catch (error) {
    alert('Invite failed. Please try again.');
  }
});

// UI Helpers
function closeProjectModal() {
  projectModal.style.display = 'none';
  projectForm.reset();
  clearFormErrors();
}

function closeDetailModal() {
  detailModal.style.display = 'none';
  currentProjectId = null;
}

function showMessage(message, type) {
  projectMessage.textContent = message;
  projectMessage.className = `message-box ${type}`;
  setTimeout(() => {
    projectMessage.className = 'message-box';
  }, 4000);
}

function showFormError(fieldId, message) {
  document.getElementById(fieldId).textContent = message;
}

function clearFormErrors() {
  document.getElementById('nameError').textContent = '';
  document.getElementById('descriptionError').textContent = '';
}

function escapeHtml(text) {
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
}

// Initialize
fetchProjects();

// Avatar dropdown toggle
const avatarButton = document.getElementById('avatarButton');
const avatarMenu = document.getElementById('avatarMenu');

avatarButton?.addEventListener('click', () => {
  avatarMenu.style.display =
    avatarMenu.style.display === 'block' ? 'none' : 'block';
  avatarButton.setAttribute(
    'aria-expanded',
    avatarButton.getAttribute('aria-expanded') === 'false' ? 'true' : 'false'
  );
});

document.addEventListener('click', (e) => {
  if (!e.target.closest('.avatar-dropdown')) {
    avatarMenu.style.display = 'none';
    avatarButton?.setAttribute('aria-expanded', 'false');
  }
});
