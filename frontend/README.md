# FixMate Frontend Application

The FixMate frontend is a responsive, modern single-page web application (SPA) built with vanilla HTML5, CSS3, and ES6+ JavaScript. It requires **zero Node.js or npm dependencies** and can be run immediately via VS Code Live Server or any static HTTP file server.

---

## 1. Features & UX Highlights

- **Modern Aesthetic**: Professional Deep Navy (`#0f172a`), Royal Blue (`#2563eb`), Slate, and Coral styling with rounded card containers and subtle drop shadows.
- **Dark / Light Mode**: Integrated theme switcher with persistent user preference stored in `localStorage`.
- **Responsive Layout**: Designed for seamless viewing across mobile viewports, tablets, and wide desktop screens.
- **Role-Based Navigation & Dashboards**:
  - **Public Portal**: Hero search banner, featured popular service categories, interactive "How It Works" workflow, provider value proposition, and customer reviews.
  - **Customer Dashboard**: Live service catalog browsing, service provider filtering, instant booking modal with address and schedule inputs, booking status tracker, and cancellation controls.
  - **Provider Dashboard**: Real-time incoming booking request cards, one-click Accept/Reject/Complete workflow buttons, service offering management modal (set hourly rates, years of experience, availability status).
  - **Admin Control Center**: KPI analytic cards (total users, active providers, completed revenue, open bookings), user management table with instant account enable/disable toggles, service catalog editor.
- **Convenience Features**: 1-Click Demo Login buttons for Admin, Customer, Plumber, and Electrician to enable instant evaluation.
- **State & Feedback**:
  - Non-blocking toast notification banner system for success and error alerts.
  - Clean empty state indicators when no records match a filter.
  - Interactive confirmation dialogs before destructive actions (e.g., booking cancellation).

---

## 2. Directory Layout

```text
FixMate/frontend/
├── index.html              # Single Page Application HTML markup with all view containers & modals
├── css/
│   └── style.css           # CSS variables, modern typography, grid/flexbox layouts & dark theme rules
├── js/
│   ├── config.js           # Central configuration (API base URL: http://localhost:8080/api)
│   ├── api.js              # Central HTTP client wrapping Fetch API with JWT auth and error handling
│   ├── auth.js             # Authentication controller: login, register, profile management & demo loaders
│   ├── customer.js         # Customer service search, provider discovery & booking creation logic
│   ├── provider.js         # Provider service catalog CRUD & booking request handling
│   ├── admin.js            # Admin analytics metrics, user moderation & service catalog administration
│   └── app.js              # Global SPA router, theme manager, toast engine & initialization
└── README.md
```

---

## 3. Running the Frontend

Because the application is written entirely in native web standards, you do not need to install Node.js, Webpack, or Babel.

### Option A: VS Code Live Server (Recommended)
1. Open Visual Studio Code.
2. Open the `FixMate/frontend` directory (or the root `FixMate` folder).
3. Right-click `index.html` and select **"Open with Live Server"** (or click the "Go Live" button in the status bar).
4. The application opens automatically at `http://127.0.0.1:5500/index.html` (or matching port).

### Option B: Python Simple HTTP Server
If Python is installed on your computer:
```bash
cd FixMate/frontend
python -m http.server 3000
```
Then visit: `http://localhost:3000`

### Option C: Direct Browser Opening
You can double-click `index.html` to open it in Chrome, Edge, or Firefox. *Note: Running through an HTTP server (such as Live Server) is recommended to prevent any browser local file origin restrictions.*

---

## 4. API Configuration

The frontend communicates with the backend via the `API_BASE_URL` defined in `js/config.js`:
```javascript
const CONFIG = {
  API_BASE_URL: 'http://localhost:8080/api',
  DEFAULT_PAGE_SIZE: 20
};
```
If your backend is hosted on a different host or port, simply update `js/config.js`.

---

## 5. Security & Token Storage

- Authentication tokens are stored in `localStorage` under the key `fixmate_token`.
- User metadata is stored under `fixmate_user`.
- On every outgoing request to protected endpoints, `api.js` automatically injects the `Authorization: Bearer <TOKEN>` header.
- If an API returns HTTP 401 Unauthorized, the application displays a friendly toast and logs the user out to protect session integrity.
