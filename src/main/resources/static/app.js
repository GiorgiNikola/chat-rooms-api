const API_BASE = 'http://localhost:8080/api';
const WS_URL = 'http://localhost:8080/ws';

let token = localStorage.getItem('token');
let currentUserEmail = localStorage.getItem('userEmail');
let stompClient = null;
let currentRoomId = null;

function showView(id) {
  document.querySelectorAll('.view').forEach(v => v.classList.add('hidden'));
  document.getElementById(id).classList.remove('hidden');
}

function showError(elementId, message) {
  const el = document.getElementById(elementId);
  el.textContent = message;
  el.classList.remove('hidden');
}

function clearError(elementId) {
  document.getElementById(elementId).classList.add('hidden');
}

function showRegister() {
  document.getElementById('loginForm').classList.add('hidden');
  document.getElementById('registerForm').classList.remove('hidden');
  clearError('authError');
}

function showLogin() {
  document.getElementById('registerForm').classList.add('hidden');
  document.getElementById('loginForm').classList.remove('hidden');
  clearError('authError');
}

// -------------------- Auth --------------------

async function register() {
  clearError('authError');
  const email = document.getElementById('registerEmail').value.trim();
  const password = document.getElementById('registerPassword').value;
  const displayName = document.getElementById('registerDisplayName').value.trim();

  try {
    const res = await fetch(`${API_BASE}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password, displayName })
    });
    const data = await res.json();
    if (!res.ok) {
      showError('authError', data.message || 'Registration failed');
      return;
    }
    showLogin();
    document.getElementById('loginEmail').value = email;
  } catch (err) {
    showError('authError', 'Could not reach server: ' + err.message);
  }
}

async function login() {
  clearError('authError');
  const email = document.getElementById('loginEmail').value.trim();
  const password = document.getElementById('loginPassword').value;

  try {
    const res = await fetch(`${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });
    const data = await res.json();
    if (!res.ok) {
      showError('authError', data.message || 'Login failed');
      return;
    }
    token = data.token;
    currentUserEmail = email;
    localStorage.setItem('token', token);
    localStorage.setItem('userEmail', email);
    enterRoomsView();
  } catch (err) {
    showError('authError', 'Could not reach server: ' + err.message);
  }
}

function logout() {
  disconnectSocket();
  token = null;
  currentUserEmail = null;
  localStorage.removeItem('token');
  localStorage.removeItem('userEmail');
  showView('authView');
  showLogin();
}

// -------------------- API helper --------------------

async function apiCall(path, options = {}) {
  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
      ...(options.headers || {})
    }
  });
  if (res.status === 204) return null;
  const data = await res.json().catch(() => null);
  if (!res.ok) {
    throw new Error((data && data.message) || `Request failed (${res.status})`);
  }
  return data;
}

// -------------------- Rooms --------------------

function enterRoomsView() {
  document.getElementById('whoami').textContent = currentUserEmail;
  showView('roomsView');
  loadRooms();
}

async function createRoom() {
  clearError('roomsError');
  const name = document.getElementById('newRoomName').value.trim();
  const description = document.getElementById('newRoomDescription').value.trim();
  const isPrivate = document.getElementById('newRoomPrivate').checked;

  try {
    await apiCall('/rooms', {
      method: 'POST',
      body: JSON.stringify({ name, description, isPrivate })
    });
    document.getElementById('newRoomName').value = '';
    document.getElementById('newRoomDescription').value = '';
    document.getElementById('newRoomPrivate').checked = false;
    loadRooms();
  } catch (err) {
    showError('roomsError', err.message);
  }
}

async function joinRoomById() {
  clearError('roomsError');
  const roomId = document.getElementById('joinRoomId').value.trim();
  if (!roomId) return;

  try {
    await apiCall(`/rooms/${roomId}/members`, { method: 'POST' });
    document.getElementById('joinRoomId').value = '';
    loadRooms();
  } catch (err) {
    showError('roomsError', err.message);
  }
}

