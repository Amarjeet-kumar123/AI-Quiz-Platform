// File Name: src/components/LandingPage.jsx
// Final update → Footer integration

import Footer from "./Footer";

function LandingPage({ onGetStarted, onAdminLogin, theme, onToggleTheme }) {
  const scrollToFeatures = () => {
    const section = document.getElementById("features-section");
    if (section) {
      section.scrollIntoView({ behavior: "smooth" });
    }
  };

  return (
    <main className="landing-page">
      <div className="floating-orb orb-one" />
      <div className="floating-orb orb-two" />
      <div className="floating-orb orb-three" />

      <header className="landing-topbar">
        <div className="project-logo">
          <div className="logo-mark">AI</div>
          <div className="logo-text">
            <strong>AI Quiz Platform</strong>
            <small>Smart Academic Intelligence</small>
          </div>
        </div>
        <div className="landing-actions">
          <button type="button" className="theme-toggle" onClick={onToggleTheme}>
            {theme === "dark" ? "☀️ Light Mode" : "🌙 Dark Mode"}
          </button>
          <button
            className="admin-link-btn"
            type="button"
            onClick={onAdminLogin}
          >
            Admin Login
          </button>
        </div>
      </header>

      <section className="hero-section">
        <div className="hero-left">
          <p className="hero-badge">
            Premium Final Year Project Innovation
          </p>

          <h1>
            Automated Knowledge Assessment
            <br />
            and Dynamic Quiz Generation
          </h1>

          <p className="hero-description">
            Transform learning with AI quiz generation, weakness analysis,
            smart recommendations, and faculty-ready performance insights
            in one intelligent academic platform.
          </p>

          <div className="hero-buttons">
            <button
              className="hero-btn primary"
              onClick={onGetStarted}
            >
              Start Smart Learning
            </button>

            <button
              className="hero-btn secondary"
              onClick={scrollToFeatures}
            >
              Explore AI Features
            </button>
          </div>

          <div className="hero-stats">
            <div className="hero-stat-card">
              <span className="stat-icon">🧠</span>
              <h3>10K+</h3>
              <p>Generated Quizzes</p>
            </div>

            <div className="hero-stat-card">
              <span className="stat-icon">📚</span>
              <h3>5K+</h3>
              <p>Study Materials</p>
            </div>

            <div className="hero-stat-card">
              <span className="stat-icon">🎯</span>
              <h3>95%</h3>
              <p>Learning Accuracy</p>
            </div>
          </div>

          <div className="scroll-down">
            <button
              className="scroll-btn"
              onClick={scrollToFeatures}
            >
              Scroll for Innovation ↓
            </button>
          </div>
        </div>

        <div className="hero-right">
          <div className="hero-image-card">
            <h2>AI Quiz Platform Experience</h2>

            <p>
              AI Generated MCQs + Weakness Analyzer + Smart Recommendations
              + Study Material Intelligence + Faculty Demo Dashboard
            </p>

            <div className="mini-cards">
              <div className="mini-card">AI Quiz Generator</div>
              <div className="mini-card">Weakness Analyzer</div>
              <div className="mini-card">Performance Insights</div>
              <div className="mini-card">Smart Recommendations</div>
            </div>

            <div className="tech-stack-row">
              <span>React</span>
              <span>Spring Boot</span>
              <span>PostgreSQL</span>
              <span>Spring AI</span>
            </div>
          </div>
        </div>
      </section>

      <section
        id="features-section"
        className="features-section"
      >
        <h2>Core Intelligence Modules</h2>
        <p>
          Designed to deliver measurable learning improvement,
          not just quiz automation.
        </p>

        <div className="features-grid">
          <div className="feature-card">
            <h3>AI Quiz Generation</h3>
            <p>
              Generate adaptive quizzes using topic and difficulty
              with fast, production-safe API integration.
            </p>
          </div>

          <div className="feature-card">
            <h3>Study Material Intelligence</h3>
            <p>
              Upload PDF, DOC, DOCX, PPT, and TXT files and transform
              them into RAG-ready learning context.
            </p>
          </div>

          <div className="feature-card">
            <h3>Weakness Analyzer</h3>
            <p>
              Detect weak subtopics, identify strong concepts,
              and guide students with smart improvement strategy.
            </p>
          </div>

          <div className="feature-card">
            <h3>Faculty Demo Dashboard</h3>
            <p>
              Showcase leaderboard, analytics, PDF reports, and
              explanation-driven results in one professional interface.
            </p>
          </div>
        </div>
      </section>

      <Footer />
    </main>
  );
}

export default LandingPage;