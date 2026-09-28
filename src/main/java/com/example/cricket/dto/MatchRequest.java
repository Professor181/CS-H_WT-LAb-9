package com.example.cricket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record MatchRequest(@NotBlank String title, @NotBlank String venue, LocalDate matchDate,
                           @NotNull Long team1Id, @NotNull Long team2Id) { }