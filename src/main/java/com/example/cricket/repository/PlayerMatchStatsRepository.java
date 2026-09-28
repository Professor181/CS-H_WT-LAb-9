package com.example.cricket.repository;

import com.example.cricket.entity.PlayerMatchStats;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PlayerMatchStatsRepository extends JpaRepository<PlayerMatchStats, Long> {
    Optional<PlayerMatchStats> findByMatchIdAndPlayerId(Long matchId, Long playerId);
    List<PlayerMatchStats> findByMatchIdOrderByRunsDesc(Long matchId);
}