package com.example.cricket.repository;

import com.example.cricket.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    List<Player> findByTeamId(Long teamId);
    long countByTeamId(Long teamId);
    boolean existsByTeamIdAndJerseyNumber(Long teamId, Integer jerseyNumber);
}