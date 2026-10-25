/**
 * FixMate Customer Module
 * Handles customer dashboard, browsing providers, booking services, viewing history, and cancellations.
 */
const Customer = {
    services: [],
    providers: [],
    bookings: [],
    selectedProviderService: null,

    async init() {
        if (!Auth.isLoggedIn() || Auth.getRole() !== 'CUSTOMER') {
            App.navigateTo('login');
            return;
        }

        await this.loadCustomerData();
        this.renderStats();
        this.renderServicesFilter();
        this.renderProviders();
        this.renderBookings();
        this.populateProfileForm();
    },

    async loadCustomerData() {
        try {
            App.showLoading(true);
            const [services, providers, bookings] = await Promise.all([
                api.get('/services'),
                api.get('/providers'),
                api.get('/bookings/my')
            ]);
            this.services = services || [];
            this.providers = providers || [];
            this.bookings = bookings || [];
        } catch (err) {
            console.error('Error loading customer dashboard:', err);
            App.showToast(err.message, 'error');
        } finally {
            App.showLoading(false);
        }
    },

    renderStats() {
        const total = this.bookings.length;
        const pending = this.bookings.filter(b => b.status === 'PENDING').length;
        const active = this.bookings.filter(b => b.status === 'ACCEPTED').length;
        const completed = this.bookings.filter(b => b.status === 'COMPLETED').length;

        const statTotal = document.getElementById('custStatTotal');
        const statPending = document.getElementById('custStatPending');
        const statActive = document.getElementById('custStatActive');
        const statCompleted = document.getElementById('custStatCompleted');

        if (statTotal) statTotal.textContent = total;
        if (statPending) statPending.textContent = pending;
        if (statActive) statActive.textContent = active;
        if (statCompleted) statCompleted.textContent = completed;

        const user = Auth.getUser();
        const welcomeEl = document.getElementById('customerWelcomeName');
        if (welcomeEl && user) {
            welcomeEl.textContent = user.fullName;
        }
    },

    renderServicesFilter() {
        const select = document.getElementById('custServiceFilter');
        if (!select) return;

        select.innerHTML = '<option value="">All Service Categories</option>' +
            this.services.map(s => `<option value="${s.id}">${s.name}</option>`).join('');
    },

    renderProviders(filteredProviders = null) {
        const container = document.getElementById('custProvidersGrid');
        if (!container) return;

        const list = filteredProviders || this.providers;

        if (list.length === 0) {
            container.innerHTML = `
                <div class="empty-state col-12">
                    <i class="bi bi-person-x empty-icon"></i>
                    <h4>No Service Providers Available</h4>
                    <p class="text-muted">Try clearing filters or check back later.</p>
                </div>
            `;
            return;
        }

        container.innerHTML = list.map(ps => `
            <div class="col-md-6 col-lg-4 mb-4">
                <div class="card h-100 provider-card shadow-sm border-0">
                    <div class="card-body d-flex flex-column">
                        <div class="d-flex align-items-center mb-3">
                            <div class="service-icon-box me-3">
                                <i class="bi ${ps.serviceIcon || 'bi-tools'} fs-3 text-primary"></i>
                            </div>
                            <div>
                                <h5 class="card-title mb-1">${this.escapeHtml(ps.serviceName)}</h5>
                                <span class="badge bg-primary-subtle text-primary border border-primary-subtle">
                                    ${this.escapeHtml(ps.providerName)}
                                </span>
                            </div>
                        </div>
                        <p class="card-text text-muted small mb-3 flex-grow-1">
                            ${this.escapeHtml(ps.serviceDescription || 'Professional home service with verified background check.')}
                        </p>
                        <div class="provider-meta bg-light p-2 rounded mb-3">
                            <div class="d-flex justify-content-between small text-secondary mb-1">
                                <span><i class="bi bi-clock-history me-1"></i> Experience:</span>
                                <strong>${ps.experienceYears} Years</strong>
                            </div>
                            <div class="d-flex justify-content-between small text-secondary mb-1">
                                <span><i class="bi bi-calendar-check me-1"></i> Hours:</span>
                                <strong>${this.escapeHtml(ps.availability)}</strong>
                            </div>
                            <div class="d-flex justify-content-between small text-secondary">
                                <span><i class="bi bi-star-fill text-warning me-1"></i> Rating:</span>
                                <strong>4.9 (Demo Verified)</strong>
                            </div>
                        </div>
                        <div class="d-flex align-items-center justify-content-between mt-auto pt-2 border-top">
                            <div>
                                <span class="small text-muted d-block">Starting from</span>
                                <span class="fs-4 fw-bold text-primary">$${Number(ps.price).toFixed(2)}</span>
                            </div>
                            <button class="btn btn-primary px-3" onclick="Customer.openBookingModal(${ps.id})">
                                <i class="bi bi-calendar-plus me-1"></i> Book Now
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `).join('');
    },

    filterProviders() {
        const serviceId = document.getElementById('custServiceFilter')?.value;
        const search = document.getElementById('custSearchInput')?.value.toLowerCase().trim();

        let filtered = [...this.providers];

        if (serviceId) {
            filtered = filtered.filter(p => String(p.serviceId) === String(serviceId));
        }

        if (search) {
            filtered = filtered.filter(p =>
                (p.serviceName && p.serviceName.toLowerCase().includes(search)) ||
                (p.providerName && p.providerName.toLowerCase().includes(search)) ||
                (p.serviceDescription && p.serviceDescription.toLowerCase().includes(search))
            );
        }

        this.renderProviders(filtered);
    },

    openBookingModal(providerServiceId) {
        const ps = this.providers.find(p => p.id === providerServiceId);
        if (!ps) {
            App.showToast('Selected provider service not found.', 'error');
            return;
        }

        this.selectedProviderService = ps;

        document.getElementById('bookServiceName').textContent = ps.serviceName;
        document.getElementById('bookProviderName').textContent = ps.providerName;
        document.getElementById('bookPricePreview').textContent = `$${Number(ps.price).toFixed(2)}`;
        document.getElementById('bookAvailabilityNote').textContent = `Availability: ${ps.availability}`;

        // Set date minimum to today
        const dateInput = document.getElementById('bookDate');
        const today = new Date().toISOString().split('T')[0];
        dateInput.min = today;
        dateInput.value = today;

        // Auto-fill address from customer profile
        const user = Auth.getUser();
        const addressInput = document.getElementById('bookAddress');
        if (addressInput && user && user.address) {
            addressInput.value = user.address;
        }

        document.getElementById('bookDescription').value = '';
        App.openModal('bookingModal');
    },

    async submitBooking(event) {
        event.preventDefault();
        if (!this.selectedProviderService) return;

        const date = document.getElementById('bookDate').value;
        const time = document.getElementById('bookTime').value;
        const address = document.getElementById('bookAddress').value.trim();
        const description = document.getElementById('bookDescription').value.trim();

        if (!date || !time || !address) {
            App.showToast('Please fill in all required booking fields.', 'warning');
            return;
        }

        const payload = {
            providerServiceId: this.selectedProviderService.id,
            bookingDate: date,
            bookingTime: time.length === 5 ? time + ':00' : time,
            serviceAddress: address,
            description: description
        };

        const submitBtn = document.getElementById('btnConfirmBooking');
        try {
            App.setButtonLoading(submitBtn, true, 'Booking...');
            const newBooking = await api.post('/bookings', payload);
            App.showToast('Booking successfully created! Provider has been notified.', 'success');
            App.closeModal('bookingModal');
            
            // Reload bookings
            this.bookings = await api.get('/bookings/my');
            this.renderStats();
            this.renderBookings();
            
            // Switch to bookings tab
            App.showCustomerTab('bookings');
        } catch (err) {
            App.showToast(err.message, 'error');
        } finally {
            App.setButtonLoading(submitBtn, false, 'Confirm Booking');
        }
    },

    renderBookings(statusFilter = '') {
        const container = document.getElementById('custBookingsList');
        if (!container) return;

        let list = [...this.bookings];
        if (statusFilter) {
            list = list.filter(b => b.status === statusFilter);
        }

        if (list.length === 0) {
            container.innerHTML = `
                <div class="empty-state p-5 text-center bg-white rounded shadow-sm">
                    <i class="bi bi-calendar2-x empty-icon text-muted fs-1 mb-3"></i>
                    <h5>No Bookings Found</h5>
                    <p class="text-muted">You have no ${statusFilter ? statusFilter.toLowerCase() : ''} bookings at the moment.</p>
                    <button class="btn btn-primary btn-sm" onclick="App.showCustomerTab('explore')">Browse Services</button>
                </div>
            `;
            return;
        }

        container.innerHTML = `
            <div class="table-responsive bg-white rounded shadow-sm">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr>
                            <th>ID</th>
                            <th>Service & Provider</th>
                            <th>Date & Time</th>
                            <th>Service Address</th>
                            <th>Price</th>
                            <th>Status</th>
                            <th class="text-end">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${list.map(b => `
                            <tr>
                                <td class="fw-bold">#${b.id}</td>
                                <td>
                                    <div class="d-flex align-items-center">
                                        <div class="service-icon-box-sm me-2">
                                            <i class="bi ${b.serviceIcon || 'bi-tools'} text-primary"></i>
                                        </div>
                                        <div>
                                            <div class="fw-semibold">${this.escapeHtml(b.serviceName || 'Home Service')}</div>
                                            <div class="small text-muted">${this.escapeHtml(b.providerName || 'Provider')} (${b.providerPhone || ''})</div>
                                        </div>
                                    </div>
                                </td>
                                <td>
                                    <div><i class="bi bi-calendar3 me-1 text-muted"></i> ${b.bookingDate}</div>
                                    <div class="small text-muted"><i class="bi bi-clock me-1 text-muted"></i> ${b.bookingTime}</div>
                                </td>
                                <td class="text-truncate" style="max-width: 200px;" title="${this.escapeHtml(b.serviceAddress)}">
                                    ${this.escapeHtml(b.serviceAddress)}
                                    ${b.description ? `<div class="small text-muted text-truncate">${this.escapeHtml(b.description)}</div>` : ''}
                                </td>
                                <td class="fw-bold text-primary">$${Number(b.price).toFixed(2)}</td>
                                <td>${this.getStatusBadge(b.status)}</td>
                                <td class="text-end">
                                    ${(b.status === 'PENDING' || b.status === 'ACCEPTED') ? `
                                        <button class="btn btn-outline-danger btn-sm" onclick="Customer.confirmCancelBooking(${b.id})">
                                            <i class="bi bi-x-circle me-1"></i> Cancel
                                        </button>
                                    ` : `
                                        <span class="text-muted small">No action</span>
                                    `}
                                </td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        `;
    },

    confirmCancelBooking(bookingId) {
        if (confirm('Are you sure you want to cancel this booking? This action cannot be undone.')) {
            this.cancelBooking(bookingId);
        }
    },

    async cancelBooking(bookingId) {
        try {
            App.showLoading(true);
            await api.patch(`/bookings/${bookingId}/cancel`);
            App.showToast('Booking cancelled successfully.', 'info');
            this.bookings = await api.get('/bookings/my');
            this.renderStats();
            this.renderBookings();
        } catch (err) {
            App.showToast(err.message, 'error');
        } finally {
            App.showLoading(false);
        }
    },

    populateProfileForm() {
        const user = Auth.getUser();
        if (!user) return;

        const nameInput = document.getElementById('custProfileName');
        const phoneInput = document.getElementById('custProfilePhone');
        const addressInput = document.getElementById('custProfileAddress');
        const emailInput = document.getElementById('custProfileEmail');

        if (nameInput) nameInput.value = user.fullName || '';
        if (phoneInput) phoneInput.value = user.phone || '';
        if (addressInput) addressInput.value = user.address || '';
        if (emailInput) emailInput.value = user.email || '';
    },

    async updateProfile(event) {
        event.preventDefault();
        const fullName = document.getElementById('custProfileName').value.trim();
        const phone = document.getElementById('custProfilePhone').value.trim();
        const address = document.getElementById('custProfileAddress').value.trim();

        if (!fullName || !phone || !address) {
            App.showToast('All profile fields are required.', 'warning');
            return;
        }

        const btn = document.getElementById('btnCustSaveProfile');
        try {
            App.setButtonLoading(btn, true, 'Saving...');
            await Auth.updateProfile({ fullName, phone, address });
            App.showToast('Profile updated successfully!', 'success');
            this.renderStats();
        } catch (err) {
            App.showToast(err.message, 'error');
        } finally {
            App.setButtonLoading(btn, false, 'Save Changes');
        }
    },

    async changePassword(event) {
        event.preventDefault();
        const currentPassword = document.getElementById('custOldPassword').value;
        const newPassword = document.getElementById('custNewPassword').value;
        const confirmPassword = document.getElementById('custConfirmPassword').value;

        if (newPassword !== confirmPassword) {
            App.showToast('New passwords do not match.', 'warning');
            return;
        }

        if (newPassword.length < 6) {
            App.showToast('New password must be at least 6 characters.', 'warning');
            return;
        }

        const btn = document.getElementById('btnCustChangePass');
        try {
            App.setButtonLoading(btn, true, 'Updating...');
            await Auth.changePassword(currentPassword, newPassword);
            App.showToast('Password changed successfully!', 'success');
            document.getElementById('custChangePassForm').reset();
        } catch (err) {
            App.showToast(err.message, 'error');
        } finally {
            App.setButtonLoading(btn, false, 'Update Password');
        }
    },

    getStatusBadge(status) {
        const badges = {
            PENDING: '<span class="badge bg-warning text-dark"><i class="bi bi-hourglass-split me-1"></i> Pending</span>',
            ACCEPTED: '<span class="badge bg-primary"><i class="bi bi-check-circle me-1"></i> Accepted</span>',
            COMPLETED: '<span class="badge bg-success"><i class="bi bi-patch-check me-1"></i> Completed</span>',
            CANCELLED: '<span class="badge bg-secondary"><i class="bi bi-slash-circle me-1"></i> Cancelled</span>',
            REJECTED: '<span class="badge bg-danger"><i class="bi bi-x-circle me-1"></i> Rejected</span>'
        };
        return badges[status] || `<span class="badge bg-secondary">${status}</span>`;
    },

    escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
};
