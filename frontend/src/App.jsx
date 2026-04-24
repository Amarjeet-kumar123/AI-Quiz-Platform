import { useEffect, useState } from "react";
import Navbar from "./components/Navbar";
import Sidebar from "./components/Sidebar";
import DashboardCard from "./components/DashboardCard";
import QuizCard from "./components/QuizCard";
import ResultSummary from "./components/ResultSummary";
import LeaderboardTable from "./components/LeaderboardTable";
import UploadForm from "./components/UploadForm";
import HistoryCard from "./components/HistoryCard";
import LandingPage from "./components/LandingPage";
import AdminPanel from "./components/AdminPanel";
import "./landing.css";

const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8081";
const TOKEN_KEY = "quiz_ai_token";
const USER_KEY = "quiz_ai_user";
const REVIEW_KEY = "quiz_attempt_reviews";
const THEME_KEY = "quiz_ai_theme";
const TIME_PER_QUESTION_SECONDS = 60;
const HEALTH_CHECK_INTERVAL_MS = 15000;

const initialQuizForm = {
  topic: "Java",
  difficulty: "Easy",
  numberOfQuestions: 5
};

function App() {
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY));
  const [theme, setTheme] = useState(() => localStorage.getItem(THEME_KEY) || "light");
  const [backendStatus, setBackendStatus] = useState({
    connected: true,
    checked: false
  });

  const [user, setUser] = useState(() => {
    return parseStoredUser(localStorage.getItem(USER_KEY));
  });

  const [showAuth, setShowAuth] = useState(false);
  const [showAdmin, setShowAdmin] = useState(false);
  const [authMode, setAuthMode] = useState("login");
  const [activePage, setActivePage] = useState("dashboard");

  const logout = () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);

    setToken(null);
    setUser(null);
    setShowAuth(false);
    setShowAdmin(false);
    setActivePage("dashboard");
  };

  const onAuthSuccess = (payload) => {
    localStorage.setItem(TOKEN_KEY, payload.token);

    const nextUser = {
      userId: payload.userId,
      username: payload.username,
      email: payload.email
    };

    localStorage.setItem(USER_KEY, JSON.stringify(nextUser));

    setToken(payload.token);
    setUser(nextUser);
    setShowAuth(false);
    setActivePage("dashboard");
  };

  useEffect(() => {
    if (token && !user) {
      logout();
    }
  }, [token, user]);

  useEffect(() => {
    document.body.setAttribute("data-theme", theme);
    localStorage.setItem(THEME_KEY, theme);
  }, [theme]);

  const toggleTheme = () => {
    setTheme((prev) => (prev === "dark" ? "light" : "dark"));
  };

  useEffect(() => {
    let active = true;

    const checkBackendHealth = async () => {
      try {
        const response = await fetch(`${API_BASE}/api/leaderboard`, {
          method: "GET"
        });
        if (!active) return;
        setBackendStatus({
          connected: response.ok,
          checked: true
        });
      } catch {
        if (!active) return;
        setBackendStatus({
          connected: false,
          checked: true
        });
      }
    };

    checkBackendHealth();
    const intervalId = setInterval(checkBackendHealth, HEALTH_CHECK_INTERVAL_MS);

    return () => {
      active = false;
      clearInterval(intervalId);
    };
  }, []);

  // Landing Page First
  if (!token && !showAuth && !showAdmin) {
    return (
      <>
        <BackendStatusBanner status={backendStatus} />
        <LandingPage
          theme={theme}
          onToggleTheme={toggleTheme}
          onGetStarted={() => {
            setAuthMode("login");
            setShowAuth(true);
            setShowAdmin(false);
          }}
          onAdminLogin={() => {
            setShowAdmin(true);
            setShowAuth(false);
          }}
        />
      </>
    );
  }

  if (!token && showAdmin) {
    return (
      <>
        <BackendStatusBanner status={backendStatus} />
        <AdminPanel
          apiBase={API_BASE}
          onBackToLanding={() => setShowAdmin(false)}
          theme={theme}
          onToggleTheme={toggleTheme}
        />
      </>
    );
  }

  // Login / Register Page
  if (!token && showAuth) {
    return (
      <main className="auth-screen">
        <BackendStatusBanner status={backendStatus} />
        <AuthPanel
          theme={theme}
          onToggleTheme={toggleTheme}
          authMode={authMode}
          onAuthMode={setAuthMode}
          onSuccess={onAuthSuccess}
          onBack={() => {
            setShowAuth(false);
            setShowAdmin(false);
          }}
        />
      </main>
    );
  }

  // Main Dashboard After Login
  return (
    <main className="app-shell">
      <BackendStatusBanner status={backendStatus} />
      <Navbar
        username={user?.username || "User"}
        onLogout={logout}
        theme={theme}
        onToggleTheme={toggleTheme}
      />

      <div className="app-body">
        <Sidebar
          activePage={activePage}
          onChangePage={setActivePage}
          onLogout={logout}
        />

        <section className="content-area">
          {activePage === "dashboard" && (
            <DashboardPage
              user={user}
              onNavigate={setActivePage}
            />
          )}

          {activePage === "generate" && (
            <QuizGenerationPage
              user={user}
              onNavigate={setActivePage}
            />
          )}

          {activePage === "leaderboard" && (
            <LeaderboardPage />
          )}

          {activePage === "history" && (
            <QuizHistoryPage user={user} />
          )}

          {activePage === "upload" && (
            <DocumentUploadPage />
          )}
        </section>
      </div>
    </main>
  );
}

