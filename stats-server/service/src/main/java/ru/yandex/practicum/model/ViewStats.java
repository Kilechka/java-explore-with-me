package ru.yandex.practicum.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ViewStats {
    private String app;
    private String uri;
    private Long hits;
}