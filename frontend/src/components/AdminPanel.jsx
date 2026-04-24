import { useEffect, useMemo, useState } from "react";
import DashboardCard from "./DashboardCard";
import LeaderboardTable from "./LeaderboardTable";

const ADMIN_TOKEN_KEY = "quiz_ai_admin_token";

function AdminPanel({ apiBase, onBackToLanding, theme, onToggleTheme }) {
  const [adminToken, setAdminToken] = useState(() => localStorage.getItem(ADMIN_TOKEN_KEY));
  const [adminUser, setAdminUser] = useState("admin");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const [analytics, setAnalytics] = useState({
    totalUsers: 0,
    totalQuizzes: 0,
    totalUploadedFiles: 0,
    averageQuizScore: 0
  });
  const [users, setUsers] = useState([]);
  const [documents, setDocuments] = useState([]);
  const [quizzes, setQuizzes] = useState([]);
  const [leaderboard, setLeaderboard] = useState([]);
  const [questions, setQuestions] = useState([]);

  const [form, setForm] = useState({ username: "", password: "" });

  const isAuthenticated = Boolean(adminToken);

  const sectionStats = useMemo(
    () => ({
      users: users.length,
      documents: documents.length,
      quizzes: quizzes.length,
      questions: questions.length
    }),
    [users, documents, quizzes, questions]
  );

  useEffect(() => {
    if (!adminToken) return;
    refreshAll();
  }, [adminToken]);

  const adminRequest = async (path, options = {}) => {
    const { method = "GET", body } = options;
    const headers = { "Content-Type": "application/json" };
    if (adminToken) {
      headers["X-Admin-Token"] = adminToken;
    }

    const response = await fetch(`${apiBase}${path}`, {
      method,
      headers,
      body: body == null ? undefined : JSON.stringify(body)
    });

    const text = await response.text();
    const payload = text ? JSON.parse(text) : null;

    if (!response.ok) {
      throw new Error(payload?.reason || payload?.message || `Admin request failed (${response.status})`);
    }
    return payload;
  };

  const refreshAll = async () => {
    setLoading(true);
    setError("");
    try {
      const [analyticsRes, usersRes, docsRes, quizzesRes, leaderboardRes, questionsRes] = await Promise.all([
        adminRequest("/api/admin/analytics"),
        adminRequest("/api/admin/users"),
        adminRequest("/api/admin/documents"),
        adminRequest("/api/admin/quizzes"),
        adminRequest("/api/admin/leaderboard"),
        adminRequest("/api/admin/questions/recent")
      ]);
      setAnalytics(analyticsRes || {});
      setUsers(Array.isArray(usersRes) ? usersRes : []);
      setDocuments(Array.isArray(docsRes) ? docsRes : []);
      setQuizzes(Array.isArray(quizzesRes) ? quizzesRes : []);
      setLeaderboard(Array.isArray(leaderboardRes) ? leaderboardRes : []);
      setQuestions(Array.isArray(questionsRes) ? questionsRes : []);
    } catch (requestError) {
      setError(requestError.message);
      if (requestError.message.toLowerCase().includes("unauthorized") || requestError.message.toLowerCase().includes("token")) {
        handleLogout();
      }
    } finally {
      setLoading(false);
    }
  };

  const handleLogin = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError("");
    try {
      const response = await fetch(`${apiBase}/api/admin/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(form)
      });
      const text = await response.text();
      const payload = text ? JSON.parse(text) : null;
      if (!response.ok) {
        throw new Error(payload?.reason || payload?.message || "Invalid admin credentials.");
      }
      localStorage.setItem(ADMIN_TOKEN_KEY, payload.token);
      setAdminToken(payload.token);
      setAdminUser(payload.username || "admin");
    } catch (loginError) {
      setError(loginError.message);
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem(ADMIN_TOKEN_KEY);
    setAdminToken("");
    setUsers([]);
    setDocuments([]);
    setQuizzes([]);
    setLeaderboard([]);
    setQuestions([]);
  };

  const deleteUser = async (id) => {
    if (!window.confirm("Delete this user and their quiz results?")) return;
    try {
      await adminRequest(`/api/admin/users/${id}`, { method: "DELETE" });
      await refreshAll();
    } catch (requestError) {
      setError(requestError.message);
    }
  };

  const deleteDocument = async (id) => {
    if (!window.confirm("Delete this study material file?")) return;
    try {
      await adminRequest(`/api/admin/documents/${id}`, { method: "DELETE" });
      await refreshAll();
    } catch (requestError) {
      setError(requestError.message);
    }
  };

  const deleteQuiz = async (id) => {
    if (!window.confirm("Delete this generated quiz and linked questions?")) return;
    try {
      await adminRequest(`/api/admin/quizzes/${id}`, { method: "DELETE" });
      await refreshAll();
    } catch (requestError) {
      setError(requestError.message);
    }
  };

  if (!isAuthenticated) {
    return (
      <main className="auth-screen">
        <section className="auth-card admin-auth-card">
          <button type="button" className="theme-toggle auth-theme-toggle" onClick={onToggleTheme}>
            {theme === "dark" ? "☀️ Light Mode" : "🌙 Dark Mode"}
          </button>
          <h1>Admin Login</h1>
          <p>Restricted access for platform administration and faculty monitoring.</p>
          <button type="button" className="text-link" onClick={onBackToLanding}>
            Back to landing page
          </button>
          <form className="form-grid" onSubmit={handleLogin}>
            <label>
              Username
              <input
                value={form.username}
                onChange={(event) => setForm((prev) => ({ ...prev, username: event.target.value }))}
                required
              />
            </label>
            <label>
              Password
              <input
                type="password"
                value={form.password}
                onChange={(event) => setForm((prev) => ({ ...prev, password: event.target.value }))}
                required
              />
            </label>
            {error && <p className="error">{error}</p>}
            <button type="submit" className="btn" disabled={loading}>
              {loading ? "Signing in..." : "Login as Admin"}
            </button>
          </form>
        </section>
      </main>
    );
  }

  return (
    <main className="app-shell">
      <header className="topbar">
        <div>
          <h1>Admin Control Center</h1>
          <p>AI Quiz Platform monitoring and management</p>
        </div>
        <div className="quick-actions">
          <button type="button" className="theme-toggle" onClick={onToggleTheme}>
            {theme === "dark" ? "☀️ Light Mode" : "🌙 Dark Mode"}
          </button>
          <span className="user-chip">{adminUser}</span>
          <button className="btn btn-ghost" onClick={refreshAll} disabled={loading}>
            Refresh
          </button>
          <button className="btn btn-ghost" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </header>

      <section className="content-area">
        {error && <p className="error">{error}</p>}

        <section className="stats-grid">
          <DashboardCard title="Total Users" value={analytics.totalUsers ?? 0} hint="Registered platform users" />
          <DashboardCard title="Total Quizzes" value={analytics.totalQuizzes ?? 0} hint="Generated quiz records" />
          <DashboardCard title="Total Uploaded Files" value={analytics.totalUploadedFiles ?? 0} hint="Study materials in system" />
          <DashboardCard title="Average Quiz Score" value={`${analytics.averageQuizScore ?? 0}%`} hint="Overall platform performance" />
        </section>

        <section className="panel">
          <h2>User Management</h2>
          <p className="panel-subtitle">Total listed users: {sectionStats.users}</p>
          <div className="admin-list">
            {users.map((item) => (
              <article key={item.id} className="admin-row">
                <div>
                  <p className="admin-title">{item.username}</p>
                  <small>{item.email}</small>
                </div>
                <button className="btn btn-danger" onClick={() => deleteUser(item.id)}>
                  Delete
                </button>
              </article>
            ))}
          </div>
        </section>

        <section className="panel">
          <h2>Study Material Management</h2>
          <p className="panel-subtitle">Uploaded files: {sectionStats.documents}</p>
          <div className="admin-list">
            {documents.map((doc) => (
              <article key={doc.id} className="admin-row">
                <div>
                  <p className="admin-title">{doc.fileName}</p>
                  <small>{Math.round(doc.fileSize / 1024)} KB</small>
                </div>
                <button className="btn btn-danger" onClick={() => deleteDocument(doc.id)}>
                  Delete
                </button>
              </article>
            ))}
          </div>
        </section>

        <section className="panel">
          <h2>Quiz Management</h2>
          <p className="panel-subtitle">Generated quizzes: {sectionStats.quizzes}</p>
          <div className="admin-list">
            {quizzes.map((quiz) => (
              <article key={quiz.id} className="admin-row">
                <div>
                  <p className="admin-title">
                    {quiz.topic} ({quiz.difficulty})
                  </p>
                  <small>{quiz.questionCount} questions</small>
                </div>
                <button className="btn btn-danger" onClick={() => deleteQuiz(quiz.id)}>
                  Delete
                </button>
              </article>
            ))}
          </div>
        </section>

        <section className="panel">
          <h2>Leaderboard Monitoring</h2>
          <LeaderboardTable rows={leaderboard} />
        </section>

        <section className="panel">
          <h2>AI Generated Question Review</h2>
          <p className="panel-subtitle">Recent generated items: {sectionStats.questions}</p>
          <div className="admin-list">
            {questions.map((question) => (
              <article key={question.id} className="review-item">
                <h4>{question.question}</h4>
                <p>Quiz ID: {question.quizId}</p>
                <p className="ok">Correct Answer: {question.correctAnswer}</p>
              </article>
            ))}
          </div>
        </section>
      </section>
    </main>
  );
}

export default AdminPanel;
