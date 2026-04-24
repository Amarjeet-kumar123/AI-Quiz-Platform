function ResultSummary({ score, total, onRetry, onBack, onDownloadPdf, downloadingPdf, pdfError }) {
  const wrong = total - score;
  const accuracy = total > 0 ? Math.round((score / total) * 100) : 0;

  return (
    <section className="result-layout">
      <div className="result-metric">
        <p>Score</p>
        <h2>
          {score} / {total}
        </h2>
      </div>
      <div className="result-grid">
        <article className="mini-card">
          <p>Accuracy</p>
          <strong>{accuracy}%</strong>
        </article>
        <article className="mini-card good">
          <p>Correct</p>
          <strong>{score}</strong>
        </article>
        <article className="mini-card bad">
          <p>Wrong</p>
          <strong>{wrong}</strong>
        </article>
      </div>
      <div className="action-row">
        <button type="button" className="btn" onClick={onRetry}>
          Retry Quiz
        </button>
        <button type="button" className="btn" onClick={onDownloadPdf} disabled={downloadingPdf}>
          {downloadingPdf ? "Preparing PDF..." : "Download PDF Report"}
        </button>
        <button type="button" className="btn btn-ghost" onClick={onBack}>
          Back to Dashboard
        </button>
      </div>
      {pdfError && <p className="error">{pdfError}</p>}
    </section>
  );
}

export default ResultSummary;
