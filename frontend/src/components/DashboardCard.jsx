function DashboardCard({ title, value, hint, icon = "📈", tone = "primary" }) {
  return (
    <article className={`stat-card stat-card-${tone}`}>
      <div className="stat-head">
        <span className="stat-icon-badge">{icon}</span>
        <p className="stat-title">{title}</p>
      </div>

      <h3>{value}</h3>

      <p className="stat-hint">
        {hint}
      </p>
    </article>
  );
}

export default DashboardCard;