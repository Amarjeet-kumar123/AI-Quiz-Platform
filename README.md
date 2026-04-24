<div align="center">

# 🧠 AI Quiz Platform
### Automated Knowledge Assessment & Dynamic Quiz Generation Platform

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=flat-square&logo=springboot)
![React](https://img.shields.io/badge/React-18-blue?style=flat-square&logo=react)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?style=flat-square&logo=postgresql)
![Gemini AI](https://img.shields.io/badge/Gemini%20AI-2.5%20Flash-purple?style=flat-square&logo=google)
![Maven](https://img.shields.io/badge/Maven-3.x-red?style=flat-square&logo=apachemaven)
![License](https://img.shields.io/badge/License-Academic-lightgrey?style=flat-square)

**B.Tech CSE — 3rd Year Project | Advanced Java Programming**

</div>

---

## 📌 Project Description

AI Quiz Platform is a full-stack web application that leverages **Gemini AI** to dynamically generate quizzes based on user-selected topics, difficulty levels, and uploaded study materials. It provides an intelligent, interactive learning experience with real-time feedback, performance analytics, and personalized recommendations — all powered by AI.

---

## 🚩 Problem Statement

Traditional quiz and assessment platforms rely on static, pre-written question banks that quickly become outdated and repetitive. Students often have no insight into their weak areas, and educators spend hours manually creating assessments. This platform solves all of that by using AI to generate fresh, contextual questions on demand — from any topic or uploaded document — while also analyzing performance and guiding students toward focused improvement.

---

## ✨ Key Features

### 👤 User Features
| Feature | Description |
|---|---|
| 🔐 Authentication | Secure user registration and login |
| 🤖 AI Quiz Generation | Dynamic questions via Gemini AI |
| 🎯 Custom Quiz Setup | Choose topic, difficulty, and number of questions |
| ⏱️ Quiz Timer | Countdown timer with live progress bar |
| 📊 Result Summary | Instant score, correct/incorrect breakdown |
| 🔍 Answer Review | Review all questions with AI explanations |
| 📁 Quiz History | Track all past quiz attempts |
| 🏆 Leaderboard | Compete with other users |
| 📄 Study Material Upload | Upload PDF, TXT, DOC, DOCX, PPT, PPTX for RAG-based quizzes |
| 🧠 AI Weakness Analyzer | AI-powered detection of weak subject areas |
| 🎯 Smart Recommendations | Personalized topic suggestions based on performance |
| 📥 PDF Download | Download quiz results as PDF |
| 🌙 Dark / Light Mode | Full theme toggle support |
| 🔔 Backend Health Banner | Live backend status indicator |

### 🛡️ Admin Features
| Feature | Description |
|---|---|
| 🔑 Admin Login | Separate secure admin portal |
| 👥 User Management | View, manage, and moderate users |
| 📝 Quiz Management | Monitor and manage all quizzes |
| 📁 Document Management | Manage uploaded study materials |
| 📈 Analytics Dashboard | Platform-wide usage statistics |
| 🏆 Leaderboard Monitoring | Track and manage leaderboard entries |
| 🔎 AI Question Review | Review recently AI-generated questions |

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| **Frontend** | React 18, Vite, JavaScript (ES6+), CSS |
| **Backend** | Spring Boot 3.x, Java 17, Maven |
| **Database** | PostgreSQL 15, pgAdmin 4 |
| **AI Integration** | Google Gemini API (2.5 Flash), Spring AI |
| **AI Strategy** | RAG (Retrieval-Augmented Generation) |
| **Document Parsing** | Apache POI, PDFBox |
| **PDF Export** | iText / OpenPDF |

---

## 🗂️ System Modules

```
1. Auth Module          → Registration, Login, JWT Authentication
2. Quiz Engine          → AI-based question generation via Gemini API
3. RAG Module           → Document parsing + vector-based retrieval
4. Result Module        → Score calculation, review, PDF export
5. Analytics Module     → Weakness analysis, smart recommendations
6. Leaderboard Module   → Ranking and score tracking
7. Admin Module         → Full platform management dashboard
8. Theme Module         → Dark/Light mode toggle
```

---

## 📁 Folder Structure

```
ai-quiz-platform/
│
├── frontend/                        # React + Vite Frontend
│   ├── public/
│   ├── src/
│   │   ├── assets/
│   │   ├── components/              # Reusable UI components
│   │   │   ├── Navbar.jsx
│   │   │   ├── Timer.jsx
│   │   │   ├── ProgressBar.jsx
│   │   │   └── ThemeToggle.jsx
│   │   ├── pages/                   # Route-level pages
│   │   │   ├── Login.jsx
│   │   │   ├── Register.jsx
│   │   │   ├── Dashboard.jsx
│   │   │   ├── QuizSetup.jsx
│   │   │   ├── QuizPlay.jsx
│   │   │   ├── QuizResult.jsx
│   │   │   ├── History.jsx
│   │   │   ├── Leaderboard.jsx
│   │   │   ├── UploadMaterial.jsx
│   │   │   └── Admin/
│   │   │       ├── AdminDashboard.jsx
│   │   │       ├── UserManagement.jsx
│   │   │       ├── QuizManagement.jsx
│   │   │       └── Analytics.jsx
│   │   ├── services/                # Axios API calls
│   │   ├── context/                 # React Context (Auth, Theme)
│   │   ├── App.jsx
│   │   └── main.jsx
│   ├── .env
│   ├── index.html
│   └── package.json
│
├── backend/                         # Spring Boot Backend
│   ├── src/
│   │   └── main/
│   │       ├── java/com/aiquiz/
│   │       │   ├── controller/      # REST Controllers
│   │       │   ├── service/         # Business Logic
│   │       │   ├── repository/      # JPA Repositories
│   │       │   ├── model/           # Entity Classes
│   │       │   ├── dto/             # Data Transfer Objects
│   │       │   ├── security/        # JWT + Spring Security
│   │       │   ├── ai/              # Gemini AI + RAG Integration
│   │       │   └── config/          # App Configuration
│   │       └── resources/
│   │           └── application.properties
│   ├── pom.xml
│   └── README.md
│
└── README.md
```

---

## ⚙️ Installation & Setup

### Prerequisites

Make sure the following are installed on your system:

- [Node.js](https://nodejs.org/) (v18+) & npm
- [Java JDK 17+](https://adoptium.net/)
- [Maven 3.x](https://maven.apache.org/)
- [PostgreSQL 15+](https://www.postgresql.org/)
- [pgAdmin 4](https://www.pgadmin.org/) *(optional, for DB GUI)*
- A valid [Gemini API Key](https://aistudio.google.com/app/apikey)

---

## 🖥️ How to Run — Frontend

```bash
# 1. Navigate to the frontend directory
cd frontend

# 2. Install dependencies
npm install

# 3. Create environment file
cp .env.example .env
# Set VITE_API_BASE_URL=http://localhost:8080

# 4. Start the development server
npm run dev
```

> Frontend runs at: **http://localhost:5173**

---

## ☕ How to Run — Backend

```bash
# 1. Navigate to the backend directory
cd backend

# 2. Update application.properties with your DB and API credentials
# (See sections below)

# 3. Build the project
mvn clean install

# 4. Run the Spring Boot application
mvn spring-boot:run
```

> Backend runs at: **http://localhost:8080**

---

## 🗃️ Database Setup

1. Open **pgAdmin** or your PostgreSQL client
2. Create a new database:

```sql
CREATE DATABASE ai_quiz_db;
```

3. Update `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ai_quiz_db
spring.datasource.username=your_postgres_username
spring.datasource.password=your_postgres_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

> Tables are auto-created by Hibernate on first run (`ddl-auto=update`).

---

## 🔑 Gemini API Key Setup

1. Get your free API key from [Google AI Studio](https://aistudio.google.com/app/apikey)
2. Add the following to `application.properties`:

```properties
gemini.api.key=YOUR_API_KEY
gemini.model=gemini-2.5-flash
```

> ⚠️ Never commit your real API key to GitHub. Use environment variables or `.gitignore` the properties file.

---

## 🛡️ Admin Login Credentials

> Default credentials for first-time setup (change after first login):

```
Username : admin
Password : admin123
```

Access the Admin Panel at: **http://localhost:5173/admin**

---

## 📸 Screenshots

> *(Add screenshots here after running the project locally)*

| Page | Preview |
|---|---|
| 🏠 Dashboard | `screenshots/dashboard.png` |
| 🎯 Quiz Setup | `screenshots/quiz-setup.png` |
| ⏱️ Quiz in Progress | `screenshots/quiz-play.png` |
| 📊 Result Summary | `screenshots/result.png` |
| 🧠 Weakness Analyzer | `screenshots/weakness.png` |
| 🏆 Leaderboard | `screenshots/leaderboard.png` |
| 🛡️ Admin Panel | `screenshots/admin.png` |

---

## 🚀 Future Scope

- [ ] **Voice-Based Quiz Mode** — Answer questions via speech input
- [ ] **Multi-Language Support** — Quiz generation in regional languages
- [ ] **Collaborative Quizzes** — Real-time multiplayer quiz sessions
- [ ] **Mobile App** — React Native version for Android/iOS
- [ ] **LMS Integration** — Connect with Moodle, Google Classroom
- [ ] **Advanced Analytics** — Trend graphs and subject-wise heatmaps
- [ ] **Custom Quiz Sharing** — Share quiz links with friends or classmates
- [ ] **Proctoring Mode** — AI-assisted cheat detection for exams

---

## 👨‍💻 Author

<table>
  <tr>
    <td align="center">
      <b>Amarjeet Kumar</b><br/>
      B.Tech CSE — 3rd Year<br/>
      Noida Institute of Engineering and Technology, Greater Noida<br/>
      📧 9262amarjeet@gmail.com<br/>
      <a href="https://github.com/Amarjeet-kumar123">GitHub</a> •
      <a href="https://www.linkedin.com/in/amarjeet-kumar-b858b02a5?utm_source=share&utm_campaign=share_via&utm_content=profile&utm_medium=android_app">LinkedIn</a>
    </td>
  </tr>
</table>

> 📌 *Project developed as part of the Advanced Java Programming course under the B.Tech Computer Science & Engineering program.*

---

## 📄 License

This project is created for **academic and educational purposes only** as part of a B.Tech CSE curriculum.

```
Academic Use License

This project may be used for learning, reference, and academic submissions.
Commercial use, redistribution, or resale is not permitted without explicit
written permission from the author.

© 2025 Amarjeet Kumar — All Rights Reserved
```

---

<div align="center">

**⭐ If you found this project helpful, consider giving it a star on GitHub!**

Made with ❤️ using Java, Spring Boot, React & Gemini AI

</div>
