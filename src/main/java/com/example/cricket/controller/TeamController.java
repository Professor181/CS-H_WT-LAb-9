package com.example.cricket.controller;

import com.example.cricket.dto.ApiViews;
import com.example.cricket.dto.TeamRequest;
import com.example.cricket.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teams")
public class TeamController {
    private final TeamService service;
    public TeamController(TeamService service) { this.service = service; }
    @GetMapping public List<Map<String, Object>> all() { return service.all().stream().map(ApiViews::team).toList(); }
    @GetMapping("/{id}") public Map<String, Object> get(@PathVariable Long id) { return ApiViews.team(service.get(id)); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public Map<String, Object> create(@Valid @RequestBody TeamRequest request) { return ApiViews.team(service.create(request)); }
    @PutMapping("/{id}") public Map<String, Object> update(@PathVariable Long id, @Valid @RequestBody TeamRequest request) { return ApiViews.team(service.update(id, request)); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) { service.delete(id); }
}