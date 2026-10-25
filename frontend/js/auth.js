/**
 * FixMate Authentication Service
 * Manages user authentication state, registration, login, logout, and profile sync.
 */
const Auth = {
    getUser() {
        const userStr = localStorage.getItem(CONFIG.USER_KEY);
        try {
            return userStr ? JSON.parse(userStr) : null;
        } catch (e) {
            return null;
        }
    },

    setUser(user) {
        if (user) {
            localStorage.setItem(CONFIG.USER_KEY, JSON.stringify(user));
        } else {
            localStorage.removeItem(CONFIG.USER_KEY);
        }
        window.dispatchEvent(new CustomEvent('fixmate:auth-changed', { detail: { user } }));
    },

    isLoggedIn() {
        return !!api.getToken() && !!this.getUser();
    },

    getRole() {
        const user = this.getUser();
        return user ? user.role : null;
    },

    async login(email, password) {
        const response = await api.post('/auth/login', { email, password }, { skipAuth: true });
        if (response && response.token) {
            api.setToken(response.token);
            this.setUser(response.user);
            return response.user;
        }
        throw new Error('Invalid response received from server.');
    },

    async register(userData) {
        const response = await api.post('/auth/register', userData, { skipAuth: true });
        if (response && response.token) {
            api.setToken(response.token);
            this.setUser(response.user);
            return response.user;
        }
        throw new Error('Registration failed.');
    },

    async refreshProfile() {
        try {
            const user = await api.get('/auth/me');
            if (user) {
                this.setUser(user);
                return user;
            }
        } catch (err) {
            console.warn('Failed to refresh user profile:', err.message);
        }
        return null;
    },

    async updateProfile(profileData) {
        const updated = await api.put('/auth/profile', profileData);
        if (updated) {
            this.setUser(updated);
            return updated;
        }
        return null;
    },

    async changePassword(oldPassword, newPassword) {
        return await api.put('/auth/password', {
            currentPassword: oldPassword,
            newPassword: newPassword
        });
    },

    logout() {
        try {
            api.post('/auth/logout').catch(() => {});
        } catch (e) {}
        api.setToken(null);
        this.setUser(null);
    }
};

// Listen for global session expiry
window.addEventListener('fixmate:auth-expired', () => {
    Auth.logout();
    if (typeof App !== 'undefined' && App.showToast) {
        App.showToast('Your session has expired. Please sign in again.', 'warning');
        App.navigateTo('login');
    }
});
