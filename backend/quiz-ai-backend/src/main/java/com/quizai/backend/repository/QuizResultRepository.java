package com.quizai.backend.repository;

import com.quizai.backend.model.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {

    List<QuizResult> findByUserIdOrderByDateDesc(Long userId);

    void deleteByUserId(Long userId);
}
