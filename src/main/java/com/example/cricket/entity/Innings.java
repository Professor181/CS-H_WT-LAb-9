package com.example.cricket.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "innings", uniqueConstraints = @UniqueConstraint(columnNames = {"match_id", "innings_number"}))
public class Innings {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) @JoinColumn(name = "match_id") public Match match;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) @JoinColumn(name = "batting_team_id") public Team battingTeam;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) @JoinColumn(name = "bowling_team_id") public Team bowlingTeam;
    @Column(nullable = false) public Integer inningsNumber;
    @Column(nullable = false) public Integer totalRuns = 0;
    @Column(nullable = false) public Integer wickets = 0;
    @Column(nullable = false) public Integer legalBalls = 0;
    @Column(nullable = false) public Boolean complete = false;
}