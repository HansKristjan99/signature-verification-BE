const BASE_URL = 'http://localhost:8080';
const TOKEN_KEY = 'session_token';

// Helper to read cookies (Spring stores the CSRF token in 'XSRF-TOKEN')
function getCsrfToken(): string | null {
  const match = document.cookie.match(new RegExp('(^| )XSRF-TOKEN=([^;]+)'));
  return match ? match[2] : null;
}

export const apiClient = {
  async get(endpoint: string): Promise<Response> {
    const token = localStorage.getItem(TOKEN_KEY);
    const headers = new Headers();

    if (token) {
      headers.set('X-Session-Token', token);
    }

    return fetch(`${BASE_URL}${endpoint}`, {
      headers,
      credentials: 'include',
    });
  },

  async post(endpoint: string, body?: BodyInit, additionalHeaders?: HeadersInit): Promise<Response> {
    return this.sendRequest('POST', endpoint, body, additionalHeaders);
  },

  async put(endpoint: string, body?: BodyInit, additionalHeaders?: HeadersInit): Promise<Response> {
    return this.sendRequest('PUT', endpoint, body, additionalHeaders);
  },

  async delete(endpoint: string): Promise<Response> {
    return this.sendRequest('DELETE', endpoint, undefined);
  },

  // Centralized request handler for state-changing methods (POST/PUT/DELETE)
  async sendRequest(method: string, endpoint: string, body?: BodyInit, additionalHeaders?: HeadersInit): Promise<Response> {
    const token = localStorage.getItem(TOKEN_KEY);
    const headers = new Headers(additionalHeaders);

    // 1. Add Custom Session Token (if you still use it alongside cookies)
    if (token) {
      headers.set('X-Session-Token', token);
    }

    // 2. Add CSRF Token (Required for Spring Security)
    const csrfToken = getCsrfToken();
    if (csrfToken) {
      headers.set('X-XSRF-TOKEN', csrfToken);
    }

    // 3. Ensure JSON content type if body is a string (Optional, but good practice)
    if (typeof body === 'string' && !headers.has('Content-Type')) {
      headers.set('Content-Type', 'application/json');
    }

    return fetch(`${BASE_URL}${endpoint}`, {
      method,
      headers,
      body,
      credentials: 'include', // CRITICAL: Sends cookies (Session & CSRF)
    });
  }
};