package com.example.cricket.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "player_match_stats", uniqueConstraints = @UniqueConstraint(columnNames = {"match_id", "player_id"}))
public class PlayerMatchStats {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "match_id") public Match match;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "player_id") public Player player;
    @Column(nullable = false) public Integer runs = 0;
    @Column(nullable = false) public Integer ballsFaced = 0;
    @Column(nullable = false) public Integer fours = 0;
    @Column(nullable = false) public Integer sixes = 0;
    @Column(nullable = false) public Integer wickets = 0;
    @Column(nullable = false) public Integer runsConceded = 0;
    @Column(nullable = false) public Integer ballsBowled = 0;
    @Column(nullable = false) public Integer maidens = 0;
    @Column(nullable = false) public Integer catches = 0;
}