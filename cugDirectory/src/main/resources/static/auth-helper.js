/**
 * auth-helper.js
 * Shared utilities used by every page.
 *
 * No CSRF tokens needed — CSRF is disabled server-side because this is a
 * same-origin SPA. All requests simply include credentials: 'same-origin'
 * so the JSESSIONID cookie is sent automatically.
 */

/**
 * Thin fetch wrapper that always sends the session cookie.
 * Automatically sets Content-Type to application/json when a body is provided.
 */
async function apiFetch(url, options = {}) {
    const opts = Object.assign({ credentials: 'same-origin' }, options);

    // Default to JSON content-type when sending a body object
    if (opts.body && typeof opts.body === 'object' && !(opts.body instanceof URLSearchParams)) {
        opts.body = JSON.stringify(opts.body);
        opts.headers = Object.assign({ 'Content-Type': 'application/json' }, opts.headers);
    }

    return fetch(url, opts);
}

/**
 * Checks if the current session is authenticated.
 * Redirects to /login and returns null if not.
 * Call this at the top of every protected page.
 */
async function requireLogin() {
    try {
        const res = await apiFetch('/api/auth/me');
        if (res.status === 401) {
            window.location.href = '/login';
            return null;
        }
        return res.json();
    } catch (err) {
        window.location.href = '/login';
        return null;
    }
}

/**
 * Logs the current user out and redirects to the login page.
 */
async function logout() {
    await apiFetch('/api/auth/logout', { method: 'POST' }).catch(() => {});
    window.location.href = '/login';
}