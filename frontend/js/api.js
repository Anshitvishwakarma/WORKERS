/**
 * FixMate API Client
 * Central HTTP client with JWT handling, error parsing, and status management.
 */
class ApiClient {
    constructor() {
        this.baseUrl = CONFIG.API_BASE_URL;
    }

    getToken() {
        return localStorage.getItem(CONFIG.TOKEN_KEY);
    }

    setToken(token) {
        if (token) {
            localStorage.setItem(CONFIG.TOKEN_KEY, token);
        } else {
            localStorage.removeItem(CONFIG.TOKEN_KEY);
        }
    }

    async request(path, options = {}) {
        const url = new URL(`${this.baseUrl}${path.startsWith('/') ? path : '/' + path}`);

        // Handle URL query parameters if provided
        if (options.params) {
            Object.keys(options.params).forEach(key => {
                const val = options.params[key];
                if (val !== undefined && val !== null && val !== '') {
                    url.searchParams.append(key, val);
                }
            });
        }

        const headers = {
            'Accept': 'application/json',
            ...(options.headers || {})
        };

        if (options.body !== undefined && !(options.body instanceof FormData)) {
            headers['Content-Type'] = 'application/json';
        }

        const token = this.getToken();
        if (token && !options.skipAuth) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const fetchOptions = {
            method: options.method || 'GET',
            headers,
            body: (options.body !== undefined && !(options.body instanceof FormData))
                ? JSON.stringify(options.body)
                : options.body
        };

        let response;
        try {
            response = await fetch(url.toString(), fetchOptions);
        } catch (networkError) {
            console.error('Network connection error:', networkError);
            throw new Error('Unable to connect to the FixMate server. Please check if the backend is running at ' + this.baseUrl);
        }

        // Handle 204 No Content
        if (response.status === 204) {
            return null;
        }

        const contentType = response.headers.get('content-type') || '';
        let data;
        try {
            data = contentType.includes('application/json')
                ? await response.json()
                : await response.text();
        } catch (e) {
            data = null;
        }

        if (!response.ok) {
            // Handle 401 Unauthorized (Expired or invalid token)
            if (response.status === 401) {
                // If it's not a login/register attempt, trigger auth failure event
                if (!path.includes('/auth/login') && !path.includes('/auth/register')) {
                    window.dispatchEvent(new CustomEvent('fixmate:auth-expired'));
                }
            }

            // Extract readable error message
            let errorMessage = `HTTP Error ${response.status}`;
            if (data) {
                if (typeof data === 'string') {
                    errorMessage = data;
                } else if (data.message) {
                    errorMessage = data.message;
                } else if (data.error) {
                    errorMessage = data.error;
                }
                if (data.validationErrors) {
                    const firstVal = Object.values(data.validationErrors)[0];
                    if (firstVal) errorMessage = firstVal;
                }
            }

            const error = new Error(errorMessage);
            error.status = response.status;
            error.data = data;
            throw error;
        }

        return data;
    }

    get(path, params = {}, options = {}) {
        return this.request(path, { ...options, method: 'GET', params });
    }

    post(path, body = {}, options = {}) {
        return this.request(path, { ...options, method: 'POST', body });
    }

    put(path, body = {}, options = {}) {
        return this.request(path, { ...options, method: 'PUT', body });
    }

    patch(path, body = {}, options = {}) {
        return this.request(path, { ...options, method: 'PATCH', body });
    }

    delete(path, options = {}) {
        return this.request(path, { ...options, method: 'DELETE' });
    }
}

// Global API instance
const api = new ApiClient();
