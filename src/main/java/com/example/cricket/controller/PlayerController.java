package com.example.cricket.controller;

import com.example.cricket.dto.ApiViews;
import com.example.cricket.dto.PlayerRequest;
import com.example.cricket.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PlayerController {
    private final PlayerService service;
    public PlayerController(PlayerService service) { this.service = service; }
    @GetMapping("/players") public List<Map<String, Object>> all() { return service.all().stream().map(ApiViews::player).toList(); }
    @GetMapping("/players/{id}") public Map<String, Object> get(@PathVariable Long id) { return ApiViews.player(service.get(id)); }
    @GetMapping("/teams/{teamId}/players") public List<Map<String, Object>> byTeam(@PathVariable Long teamId) { return service.byTeam(teamId).stream().map(ApiViews::player).toList(); }
    @PostMapping("/players") @ResponseStatus(HttpStatus.CREATED) public Map<String, Object> create(@Valid @RequestBody PlayerRequest request) { return ApiViews.player(service.create(request)); }
    @PutMapping("/players/{id}") public Map<String, Object> update(@PathVariable Long id, @Valid @RequestBody PlayerRequest request) { return ApiViews.player(service.update(id, request)); }
    @DeleteMapping("/players/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) { service.delete(id); }
}