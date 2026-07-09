const projectForm = document.getElementById('projectForm');
const projectIdInput = document.getElementById('projectId');
const projectNameInput = document.getElementById('projectName');
const projectDescriptionInput = document.getElementById('projectDescription');
const projectList = document.getElementById('projectList');
const projectMessage = document.getElementById('projectMessage');
const projectSubmitButton = document.getElementById('projectSubmitButton');

const projectCountDisplay = document.getElementById('projectCount');
const inviteCountDisplay = document.getElementById('inviteCount');

function setMessage(message, color = '#22c55e') {
  projectMessage.textContent = message;
  projectMessage.style.color = color;
}

function resetProjectForm() {
  projectForm.reset();
  projectIdInput.value = '';
  projectSubmitButton.textContent = 'Create project';
}

async function fetchProjects() {
  try {
    const response = await fetch('/api/projects');
    if (!response.ok) {
      throw new Error('Unable to load projects');
    }
    const projects = await response.json();
    renderProjects(projects);
    updateOverview(projects);
  } catch (error) {
    setMessage('Unable to load projects. Please refresh.', '#dc2626');
  }
}

function updateOverview(projects) {
  if (projectCountDisplay) {
    projectCountDisplay.textContent = projects.length;
  }
  if (inviteCountDisplay) {
    const inviteCount = projects.reduce((count, project) => {
      const members = project.members || [];
      return count + members.filter(member => member.role && member.role.toUpperCase() !== 'OWNER').length;
    }, 0);
    inviteCountDisplay.textContent = inviteCount;
  }
}

function createMemberList(members) {
  if (!members?.length) {
    return '<p class="empty-state">No members have been invited yet.</p>';
  }
  return members
    .map(member => `<li>${member.username} <span class="role-tag">${member.role}</span></li>`)
    .join('');
}

function renderProjects(projects) {
  if (!projects.length) {
    projectList.innerHTML = '<div class="empty-card"><p>No projects yet. Create one to get started.</p></div>';
    return;
  }

  projectList.innerHTML = projects.map(project => `
    <article class="project-card" data-id="${project.id}">
      <div class="project-card-header">
        <div>
          <h3>${project.name}</h3>
          <p class="text-muted">Created by ${project.createdBy}</p>
        </div>
      </div>
      <p class="project-description">${project.description || 'No description added yet.'}</p>
      <div class="project-members">
        <h4>Members</h4>
        <ul>${createMemberList(project.members)}</ul>
      </div>
      <div class="project-actions" style="display:flex; gap:0.5rem; margin-top:0.75rem; flex-wrap:wrap;">
        <button type="button" class="text-button" data-action="join" data-id="${project.id}">Join</button>
        <button type="button" class="text-button" data-action="edit" data-id="${project.id}" data-name="${project.name}" data-description="${project.description || ''}">Edit</button>
        <button type="button" class="text-button text-danger" data-action="delete" data-id="${project.id}">Delete</button>
      </div>
      <form class="invite-form" onsubmit="inviteMember(event, ${project.id})">
        <label for="inviteEmail-${project.id}">Invite by email</label>
        <div class="invite-row">
          <input id="inviteEmail-${project.id}" name="email" type="email" placeholder="team@example.com" required />
          <button type="submit" class="btn-secondary">Invite</button>
        </div>
      </form>
    </article>
  `).join('');
}

async function inviteMember(event, projectId) {
  event.preventDefault();
  const form = event.target;
  const emailInput = form.querySelector('input[name="email"]');
  const email = emailInput.value.trim();

  if (!email) {
    setMessage('Please provide an email to invite.', '#dc2626');
    return;
  }

  try {
    const response = await fetch(`/api/projects/${projectId}/invite`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email }),
    });

    const body = await response.json();
    if (!response.ok) {
      setMessage(body.message || 'Unable to invite member.', '#dc2626');
      return;
    }

    setMessage(`Invited ${email} successfully.`);
    fetchProjects();
    emailInput.value = '';
  } catch (error) {
    setMessage('Invite failed. Please try again.', '#dc2626');
  }
}

async function joinProject(projectId) {
  try {
    const response = await fetch(`/api/projects/${projectId}/join`, { method: 'POST' });
    const body = await response.json().catch(() => ({}));
    if (!response.ok) {
      setMessage(body.message || 'Unable to join project.', '#dc2626');
      return;
    }

    setMessage('You joined the project successfully.');
    fetchProjects();
  } catch (error) {
    setMessage('Unable to join project. Please try again.', '#dc2626');
  }
}

async function deleteProject(projectId) {
  if (!window.confirm('Delete this project?')) {
    return;
  }

  try {
    const response = await fetch(`/api/projects/${projectId}`, { method: 'DELETE' });
    if (!response.ok) {
      const body = await response.json().catch(() => ({}));
      setMessage(body.message || 'Unable to delete project.', '#dc2626');
      return;
    }

    setMessage('Project deleted successfully.');
    fetchProjects();
  } catch (error) {
    setMessage('Delete project failed. Please try again.', '#dc2626');
  }
}

function startEditProject(projectId, name, description) {
  projectIdInput.value = projectId;
  projectNameInput.value = name;
  projectDescriptionInput.value = description;
  projectSubmitButton.textContent = 'Save changes';
  projectNameInput.focus();
}

projectForm.addEventListener('submit', async (event) => {
  event.preventDefault();
  setMessage('');

  const name = projectNameInput.value.trim();
  const description = projectDescriptionInput.value.trim();
  const projectId = projectIdInput.value;

  if (!name) {
    setMessage('Project name is required.', '#dc2626');
    return;
  }

  try {
    const response = await fetch(projectId ? `/api/projects/${projectId}` : '/api/projects', {
      method: projectId ? 'PUT' : 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, description }),
    });

    const body = await response.json().catch(() => ({}));
    if (!response.ok) {
      setMessage(body.message || (projectId ? 'Unable to update project.' : 'Unable to create project.'), '#dc2626');
      return;
    }

    setMessage(projectId ? 'Project updated successfully.' : 'Project created successfully.');
    resetProjectForm();
    fetchProjects();
  } catch (error) {
    setMessage(projectId ? 'Update project failed. Please try again.' : 'Create project failed. Please try again.', '#dc2626');
  }
});

projectList.addEventListener('click', (event) => {
  const button = event.target.closest('button[data-action]');
  if (!button) {
    return;
  }

  const action = button.getAttribute('data-action');
  const id = button.getAttribute('data-id');

  if (action === 'delete') {
    deleteProject(id);
  }

  if (action === 'join') {
    joinProject(id);
  }

  if (action === 'edit') {
    startEditProject(id, button.getAttribute('data-name'), button.getAttribute('data-description'));
  }
});

resetProjectForm();
fetchProjects();
