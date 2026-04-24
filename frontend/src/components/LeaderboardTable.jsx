function LeaderboardTable({ rows }) {
  return (
    <table className="pro-table">
      <thead>
        <tr>
          <th>Rank</th>
          <th>Username</th>
          <th>Score</th>
          <th>Date</th>
        </tr>
      </thead>
      <tbody>
        {rows.map((row, index) => (
          <tr key={row.userId} className={index < 3 ? "top-rank" : ""}>
            <td>#{index + 1}</td>
            <td>{row.username}</td>
            <td>{row.bestScore}</td>
            <td>{row.date ? new Date(row.date).toLocaleDateString() : "-"}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

export default LeaderboardTable;
