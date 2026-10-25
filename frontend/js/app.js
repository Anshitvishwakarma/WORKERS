
/**
 * FixMate Main Application Controller
 * Handles view routing, navigation bar state, theme toggling,
 * toasts, and UI interactions.
 */

const App = {
    currentView: 'home',

    init() {
        this.initTheme();
        this.setupEventListeners();
        this.updateNav();

        // Restore route from hash if available
        const hash = window.location.hash.replace('#', '');

        if (hash) {
            this.navigateTo(hash);
        } else {
            this.navigateTo('home');
        }

        // Load public services
        this.loadPublicServices();
    },

    setupEventListeners() {
        // Auth state changes
        window.addEventListener('fixmate:auth-changed', () => {
            this.updateNav();
        });

        // Hash change navigation
        window.addEventListener('hashchange', () => {
            const hash = window.location.hash.replace('#', '');

            if (hash && hash !== this.currentView) {
                this.navigateTo(hash, false);
            }
        });

        // Dark mode toggle
        document.getElementById('themeToggleBtn')
            ?.addEventListener('click', () => {
                this.toggleTheme();
            });

        // Mobile navbar collapse
        document.querySelectorAll('.navbar-nav .nav-link')
            .forEach(link => {
                link.addEventListener('click', () => {
                    const navCollapse =
                        document.getElementById('navbarNav');

                    if (
                        navCollapse &&
                        navCollapse.classList.contains('show') &&
                        typeof bootstrap !== 'undefined'
                    ) {
                        const bsCollapse =
                            bootstrap.Collapse.getInstance(navCollapse);

                        if (bsCollapse) {
                            bsCollapse.hide();
                        }
                    }
                });
            });
    },

    navigateTo(viewId, updateHash = true) {
        const allowedViews = [
            'home',
            'login',
            'register',
            'customer',
            'provider',
            'admin'
        ];

        if (!allowedViews.includes(viewId)) {
            viewId = 'home';
        }

        // Customer route protection
        if (viewId === 'customer') {
            if (!Auth.isLoggedIn()) {
                this.showToast(
                    'Please sign in to access your customer dashboard.',
                    'warning'
                );
                return this.navigateTo('login');
            }

            if (Auth.getRole() !== 'CUSTOMER') {
                this.showToast(
                    'Access restricted: Customer account required.',
                    'error'
                );

                const role = Auth.getRole();

                if (role) {
                    return this.navigateTo(role.toLowerCase());
                }

                return this.navigateTo('login');
            }
        }

        // Provider route protection
        if (viewId === 'provider') {
            if (!Auth.isLoggedIn()) {
                this.showToast(
                    'Please sign in to access your provider dashboard.',
                    'warning'
                );
                return this.navigateTo('login');
            }

            if (Auth.getRole() !== 'PROVIDER') {
                this.showToast(
                    'Access restricted: Provider account required.',
                    'error'
                );

                const role = Auth.getRole();

                if (role) {
                    return this.navigateTo(role.toLowerCase());
                }

                return this.navigateTo('login');
            }
        }

        // Admin route protection
        if (viewId === 'admin') {
            if (!Auth.isLoggedIn()) {
                this.showToast(
                    'Administrator login required.',
                    'warning'
                );
                return this.navigateTo('login');
            }

            if (Auth.getRole() !== 'ADMIN') {
                this.showToast(
                    'Access denied: Administrator privileges required.',
                    'error'
                );

                const role = Auth.getRole();

                if (role) {
                    return this.navigateTo(role.toLowerCase());
                }

                return this.navigateTo('login');
            }
        }

        // Redirect logged-in users from login/register
        if (
            (viewId === 'login' || viewId === 'register') &&
            Auth.isLoggedIn()
        ) {
            const role = Auth.getRole();

            if (role) {
                return this.navigateTo(role.toLowerCase());
            }
        }

        // Hide all views
        document.querySelectorAll('.view-section').forEach(section => {
            section.style.display = 'none';
        });

        // Show target view
        const targetSection =
            document.getElementById(`view-${viewId}`);

        if (!targetSection) {
            console.error(`View not found: view-${viewId}`);
            return;
        }

        targetSection.style.display = 'block';
        this.currentView = viewId;

        if (updateHash) {
            window.location.hash = viewId;
        }

        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });

        // Initialize module controllers
        if (viewId === 'customer') {
            Customer.init();
        }

        if (viewId === 'provider') {
            Provider.init();
        }

        if (viewId === 'admin') {
            Admin.init();
        }
    },

    updateNav() {
        const isLoggedIn = Auth.isLoggedIn();
        const role = Auth.getRole();
        const user = Auth.getUser();

        const authLinks =
            document.getElementById('navAuthLinks');

        const userMenu =
            document.getElementById('navUserMenu');

        const navDashboardLink =
            document.getElementById('navDashboardLink');

        const userNameDisplay =
            document.getElementById('navUserName');

        if (isLoggedIn && user) {
            if (authLinks) {
                authLinks.style.display = 'none';
            }

            if (userMenu) {
                userMenu.style.display = 'flex';
            }

            if (userNameDisplay) {
                userNameDisplay.textContent =
                    (user.fullName || user.name || 'User')
                        .split(' ')[0];
            }

            if (navDashboardLink && role) {
                navDashboardLink.style.display = 'block';

                navDashboardLink.setAttribute(
                    'onclick',
                    `App.navigateTo('${role.toLowerCase()}')`
                );

                navDashboardLink.textContent =
                    `${role.charAt(0) +
                    role.slice(1).toLowerCase()} Dashboard`;
            }
        } else {
            if (authLinks) {
                authLinks.style.display = 'flex';
            }

            if (userMenu) {
                userMenu.style.display = 'none';
            }

            if (navDashboardLink) {
                navDashboardLink.style.display = 'none';
            }
        }
    },

    // Load public services on home page

    async loadPublicServices() {
        try {
            const services = await api.get('/services');
            const grid = document.getElementById('publicServicesGrid');

            if (!grid || !Array.isArray(services)) {
                console.error('Services API did not return a valid list.');
                return;
            }

            const getServiceImage = (name) => {
                const service = (name || '').toLowerCase().trim();

                if (service.includes('plumb')) {
                    return 'https://images.unsplash.com/photo-1607472586893-edb57bdc0e39?auto=format&fit=crop&w=800&q=80';
                }

                if (service.includes('electric')) {
                    return 'https://images.unsplash.com/photo-1621905251189-08b45d6a269e?auto=format&fit=crop&w=800&q=80';
                }

                if (service.includes('clean')) {
                    return 'https://images.unsplash.com/photo-1581578731548-c64695cc6952?auto=format&fit=crop&w=800&q=80';
                }

                if (
                    service === 'ac' ||
                    service.startsWith('ac ') ||
                    service.includes('air condition') ||
                    service.includes('air conditioner')
                ) {
                    return 'https://images.unsplash.com/photo-1631545806609-3e7f2d7d4a68?auto=format&fit=crop&w=800&q=80';
                }

                if (service.includes('carpent')) {
                    return 'https://images.unsplash.com/photo-1504148455328-c376907d081c?auto=format&fit=crop&w=800&q=80';
                }

                if (service.includes('paint')) {
                    return 'https://images.unsplash.com/photo-1562259949-e8e7689d7828?auto=format&fit=crop&w=800&q=80';
                }

                if (
                    service.includes('appliance') ||
                    service.includes('washing machine') ||
                    service.includes('refrigerator') ||
                    service.includes('microwave') ||
                    service.includes('dishwasher')
                ) {
                    return 'https://images.unsplash.com/photo-1626806787461-102c1bfaaea1?auto=format&fit=crop&w=800&q=80';
                }

                if (
                    service.includes('ro ') ||
                    service.includes('water purifier') ||
                    service.includes('purifier')
                ) {
                    return 'https://images.unsplash.com/photo-1523217582562-09d0def993a6?auto=format&fit=crop&w=800&q=80';
                }

                return 'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=800&q=80';
            };

            grid.innerHTML = services.slice(0, 8).map(s => `
                <div class="col-sm-6 col-lg-3">
                    <div class="card h-100 service-item-card shadow-sm border-0">
                        <div class="service-photo-wrapper">
                            <img
                                class="service-photo"
                                src="${getServiceImage(s.name)}"
                                alt="${Customer.escapeHtml(s.name || 'Home service')}"
                                loading="lazy"
                                onerror="this.onerror=null; this.src='https://placehold.co/800x450?text=Service';"
                            >
                        </div>

                        <div class="service-card-content text-center p-3">
                            <div class="service-icon-box mx-auto mb-3">
                                <i class="bi ${s.icon || 'bi-tools'} fs-2 text-primary"></i>
                            </div>

                            <h5 class="card-title fw-bold">
                                ${Customer.escapeHtml(s.name || '')}
                            </h5>

                            <p class="card-text text-muted small flex-grow-1">
                                ${Customer.escapeHtml(s.description || '')}
                            </p>

                            <div class="mt-3">
                                <button
                                    class="btn btn-outline-primary btn-sm rounded-pill px-3"
                                    onclick="App.handleServiceBookClick(${JSON.stringify(s.id)})">
                                    Find Providers
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            `).join('');

        } catch (err) {
            console.error('Error loading public services:', err);
        }
    },

    async handleHeroSearch() {
        const locationInput = document.getElementById('heroLocation');
        const serviceInput = document.getElementById('heroQuickSearch');

        const location = locationInput?.value.trim() || '';
        const serviceName = serviceInput?.value.trim().toLowerCase() || '';

        if (!location || !serviceName) {
            this.showToast('Please enter your location and service name.', 'warning');
            return;
        }

        try {
            const services = await api.get('/services');

            if (!Array.isArray(services)) {
                this.showToast('Unable to load services.', 'error');
                return;
            }

            const matchedService = services.find(s => {
                const name = (s.name || '').toLowerCase().trim();

                return name === serviceName ||
                    name.includes(serviceName) ||
                    serviceName.includes(name);
            });

            if (!matchedService) {
                this.showToast('Service not found. Please check the service name.', 'warning');
                return;
            }

            localStorage.setItem('fixmate_search_location', location);
            localStorage.setItem('fixmate_search_service', matchedService.name);
            localStorage.setItem(
                'fixmate_search_service_id',
                String(matchedService.id)
            );

            this.handleServiceBookClick(matchedService.id);

        } catch (error) {
            console.error('Service search failed:', error);
            this.showToast('Unable to search services. Please try again.', 'error');
        }
    },


    // Search services using the home page search bar
    async handleHeroSearch() {
        const locationInput =
            document.getElementById('heroLocation');

        const serviceInput =
            document.getElementById('heroQuickSearch');

        const location = locationInput?.value.trim();

        const serviceName =
            serviceInput?.value.trim().toLowerCase();

        if (!location || !serviceName) {
            this.showToast(
                'Please enter your location and service name.',
                'warning'
            );
            return;
        }

        try {
            const services = await api.get('/services');

            if (!Array.isArray(services)) {
                this.showToast(
                    'Unable to read services from the server.',
                    'error'
                );
                return;
            }

            const matchedService = services.find(s => {
                const name = (s.name || '').toLowerCase();

                return (
                    name.includes(serviceName) ||
                    serviceName.includes(name)
                );
            });

            if (!matchedService) {
                this.showToast(
                    'Service not found. Please enter the correct service name.',
                    'warning'
                );
                return;
            }

            // Save search information
            localStorage.setItem(
                'fixmate_search_location',
                location
            );

            localStorage.setItem(
                'fixmate_search_service',
                matchedService.name || ''
            );

            localStorage.setItem(
                'fixmate_search_service_id',
                String(matchedService.id)
            );

            // Continue to the existing provider-search flow
            this.handleServiceBookClick(matchedService.id);

        } catch (error) {
            console.error('Service search failed:', error);

            this.showToast(
                'Unable to load services. Please try again.',
                'error'
            );
        }
    },

    // Book a service / find providers
    handleServiceBookClick(serviceId) {
        if (!Auth.isLoggedIn()) {
            this.showToast(
                'Please sign in as a customer to book services.',
                'info'
            );

            this.navigateTo('login');
            return;
        }

        if (Auth.getRole() !== 'CUSTOMER') {
            this.showToast(
                'Please sign in with a Customer account to book.',
                'warning'
            );
            return;
        }

        this.navigateTo('customer');

        setTimeout(() => {
            const filter =
                document.getElementById('custServiceFilter');

            if (filter) {
                filter.value = serviceId;
                Customer.filterProviders();
            }
        }, 300);
    },

    // Customer dashboard tabs
    showCustomerTab(tabId) {
        document.querySelectorAll('.cust-tab-content')
            .forEach(c => {
                c.style.display = 'none';
            });

        document.querySelectorAll('.cust-nav-btn')
            .forEach(b => {
                b.classList.remove('active');
            });

        const target =
            document.getElementById(`cust-tab-${tabId}`);

        const btn =
            document.getElementById(`btn-cust-tab-${tabId}`);

        if (target) {
            target.style.display = 'block';
        }

        if (btn) {
            btn.classList.add('active');
        }
    },

    // Provider dashboard tabs
    showProviderTab(tabId) {
        document.querySelectorAll('.prov-tab-content')
            .forEach(c => {
                c.style.display = 'none';
            });

        document.querySelectorAll('.prov-nav-btn')
            .forEach(b => {
                b.classList.remove('active');
            });

        const target =
            document.getElementById(`prov-tab-${tabId}`);

        const btn =
            document.getElementById(`btn-prov-tab-${tabId}`);

        if (target) {
            target.style.display = 'block';
        }

        if (btn) {
            btn.classList.add('active');
        }
    },

    // Admin dashboard tabs
    showAdminTab(tabId) {
        document.querySelectorAll('.admin-tab-content')
            .forEach(c => {
                c.style.display = 'none';
            });

        document.querySelectorAll('.admin-nav-btn')
            .forEach(b => {
                b.classList.remove('active');
            });

        const target =
            document.getElementById(`admin-tab-${tabId}`);

        const btn =
            document.getElementById(`btn-admin-tab-${tabId}`);

        if (target) {
            target.style.display = 'block';
        }

        if (btn) {
            btn.classList.add('active');
        }
    },

    // Demo account fillers
    fillDemoLogin(role) {
        const emailInput =
            document.getElementById('loginEmail');

        const passInput =
            document.getElementById('loginPassword');

        if (!emailInput || !passInput) {
            this.showToast(
                'Login form fields were not found.',
                'error'
            );
            return;
        }

        if (role === 'ADMIN') {
            emailInput.value = 'admin@fixmate.com';
            passInput.value = 'Admin@123456';
        } else if (role === 'CUSTOMER') {
            emailInput.value = 'customer@fixmate.com';
            passInput.value = 'Customer@123';
        } else if (role === 'PROVIDER') {
            emailInput.value = 'plumber@fixmate.com';
            passInput.value = 'Provider@123';
        }

        this.showToast(
            `Filled ${role} demo credentials. Click 'Sign In' to proceed.`,
            'info'
        );
    },

    // Login
    async handleLogin(event) {
        event.preventDefault();

        const email =
            document.getElementById('loginEmail')?.value.trim();

        const password =
            document.getElementById('loginPassword')?.value;

        if (!email || !password) {
            this.showToast(
                'Please enter both email and password.',
                'warning'
            );
            return;
        }

        const btn =
            document.getElementById('btnLoginSubmit');

        try {
            this.setButtonLoading(btn, true, 'Signing in...');

            const user = await Auth.login(email, password);

            this.showToast(
                `Welcome back, ${user.fullName || user.name || 'User'}!`,
                'success'
            );

            document.getElementById('loginForm')?.reset();

            const role = user.role || Auth.getRole();

            if (role) {
                this.navigateTo(role.toLowerCase());
            } else {
                this.navigateTo('home');
            }

        } catch (err) {
            this.showToast(
                err.message || 'Login failed. Please try again.',
                'error'
            );
        } finally {
            this.setButtonLoading(btn, false, 'Sign In');
        }
    },

    // Register
    async handleRegister(event) {
        event.preventDefault();

        const fullName =
            document.getElementById('regFullName')?.value.trim();

        const email =
            document.getElementById('regEmail')?.value.trim();

        const phone =
            document.getElementById('regPhone')?.value.trim();

        const address =
            document.getElementById('regAddress')?.value.trim();

        const password =
            document.getElementById('regPassword')?.value;

        const confirmPass =
            document.getElementById('regConfirmPassword')?.value;

        const role =
            document.querySelector('input[name="regRole"]:checked')
                ?.value || 'CUSTOMER';

        if (
            !fullName ||
            !email ||
            !phone ||
            !address ||
            !password ||
            !confirmPass
        ) {
            this.showToast(
                'Please fill in all required fields.',
                'warning'
            );
            return;
        }

        if (password !== confirmPass) {
            this.showToast(
                'Passwords do not match.',
                'warning'
            );
            return;
        }

        if (password.length < 6) {
            this.showToast(
                'Password must be at least 6 characters.',
                'warning'
            );
            return;
        }

        const payload = {
            fullName,
            email,
            phone,
            address,
            password,
            role
        };

        const btn =
            document.getElementById('btnRegisterSubmit');

        try {
            this.setButtonLoading(
                btn,
                true,
                'Creating Account...'
            );

            const user = await Auth.register(payload);

            this.showToast(
                `Account created successfully! Welcome to FixMate, ${user.fullName || user.name || 'User'}.`,
                'success'
            );

            document.getElementById('registerForm')?.reset();

            const userRole = user.role || Auth.getRole();

            if (userRole) {
                this.navigateTo(userRole.toLowerCase());
            } else {
                this.navigateTo('login');
            }

        } catch (err) {
            this.showToast(
                err.message || 'Registration failed. Please try again.',
                'error'
            );
        } finally {
            this.setButtonLoading(
                btn,
                false,
                'Register Account'
            );
        }
    },

    // Logout
    handleLogout() {
        Auth.logout();

        this.showToast(
            'You have been signed out successfully.',
            'info'
        );

        this.navigateTo('home');
        this.updateNav();
    },

    // Theme management
    initTheme() {
        const savedTheme =
            localStorage.getItem(CONFIG.THEME_KEY) || 'light';

        this.setTheme(savedTheme);
    },

    toggleTheme() {
        const current =
            document.documentElement.getAttribute('data-bs-theme') ||
            'light';

        const next = current === 'dark' ? 'light' : 'dark';

        this.setTheme(next);
    },

    setTheme(theme) {
        document.documentElement.setAttribute(
            'data-bs-theme',
            theme
        );

        localStorage.setItem(CONFIG.THEME_KEY, theme);

        const icon =
            document.getElementById('themeToggleIcon');

        if (icon) {
            icon.className =
                theme === 'dark'
                    ? 'bi bi-sun-fill text-warning'
                    : 'bi bi-moon-stars-fill text-secondary';
        }
    },

    // UI feedback: Toast
    showToast(message, type = 'info') {
        const container =
            document.getElementById('toastContainer');

        if (!container) {
            console.log(`[${type}] ${message}`);
            return;
        }

        const icons = {
            success: 'bi-check-circle-fill text-success',
            error: 'bi-exclamation-triangle-fill text-danger',
            warning: 'bi-exclamation-circle-fill text-warning',
            info: 'bi-info-circle-fill text-primary'
        };

        const toastId = 'toast_' + Date.now();

        const toastEl = document.createElement('div');

        toastEl.className =
            'toast align-items-center border-0 shadow-lg mb-2 show';

        toastEl.id = toastId;
        toastEl.setAttribute('role', 'alert');
        toastEl.setAttribute('aria-live', 'assertive');
        toastEl.setAttribute('aria-atomic', 'true');

        // Use textContent for the message to avoid inserting raw HTML
        const row = document.createElement('div');
        row.className = 'd-flex';

        const body = document.createElement('div');
        body.className = 'toast-body d-flex align-items-center';

        const icon = document.createElement('i');
        icon.className =
            `bi ${icons[type] || icons.info} fs-5 me-2`;

        const messageElement = document.createElement('div');
        messageElement.textContent = String(message);

        const closeButton = document.createElement('button');
        closeButton.type = 'button';
        closeButton.className = 'btn-close me-2 m-auto';
        closeButton.setAttribute('aria-label', 'Close');

        closeButton.addEventListener('click', () => {
            toastEl.remove();
        });

        body.appendChild(icon);
        body.appendChild(messageElement);

        row.appendChild(body);
        row.appendChild(closeButton);

        toastEl.appendChild(row);
        container.appendChild(toastEl);

        setTimeout(() => {
            toastEl.remove();
        }, 5000);
    },

    // Global loading spinner
    showLoading(show = true) {
        const spinner =
            document.getElementById('globalLoadingSpinner');

        if (spinner) {
            spinner.style.display = show ? 'flex' : 'none';
        }
    },

    // Button loading state
    setButtonLoading(btn, isLoading, text) {
        if (!btn) return;

        if (isLoading) {
            btn.dataset.origText = btn.innerHTML;
            btn.disabled = true;

            btn.innerHTML = `
                <span
                    class="spinner-border spinner-border-sm me-2"
                    role="status"
                    aria-hidden="true">
                </span>
                ${text || 'Loading...'}
            `;
        } else {
            btn.disabled = false;

            btn.innerHTML =
                text || btn.dataset.origText || 'Submit';
        }
    },

    // Open modal
    openModal(modalId) {
        const modalEl =
            document.getElementById(modalId);

        if (
            modalEl &&
            typeof bootstrap !== 'undefined'
        ) {
            const modal =
                bootstrap.Modal.getOrCreateInstance(modalEl);

            modal.show();
        }
    },

    // Close modal
    closeModal(modalId) {
        const modalEl =
            document.getElementById(modalId);

        if (
            modalEl &&
            typeof bootstrap !== 'undefined'
        ) {
            const modal =
                bootstrap.Modal.getInstance(modalEl);

            if (modal) {
                modal.hide();
            }
        }
    }
};

// Initialize App once DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    App.init();
});
