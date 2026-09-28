package com.example.cricket.config;

import com.example.cricket.dto.RecordBallRequest;
import com.example.cricket.entity.*;
import com.example.cricket.repository.*;
import com.example.cricket.service.ScoreService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class SampleData {
    @Bean
    CommandLineRunner seedDemoData(TeamRepository teams, PlayerRepository players, MatchRepository matches,
                                   InningsRepository innings, ScoreService scores) {
        return args -> {
            if (teams.count() > 0) return;
            Team india = team(teams, "India", "IND", "India", "Rahul Dravid");
            Team australia = team(teams, "Australia", "AUS", "Australia", "Andrew McDonald");
            Team england = team(teams, "England", "ENG", "England", "Brendon McCullum");
            Team southAfrica = team(teams, "South Africa", "SA", "South Africa", "Shukri Conrad");
            List<Player> indiaPlayers = addPlayers(players, india, new String[]{"Rohit Sharma", "Virat Kohli", "Shubman Gill", "KL Rahul", "Hardik Pandya", "Ravindra Jadeja", "Jasprit Bumrah", "Mohammed Siraj", "Kuldeep Yadav", "Mohammed Shami", "Rishabh Pant"},
                    new PlayerRole[]{PlayerRole.BATSMAN, PlayerRole.BATSMAN, PlayerRole.BATSMAN, PlayerRole.WICKET_KEEPER, PlayerRole.ALL_ROUNDER, PlayerRole.ALL_ROUNDER, PlayerRole.BOWLER, PlayerRole.BOWLER, PlayerRole.BOWLER, PlayerRole.BOWLER, PlayerRole.WICKET_KEEPER});
            addPlayers(players, australia, new String[]{"David Warner", "Travis Head", "Steve Smith", "Glenn Maxwell", "Mitchell Marsh", "Alex Carey", "Pat Cummins", "Mitchell Starc", "Josh Hazlewood", "Adam Zampa", "Marcus Stoinis"},
                    new PlayerRole[]{PlayerRole.BATSMAN, PlayerRole.BATSMAN, PlayerRole.BATSMAN, PlayerRole.ALL_ROUNDER, PlayerRole.ALL_ROUNDER, PlayerRole.WICKET_KEEPER, PlayerRole.BOWLER, PlayerRole.BOWLER, PlayerRole.BOWLER, PlayerRole.BOWLER, PlayerRole.ALL_ROUNDER});
            addPlayers(players, england, new String[]{"Joe Root", "Jos Buttler", "Jofra Archer"}, new PlayerRole[]{PlayerRole.BATSMAN, PlayerRole.WICKET_KEEPER, PlayerRole.BOWLER});
            addPlayers(players, southAfrica, new String[]{"Aiden Markram", "Quinton de Kock", "Kagiso Rabada"}, new PlayerRole[]{PlayerRole.BATSMAN, PlayerRole.WICKET_KEEPER, PlayerRole.BOWLER});
            Match match = new Match(); match.title = "India vs Australia"; match.venue = "Wankhede Stadium";
            match.matchDate = LocalDate.now(); match.team1 = india; match.team2 = australia; match.status = MatchStatus.LIVE; match.currentInnings = 1;
            match = matches.save(match);
            Innings first = new Innings(); first.match = match; first.battingTeam = india; first.bowlingTeam = australia; first.inningsNumber = 1;
            first = innings.save(first);
            Player bowler = players.findByTeamId(australia.id).stream().filter(player -> player.name.equals("Pat Cummins")).findFirst().orElseThrow();
            int[] batterIds = {0, 1, 0, 1, 0, 1}; int[] batRuns = {4, 1, 0, 2, 6, 1};
            for (int index = 0; index < batRuns.length; index++) {
                Player striker = indiaPlayers.get(batterIds[index]);
                Player nonStriker = indiaPlayers.get(batterIds[index] == 0 ? 1 : 0);
                scores.record(first.id, new RecordBallRequest(striker.id, nonStriker.id, bowler.id, batRuns[index], 0,
                        ExtraType.NONE, false, WicketType.NONE, null));
            }
        };
    }
    private Team team(TeamRepository repository, String name, String shortName, String country, String coach) {
        Team value = new Team(); value.name = name; value.shortName = shortName; value.country = country; value.coach = coach;
        return repository.save(value);
    }
    private List<Player> addPlayers(PlayerRepository repository, Team team, String[] names, PlayerRole[] roles) {
        List<Player> result = new ArrayList<>();
        for (int index = 0; index < names.length; index++) {
            Player player = new Player(); player.name = names[index]; player.role = roles[index]; player.jerseyNumber = index + 1;
            player.battingStyle = "Right Hand";
            player.bowlingStyle = roles[index] == PlayerRole.BOWLER || roles[index] == PlayerRole.ALL_ROUNDER ? "Right Arm Medium" : "Not applicable";
            player.team = team; result.add(repository.save(player));
        }
        return result;
    }
}