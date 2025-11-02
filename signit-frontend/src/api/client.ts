const BASE_URL = 'http://localhost:8080';

const TOKEN_KEY = 'session_token';

export const apiClient = {
  async get(endpoint: string): Promise<Response> {
    const token = localStorage.getItem(TOKEN_KEY);
    const headers = new Headers();

    if (token) {
      headers.set('X-Session-Token', token);
    }

    return fetch(`${BASE_URL}${endpoint}`, {
      method: 'GET',
      headers,
    });
  },

  async post(endpoint: string, body?: BodyInit, additionalHeaders?: HeadersInit): Promise<Response> {
    const token = localStorage.getItem(TOKEN_KEY);
    const headers = new Headers(additionalHeaders);

    if (token) {
      headers.set('X-Session-Token', token);
    }

    return fetch(`${BASE_URL}${endpoint}`, {
      method: 'POST',
      headers,
      body,
    });
  },

  async put(endpoint: string, body?: BodyInit, additionalHeaders?: HeadersInit): Promise<Response> {
    const token = localStorage.getItem(TOKEN_KEY);
    const headers = new Headers(additionalHeaders);

    if (token) {
      headers.set('X-Session-Token', token);
    }

    return fetch(`${BASE_URL}${endpoint}`, {
      method: 'PUT',
      headers,
      body,
    });
  },

  async delete(endpoint: string): Promise<Response> {
    const token = localStorage.getItem(TOKEN_KEY);
    const headers = new Headers();

    if (token) {
      headers.set('X-Session-Token', token);
    }

    return fetch(`${BASE_URL}${endpoint}`, {
      method: 'DELETE',
      headers,
    });
  },
};
