package com.quizai.backend.repository;

import com.quizai.backend.model.QuizResultReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizResultReportRepository extends JpaRepository<QuizResultReport, Long> {

    Optional<QuizResultReport> findByQuizResultId(Long quizResultId);

    List<QuizResultReport> findByQuizResultIdIn(List<Long> quizResultIds);
}
