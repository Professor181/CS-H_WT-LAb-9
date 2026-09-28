package com.example.cricket.service;

import com.example.cricket.dto.TeamRequest;
import com.example.cricket.entity.Team;
import com.example.cricket.exception.ApiException;
import com.example.cricket.repository.TeamRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class TeamService {
    private final TeamRepository teams;
    public TeamService(TeamRepository teams) { this.teams = teams; }
    public List<Team> all() { return teams.findAll(); }
    public Team get(Long id) { return teams.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Team not found")); }
    public Team create(TeamRequest request) {
        if (teams.existsByNameIgnoreCase(request.name()) || teams.existsByShortNameIgnoreCase(request.shortName()))
            throw new ApiException(HttpStatus.CONFLICT, "Team name or short name already exists");
        Team team = new Team();
        copy(request, team);
        return teams.save(team);
    }
    public Team update(Long id, TeamRequest request) {
        Team team = get(id);
        boolean duplicate = teams.findAll().stream().anyMatch(other -> !other.id.equals(id)
                && (other.name.equalsIgnoreCase(request.name()) || other.shortName.equalsIgnoreCase(request.shortName())));
        if (duplicate) throw new ApiException(HttpStatus.CONFLICT, "Team name or short name already exists");
        copy(request, team);
        return teams.save(team);
    }
    public void delete(Long id) { teams.delete(get(id)); }
    private void copy(TeamRequest request, Team team) {
        team.name = request.name().trim(); team.shortName = request.shortName().trim().toUpperCase();
        team.country = request.country().trim(); team.coach = request.coach();
    }
}