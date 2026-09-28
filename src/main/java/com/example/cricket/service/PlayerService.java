package com.example.cricket.service;

import com.example.cricket.dto.PlayerRequest;
import com.example.cricket.entity.Player;
import com.example.cricket.entity.Team;
import com.example.cricket.exception.ApiException;
import com.example.cricket.repository.PlayerRepository;
import com.example.cricket.repository.TeamRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class PlayerService {
    private final PlayerRepository players;
    private final TeamRepository teams;
    public PlayerService(PlayerRepository players, TeamRepository teams) { this.players = players; this.teams = teams; }
    public List<Player> all() { return players.findAll(); }
    public List<Player> byTeam(Long teamId) {
        if (!teams.existsById(teamId)) throw new ApiException(HttpStatus.NOT_FOUND, "Team not found");
        return players.findByTeamId(teamId);
    }
    public Player get(Long id) { return players.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Player not found")); }
    public Player create(PlayerRequest request) { return save(request, new Player()); }
    public Player update(Long id, PlayerRequest request) { return save(request, get(id)); }
    public void delete(Long id) { players.delete(get(id)); }
    private Player save(PlayerRequest request, Player player) {
        Team team = teams.findById(request.teamId()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Team not found"));
        if (request.jerseyNumber() != null && players.existsByTeamIdAndJerseyNumber(team.id, request.jerseyNumber())
                && (player.id == null || !player.team.id.equals(team.id) || !player.jerseyNumber.equals(request.jerseyNumber())))
            throw new ApiException(HttpStatus.CONFLICT, "Jersey number is already used by this team");
        player.name = request.name().trim(); player.role = request.role(); player.jerseyNumber = request.jerseyNumber();
        player.battingStyle = request.battingStyle(); player.bowlingStyle = request.bowlingStyle(); player.team = team;
        return players.save(player);
    }
}