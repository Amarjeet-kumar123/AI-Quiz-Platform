package com.quizai.backend.repository;

import com.quizai.backend.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByQuizId(Long quizId);

    void deleteByQuizId(Long quizId);

    List<Question> findTop30ByOrderByIdDesc();
}
