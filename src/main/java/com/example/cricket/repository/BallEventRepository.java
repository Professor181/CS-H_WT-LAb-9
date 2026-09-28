package com.example.cricket.repository;

import com.example.cricket.entity.BallEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BallEventRepository extends JpaRepository<BallEvent, Long> {
    List<BallEvent> findByInningsIdOrderByIdDesc(Long inningsId);
}