package com.example.cricket.dto;

import com.example.cricket.entity.PlayerRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record PlayerRequest(@NotBlank String name, @NotNull PlayerRole role,
                            @PositiveOrZero Integer jerseyNumber, String battingStyle,
                            String bowlingStyle, @NotNull Long teamId) { }