package com.example.cricket.controller;

import com.example.cricket.dto.*;
import com.example.cricket.entity.Match;
import com.example.cricket.service.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MatchController {
    private final MatchService matches;
    private final InningsService innings;
    private final ScoreService scores;
    public MatchController(MatchService matches, InningsService innings, ScoreService scores) {
        this.matches = matches; this.innings = innings; this.scores = scores;
    }
    @GetMapping("/matches") public List<Map<String, Object>> all() { return matches.all().stream().map(ApiViews::match).toList(); }
    @GetMapping("/matches/{id}") public Map<String, Object> get(@PathVariable Long id) { return ApiViews.match(matches.get(id)); }
    @PostMapping("/matches") @ResponseStatus(HttpStatus.CREATED) public Map<String, Object> create(@Valid @RequestBody MatchRequest request) { return ApiViews.match(matches.create(request)); }
    @PutMapping("/matches/{id}") public Map<String, Object> update(@PathVariable Long id, @Valid @RequestBody MatchRequest request) { return ApiViews.match(matches.update(id, request)); }
    @DeleteMapping("/matches/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) { matches.delete(id); }
    @PostMapping("/matches/{id}/start") public Map<String, Object> start(@PathVariable Long id) { return ApiViews.match(matches.start(id)); }
    @PostMapping("/matches/{id}/complete") public Map<String, Object> complete(@PathVariable Long id) { return ApiViews.match(matches.complete(id)); }
    @PostMapping("/matches/{id}/next-innings") public Map<String, Object> nextInnings(@PathVariable Long id, @Valid @RequestBody InningsRequest request) {
        return ApiViews.match(matches.nextInnings(id, request.battingTeamId()));
    }
    @GetMapping("/matches/{id}/score") public Map<String, Object> score(@PathVariable Long id) { return scores.score(id); }
    @GetMapping("/matches/{id}/summary") public Map<String, Object> summary(@PathVariable Long id) { return scores.summary(id); }
    @GetMapping("/matches/{id}/innings") public List<Map<String, Object>> innings(@PathVariable Long id) { return innings.forMatch(id).stream().map(ApiViews::innings).toList(); }
    @GetMapping("/matches/{id}/innings/{inningsId}") public Map<String, Object> innings(@PathVariable Long id, @PathVariable Long inningsId) {
        return ApiViews.innings(innings.get(id, inningsId));
    }
    @PostMapping("/matches/{id}/innings") @ResponseStatus(HttpStatus.CREATED) public Map<String, Object> createInnings(@PathVariable Long id, @Valid @RequestBody InningsRequest request) {
        return ApiViews.innings(innings.create(id, request));
    }
    @PostMapping("/innings/{inningsId}/ball") @ResponseStatus(HttpStatus.CREATED) public Map<String, Object> recordBall(@PathVariable Long inningsId, @Valid @RequestBody RecordBallRequest request) {
        return scores.ballViewForApi(scores.record(inningsId, request));
    }
    @GetMapping("/innings/{inningsId}/balls") public List<Map<String, Object>> ballHistory(@PathVariable Long inningsId) { return scores.history(inningsId); }
}