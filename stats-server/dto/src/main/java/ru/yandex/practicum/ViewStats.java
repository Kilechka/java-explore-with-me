package ru.yandex.practicum;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ViewStats {
    private String app;
    private String uri;
    private int hits;
}