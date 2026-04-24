import { useEffect, useRef, useState } from "react";

function Navbar({ username, onLogout, theme, onToggleTheme }) {
  const [open, setOpen] = useState(false);
  const menuRef = useRef(null);

  useEffect(() => {
    const closeOutside = (event) => {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setOpen(false);
      }
    };
    document.addEventListener("mousedown", closeOutside);
    return () => document.removeEventListener("mousedown", closeOutside);
  }, []);

  return (
    <header className="topbar">
      <div>
        <h1>AI Quiz Platform</h1>
        <p>Automated Knowledge Assessment & Dynamic Quiz Generation</p>
      </div>

      <div className="topbar-actions" ref={menuRef}>
        <button type="button" className="theme-toggle" onClick={onToggleTheme}>
          {theme === "dark" ? "☀️ Light Mode" : "🌙 Dark Mode"}
        </button>
        <button
          type="button"
          className="user-chip"
          onClick={() => setOpen((prev) => !prev)}
          aria-expanded={open}
          aria-haspopup="menu"
        >
          {username}
          <span className="caret">{open ? "▲" : "▼"}</span>
        </button>
        {open && (
          <div className="user-dropdown" role="menu">
            <button
              type="button"
              className="dropdown-item"
              onClick={onLogout}
            >
              Logout
            </button>
          </div>
        )}
      </div>
    </header>
  );
}

export default Navbar;