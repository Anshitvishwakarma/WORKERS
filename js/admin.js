/**
 * FixMate Admin Module
 * Handles administrator dashboard, KPI analytics, user management, service catalog CRUD, and booking monitoring.
 */
const Admin = {
    stats: null,
    users: [],
    services: [],
    bookings: [],
    editingServiceId: null,

    async init() {
        if (!Auth.isLoggedIn() || Auth.getRole() !== 'ADMIN') {
            App.navigateTo('login');
            return;
        }

        await this.loadAdminData();
        this.renderStats();
        this.renderUsers();
        this.renderServices();
        this.renderBookings();
    },

    async loadAdminData() {
        try {
            App.showLoading(true);
            const [stats, users, services, bookings] = await Promise.all([
                api.get('/admin/dashboard'),
                api.get('/admin/users'),
                api.get('/admin/services'),
                api.get('/admin/bookings')
            ]);
            this.stats = stats;
            this.users = users || [];
            this.services = services || [];
            this.bookings = bookings || [];
        } catch (err) {
            console.error('Error loading admin data:', err);
            App.showToast(err.message, 'error');
        } finally {
            App.showLoading(false);
        }
    },

    renderStats() {
        if (!this.stats) return;

        const elUsers = document.getElementById('adminStatUsers');
        const elCustomers = document.getElementById('adminStatCustomers');
        const elProviders = document.getElementById('adminStatProviders');
        const elServices = document.getElementById('adminStatServices');
        const elBookings = document.getElementById('adminStatBookings');
        const elPending = document.getElementById('adminStatPending');
        const elCompleted = document.getElementById('adminStatCompleted');
        const elRevenue = document.getElementById('adminStatRevenue');

        if (elUsers) elUsers.textContent = this.stats.totalUsers;
        if (elCustomers) elCustomers.textContent = this.stats.totalCustomers;
        if (elProviders) elProviders.textContent = this.stats.totalProviders;
        if (elServices) elServices.textContent = this.stats.totalServices;
        if (elBookings) elBookings.textContent = this.stats.totalBookings;
        if (elPending) elPending.textContent = this.stats.pendingBookings;
        if (elCompleted) elCompleted.textContent = this.stats.completedBookings;
        if (elRevenue) elRevenue.textContent = `$${Number(this.stats.totalRevenue || 0).toFixed(2)}`;
    },

    // =========================================================================
    // Users Management
    // =========================================================================

    renderUsers(filteredList = null) {
        const container = document.getElementById('adminUsersTableBody');
        if (!container) return;

        const list = filteredList || this.users;

        if (list.length === 0) {
            container.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center text-muted py-4">No users found matching the filter.</td>
                </tr>
            `;
            return;
        }

        container.innerHTML = list.map(u => `
            <tr>
                <td class="fw-bold">#${u.id}</td>
                <td>
                    <div class="fw-semibold">${this.escapeHtml(u.fullName)}</div>
                    <div class="small text-muted">${this.escapeHtml(u.address || '')}</div>
                </td>
                <td>
                    <div>${this.escapeHtml(u.email)}</div>
                    <div class="small text-muted">${this.escapeHtml(u.phone || '')}</div>
                </td>
                <td>${this.getRoleBadge(u.role)}</td>
                <td>
                    ${u.active ?
                        '<span class="badge bg-success-subtle text-success border border-success-subtle"><i class="bi bi-check-circle me-1"></i> Active</span>' :
                        '<span class="badge bg-danger-subtle text-danger border border-danger-subtle"><i class="bi bi-dash-circle me-1"></i> Inactive</span>'
                    }
                </td>
                <td class="small text-muted">${u.createdAt ? u.createdAt.split('T')[0] : 'N/A'}</td>
                <td class="text-end">
                    ${u.role === 'ADMIN' ? `
                        <span class="badge bg-secondary">System Admin</span>
                    ` : `
                        <button class="btn btn-sm ${u.active ? 'btn-outline-danger' : 'btn-outline-success'}"
                                onclick="Admin.toggleUserStatus(${u.id}, ${!u.active})">
                            <i class="bi ${u.active ? 'bi-person-slash' : 'bi-person-check'} me-1"></i>
                            ${u.active ? 'Deactivate' : 'Activate'}
                        </button>
                    `}
                </td>
            </tr>
        `).join('');
    },

    filterUsers() {
        const role = document.getElementById('adminUserRoleFilter')?.value;
        const search = document.getElementById('adminUserSearchInput')?.value.toLowerCase().trim();

        let list = [...this.users];
        if (role) {
            list = list.filter(u => u.role === role);
        }
        if (search) {
            list = list.filter(u =>
                (u.fullName && u.fullName.toLowerCase().includes(search)) ||
                (u.email && u.email.toLowerCase().includes(search)) ||
                (u.phone && u.phone.toLowerCase().includes(search))
            );
        }
        this.renderUsers(list);
    },

    async toggleUserStatus(userId, targetActiveState) {
        const action = targetActiveState ? 'activate' : 'deactivate';
        if (!confirm(`Are you sure you want to ${action} this user account?`)) return;

        try {
            App.showLoading(true);
            const updated = await api.patch(`/admin/users/${userId}/status?active=${targetActiveState}`);
            App.showToast(`User account ${action}d successfully.`, 'success');
            
            // Update in local list
            const idx = this.users.findIndex(u => u.id === userId);
            if (idx !== -1) {
                this.users[idx] = updated;
            }
            this.filterUsers();
        } catch (err) {
            App.showToast(err.message, 'error');
        } finally {
            App.showLoading(false);
        }
    },

    // =========================================================================
    // Services Management
    // =========================================================================

    renderServices() {
        const container = document.getElementById('adminServicesTableBody');
        if (!container) return;

        if (this.services.length === 0) {
            container.innerHTML = `
                <tr>
                    <td colspan="6" class="text-center text-muted py-4">No services in catalog.</td>
                </tr>
            `;
            return;
        }

        container.innerHTML = this.services.map(s => `
            <tr>
                <td class="fw-bold">#${s.id}</td>
                <td>
                    <div class="d-flex align-items-center">
                        <div class="service-icon-box-sm me-2">
                            <i class="bi ${s.icon || 'bi-tools'} text-primary"></i>
                        </div>
                        <span class="fw-semibold">${this.escapeHtml(s.name)}</span>
                    </div>
                </td>
                <td class="small text-muted" style="max-width: 300px;">${this.escapeHtml(s.description || '')}</td>
                <td><code>${this.escapeHtml(s.icon || '')}</code></td>
                <td>
                    ${s.active ?
                        '<span class="badge bg-success-subtle text-success">Active</span>' :
                        '<span class="badge bg-secondary">Disabled</span>'
                    }
                </td>
                <td class="text-end">
                    <button class="btn btn-outline-primary btn-sm me-1" onclick="Admin.openEditServiceModal(${s.id})">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-outline-${s.active ? 'warning' : 'success'} btn-sm me-1"
                            onclick="Admin.toggleServiceStatus(${s.id}, ${!s.active})" title="${s.active ? 'Disable' : 'Enable'}">
                        <i class="bi ${s.active ? 'bi-eye-slash' : 'bi-eye'}"></i>
                    </button>
                    <button class="btn btn-outline-danger btn-sm" onclick="Admin.deleteService(${s.id})" title="Delete">
                        <i class="bi bi-trash"></i>
                    </button>
                </td>
            </tr>
        `).join('');
    },

    openAddServiceModal() {
        this.editingServiceId = null;
        document.getElementById('adminServiceModalTitle').textContent = 'Add New Service Category';
        document.getElementById('adminServiceName').value = '';
        document.getElementById('adminServiceDesc').value = '';
        document.getElementById('adminServiceIcon').value = 'bi-tools';
        document.getElementById('adminServiceActiveGroup').style.display = 'none';

        App.openModal('adminServiceModal');
    },

    openEditServiceModal(serviceId) {
        const s = this.services.find(item => item.id === serviceId);
        if (!s) return;

        this.editingServiceId = serviceId;
        document.getElementById('adminServiceModalTitle').textContent = `Edit Service: ${s.name}`;
        document.getElementById('adminServiceName').value = s.name;
        document.getElementById('adminServiceDesc').value = s.description || '';
        document.getElementById('adminServiceIcon').value = s.icon || 'bi-tools';
        document.getElementById('adminServiceActiveGroup').style.display = 'block';
        document.getElementById('adminServiceActive').checked = s.active;

        App.openModal('adminServiceModal');
    },

    async saveService(event) {
        event.preventDefault();

        const name = document.getElementById('adminServiceName').value.trim();
        const description = document.getElementById('adminServiceDesc').value.trim();
        const icon = document.getElementById('adminServiceIcon').value.trim();

        if (!name || !description) {
            App.showToast('Service name and description are required.', 'warning');
            return;
        }

        const btn = document.getElementById('btnAdminSaveService');
        try {
            App.setButtonLoading(btn, true, 'Saving...');

            if (this.editingServiceId) {
                const active = document.getElementById('adminServiceActive').checked;
                await api.put(`/admin/services/${this.editingServiceId}`, {
                    name,
                    description,
                    icon,
                    active
                });
                App.showToast('Service updated successfully!', 'success');
            } else {
                await api.post('/admin/services', {
                    name,
                    description,
                    icon
                });
                App.showToast('Service created successfully!', 'success');
            }

            App.closeModal('adminServiceModal');
            this.services = await api.get('/admin/services');
            this.renderServices();
            // Refresh dashboard KPIs
            this.stats = await api.get('/admin/dashboard');
            this.renderStats();
        } catch (err) {
            App.showToast(err.message, 'error');
        } finally {
            App.setButtonLoading(btn, false, 'Save Service');
        }
    },

    async toggleServiceStatus(serviceId, targetActiveState) {
        try {
            App.showLoading(true);
            await api.patch(`/admin/services/${serviceId}/status?active=${targetActiveState}`);
            App.showToast(`Service status updated.`, 'info');
            this.services = await api.get('/admin/services');
            this.renderServices();
        } catch (err) {
            App.showToast(err.message, 'error');
        } finally {
            App.showLoading(false);
        }
    },

    async deleteService(serviceId) {
        if (!confirm('Are you sure you want to deactivate/delete this service?')) return;
        try {
            App.showLoading(true);
            await api.delete(`/admin/services/${serviceId}`);
            App.showToast('Service deactivated successfully.', 'info');
            this.services = await api.get('/admin/services');
            this.renderServices();
        } catch (err) {
            App.showToast(err.message, 'error');
        } finally {
            App.showLoading(false);
        }
    },

    // =========================================================================
    // Bookings Monitoring
    // =========================================================================

    renderBookings(statusFilter = '') {
        const container = document.getElementById('adminBookingsTableBody');
        if (!container) return;

        let list = [...this.bookings];
        if (statusFilter) {
            list = list.filter(b => b.status === statusFilter);
        }

        if (list.length === 0) {
            container.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center text-muted py-4">No bookings found.</td>
                </tr>
            `;
            return;
        }

        container.innerHTML = list.map(b => `
            <tr>
                <td class="fw-bold">#${b.id}</td>
                <td>
                    <div class="fw-semibold">${this.escapeHtml(b.customerName || 'Customer')}</div>
                    <div class="small text-muted">${this.escapeHtml(b.customerEmail || '')}</div>
                </td>
                <td>
                    <div class="fw-semibold">${this.escapeHtml(b.providerName || 'Provider')}</div>
                    <div class="small text-muted">${this.escapeHtml(b.providerPhone || '')}</div>
                </td>
                <td>
                    <span class="badge bg-light text-dark border">
                        <i class="bi ${b.serviceIcon || 'bi-tools'} me-1 text-primary"></i> ${this.escapeHtml(b.serviceName || '')}
                    </span>
                </td>
                <td>
                    <div>${b.bookingDate}</div>
                    <div class="small text-muted">${b.bookingTime}</div>
                </td>
                <td class="fw-bold text-success">$${Number(b.price).toFixed(2)}</td>
                <td>${Customer.getStatusBadge(b.status)}</td>
            </tr>
        `).join('');
    },

    filterBookings() {
        const status = document.getElementById('adminBookingStatusFilter')?.value;
        this.renderBookings(status);
    },

    getRoleBadge(role) {
        switch (role) {
            case 'ADMIN':
                return '<span class="badge bg-danger">ADMIN</span>';
            case 'PROVIDER':
                return '<span class="badge bg-info text-dark">PROVIDER</span>';
            case 'CUSTOMER':
                return '<span class="badge bg-primary">CUSTOMER</span>';
            default:
                return `<span class="badge bg-secondary">${role}</span>`;
        }
    },

    escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
};
