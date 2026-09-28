package com.example.cricket;

import com.example.cricket.dto.MatchRequest;
import com.example.cricket.dto.RecordBallRequest;
import com.example.cricket.entity.*;
import com.example.cricket.exception.ApiException;
import com.example.cricket.repository.*;
import com.example.cricket.service.MatchService;
import com.example.cricket.service.ScoreService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CricketScoreApplicationTests {
    @Autowired TeamRepository teams;
    @Autowired PlayerRepository players;
    @Autowired MatchRepository matches;
    @Autowired InningsRepository innings;
    @Autowired PlayerMatchStatsRepository playerStats;
    @Autowired MatchService matchService;
    @Autowired ScoreService scoreService;

    @Test
    void applicationContextLoadsAndDemoDataIsAvailable() {
        assertTrue(teams.count() >= 4);
        assertTrue(matches.count() >= 1);
    }

    @Test
    void matchCanBeCreatedAndStarted() {
        Fixture fixture = fixture();
        Match match = matchService.create(new MatchRequest("Test fixture", "Test ground", LocalDate.now(), fixture.batting.id, fixture.bowling.id));
        assertEquals(MatchStatus.UPCOMING, match.status);
        Match started = matchService.start(match.id);
        assertEquals(MatchStatus.LIVE, started.status);
        assertEquals(1, innings.findByMatchIdOrderByInningsNumber(match.id).size());
    }

    @Test
    void normalRunsFourAndSixUpdateTheScoreAndBattingStats() {
        Fixture fixture = fixture();
        for (int runs : new int[]{1, 4, 6}) scoreService.record(fixture.innings.id, delivery(fixture, runs, 0, ExtraType.NONE, false, WicketType.NONE));
        assertEquals(11, innings.findById(fixture.innings.id).orElseThrow().totalRuns);
        PlayerMatchStats stats = playerStats.findByMatchIdAndPlayerId(fixture.match.id, fixture.striker.id).orElseThrow();
        assertEquals(11, stats.runs);
        assertEquals(3, stats.ballsFaced);
        assertEquals(1, stats.fours);
        assertEquals(1, stats.sixes);
    }

    @Test
    void widesAndNoBallsDoNotAdvanceLegalOvers() {
        Fixture fixture = fixture();
        for (int index = 0; index < 6; index++) scoreService.record(fixture.innings.id, delivery(fixture, 0, 0, ExtraType.NONE, false, WicketType.NONE));
        scoreService.record(fixture.innings.id, delivery(fixture, 0, 1, ExtraType.WIDE, false, WicketType.NONE));
        scoreService.record(fixture.innings.id, delivery(fixture, 2, 1, ExtraType.NO_BALL, false, WicketType.NONE));
        Innings updated = innings.findById(fixture.innings.id).orElseThrow();
        assertEquals(6, updated.legalBalls);
        assertEquals(4, updated.totalRuns);
        assertEquals(2, playerStats.findByMatchIdAndPlayerId(fixture.match.id, fixture.striker.id).orElseThrow().runs);
        assertEquals("1.0", scoreService.score(fixture.match.id).get("overs"));
    }

    @Test
    void wicketUpdatesInningsAndBowlerStats() {
        Fixture fixture = fixture();
        scoreService.record(fixture.innings.id, delivery(fixture, 0, 0, ExtraType.NONE, true, WicketType.BOWLED));
        assertEquals(1, innings.findById(fixture.innings.id).orElseThrow().wickets);
        assertEquals(1, playerStats.findByMatchIdAndPlayerId(fixture.match.id, fixture.bowler.id).orElseThrow().wickets);
    }

    @Test
    void oversUseSixLegalBallsAndInvalidIdsReturnNotFound() {
        Fixture fixture = fixture();
        for (int index = 0; index < 10; index++) scoreService.record(fixture.innings.id, delivery(fixture, 0, 0, ExtraType.NONE, false, WicketType.NONE));
        assertEquals("1.4", scoreService.score(fixture.match.id).get("overs"));
        assertEquals(10, innings.findById(fixture.innings.id).orElseThrow().legalBalls);
        assertEquals(404, assertThrows(ApiException.class, () -> matchService.get(-1L)).getStatus().value());
        RecordBallRequest invalidPlayer = new RecordBallRequest(999999L, fixture.nonStriker.id, fixture.bowler.id, 0, 0, ExtraType.NONE, false, WicketType.NONE, null);
        assertEquals(404, assertThrows(ApiException.class, () -> scoreService.record(fixture.innings.id, invalidPlayer)).getStatus().value());
    }

    private Fixture fixture() {
        Team batting = team("Test Batting"); Team bowling = team("Test Bowling");
        Player striker = player(batting, "Test Striker"); Player nonStriker = player(batting, "Test Partner"); player(batting, "Test Reserve");
        Player bowler = player(bowling, "Test Bowler");
        Match match = matchService.create(new MatchRequest("Unit Test Match", "Test Ground", LocalDate.now(), batting.id, bowling.id));
        match = matchService.start(match.id);
        Innings inning = innings.findByMatchIdAndInningsNumber(match.id, 1).orElseThrow();
        return new Fixture(batting, bowling, striker, nonStriker, bowler, match, inning);
    }
    private Team team(String name) {
        Team team = new Team(); team.name = name; team.shortName = name.endsWith("Batting") ? "BAT" : "BOW"; team.country = name; return teams.save(team);
    }
    private Player player(Team team, String name) {
        Player player = new Player(); player.team = team; player.name = name; player.role = PlayerRole.BATSMAN; return players.save(player);
    }
    private RecordBallRequest delivery(Fixture fixture, int runs, int extras, ExtraType type, boolean wicket, WicketType wicketType) {
        return new RecordBallRequest(fixture.striker.id, fixture.nonStriker.id, fixture.bowler.id, runs, extras, type,
                wicket, wicketType, wicket ? fixture.striker.id : null);
    }
    private record Fixture(Team batting, Team bowling, Player striker, Player nonStriker, Player bowler, Match match, Innings innings) { }
}