# WORKERS – Home Services Booking Platform

WORKERS is a home services booking platform designed to connect customers with local service providers. Users can explore services, find professionals, and manage service bookings through a simple web interface.

## 🚀 Features

### Customer
- Browse available home services.
- Find local service providers.
- Register and log in.
- Book home services.
- View booking information.

### Service Provider
- Log in to the provider account.
- Manage service-related information.
- View and manage customer bookings.

### Admin
- Dedicated administration functionality.
- Manage platform information and users, depending on backend implementation.

## 🛠️ Tech Stack

### Frontend
- HTML5
- CSS3
- JavaScript
- Responsive web design

### Backend
- Java
- Spring Boot
- Spring Data JPA / Hibernate
- REST APIs

### Database
- MySQL

## 📁 Project Structure

```text
WORKERS/
├── frontend/
│   ├── css/
│   ├── image/
│   ├── js/
│   ├── README.md
│   └── workers-frontend.html
├── backend/
│   ├── src/
│   ├── pom.xml
│   └── README.md
└── .gitignore
```

## ⚙️ Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/Anshitvishwakarma/WORKERS.git
cd WORKERS
```

### 2. Run the Backend

Open the `backend` folder in IntelliJ IDEA.

1. Configure your MySQL database.
2. Update the database settings in `backend/src/main/resources/application.properties` or `application.yml`.
3. Make sure the required database exists.
4. Run the Spring Boot main application class.

The backend typically runs at:

```text
http://localhost:8080
```

The actual port depends on your application configuration.

### 3. Run the Frontend

Open the `frontend` folder in Visual Studio Code.

1. Install the Live Server extension if needed.
2. Open `workers-frontend.html`.
3. Right-click and select **Open with Live Server**.
4. Configure the frontend API URL to match the running backend.

Make sure the backend allows requests from the frontend origin through CORS configuration.

## 🔐 Configuration and Security

- Keep database passwords and secret keys out of public repositories.
- Use environment variables for sensitive production configuration.
- Configure the frontend API URL for the deployed backend before production deployment.
- Do not commit real credentials or private tokens.

## 🌐 Deployment

The application can be prepared for deployment using:
- **Frontend:** Static hosting service.
- **Backend:** Java-compatible hosting service such as Render.
- **Database:** A hosted MySQL-compatible database.

Deployment requires configuring the production database, environment variables, API URL, and CORS settings.

## 🎯 Project Goal

The goal of WORKERS is to make it easier for customers to discover local professionals and arrange home services through a convenient online platform.

## 👨‍💻 Developer

**Anshit Vishwakarma**

GitHub: [Anshitvishwakarma](https://github.com/Anshitvishwakarma)

## 📄 License

This project is currently intended for learning and development. Add a license if you decide to distribute it under specific terms.
