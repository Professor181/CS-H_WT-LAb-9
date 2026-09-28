package com.example.cricket.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cricket_matches")
public class Match {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false) public String title;
    @Column(nullable = false) public String venue;
    public LocalDate matchDate;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) @JoinColumn(name = "team1_id") public Team team1;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) @JoinColumn(name = "team2_id") public Team team2;
    @Enumerated(EnumType.STRING) @Column(nullable = false) public MatchStatus status = MatchStatus.UPCOMING;
    public Integer currentInnings = 1;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "toss_winner_id") public Team tossWinner;
    public String tossDecision;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "winner_id") public Team winner;
    @Column(nullable = false) public LocalDateTime createdAt = LocalDateTime.now();
    public LocalDateTime updatedAt = LocalDateTime.now();
}