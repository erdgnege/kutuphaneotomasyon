# ⚡️ kutuphaneotomasyon - Full Stack Application

This project is a modern, full-stack web application implementing a **Decoupled Architecture**.

The user interface is developed as a fast and dynamic **Single Page Application (SPA)** using **ReactJS**. The backend logic is provided by a reliable and scalable **RESTful API** built with the powerful **Java Spring Boot** framework. The two components communicate seamlessly using standard JSON over HTTP.

## ✨ Architecture & Key Technologies

This project adheres to a Decoupled Architecture, separating the presentation layer (Frontend) from the business logic and data persistence layer (Backend).

| Category | Technology | Role and Description |
| :--- | :--- | :--- |
| **Frontend** | **ReactJS** | Component-based library for building the dynamic and responsive user interface. |
| **Backend** | **Java (Spring Boot)** | Highly scalable framework used to develop the secure and robust RESTful API. |
| **Communication** | **RESTful API (JSON)** | Standard protocol for data exchange between the client and server. |
| **Database** | **MSSQL** | Manages persistent application data. |
| **Build Tools** | **npm/Yarn (Frontend) & Maven/Gradle (Backend)** | Dependency management and build automation for both services. |

## 🛠️ Local Setup and Running

To run this project locally, you need to start **two separate services**.

### Step 1: Starting the Backend Service

1.  Navigate to the `backend/` directory.
2.  **[DB Step: e.g., Update your database configuration in the application.properties file.]**
3.  Run the application using your build tool or IDE: `mvn spring-boot:run` (if using Maven).
    > The Backend API will typically start on `http://localhost:8080`.

### Step 2: Starting the Frontend Application

1.  Navigate to the `frontend/` directory.
2.  Install the required dependencies: `npm install` (or `yarn install`).
3.  Start the React development server: `npm start` (or `yarn start`).
    > The Frontend application will usually launch in your browser at `http://localhost:3000`.

The application should now be fully functional, with the React frontend consuming data from the Spring Boot API!
