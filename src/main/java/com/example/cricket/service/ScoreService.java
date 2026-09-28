package com.example.cricket.service;

import com.example.cricket.dto.RecordBallRequest;
import com.example.cricket.entity.*;
import com.example.cricket.exception.ApiException;
import com.example.cricket.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@Transactional
public class ScoreService {
    private final InningsRepository innings;
    private final PlayerRepository players;
    private final BallEventRepository balls;
    private final PlayerMatchStatsRepository stats;
    public ScoreService(InningsRepository innings, PlayerRepository players, BallEventRepository balls,
                        PlayerMatchStatsRepository stats) {
        this.innings = innings; this.players = players; this.balls = balls; this.stats = stats;
    }

    public BallEvent record(Long inningsId, RecordBallRequest request) {
        Innings item = innings.findById(inningsId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Innings not found"));
        if (item.complete || item.match.status != MatchStatus.LIVE)
            throw new ApiException(HttpStatus.CONFLICT, "This innings is not accepting deliveries");
        Player striker = player(request.strikerId()); Player nonStriker = player(request.nonStrikerId()); Player bowler = player(request.bowlerId());
        if (striker.id.equals(nonStriker.id)) throw new ApiException(HttpStatus.BAD_REQUEST, "Striker and non-striker must be different players");
        requireTeam(striker, item.battingTeam, "Striker"); requireTeam(nonStriker, item.battingTeam, "Non-striker"); requireTeam(bowler, item.bowlingTeam, "Bowler");
        int runs = request.runs() == null ? 0 : request.runs(); int extras = request.extras() == null ? 0 : request.extras();
        ExtraType extraType = request.extraType();
        if (extraType == ExtraType.NONE && extras != 0) throw new ApiException(HttpStatus.BAD_REQUEST, "Extras require an extra type");
        if ((extraType == ExtraType.WIDE || extraType == ExtraType.NO_BALL) && extras < 1) extras = 1;
        if (extraType == ExtraType.WIDE || extraType == ExtraType.BYE || extraType == ExtraType.LEG_BYE) runs = 0;
        if (request.wicket() && (request.dismissedPlayerId() == null || request.wicketType() == WicketType.NONE))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Select a dismissed player and wicket type");
        Player dismissed = request.wicket() ? player(request.dismissedPlayerId()) : null;
        if (request.wicket()) {
            if (!dismissed.id.equals(striker.id) && !dismissed.id.equals(nonStriker.id))
                throw new ApiException(HttpStatus.BAD_REQUEST, "Dismissed player must be at the crease");
            requireTeam(dismissed, item.battingTeam, "Dismissed player");
            int maximumWickets = (int) Math.min(10, players.countByTeamId(item.battingTeam.id) - 1);
            if (item.wickets >= maximumWickets) throw new ApiException(HttpStatus.CONFLICT, "All available wickets have fallen");
        }
        BallEvent event = new BallEvent(); event.innings = item; event.striker = striker; event.nonStriker = nonStriker;
        event.bowler = bowler; event.runs = runs; event.extras = extras; event.extraType = extraType;
        event.wicket = request.wicket(); event.wicketType = request.wicketType(); event.dismissedPlayer = dismissed;
        event.overNumber = item.legalBalls / 6 + 1; event.ballNumber = item.legalBalls % 6 + 1;
        item.totalRuns += runs + extras;
        boolean legal = extraType != ExtraType.WIDE && extraType != ExtraType.NO_BALL;
        if (legal) { item.legalBalls++; batter(striker, item.match).ballsFaced++; bowling(bowler, item.match).ballsBowled++; }
        else if (extraType == ExtraType.NO_BALL) batter(striker, item.match).ballsFaced++;
        if (extraType != ExtraType.BYE && extraType != ExtraType.LEG_BYE) bowling(bowler, item.match).runsConceded += runs + extras;
        PlayerMatchStats batterStats = batter(striker, item.match);
        batterStats.runs += runs;
        if (runs == 4 && (extraType == ExtraType.NONE || extraType == ExtraType.NO_BALL)) batterStats.fours++;
        if (runs == 6 && (extraType == ExtraType.NONE || extraType == ExtraType.NO_BALL)) batterStats.sixes++;
        if (request.wicket()) {
            item.wickets++;
            if (request.wicketType() != WicketType.RUN_OUT) bowling(bowler, item.match).wickets++;
        }
        int maximumWickets = (int) Math.min(10, players.countByTeamId(item.battingTeam.id) - 1);
        if (item.wickets >= maximumWickets || item.legalBalls >= 120) item.complete = true;
        item.match.updatedAt = java.time.LocalDateTime.now();
        innings.save(item);
        return balls.save(event);
    }

    public Map<String, Object> score(Long matchId) {
        List<Innings> matchInnings = innings.findByMatchIdOrderByInningsNumber(matchId);
        if (matchInnings.isEmpty()) throw new ApiException(HttpStatus.NOT_FOUND, "No score available for this match");
        Innings current = matchInnings.stream().filter(value -> !value.complete).reduce((first, second) -> second).orElse(matchInnings.get(matchInnings.size() - 1));
        List<BallEvent> history = balls.findByInningsIdOrderByIdDesc(current.id);
        BallEvent latest = history.isEmpty() ? null : history.get(0);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("matchId", matchId); response.put("match", current.match.title); response.put("status", current.match.status);
        response.put("innings", current.inningsNumber); response.put("battingTeam", current.battingTeam.name);
        response.put("bowlingTeam", current.bowlingTeam.name); response.put("runs", current.totalRuns);
        response.put("wickets", current.wickets); response.put("overs", overs(current.legalBalls));
        response.put("legalBalls", current.legalBalls);
        response.put("runRate", current.legalBalls == 0 ? 0.0 : Math.round((double) current.totalRuns * 6 / current.legalBalls * 100.0) / 100.0);
        response.put("striker", latest == null ? null : battingStats(latest.striker, current.match));
        response.put("nonStriker", latest == null ? null : battingStats(latest.nonStriker, current.match));
        response.put("bowler", latest == null ? null : bowlingStats(latest.bowler, current.match));
        response.put("recentBalls", history.stream().limit(12).map(this::ballView).toList());
        response.put("inningsScores", matchInnings.stream().map(this::inningsView).toList());
        return response;
    }
    public List<Map<String, Object>> history(Long inningsId) {
        if (!innings.existsById(inningsId)) throw new ApiException(HttpStatus.NOT_FOUND, "Innings not found");
        List<BallEvent> events = balls.findByInningsIdOrderByIdDesc(inningsId);
        Collections.reverse(events);
        return events.stream().map(this::ballView).toList();
    }
    public Map<String, Object> ballViewForApi(BallEvent event) { return ballView(event); }
    public Map<String, Object> summary(Long matchId) {
        List<Innings> matchInnings = innings.findByMatchIdOrderByInningsNumber(matchId);
        if (matchInnings.isEmpty()) throw new ApiException(HttpStatus.NOT_FOUND, "No score available for this match");
        Match match = matchInnings.get(0).match;
        List<PlayerMatchStats> performances = stats.findByMatchIdOrderByRunsDesc(matchId);
        PlayerMatchStats topBowler = performances.stream().max(Comparator.comparingInt(value -> value.wickets)).orElse(null);
        Map<String, Object> result = new LinkedHashMap<>(); result.put("matchId", matchId); result.put("title", match.title);
        result.put("venue", match.venue); result.put("matchDate", match.matchDate); result.put("status", match.status);
        result.put("winner", match.winner == null ? null : match.winner.name);
        result.put("result", match.winner == null ? "Match tied or winner not decided" : match.winner.name + " won");
        result.put("innings", matchInnings.stream().map(this::inningsView).toList());
        result.put("topScorer", performances.isEmpty() ? null : battingStats(performances.get(0).player, match));
        result.put("bestBowler", topBowler == null ? null : bowlingStats(topBowler.player, match));
        result.put("performances", performances.stream().map(value -> Map.of("player", value.player.name,
                "team", value.player.team.name, "runs", value.runs, "ballsFaced", value.ballsFaced,
                "fours", value.fours, "sixes", value.sixes, "wickets", value.wickets,
                "runsConceded", value.runsConceded)).toList());
        return result;
    }
    private Player player(Long id) { return players.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Player not found")); }
    private void requireTeam(Player player, Team team, String label) {
        if (!player.team.id.equals(team.id)) throw new ApiException(HttpStatus.BAD_REQUEST, label + " must belong to " + team.name);
    }
    private PlayerMatchStats getStats(Player player, Match match) {
        return stats.findByMatchIdAndPlayerId(match.id, player.id)
                .orElseGet(() -> stats.save(createStats(player, match)));
    }
    private PlayerMatchStats createStats(Player player, Match match) {
        PlayerMatchStats value = new PlayerMatchStats(); value.match = match; value.player = player; return value;
    }
    private PlayerMatchStats batter(Player player, Match match) { return getStats(player, match); }
    private PlayerMatchStats bowling(Player player, Match match) { return getStats(player, match); }
    private String overs(int legalBalls) { return legalBalls / 6 + "." + legalBalls % 6; }
    private Map<String, Object> battingStats(Player player, Match match) {
        PlayerMatchStats value = getStats(player, match);
        return Map.of("id", player.id, "name", player.name, "runs", value.runs, "balls", value.ballsFaced, "fours", value.fours, "sixes", value.sixes);
    }
    private Map<String, Object> bowlingStats(Player player, Match match) {
        PlayerMatchStats value = getStats(player, match);
        return Map.of("id", player.id, "name", player.name, "overs", overs(value.ballsBowled), "balls", value.ballsBowled,
                "runs", value.runsConceded, "wickets", value.wickets);
    }
    private Map<String, Object> inningsView(Innings value) {
        return Map.of("innings", value.inningsNumber, "team", value.battingTeam.name, "runs", value.totalRuns,
                "wickets", value.wickets, "overs", overs(value.legalBalls), "complete", value.complete);
    }
    private Map<String, Object> ballView(BallEvent event) {
        String label = event.wicket ? "W" : event.extraType == ExtraType.WIDE ? "Wd" : event.extraType == ExtraType.NO_BALL ? "Nb" :
                event.extraType == ExtraType.BYE ? "B" + event.extras : event.extraType == ExtraType.LEG_BYE ? "Lb" + event.extras : String.valueOf(event.runs);
        return Map.of("id", event.id, "over", event.overNumber, "ball", event.ballNumber, "label", label,
                "runs", event.runs, "extras", event.extras, "extraType", event.extraType, "wicket", event.wicket,
                "striker", event.striker.name, "bowler", event.bowler.name);
    }
}