package com.example.cricket.service;

import com.example.cricket.dto.MatchRequest;
import com.example.cricket.entity.*;
import com.example.cricket.exception.ApiException;
import com.example.cricket.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class MatchService {
    private final MatchRepository matches;
    private final TeamRepository teams;
    private final InningsRepository innings;
    public MatchService(MatchRepository matches, TeamRepository teams, InningsRepository innings) {
        this.matches = matches; this.teams = teams; this.innings = innings;
    }
    public List<Match> all() { return matches.findAllByOrderByMatchDateDesc(); }
    public Match get(Long id) { return matches.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Match not found")); }
    public Match create(MatchRequest request) {
        Team one = team(request.team1Id()); Team two = team(request.team2Id());
        if (one.id.equals(two.id)) throw new ApiException(HttpStatus.BAD_REQUEST, "A match must have two different teams");
        Match match = new Match(); match.title = request.title().trim(); match.venue = request.venue().trim();
        match.matchDate = request.matchDate(); match.team1 = one; match.team2 = two;
        return matches.save(match);
    }
    public Match update(Long id, MatchRequest request) {
        Match match = get(id);
        if (match.status != MatchStatus.UPCOMING) throw new ApiException(HttpStatus.CONFLICT, "Only upcoming matches can be edited");
        Team one = team(request.team1Id()); Team two = team(request.team2Id());
        if (one.id.equals(two.id)) throw new ApiException(HttpStatus.BAD_REQUEST, "A match must have two different teams");
        match.title = request.title().trim(); match.venue = request.venue().trim(); match.matchDate = request.matchDate();
        match.team1 = one; match.team2 = two; match.updatedAt = LocalDateTime.now();
        return matches.save(match);
    }
    public void delete(Long id) {
        Match match = get(id);
        if (match.status != MatchStatus.UPCOMING) throw new ApiException(HttpStatus.CONFLICT, "Only upcoming matches can be deleted");
        matches.delete(match);
    }
    public Match start(Long id) {
        Match match = get(id);
        if (match.status != MatchStatus.UPCOMING) throw new ApiException(HttpStatus.CONFLICT, "Only upcoming matches can be started");
        match.status = MatchStatus.LIVE; match.currentInnings = 1; match.updatedAt = LocalDateTime.now();
        matches.save(match); createInnings(match, 1, match.team1);
        return match;
    }
    public Match nextInnings(Long id, Long battingTeamId) {
        Match match = get(id);
        if (match.status != MatchStatus.LIVE || match.currentInnings != 1)
            throw new ApiException(HttpStatus.CONFLICT, "The next innings can only start after the first innings");
        Team batting = team(battingTeamId);
        if (!batting.id.equals(match.team1.id) && !batting.id.equals(match.team2.id))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Batting team must belong to this match");
        innings.findByMatchIdAndInningsNumber(id, 1).ifPresent(first -> first.complete = true);
        match.currentInnings = 2; match.updatedAt = LocalDateTime.now();
        matches.save(match); createInnings(match, 2, batting);
        return match;
    }
    public Match complete(Long id) {
        Match match = get(id);
        if (match.status != MatchStatus.LIVE) throw new ApiException(HttpStatus.CONFLICT, "Only live matches can be completed");
        List<Innings> scores = innings.findByMatchIdOrderByInningsNumber(id);
        if (scores.size() == 2) {
            int first = scores.get(0).totalRuns; int second = scores.get(1).totalRuns;
            match.winner = first == second ? null : (first > second ? scores.get(0).battingTeam : scores.get(1).battingTeam);
            scores.forEach(item -> item.complete = true);
        }
        match.status = MatchStatus.COMPLETED; match.updatedAt = LocalDateTime.now();
        return matches.save(match);
    }
    private Team team(Long id) { return teams.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Team not found")); }
    private void createInnings(Match match, int number, Team batting) {
        Innings item = new Innings(); item.match = match; item.inningsNumber = number; item.battingTeam = batting;
        item.bowlingTeam = batting.id.equals(match.team1.id) ? match.team2 : match.team1; innings.save(item);
    }
}