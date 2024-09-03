package ru.yandex.practicum.compilations.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCompilationRequest {
    @NotNull
    private String title;
    @Builder.Default
    private boolean pinned = false;
    List<Long> events;
}