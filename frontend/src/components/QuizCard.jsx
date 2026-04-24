function QuizCard({
  question,
  questionNumber,
  selectedKey,
  onSelectKey,
  correctKey,
  isSubmitted
}) {
  const options = [
    { key: "optionA", label: question.optionA },
    { key: "optionB", label: question.optionB },
    { key: "optionC", label: question.optionC },
    { key: "optionD", label: question.optionD }
  ];

  const optionClass = (key) => {
    if (!isSubmitted) {
      return selectedKey === key ? "option-btn selected" : "option-btn";
    }
    if (key === correctKey) {
      return "option-btn correct";
    }
    if (selectedKey === key && key !== correctKey) {
      return "option-btn wrong";
    }
    return "option-btn";
  };

  return (
    <article className="quiz-card">
      <p className="question-index">Question {questionNumber}</p>
      <h3>{question.question}</h3>
      <div className="option-grid">
        {options.map((option) => (
          <button
            key={option.key}
            type="button"
            className={optionClass(option.key)}
            onClick={() => onSelectKey(option.key)}
            disabled={isSubmitted}
          >
            {option.label}
          </button>
        ))}
      </div>
    </article>
  );
}

export default QuizCard;
