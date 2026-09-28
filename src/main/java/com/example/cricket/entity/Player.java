package com.example.cricket.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "players", uniqueConstraints = @UniqueConstraint(columnNames = {"team_id", "jersey_number"}))
public class Player {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false) public String name;
    @Enumerated(EnumType.STRING) @Column(nullable = false) public PlayerRole role;
    @Column(name = "jersey_number") public Integer jerseyNumber;
    public String battingStyle;
    public String bowlingStyle;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) @JoinColumn(name = "team_id") public Team team;
}