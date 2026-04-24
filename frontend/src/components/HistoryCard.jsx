function HistoryCard({ item, onView }) {
  const percentage = Math.round((item.score / item.totalQuestions) * 100);
  return (
    <article className="history-card">
      <div>
        <p className="history-topic">{item.topic || "Quiz Attempt"}</p>
        <small>{new Date(item.date).toLocaleString()}</small>
      </div>
      <div className="history-metrics">
        <span>
          {item.score}/{item.totalQuestions}
        </span>
        <span>{percentage}%</span>
      </div>
      <button type="button" className="btn btn-ghost" onClick={() => onView(item)}>
        View Result
      </button>
    </article>
  );
}

export default HistoryCard;