async function apiRequest(path, options = {}) {
  const { method = "GET", body, auth = true, isFormData = false } = options;
  const headers = {};
  if (!isFormData) {
    headers["Content-Type"] = "application/json";
  }
  if (auth) {
    const token = localStorage.getItem(TOKEN_KEY);
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }
  }

  try {
    const response = await fetch(`${API_BASE}${path}`, {
      method,
      headers,
      body: body == null ? undefined : isFormData ? body : JSON.stringify(body)
    });
    const data = await safeJson(response);
    if (!response.ok) {
      throw new Error(
        data?.reason || data?.message || `Request failed with status ${response.status}`
      );
    }
    return data;
  } catch (error) {
    if (error instanceof TypeError) {
      throw new Error(
        "Unable to connect to server. Please ensure backend is running on http://localhost:8081."
      );
    }
    throw error;
  }
}

function AuthPanel({ authMode, onAuthMode, onSuccess, onBack, theme, onToggleTheme }) {
  const [form, setForm] = useState({ username: "", email: "", password: "" });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const submit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError("");
    try {
      const endpoint = authMode === "login" ? "/api/auth/login" : "/api/auth/register";
      const body =
        authMode === "login"
          ? { email: form.email, password: form.password }
          : { username: form.username, email: form.email, password: form.password };
      const data = await apiRequest(endpoint, { method: "POST", auth: false, body });
      onSuccess(data);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="auth-card">
      <button type="button" className="theme-toggle auth-theme-toggle" onClick={onToggleTheme}>
        {theme === "dark" ? "☀️ Light Mode" : "🌙 Dark Mode"}
      </button>
      <h1>AI Quiz Platform</h1>
      <p>Sign in to continue your AI-powered learning journey.</p>
      <button type="button" className="text-link" onClick={onBack}>
        Back to landing page
      </button>
      <div className="auth-tabs">
        <button type="button" className={authMode === "login" ? "tab active" : "tab"} onClick={() => onAuthMode("login")}>
          Login
        </button>
        <button type="button" className={authMode === "register" ? "tab active" : "tab"} onClick={() => onAuthMode("register")}>
          Register
        </button>
      </div>
      <form className="form-grid" onSubmit={submit}>
        {authMode === "register" && (
          <label>
            Username
            <input
              value={form.username}
              onChange={(e) => setForm((prev) => ({ ...prev, username: e.target.value }))}
              required
            />
          </label>
        )}
        <label>
          Email
          <input
            type="email"
            value={form.email}
            onChange={(e) => setForm((prev) => ({ ...prev, email: e.target.value }))}
            required
          />
        </label>
        <label>
          Password
          <input
            type="password"
            value={form.password}
            onChange={(e) => setForm((prev) => ({ ...prev, password: e.target.value }))}
            required
          />
        </label>
        {error && <p className="error">{error}</p>}
        <button className="btn" type="submit" disabled={loading}>
          {loading ? "Please wait..." : authMode === "login" ? "Login" : "Create account"}
        </button>
      </form>
    </section>
  );
}

