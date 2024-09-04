package ru.yandex.practicum.compilations.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCompilationRequest {
    @Size(min = 1, max = 50)
    private String title;
    @Builder.Default
    private boolean pinned = false;
    @Builder.Default
    private List<Long> events = new ArrayList<>();
}