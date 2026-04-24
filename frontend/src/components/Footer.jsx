// File Name: src/components/Footer.jsx

function Footer() {
  return (
    <footer className="landing-footer">
      <div className="footer-content">
        <div className="footer-left">
          <h2>AI Quiz Platform</h2>
          <p>
            Automated Knowledge Assessment and Dynamic Quiz Generation Platform
            built for intelligent academic improvement and faculty-ready demos.
          </p>
        </div>

        <div className="footer-right">
          <div className="footer-links">
            <h3>Project Details</h3>
            <p>Final Year Project</p>
            <p>Department of Computer Science</p>
            <p>College Name Placeholder</p>
            <p>Year: 2026</p>
          </div>

          <div className="footer-links">
            <h3>Academic Credits</h3>
            <p>Guide Faculty: [Name Placeholder]</p>
            <p>Team: [Student Team Placeholder]</p>
            <p>Technology Stack: React + Spring Boot</p>
            <p>AI Integration: Spring AI + RAG</p>
          </div>

          <div className="footer-links">
            <h3>Core Technologies</h3>
            <p>React</p>
            <p>Spring Boot</p>
            <p>PostgreSQL</p>
            <p>Gemini + Spring AI</p>
          </div>
        </div>
      </div>

      <div className="footer-bottom">
        <p>
          © 2026 AI Quiz Platform | Department of Computer Science | Faculty Demo Edition
        </p>
      </div>
    </footer>
  );
}

export default Footer;