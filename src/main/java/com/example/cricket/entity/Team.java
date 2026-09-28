package com.example.cricket.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teams", uniqueConstraints = @UniqueConstraint(columnNames = "short_name"))
public class Team {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false, unique = true) public String name;
    @Column(name = "short_name", nullable = false, unique = true, length = 8) public String shortName;
    @Column(nullable = false) public String country;
    public String coach;
    @Column(nullable = false) public LocalDateTime createdAt = LocalDateTime.now();
    @OneToMany(mappedBy = "team") public List<Player> players = new ArrayList<>();
}