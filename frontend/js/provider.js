/**
 * FixMate Provider Module
 * Handles provider dashboard, service offerings management, and booking workflow (accept, reject, complete).
 */
const Provider = {
    myServices: [],
    catalogServices: [],
    bookings: [],
    editingServiceId: null,

    async init() {
        if (!Auth.isLoggedIn() || Auth.getRole() !== 'PROVIDER') {
            App.navigateTo('login');
            return;
        }

        await this.loadProviderData();
        this.renderStats();
        this.renderRequests();
        this.renderActiveJobs();
        this.renderMyServices();
        this.renderHistory();
        this.populateProfileForm();
    },

    async loadProviderData() {
        try {
            App.showLoading(true);
            const [myServices, catalogServices, bookings] = await Promise.all([
                api.get('/providers/me/services'),
                api.get('/services'),
                api.get('/provider/bookings')
            ]);
            this.myServices = myServices || [];
            this.catalogServices = catalogServices || [];
            this.bookings = bookings || [];
        } catch (err) {
            console.error('Error loading provider data:', err);
            App.showToast(err.message, 'error');
        } finally {
            App.showLoading(false);
        }
    },

    renderStats() {
        const pending = this.bookings.filter(b => b.status === 'PENDING').length;
        const accepted = this.bookings.filter(b => b.status === 'ACCEPTED').length;
        const completed = this.bookings.filter(b => b.status === 'COMPLETED');
        const completedCount = completed.length;
        const totalEarnings = completed.reduce((sum, b) => sum + Number(b.price || 0), 0);

        const elPending = document.getElementById('provStatPending');
        const elActive = document.getElementById('provStatActive');
        const elCompleted = document.getElementById('provStatCompleted');
        const elEarnings = document.getElementById('provStatEarnings');

        if (elPending) elPending.textContent = pending;
        if (elActive) elActive.textContent = accepted;
        if (elCompleted) elCompleted.textContent = completedCount;
        if (elEarnings) elEarnings.textContent = `$${totalEarnings.toFixed(2)}`;

        const user = Auth.getUser();
        const welcomeEl = document.getElementById('providerWelcomeName');
        if (welcomeEl && user) {
            welcomeEl.textContent = user.fullName;
        }
    },

    renderRequests() {
        const container = document.getElementById('provRequestsList');
        if (!container) return;

        const pending = this.bookings.filter(b => b.status === 'PENDING');

        if (pending.length === 0) {
            container.innerHTML = `
                <div class="empty-state p-4 text-center bg-white rounded shadow-sm">
                    <i class="bi bi-inbox text-muted fs-2 mb-2"></i>
                    <h6>No Pending Requests</h6>
                    <p class="text-muted small mb-0">You have responded to all incoming customer requests.</p>
                </div>
            `;
            return;
        }

        container.innerHTML = `
            <div class="table-responsive bg-white rounded shadow-sm">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr>
                            <th>Booking #</th>
                            <th>Customer & Address</th>
                            <th>Service Requested</th>
                            <th>Scheduled Date</th>
                            <th>Earnings</th>
                            <th class="text-end">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${pending.map(b => `
                            <tr>
                                <td class="fw-bold">#${b.id}</td>
                                <td>
                                    <div class="fw-semibold">${this.escapeHtml(b.customerName || 'Customer')}</div>
                                    <div class="small text-muted"><i class="bi bi-geo-alt me-1"></i> ${this.escapeHtml(b.serviceAddress)}</div>
                                    <div class="small text-muted"><i class="bi bi-telephone me-1"></i> ${this.escapeHtml(b.customerPhone || 'N/A')}</div>
                                    ${b.description ? `<div class="small text-secondary mt-1 fst-italic">"${this.escapeHtml(b.description)}"</div>` : ''}
                                </td>
                                <td>
                                    <span class="badge bg-primary-subtle text-primary border border-primary-subtle">
                                        <i class="bi ${b.serviceIcon || 'bi-tools'} me-1"></i> ${this.escapeHtml(b.serviceName || '')}
                                    </span>
                                </td>
                                <td>
                                    <div><i class="bi bi-calendar3 me-1 text-muted"></i> ${b.bookingDate}</div>
                                    <div class="small text-muted"><i class="bi bi-clock me-1 text-muted"></i> ${b.bookingTime}</div>
                                </td>
                                <td class="fw-bold text-success">$${Number(b.price).toFixed(2)}</td>
                                <td class="text-end">
                                    <button class="btn btn-success btn-sm me-1" onclick="Provider.acceptBooking(${b.id})">
                                        <i class="bi bi-check-lg me-1"></i> Accept
                                    </button>
                                    <button class="btn btn-outline-danger btn-sm" onclick="Provider.rejectBooking(${b.id})">
                                        <i class="bi bi-x-lg me-1"></i> Reject
                                    </button>
                                </td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        `;
    },

    renderActiveJobs() {
        const container = document.getElementById('provActiveJobsList');
        if (!container) return;

        const active = this.bookings.filter(b => b.status === 'ACCEPTED');

        if (active.length === 0) {
            container.innerHTML = `
                <div class="empty-state p-4 text-center bg-white rounded shadow-sm">
                    <i class="bi bi-check2-all text-muted fs-2 mb-2"></i>
                    <h6>No Active Jobs Right Now</h6>
                    <p class="text-muted small mb-0">Accept incoming requests to schedule jobs.</p>
                </div>
            `;
            return;
        }

        container.innerHTML = `
            <div class="table-responsive bg-white rounded shadow-sm">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr>
                            <th>Job #</th>
                            <th>Customer Contact</th>
                            <th>Service & Notes</th>
                            <th>Date & Time</th>
                            <th>Payment</th>
                            <th class="text-end">Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${active.map(b => `
                            <tr>
                                <td class="fw-bold">#${b.id}</td>
                                <td>
                                    <div class="fw-semibold">${this.escapeHtml(b.customerName || 'Customer')}</div>
                                    <div class="small text-muted"><i class="bi bi-geo-alt me-1"></i> ${this.escapeHtml(b.serviceAddress)}</div>
                                    <div class="small text-muted"><i class="bi bi-telephone me-1"></i> ${this.escapeHtml(b.customerPhone || 'N/A')}</div>
                                </td>
                                <td>
                                    <div class="fw-semibold">${this.escapeHtml(b.serviceName || 'Home Service')}</div>
                                    ${b.description ? `<div class="small text-muted text-truncate" style="max-width: 200px;">${this.escapeHtml(b.description)}</div>` : ''}
                                </td>
                                <td>
                                    <div><i class="bi bi-calendar-event me-1 text-primary"></i> ${b.bookingDate}</div>
                                    <div class="small text-muted">${b.bookingTime}</div>
                                </td>
                                <td class="fw-bold text-success">$${Number(b.price).toFixed(2)}</td>
                                <td class="text-end">
                                    <button class="btn btn-primary btn-sm" onclick="Provider.completeBooking(${b.id})">
                                        <i class="bi bi-patch-check me-1"></i> Complete Job
                                    </button>
                                </td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        `;
    },

    async acceptBooking(bookingId) {
        try {
            App.showLoading(true);
            await api.patch(`/provider/bookings/${bookingId}/accept`);
            App.showToast('Booking accepted! It has been moved to Active Jobs.', 'success');
            await this.refreshBookings();
        } catch (err) {
            App.showToast(err.message, 'error');
        } finally {
            App.showLoading(false);
        }
    },

    async rejectBooking(bookingId) {
        if (!confirm('Are you sure you want to decline this booking request?')) return;
        try {
            App.showLoading(true);
            await api.patch(`/provider/bookings/${bookingId}/reject`);
            App.showToast('Booking request declined.', 'info');
            await this.refreshBookings();
        } catch (err) {
            App.showToast(err.message, 'error');
        } finally {
            App.showLoading(false);
        }
    },

    async completeBooking(bookingId) {
        if (!confirm('Confirm that this service has been completed and payment settled?')) return;
        try {
            App.showLoading(true);
            await api.patch(`/provider/bookings/${bookingId}/complete`);
            App.showToast('Job marked as COMPLETED! Great work.', 'success');
            await this.refreshBookings();
        } catch (err) {
            App.showToast(err.message, 'error');
        } finally {
            App.showLoading(false);
        }
    },

    async refreshBookings() {
        this.bookings = await api.get('/provider/bookings');
        this.renderStats();
        this.renderRequests();
        this.renderActiveJobs();
        this.renderHistory();
    },

    renderMyServices() {
        const container = document.getElementById('provServicesList');
        if (!container) return;

        if (this.myServices.length === 0) {
            container.innerHTML = `
                <div class="empty-state p-5 text-center bg-white rounded shadow-sm">
                    <i class="bi bi-tools text-muted fs-1 mb-3"></i>
                    <h5>No Services Offered Yet</h5>
                    <p class="text-muted">Add services from our platform catalog to start receiving bookings from customers.</p>
                    <button class="btn btn-primary" onclick="Provider.openAddServiceModal()">
                        <i class="bi bi-plus-circle me-1"></i> Add Service Offering
                    </button>
                </div>
            `;
            return;
        }

        container.innerHTML = `
            <div class="row g-4">
                ${this.myServices.map(s => `
                    <div class="col-md-6 col-lg-4">
                        <div class="card h-100 shadow-sm border-0 service-offering-card">
                            <div class="card-body">
                                <div class="d-flex justify-content-between align-items-start mb-3">
                                    <div class="service-icon-box">
                                        <i class="bi ${s.serviceIcon || 'bi-tools'} fs-3 text-primary"></i>
                                    </div>
                                    <span class="fs-4 fw-bold text-primary">$${Number(s.price).toFixed(2)}</span>
                                </div>
                                <h5 class="card-title">${this.escapeHtml(s.serviceName)}</h5>
                                <p class="card-text text-muted small">${this.escapeHtml(s.serviceDescription || '')}</p>
                                <div class="bg-light p-2 rounded small mb-3">
                                    <div><i class="bi bi-briefcase me-1 text-secondary"></i> <strong>${s.experienceYears} Years</strong> Experience</div>
                                    <div><i class="bi bi-clock me-1 text-secondary"></i> <strong>${this.escapeHtml(s.availability)}</strong></div>
                                </div>
                                <div class="d-flex justify-content-end gap-2 border-top pt-2">
                                    <button class="btn btn-outline-primary btn-sm" onclick="Provider.openEditServiceModal(${s.id})">
                                        <i class="bi bi-pencil me-1"></i> Edit
                                    </button>
                                    <button class="btn btn-outline-danger btn-sm" onclick="Provider.deleteService(${s.id})">
                                        <i class="bi bi-trash me-1"></i> Remove
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                `).join('')}
            </div>
        `;
    },

    openAddServiceModal() {
        this.editingServiceId = null;
        document.getElementById('provServiceModalTitle').textContent = 'Add Service Offering';
        document.getElementById('provServiceSelectGroup').style.display = 'block';

        const select = document.getElementById('provModalServiceSelect');
        // Filter out services already offered
        const offeredServiceIds = new Set(this.myServices.map(s => s.serviceId));
        const availableServices = this.catalogServices.filter(s => s.active && !offeredServiceIds.has(s.id));

        if (availableServices.length === 0) {
            select.innerHTML = '<option value="">All catalog services are already in your offerings</option>';
        } else {
            select.innerHTML = '<option value="">Select Service Category</option>' +
                availableServices.map(s => `<option value="${s.id}">${s.name}</option>`).join('');
        }

        document.getElementById('provModalPrice').value = '';
        document.getElementById('provModalExp').value = '2';
        document.getElementById('provModalAvail').value = 'Mon-Sat 9AM-6PM';

        App.openModal('providerServiceModal');
    },

    openEditServiceModal(providerServiceId) {
        const ps = this.myServices.find(s => s.id === providerServiceId);
        if (!ps) return;

        this.editingServiceId = providerServiceId;
        document.getElementById('provServiceModalTitle').textContent = `Edit Offering: ${ps.serviceName}`;
        document.getElementById('provServiceSelectGroup').style.display = 'none';

        document.getElementById('provModalPrice').value = ps.price;
        document.getElementById('provModalExp').value = ps.experienceYears;
        document.getElementById('provModalAvail').value = ps.availability;

        App.openModal('providerServiceModal');
    },

    async saveServiceOffering(event) {
        event.preventDefault();

        const price = document.getElementById('provModalPrice').value;
        const experienceYears = parseInt(document.getElementById('provModalExp').value, 10);
        const availability = document.getElementById('provModalAvail').value.trim();

        if (!price || isNaN(experienceYears) || !availability) {
            App.showToast('Please enter all service offering details.', 'warning');
            return;
        }

        const btn = document.getElementById('btnProvSaveService');
        try {
            App.setButtonLoading(btn, true, 'Saving...');

            if (this.editingServiceId) {
                // Update
                await api.put(`/providers/me/services/${this.editingServiceId}`, {
                    price: parseFloat(price),
                    experienceYears,
                    availability
                });
                App.showToast('Service offering updated successfully!', 'success');
            } else {
                // Create
                const serviceId = document.getElementById('provModalServiceSelect').value;
                if (!serviceId) {
                    App.showToast('Please select a service category.', 'warning');
                    return;
                }
                await api.post('/providers/me/services', {
                    serviceId: parseInt(serviceId, 10),
                    price: parseFloat(price),
                    experienceYears,
                    availability
                });
                App.showToast('New service offering added to your catalog!', 'success');
            }

            App.closeModal('providerServiceModal');
            this.myServices = await api.get('/providers/me/services');
            this.renderMyServices();
        } catch (err) {
            App.showToast(err.message, 'error');
        } finally {
            App.setButtonLoading(btn, false, 'Save Offering');
        }
    },

    async deleteService(providerServiceId) {
        if (!confirm('Are you sure you want to remove this service from your offerings?')) return;
        try {
            App.showLoading(true);
            await api.delete(`/providers/me/services/${providerServiceId}`);
            App.showToast('Service offering removed successfully.', 'info');
            this.myServices = await api.get('/providers/me/services');
            this.renderMyServices();
        } catch (err) {
            App.showToast(err.message, 'error');
        } finally {
            App.showLoading(false);
        }
    },

    renderHistory() {
        const container = document.getElementById('provHistoryList');
        if (!container) return;

        const history = this.bookings.filter(b => b.status === 'COMPLETED' || b.status === 'CANCELLED' || b.status === 'REJECTED');

        if (history.length === 0) {
            container.innerHTML = `
                <div class="empty-state p-4 text-center bg-white rounded shadow-sm">
                    <i class="bi bi-clock-history text-muted fs-2 mb-2"></i>
                    <h6>No Past Booking History</h6>
                    <p class="text-muted small mb-0">Completed, cancelled, or rejected bookings will appear here.</p>
                </div>
            `;
            return;
        }

        container.innerHTML = `
            <div class="table-responsive bg-white rounded shadow-sm">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr>
                            <th>Job #</th>
                            <th>Customer</th>
                            <th>Service</th>
                            <th>Date</th>
                            <th>Price</th>
                            <th>Final Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${history.map(b => `
                            <tr>
                                <td class="fw-bold">#${b.id}</td>
                                <td>${this.escapeHtml(b.customerName || 'Customer')}</td>
                                <td>${this.escapeHtml(b.serviceName || '')}</td>
                                <td>${b.bookingDate}</td>
                                <td class="fw-bold">$${Number(b.price).toFixed(2)}</td>
                                <td>${Customer.getStatusBadge(b.status)}</td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        `;
    },

    populateProfileForm() {
        const user = Auth.getUser();
        if (!user) return;

        const nameInput = document.getElementById('provProfileName');
        const phoneInput = document.getElementById('provProfilePhone');
        const addressInput = document.getElementById('provProfileAddress');
        const emailInput = document.getElementById('provProfileEmail');

        if (nameInput) nameInput.value = user.fullName || '';
        if (phoneInput) phoneInput.value = user.phone || '';
        if (addressInput) addressInput.value = user.address || '';
        if (emailInput) emailInput.value = user.email || '';
    },

    async updateProfile(event) {
        event.preventDefault();
        const fullName = document.getElementById('provProfileName').value.trim();
        const phone = document.getElementById('provProfilePhone').value.trim();
        const address = document.getElementById('provProfileAddress').value.trim();

        if (!fullName || !phone || !address) {
            App.showToast('All profile fields are required.', 'warning');
            return;
        }

        const btn = document.getElementById('btnProvSaveProfile');
        try {
            App.setButtonLoading(btn, true, 'Saving...');
            await Auth.updateProfile({ fullName, phone, address });
            App.showToast('Provider profile updated successfully!', 'success');
            this.renderStats();
        } catch (err) {
            App.showToast(err.message, 'error');
        } finally {
            App.setButtonLoading(btn, false, 'Save Changes');
        }
    },

    async changePassword(event) {
        event.preventDefault();
        const currentPassword = document.getElementById('provOldPassword').value;
        const newPassword = document.getElementById('provNewPassword').value;
        const confirmPassword = document.getElementById('provConfirmPassword').value;

        if (newPassword !== confirmPassword) {
            App.showToast('New passwords do not match.', 'warning');
            return;
        }

        if (newPassword.length < 6) {
            App.showToast('New password must be at least 6 characters.', 'warning');
            return;
        }

        const btn = document.getElementById('btnProvChangePass');
        try {
            App.setButtonLoading(btn, true, 'Updating...');
            await Auth.changePassword(currentPassword, newPassword);
            App.showToast('Password changed successfully!', 'success');
            document.getElementById('provChangePassForm').reset();
        } catch (err) {
            App.showToast(err.message, 'error');
        } finally {
            App.setButtonLoading(btn, false, 'Update Password');
        }
    },

    escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
};
