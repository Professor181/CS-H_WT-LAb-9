package com.example.cricket.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ball_events")
public class BallEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) @JoinColumn(name = "innings_id") public Innings innings;
    public Integer overNumber;
    public Integer ballNumber;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "striker_id") public Player striker;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "non_striker_id") public Player nonStriker;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "bowler_id") public Player bowler;
    @Column(nullable = false) public Integer runs = 0;
    @Column(nullable = false) public Integer extras = 0;
    @Enumerated(EnumType.STRING) public ExtraType extraType = ExtraType.NONE;
    @Column(nullable = false) public Boolean wicket = false;
    @Enumerated(EnumType.STRING) public WicketType wicketType = WicketType.NONE;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "dismissed_player_id") public Player dismissedPlayer;
    @Column(nullable = false) public LocalDateTime timestamp = LocalDateTime.now();
}