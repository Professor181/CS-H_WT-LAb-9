package com.example.cricket.dto;

import com.example.cricket.entity.*;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ApiViews {
    private ApiViews() { }
    public static Map<String, Object> team(Team value) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", value.id); result.put("name", value.name); result.put("shortName", value.shortName);
        result.put("country", value.country); result.put("coach", value.coach); result.put("createdAt", value.createdAt);
        return result;
    }
    public static Map<String, Object> player(Player value) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", value.id); result.put("name", value.name); result.put("role", value.role);
        result.put("jerseyNumber", value.jerseyNumber); result.put("battingStyle", value.battingStyle);
        result.put("bowlingStyle", value.bowlingStyle); result.put("team", team(value.team)); result.put("teamId", value.team.id);
        return result;
    }
    public static Map<String, Object> match(Match value) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", value.id); result.put("title", value.title); result.put("venue", value.venue);
        result.put("matchDate", value.matchDate); result.put("team1", team(value.team1)); result.put("team2", team(value.team2));
        result.put("status", value.status); result.put("currentInnings", value.currentInnings);
        result.put("tossWinner", value.tossWinner == null ? null : team(value.tossWinner));
        result.put("tossDecision", value.tossDecision); result.put("winner", value.winner == null ? null : team(value.winner));
        result.put("createdAt", value.createdAt); result.put("updatedAt", value.updatedAt);
        return result;
    }
    public static Map<String, Object> innings(Innings value) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", value.id); result.put("matchId", value.match.id); result.put("battingTeam", team(value.battingTeam));
        result.put("bowlingTeam", team(value.bowlingTeam)); result.put("inningsNumber", value.inningsNumber);
        result.put("totalRuns", value.totalRuns); result.put("wickets", value.wickets); result.put("legalBalls", value.legalBalls);
        result.put("overs", value.legalBalls / 6 + "." + value.legalBalls % 6); result.put("complete", value.complete);
        return result;
    }
}