package com.example.cricket.dto;

import jakarta.validation.constraints.NotNull;

public record InningsRequest(@NotNull Long battingTeamId) { }