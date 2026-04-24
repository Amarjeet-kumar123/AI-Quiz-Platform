const menuItems = [
  { id: "dashboard", label: "Dashboard" },
  { id: "generate", label: "Generate Quiz" },
  { id: "upload", label: "Upload Study Material" },
  { id: "history", label: "Quiz History" },
  { id: "leaderboard", label: "Leaderboard" }
];

function Sidebar({ activePage, onChangePage, onLogout }) {
  return (
    <aside className="sidebar">
      <p className="sidebar-title">Learning Hub</p>

      <nav>
        {menuItems.map((item) => (
          <button
            key={item.id}
            type="button"
            className={
              activePage === item.id
                ? "sidebar-link active"
                : "sidebar-link"
            }
            onClick={() => onChangePage(item.id)}
          >
            {item.label}
          </button>
        ))}
      </nav>

      <button
        type="button"
        className="sidebar-link danger"
        onClick={onLogout}
      >
        Logout
      </button>
    </aside>
  );
}

export default Sidebar;