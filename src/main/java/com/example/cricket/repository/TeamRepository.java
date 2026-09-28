package com.example.cricket.repository;

import com.example.cricket.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByShortNameIgnoreCase(String shortName);
}