package ru.yandex.practicum.caregories.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {
    private int id;
    @NotBlank
    @Size(min = 1, max = 50)
    private String name;
}