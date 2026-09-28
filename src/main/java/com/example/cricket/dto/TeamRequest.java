package com.example.cricket.dto;

import jakarta.validation.constraints.NotBlank;

public record TeamRequest(@NotBlank String name, @NotBlank String shortName,
                          @NotBlank String country, String coach) { }