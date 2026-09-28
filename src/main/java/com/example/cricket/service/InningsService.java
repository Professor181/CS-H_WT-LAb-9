package com.example.cricket.service;

import com.example.cricket.dto.InningsRequest;
import com.example.cricket.entity.*;
import com.example.cricket.exception.ApiException;
import com.example.cricket.repository.InningsRepository;
import com.example.cricket.repository.MatchRepository;
import com.example.cricket.repository.TeamRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class InningsService {
    private final InningsRepository innings;
    private final MatchRepository matches;
    private final TeamRepository teams;
    public InningsService(InningsRepository innings, MatchRepository matches, TeamRepository teams) {
        this.innings = innings; this.matches = matches; this.teams = teams;
    }
    public List<Innings> forMatch(Long matchId) { requireMatch(matchId); return innings.findByMatchIdOrderByInningsNumber(matchId); }
    public Innings get(Long matchId, Long inningsId) {
        Innings item = innings.findById(inningsId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Innings not found"));
        if (!item.match.id.equals(matchId)) throw new ApiException(HttpStatus.NOT_FOUND, "Innings not found for this match");
        return item;
    }
    public Innings create(Long matchId, InningsRequest request) {
        Match match = requireMatch(matchId);
        if (match.status != MatchStatus.LIVE) throw new ApiException(HttpStatus.CONFLICT, "Start the match before creating an innings");
        Team batting = teams.findById(request.battingTeamId()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Team not found"));
        if (!batting.id.equals(match.team1.id) && !batting.id.equals(match.team2.id))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Batting team must belong to this match");
        Innings existing = innings.findByMatchIdAndInningsNumber(matchId, match.currentInnings).orElse(null);
        if (existing != null) {
            if (existing.battingTeam.id.equals(batting.id)) return existing;
            throw new ApiException(HttpStatus.CONFLICT, "Current innings already exists for another batting team");
        }
        Innings item = new Innings(); item.match = match; item.inningsNumber = match.currentInnings; item.battingTeam = batting;
        item.bowlingTeam = batting.id.equals(match.team1.id) ? match.team2 : match.team1;
        return innings.save(item);
    }
    public Innings getById(Long id) { return innings.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Innings not found")); }
    private Match requireMatch(Long id) { return matches.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Match not found")); }
}