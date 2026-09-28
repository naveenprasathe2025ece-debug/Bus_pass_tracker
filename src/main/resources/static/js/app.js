/* Shared helpers for all pages: token storage, API calls, formatting. */
const Auth = {
  get token() { return localStorage.getItem('bp_token'); },
  get user() {
    try { return JSON.parse(localStorage.getItem('bp_user') || 'null'); } catch (e) { return null; }
  },
  save(r) {
    localStorage.setItem('bp_token', r.token);
    localStorage.setItem('bp_user', JSON.stringify({ id: r.userId, name: r.name, email: r.email, role: r.role }));
  },
  clear() { localStorage.removeItem('bp_token'); localStorage.removeItem('bp_user'); },
  logout() { Auth.clear(); location.href = '/index.html'; }
};

async function api(path, options = {}) {
  const headers = Object.assign({ 'Content-Type': 'application/json' }, options.headers || {});
  if (Auth.token) headers['Authorization'] = 'Bearer ' + Auth.token;
  const res = await fetch(path, Object.assign({}, options, { headers }));
  let data = null;
  if (res.status !== 204) {
    const text = await res.text();
    try { data = text ? JSON.parse(text) : null; } catch (e) { data = null; }
  }
  if (res.status === 401 && !path.startsWith('/api/auth/')) {
    Auth.logout();
    throw new Error('Session expired. Please log in again.');
  }
  if (!res.ok) {
    const err = new Error((data && data.message) || 'Request failed (' + res.status + ')');
    err.status = res.status;
    err.fieldErrors = data && data.fieldErrors;
    throw err;
  }
  return data;
}

function esc(value) {
  if (value === null || value === undefined) return '';
  return String(value).replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

function fmtDate(d) { return d ? esc(d) : '-'; }
function fmtDateTime(d) { return d ? esc(String(d).replace('T', ' ').substring(0, 16)) : '-'; }

function errorText(e) {
  if (e && e.fieldErrors) {
    return Object.entries(e.fieldErrors).map(([k, v]) => k + ': ' + v).join(' | ');
  }
  return (e && e.message) || 'Something went wrong';
}

function showMessage(el, text, type) {
  el.textContent = text;
  el.className = 'msg ' + (type || 'success');
  el.classList.remove('hidden');
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function requireRole(role) {
  const u = Auth.user;
  if (!Auth.token || !u || u.role !== role) {
    Auth.clear();
    location.href = '/index.html';
    return false;
  }
  return true;
}

function badge(text) { return '<span class="badge badge-' + esc(text) + '">' + esc(text) + '</span>'; }
