package com.example.cricket.dto;

import com.example.cricket.entity.ExtraType;
import com.example.cricket.entity.WicketType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RecordBallRequest(@NotNull Long strikerId, @NotNull Long nonStrikerId,
                                @NotNull Long bowlerId, @Min(0) Integer runs,
                                @Min(0) Integer extras, @NotNull ExtraType extraType,
                                boolean wicket, @NotNull WicketType wicketType,
                                Long dismissedPlayerId) { }