function DashboardPage({ user, onNavigate }) {
  const [history, setHistory] = useState([]);
  const [docs, setDocs] = useState([]);
  const [historyFilter, setHistoryFilter] = useState("all");
  const [weaknessAnalytics, setWeaknessAnalytics] = useState({
    topicPerformance: [],
    subtopicInsights: [],
    weakAreas: [],
    strongAreas: [],
    recommendedNextTopics: [],
    personalizedSuggestions: [],
    suggestedNextDifficulty: "Continue practicing Easy difficulty first."
  });

  useEffect(() => {
    const load = async () => {
      try {
        const [historyRes, docRes, analyticsRes] = await Promise.all([
          apiRequest(`/api/quiz-history/${user.userId}`),
          apiRequest("/api/documents"),
          apiRequest(`/api/analytics/weakness/${user.userId}`)
        ]);
        setHistory(Array.isArray(historyRes) ? historyRes : []);
        setDocs(Array.isArray(docRes) ? docRes : []);
        setWeaknessAnalytics(analyticsRes || {});
      } catch {
        setHistory([]);
        setDocs([]);
        setWeaknessAnalytics({
          topicPerformance: [],
          subtopicInsights: [],
          weakAreas: [],
          strongAreas: [],
          recommendedNextTopics: [],
          personalizedSuggestions: [],
          suggestedNextDifficulty: "Continue practicing Easy difficulty first."
        });
      }
    };
    load();
  }, [user.userId]);

  const bestScore = history.length ? Math.max(...history.map((x) => x.score)) : 0;
  const avg =
    history.length > 0
      ? Math.round(
          history.reduce((sum, x) => sum + (x.score / x.totalQuestions) * 100, 0) / history.length
        )
      : 0;
  const lastFive = history.slice(0, 5).reverse();
  const avgAccuracy =
    history.length > 0
      ? Math.round(history.reduce((sum, x) => sum + Math.round((x.score / x.totalQuestions) * 100), 0) / history.length)
      : 0;
  const weakCount = (weaknessAnalytics.weakAreas || []).length;
  const strongCount = (weaknessAnalytics.strongAreas || []).length;
  const nextTopic = weaknessAnalytics.recommendedNextTopics?.[0] || "Start a quiz to unlock recommendation";

  const filteredHistory = history.filter((item) => {
    if (historyFilter === "all") return true;
    const percent = Math.round((item.score / item.totalQuestions) * 100);
    if (historyFilter === "high") return percent >= 75;
    if (historyFilter === "mid") return percent >= 50 && percent < 75;
    return percent < 50;
  });

  return (
    <>
      <div className="page-header">
        <h2>Welcome back, {user.username}</h2>
        <p>Track your learning performance and create AI-powered quizzes.</p>
      </div>
      <section className="stats-grid">
        <DashboardCard title="Total Quizzes Attempted" value={history.length} hint="All attempts saved" icon="🧠" tone="primary" />
        <DashboardCard title="Best Score" value={bestScore} hint="Highest correct answers" icon="🏅" tone="success" />
        <DashboardCard title="Average Score" value={`${avg}%`} hint="Score trend across attempts" icon="📊" tone="primary" />
        <DashboardCard title="Accuracy %" value={`${avgAccuracy}%`} hint="Correctness consistency" icon="🎯" tone="primary" />
        <DashboardCard title="Weak Areas" value={weakCount} hint="Detected weak subtopics" icon="⚠️" tone="danger" />
        <DashboardCard title="Strong Areas" value={strongCount} hint="Performing concepts" icon="✅" tone="success" />
        <DashboardCard title="Recommended Next Topic" value={nextTopic} hint="AI-guided next learning target" icon="🧭" tone="primary" />
        <DashboardCard title="Documents Uploaded" value={docs.length} hint="RAG-ready study materials" icon="📚" tone="primary" />
      </section>
      <section className="panel">
        <h3>Quick Actions</h3>
        <div className="quick-actions">
          <button className="btn" onClick={() => onNavigate("generate")}>Generate New Quiz</button>
          <button className="btn btn-ghost" onClick={() => onNavigate("upload")}>Upload Study Material</button>
          <button className="btn btn-ghost" onClick={() => onNavigate("leaderboard")}>View Leaderboard</button>
        </div>
      </section>
      <section className="panel">
        <h3>Performance Visualization</h3>
        {history.length === 0 ? (
          <div className="empty-state">
            <h4>Start your first smart quiz to unlock performance insights</h4>
            <p>Your analytics charts and trends will appear here after your first attempt.</p>
            <button className="btn" onClick={() => onNavigate("generate")}>Generate First Quiz</button>
          </div>
        ) : (
          <div className="analytics-grid">
            <article className="analytics-card">
              <h4>Topic-wise Performance</h4>
              <div className="chart-bars">
                {(weaknessAnalytics.topicPerformance || []).slice(0, 6).map((item) => (
                  <div key={item.topic} className="chart-row">
                    <span>{item.topic}</span>
                    <div className="chart-track">
                      <div className="chart-fill" style={{ width: `${item.accuracyPercentage}%` }} />
                    </div>
                    <strong>{item.accuracyPercentage}%</strong>
                  </div>
                ))}
              </div>
            </article>
            <article className="analytics-card">
              <h4>Last 5 Quiz Accuracy Trend</h4>
              <div className="trend-bars">
                {lastFive.map((attempt) => {
                  const percentage = Math.round((attempt.score / attempt.totalQuestions) * 100);
                  return (
                    <div key={attempt.id} className="trend-col">
                      <div className="trend-stick-wrap">
                        <div className="trend-stick" style={{ height: `${Math.max(percentage, 8)}%` }} />
                      </div>
                      <small>{percentage}%</small>
                    </div>
                  );
                })}
              </div>
            </article>
            <article className="analytics-card">
              <h4>Strong vs Weak Comparison</h4>
              <div className="chart-row">
                <span>Strong topics</span>
                <div className="chart-track">
                  <div className="chart-fill success" style={{ width: `${Math.min(strongCount * 20, 100)}%` }} />
                </div>
                <strong>{strongCount}</strong>
              </div>
              <div className="chart-row">
                <span>Weak topics</span>
                <div className="chart-track">
                  <div className="chart-fill danger" style={{ width: `${Math.min(weakCount * 20, 100)}%` }} />
                </div>
                <strong>{weakCount}</strong>
              </div>
            </article>
          </div>
        )}
      </section>
      <section className="panel">
        <h3>AI Weakness Analyzer</h3>
        <p className="panel-subtitle">Intelligent diagnosis based on your quiz attempts and repeated mistakes.</p>
        <div className="analytics-grid">
          <article className="analytics-card">
            <h4>Topic-wise Performance</h4>
            {(weaknessAnalytics.topicPerformance || []).length === 0 ? (
              <p className="panel-subtitle">Not enough data yet.</p>
            ) : (
              <div className="analytics-list">
                {weaknessAnalytics.topicPerformance.map((item) => (
                  <div key={item.topic} className="analytics-row">
                    <span>{item.topic}</span>
                    <strong>{item.accuracyPercentage}%</strong>
                  </div>
                ))}
              </div>
            )}
          </article>
          <article className="analytics-card">
            <h4>Suggested Next Difficulty</h4>
            <p>{weaknessAnalytics.suggestedNextDifficulty}</p>
            <h4>Recommended Next Topics</h4>
            <ul className="analytics-bullets">
              {(weaknessAnalytics.recommendedNextTopics || []).map((item) => (
                <li key={item}>{item}</li>
              ))}
            </ul>
          </article>
        </div>
        <div className="analytics-grid">
          <article className="analytics-card">
            <h4>Weak Areas</h4>
            <ul className="analytics-bullets">
              {(weaknessAnalytics.weakAreas || []).map((item) => (
                <li key={item}>{item}</li>
              ))}
            </ul>
          </article>
          <article className="analytics-card">
            <h4>Strong Areas</h4>
            <ul className="analytics-bullets">
              {(weaknessAnalytics.strongAreas || []).map((item) => (
                <li key={item}>{item}</li>
              ))}
            </ul>
          </article>
        </div>
        <article className="analytics-card">
          <h4>Personalized Improvement Suggestions</h4>
          <ul className="analytics-bullets">
            {(weaknessAnalytics.personalizedSuggestions || []).map((item) => (
              <li key={item}>{item}</li>
            ))}
          </ul>
        </article>
        <article className="analytics-card">
          <h4>Subtopic Insights</h4>
          {(weaknessAnalytics.subtopicInsights || []).length === 0 ? (
            <p className="panel-subtitle">Attempt more quizzes to unlock subtopic detection.</p>
          ) : (
            <div className="analytics-list">
              {weaknessAnalytics.subtopicInsights.slice(0, 8).map((item) => (
                <div key={`${item.topic}-${item.subtopic}`} className="analytics-row">
                  <span>{item.topic} - {item.subtopic}</span>
                  <strong>{item.level} ({item.accuracyPercentage}%)</strong>
                </div>
              ))}
            </div>
          )}
        </article>
      </section>
      <section className="panel">
        <div className="history-head">
          <h3>Quiz History</h3>
          <select value={historyFilter} onChange={(event) => setHistoryFilter(event.target.value)}>
            <option value="all">All Attempts</option>
            <option value="high">High (75%+)</option>
            <option value="mid">Medium (50-74%)</option>
            <option value="low">Low (&lt;50%)</option>
          </select>
        </div>
        {filteredHistory.length === 0 ? (
          <div className="empty-state compact">
            <h4>No attempts in this filter yet.</h4>
            <p>Try generating a new quiz to build your analytics profile.</p>
          </div>
        ) : (
          <div className="history-table-wrap">
            <table className="pro-table">
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Score</th>
                  <th>Accuracy</th>
                </tr>
              </thead>
              <tbody>
                {filteredHistory.slice(0, 12).map((item) => {
                  const percentage = Math.round((item.score / item.totalQuestions) * 100);
                  return (
                    <tr key={item.id}>
                      <td>{new Date(item.date).toLocaleString()}</td>
                      <td>{item.score}/{item.totalQuestions}</td>
                      <td>{percentage}%</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </>
  );
}

function QuizGenerationPage({ user, onNavigate }) {
  const [form, setForm] = useState(initialQuizForm);
  const [questions, setQuestions] = useState([]);
  const [quizId, setQuizId] = useState(null);
  const [selected, setSelected] = useState({});
  const [current, setCurrent] = useState(0);
  const [loading, setLoading] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [score, setScore] = useState(0);
  const [timeLeft, setTimeLeft] = useState(0);
  const [error, setError] = useState("");
  const [latestResultId, setLatestResultId] = useState(null);
  const [downloadingPdf, setDownloadingPdf] = useState(false);
  const [pdfError, setPdfError] = useState("");
  const [explanationLoading, setExplanationLoading] = useState(false);
  const [explanationError, setExplanationError] = useState("");
  const [explanationsByIndex, setExplanationsByIndex] = useState({});
  const [expandedExplanation, setExpandedExplanation] = useState({});

  const total = questions.length;
  const currentQuestion = questions[current];
  const progress = total ? Math.round(((current + 1) / total) * 100) : 0;
  const answeredCount = Object.keys(selected).length;

  useEffect(() => {
    if (!total || submitted || timeLeft <= 0) return;
    const id = setInterval(() => setTimeLeft((x) => Math.max(0, x - 1)), 1000);
    return () => clearInterval(id);
  }, [total, submitted, timeLeft]);

  useEffect(() => {
    if (total > 0 && !submitted && timeLeft === 0) {
      submitQuiz();
    }
  }, [timeLeft, total, submitted]);

  const generateQuiz = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError("");
    setSubmitted(false);
    setSelected({});
    setQuestions([]);
    setLatestResultId(null);
    setPdfError("");
    setExplanationsByIndex({});
    setExpandedExplanation({});
    setExplanationError("");
    try {
      const data = await apiRequest("/api/quiz/generate", { method: "POST", body: form });
      const list = Array.isArray(data.questions) ? data.questions : [];
      setQuizId(data.quizId);
      setQuestions(list);
      setCurrent(0);
      setTimeLeft(list.length * TIME_PER_QUESTION_SECONDS);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  const submitQuiz = async () => {
    if (!total || submitted) return;
    let correct = 0;
    questions.forEach((q, i) => {
      const key = String(q.id ?? i);
      if (isAnswerCorrect(q, selected[key])) correct += 1;
    });
    setScore(correct);
    setSubmitted(true);
    setPdfError("");

    try {
      const saved = await apiRequest("/api/quiz-results", {
        method: "POST",
        body: {
          userId: user.userId,
          score: correct,
          totalQuestions: total
        }
      });
      if (saved?.id) {
        setLatestResultId(saved.id);
        const answerReview = buildAnswerReview(questions, selected);
        const performanceSummary = derivePerformanceSummary(answerReview);
        const improvementSuggestions = deriveImprovementSuggestions(answerReview);

        persistReview(saved.id, {
          id: saved.id,
          date: saved.date,
          topic: form.topic,
          difficulty: form.difficulty,
          score: correct,
          totalQuestions: total,
          questions,
          selected
        });

        await apiRequest(`/api/quiz-results/${saved.id}/report`, {
          method: "POST",
          body: {
            topic: form.topic,
            difficulty: form.difficulty,
            numberOfQuestions: total,
            score: correct,
            accuracyPercentage: total > 0 ? Math.round((correct / total) * 100) : 0,
            takenAt: new Date().toISOString(),
            answerReview,
            performanceSummary,
            improvementSuggestions
          }
        });
      }
    } catch (submitError) {
      setPdfError(submitError.message || "Quiz saved, but detailed PDF report is unavailable for this attempt.");
      // keep UI flow stable even if save fails
    }
  };

  const retryQuiz = () => {
    setSelected({});
    setSubmitted(false);
    setScore(0);
    setCurrent(0);
    setTimeLeft(total * TIME_PER_QUESTION_SECONDS);
    setLatestResultId(null);
    setPdfError("");
    setExplanationsByIndex({});
    setExpandedExplanation({});
    setExplanationError("");
  };

  useEffect(() => {
    if (!submitted || !questions.length) return;

    const fetchExplanations = async () => {
      setExplanationLoading(true);
      setExplanationError("");
      try {
        const items = questions.map((q, idx) => {
          const key = String(q.id ?? idx);
          const chosenKey = selected[key];
          const correctKey = normalizeCorrectAnswer(q.correctAnswer);
          const chosenText = optionTextByKey(q, chosenKey) || "Not answered";
          const correctText = optionTextByKey(q, correctKey) || String(q.correctAnswer || "");
          return {
            questionIndex: idx,
            question: q.question,
            optionA: q.optionA,
            optionB: q.optionB,
            optionC: q.optionC,
            optionD: q.optionD,
            selectedAnswer: chosenText,
            correctAnswer: correctText,
            status: isAnswerCorrect(q, chosenKey) ? "Correct" : "Wrong"
          };
        });

        const response = await apiRequest("/api/quiz/explanation", {
          method: "POST",
          body: {
            topic: form.topic,
            difficulty: form.difficulty,
            items
          }
        });
        const next = {};
        const rows = Array.isArray(response?.items) ? response.items : [];
        rows.forEach((item) => {
          if (typeof item.questionIndex === "number") {
            next[item.questionIndex] = {
              explanation: item.explanation,
              wrongOptionExplanation: item.wrongOptionExplanation
            };
          }
        });
        setExplanationsByIndex(next);
      } catch (fetchError) {
        setExplanationError(fetchError.message || "AI explanation is temporarily unavailable.");
      } finally {
        setExplanationLoading(false);
      }
    };

    fetchExplanations();
  }, [submitted, questions, selected, form.topic, form.difficulty]);

  const downloadPdfReport = async () => {
    if (!latestResultId) {
      setPdfError("Report is not ready yet. Submit the quiz to generate a PDF report.");
      return;
    }

    setDownloadingPdf(true);
    setPdfError("");
    try {
      const response = await fetch(`${API_BASE}/api/quiz/result/${latestResultId}/pdf`);
      if (!response.ok) {
        const payload = await safeJson(response);
        throw new Error(payload?.reason || payload?.message || "Unable to generate PDF report.");
      }

      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = `quiz-result-${latestResultId}.pdf`;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (downloadError) {
      setPdfError(downloadError.message || "Unable to download PDF report at the moment.");
    } finally {
      setDownloadingPdf(false);
    }
  };

  const timeLabel = formatTime(timeLeft);

  return (
    <>
      <section className="panel">
        <h2>Generate Quiz</h2>
        <form className="form-grid three-cols" onSubmit={generateQuiz}>
          <label>
            Topic
            <input value={form.topic} onChange={(e) => setForm((p) => ({ ...p, topic: e.target.value }))} required />
          </label>
          <label>
            Difficulty
            <select value={form.difficulty} onChange={(e) => setForm((p) => ({ ...p, difficulty: e.target.value }))}>
              <option>Easy</option>
              <option>Medium</option>
              <option>Hard</option>
            </select>
          </label>
          <label>
            Number of Questions
            <input
              type="number"
              min="1"
              max="20"
              value={form.numberOfQuestions}
              onChange={(e) => setForm((p) => ({ ...p, numberOfQuestions: Number(e.target.value) }))}
            />
          </label>
          <button className="btn" disabled={loading}>{loading ? "Generating..." : "Generate Quiz"}</button>
        </form>
        {error && <p className="error">{error}</p>}
      </section>

      {total > 0 && !submitted && (
        <section className="panel">
          <div className="quiz-toolbar">
            <span>Quiz #{quizId}</span>
            <span>{`Question ${current + 1} of ${total}`}</span>
            <span className="timer">Time Left: {timeLabel}</span>
          </div>
          <div className="progress-wrap">
            <div className="progress-bar" style={{ width: `${progress}%` }} />
          </div>
          <QuizCard
            question={currentQuestion}
            questionNumber={current + 1}
            selectedKey={selected[String(currentQuestion.id ?? current)]}
            correctKey={normalizeCorrectAnswer(currentQuestion.correctAnswer)}
            onSelectKey={(optionKey) =>
              setSelected((prev) => ({ ...prev, [String(currentQuestion.id ?? current)]: optionKey }))
            }
            isSubmitted={submitted}
          />
          <div className="action-row">
            <button className="btn btn-ghost" onClick={() => setCurrent((c) => Math.max(0, c - 1))} disabled={current === 0}>Previous</button>
            <button className="btn btn-ghost" onClick={() => setCurrent((c) => Math.min(total - 1, c + 1))} disabled={current === total - 1}>Next</button>
            <button className="btn" onClick={submitQuiz} disabled={answeredCount !== total}>Submit Quiz</button>
          </div>
        </section>
      )}

      {submitted && (
        <section className="panel">
          <ResultSummary
            score={score}
            total={total}
            onRetry={retryQuiz}
            onBack={() => onNavigate("dashboard")}
            onDownloadPdf={downloadPdfReport}
            downloadingPdf={downloadingPdf}
            pdfError={pdfError}
          />
          <h3>Question Review</h3>
          {explanationLoading && (
            <div className="loading-block">
              <span className="loading-dot" />
              <span className="loading-dot" />
              <span className="loading-dot" />
              <p>Generating AI explanations...</p>
            </div>
          )}
          {explanationError && <p className="error">{explanationError}</p>}
          <div className="review-list">
            {questions.map((q, idx) => {
              const key = String(q.id ?? idx);
              const chosenKey = selected[key];
              const correctKey = normalizeCorrectAnswer(q.correctAnswer);
              const chosenText = optionTextByKey(q, chosenKey);
              const correctText = optionTextByKey(q, correctKey);
              const explanation = explanationsByIndex[idx];
              const isExpanded = Boolean(expandedExplanation[idx]);
              return (
                <article key={key} className="review-item">
                  <h4>{idx + 1}. {q.question}</h4>
                  <p className="ok">Correct: {correctText || q.correctAnswer}</p>
                  <p className={isAnswerCorrect(q, chosenKey) ? "ok" : "bad"}>
                    Your Answer: {chosenText || "Not answered"}
                  </p>
                  <p className={isAnswerCorrect(q, chosenKey) ? "ok" : "bad"}>
                    Status: {isAnswerCorrect(q, chosenKey) ? "Correct" : "Wrong"}
                  </p>
                  <button
                    type="button"
                    className="btn btn-ghost explanation-toggle"
                    onClick={() => setExpandedExplanation((prev) => ({ ...prev, [idx]: !prev[idx] }))}
                  >
                    {isExpanded ? "Hide AI Explanation" : "Show AI Explanation"}
                  </button>
                  {isExpanded && (
                    <div className="explanation-box">
                      <p>
                        <strong>Why this answer is correct:</strong>{" "}
                        {explanation?.explanation || "Explanation is being prepared."}
                      </p>
                      <p>
                        <strong>Why other options are incorrect:</strong>{" "}
                        {explanation?.wrongOptionExplanation || "Comparison summary is being prepared."}
                      </p>
                    </div>
                  )}
                </article>
              );
            })}
          </div>
        </section>
      )}
    </>
  );
}

function LeaderboardPage() {
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const data = await apiRequest("/api/leaderboard");
        setRows(Array.isArray(data) ? data : []);
      } catch {
        setRows([]);
      }
      setLoading(false);
    };
    load();
  }, []);

  return (
    <section className="panel">
      <h2>Leaderboard</h2>
      {loading ? (
        <div className="loading-block">
          <span className="loading-dot" />
          <span className="loading-dot" />
          <span className="loading-dot" />
          <p>Loading leaderboard...</p>
        </div>
      ) : (
        <LeaderboardTable rows={rows} />
      )}
    </section>
  );
}

function QuizHistoryPage({ user }) {
  const [rows, setRows] = useState([]);
  const [selectedReview, setSelectedReview] = useState(null);

  useEffect(() => {
    const load = async () => {
      try {
        const data = await apiRequest(`/api/quiz-history/${user.userId}`);
        setRows(Array.isArray(data) ? data : []);
      } catch {
        setRows([]);
      }
    };
    load();
  }, [user.userId]);

  const openReview = (item) => {
    const saved = getSavedReview(item.id);
    setSelectedReview(saved ? { ...saved, date: item.date } : { missing: true, ...item });
  };

  return (
    <section className="panel">
      <h2>Quiz History</h2>
      <div className="history-list">
        {rows.map((item) => (
          <HistoryCard key={item.id} item={item} onView={openReview} />
        ))}
      </div>
      {selectedReview && (
        <div className="review-modal">
          <div className="panel">
            <h3>Attempt Details</h3>
            {selectedReview.missing ? (
              <p>Detailed review not available for this older attempt.</p>
            ) : (
              <>
                <p>{selectedReview.topic}</p>
                <p>
                  Score: {selectedReview.score}/{selectedReview.totalQuestions}
                </p>
              </>
            )}
            <button className="btn btn-ghost" onClick={() => setSelectedReview(null)}>Close</button>
          </div>
        </div>
      )}
    </section>
  );
}

function DocumentUploadPage() {
  const [docs, setDocs] = useState([]);
  const [uploading, setUploading] = useState(false);
  const [message, setMessage] = useState("");

  const loadDocs = async () => {
    try {
      const data = await apiRequest("/api/documents");
      setDocs(Array.isArray(data) ? data : []);
    } catch {
      setDocs([]);
    }
  };

  useEffect(() => {
    loadDocs();
  }, []);

  const uploadFile = async (file) => {
    setUploading(true);
    setMessage("");
    const formData = new FormData();
    formData.append("file", file);
    try {
      await apiRequest("/api/documents/upload", {
        method: "POST",
        body: formData,
        isFormData: true
      });
      setMessage("Document uploaded successfully.");
      await loadDocs();
    } catch (e) {
      setMessage(e.message);
    } finally {
      setUploading(false);
    }
  };

  return (
    <section className="panel">
      <h2>Upload Study Material</h2>
      <UploadForm onUpload={uploadFile} uploading={uploading} />
      {message && <p className="status">{message}</p>}
      <div className="doc-list">
        {docs.map((doc) => (
          <article key={doc.id} className="doc-item">
            <p>{doc.fileName}</p>
            <small>{new Date(doc.uploadedAt).toLocaleString()}</small>
            <small>{Math.round(doc.fileSize / 1024)} KB</small>
          </article>
        ))}
      </div>
    </section>
  );
}

function safeJson(response) {
  return response
    .text()
    .then((text) => {
      if (!text) return null;
      try {
        return JSON.parse(text);
      } catch {
        return null;
      }
    })
    .catch(() => null);
}

function formatTime(seconds) {
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return `${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
}

function persistReview(id, payload) {
  const raw = localStorage.getItem(REVIEW_KEY);
  const parsed = raw ? JSON.parse(raw) : {};
  parsed[id] = payload;
  localStorage.setItem(REVIEW_KEY, JSON.stringify(parsed));
}

function getSavedReview(id) {
  const raw = localStorage.getItem(REVIEW_KEY);
  const parsed = raw ? JSON.parse(raw) : {};
  return parsed[id] || null;
}

function normalizeCorrectAnswer(value) {
  const normalized = String(value || "").trim().toLowerCase();
  if (normalized === "optiona" || normalized === "a") return "optionA";
  if (normalized === "optionb" || normalized === "b") return "optionB";
  if (normalized === "optionc" || normalized === "c") return "optionC";
  if (normalized === "optiond" || normalized === "d") return "optionD";
  return normalized;
}

function optionTextByKey(question, key) {
  if (key === "optionA") return question.optionA;
  if (key === "optionB") return question.optionB;
  if (key === "optionC") return question.optionC;
  if (key === "optionD") return question.optionD;
  return null;
}

function isAnswerCorrect(question, selectedKey) {
  const correctKey = normalizeCorrectAnswer(question.correctAnswer);
  const correctText = optionTextByKey(question, correctKey) ?? String(question.correctAnswer || "");
  const selectedText = optionTextByKey(question, selectedKey) ?? String(selectedKey || "");
  return normalizeText(selectedText) === normalizeText(correctText);
}

function normalizeText(value) {
  return String(value || "").trim().toLowerCase();
}

function buildAnswerReview(questions, selected) {
  return questions.map((question, index) => {
    const key = String(question.id ?? index);
    const chosenKey = selected[key];
    const correctKey = normalizeCorrectAnswer(question.correctAnswer);
    const selectedAnswer = optionTextByKey(question, chosenKey) || "Not answered";
    const correctAnswer = optionTextByKey(question, correctKey) || String(question.correctAnswer || "");
    const status = isAnswerCorrect(question, chosenKey) ? "Correct" : "Wrong";
    return {
      question: question.question,
      selectedAnswer,
      correctAnswer,
      status
    };
  });
}

function derivePerformanceSummary(answerReview) {
  const strengths = new Set();
  const weaknesses = new Set();

  answerReview.forEach((item) => {
    const topic = classifyQuestionTopic(item.question);
    if (item.status === "Correct") {
      strengths.add(`Strong in ${topic}`);
    } else {
      weaknesses.add(`Needs improvement in ${topic}`);
    }
  });

  const summary = [...strengths, ...weaknesses];
  if (summary.length === 0) {
    return ["Consistent performance across attempted questions."];
  }
  return summary.slice(0, 6);
}

function deriveImprovementSuggestions(answerReview) {
  const improvements = new Set();

  answerReview
    .filter((item) => item.status !== "Correct")
    .forEach((item) => {
      const topic = classifyQuestionTopic(item.question);
      improvements.add(`Revise ${topic} and practice similar MCQs.`);
    });

  if (improvements.size === 0) {
    improvements.add("Keep practicing mixed-difficulty questions to maintain performance.");
  }

  return [...improvements].slice(0, 6);
}

function classifyQuestionTopic(questionText) {
  const q = String(questionText || "").toLowerCase();
  if (q.includes("exception")) return "Exception Handling";
  if (q.includes("polymorphism")) return "Polymorphism";
  if (q.includes("inheritance")) return "Inheritance";
  if (q.includes("encapsulation")) return "Encapsulation";
  if (q.includes("abstraction")) return "Abstraction";
  if (q.includes("interface")) return "Interfaces";
  if (q.includes("class") || q.includes("object")) return "Java OOP";
  if (q.includes("collection")) return "Collections Framework";
  if (q.includes("thread") || q.includes("concurrency")) return "Concurrency";
  return "Core Concepts";
}

function parseStoredUser(raw) {
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

function BackendStatusBanner({ status }) {
  if (!status.checked) {
    return null;
  }

  if (status.connected) {
    return (
      <div className="health-banner health-banner-ok" role="status" aria-live="polite">
        Backend Connected
      </div>
    );
  }

  return (
    <div className="health-banner health-banner-bad" role="alert" aria-live="assertive">
      Backend Not Running. Please start backend server on port 8081.
    </div>
  );
}

export default App;
