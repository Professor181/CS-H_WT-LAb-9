package com.example.cricket.repository;

import com.example.cricket.entity.Match;
import com.example.cricket.entity.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findAllByOrderByMatchDateDesc();
    List<Match> findByStatusOrderByMatchDateDesc(MatchStatus status);
}