async function loadRooms() {
  clearError('roomsError');
  try {
    const page = await apiCall('/rooms?page=0&size=50');
    const listEl = document.getElementById('roomList');
    listEl.innerHTML = '';

    if (!page.content.length) {
      listEl.innerHTML = '<p class="switchLink">No rooms yet. Create one above.</p>';
      return;
    }

    page.content.forEach(room => {
      const row = document.createElement('div');
      row.className = 'roomRow';
      row.innerHTML = `
        <div>
          <strong>${escapeHtml(room.name)}</strong>
          <div class="meta">#${room.id} · owner: ${escapeHtml(room.ownerDisplayName)}${room.private ? ' · private' : ''}</div>
        </div>
        <button onclick="openChat(${room.id}, '${escapeHtml(room.name).replace(/'/g, "\\'")}')">Open</button>
      `;
      listEl.appendChild(row);
    });
  } catch (err) {
    showError('roomsError', err.message);
  }
}

function escapeHtml(str) {
  const div = document.createElement('div');
  div.textContent = str ?? '';
  return div.innerHTML;
}

// -------------------- Chat / WebSocket --------------------

function openChat(roomId, roomName) {
  currentRoomId = roomId;
  document.getElementById('chatRoomTitle').textContent = roomName;
  document.getElementById('messageList').innerHTML = '';
  showView('chatView');
  loadMessageHistory(roomId);
  connectSocket(roomId);
}

function leaveChatView() {
  disconnectSocket();
  showView('roomsView');
  loadRooms();
}

async function loadMessageHistory(roomId) {
  try {
    const page = await apiCall(`/rooms/${roomId}/messages?page=0&size=50`);
    const messages = [...page.content].reverse();
    messages.forEach(renderMessage);
    scrollMessagesToBottom();
  } catch (err) {
    console.error('Failed to load history:', err.message);
  }
}

function connectSocket(roomId) {
  const socket = new SockJS(WS_URL);
  stompClient = Stomp.over(socket);
  stompClient.debug = null; // quiet console

  stompClient.connect(
    { Authorization: `Bearer ${token}` },
    () => {
      setWsStatus(true);
      stompClient.subscribe(`/topic/rooms/${roomId}`, (frame) => {
        const message = JSON.parse(frame.body);
        renderMessage(message);
        scrollMessagesToBottom();
      });
    },
    (error) => {
      setWsStatus(false);
      console.error('WebSocket error:', error);
    }
  );
}

function disconnectSocket() {
  if (stompClient && stompClient.connected) {
    stompClient.disconnect();
  }
  stompClient = null;
  setWsStatus(false);
}

function setWsStatus(connected) {
  const el = document.getElementById('wsStatus');
  el.textContent = connected ? 'connected' : 'disconnected';
  el.className = 'status ' + (connected ? 'connected' : 'disconnected');
}

function sendMessage() {
  const input = document.getElementById('messageInput');
  const content = input.value.trim();
  if (!content || !stompClient || !stompClient.connected) return;

  stompClient.send(`/app/rooms/${currentRoomId}/send`, {}, JSON.stringify({ content }));
  input.value = '';
}

function renderMessage(message) {
  const listEl = document.getElementById('messageList');
  const isOwn = message.senderId && currentUserEmail && message.senderDisplayName;
  const row = document.createElement('div');
  row.className = 'message';
  const time = new Date(message.sentAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  row.innerHTML = `
    <span class="sender">${escapeHtml(message.senderDisplayName)}</span>
    <span class="time">${time}</span>
    <div class="content">${escapeHtml(message.content)}</div>
  `;
  listEl.appendChild(row);
}

function scrollMessagesToBottom() {
  const listEl = document.getElementById('messageList');
  listEl.scrollTop = listEl.scrollHeight;
}

// -------------------- Init --------------------

if (token) {
  enterRoomsView();
} else {
  showView('authView');
}
