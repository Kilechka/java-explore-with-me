package ru.yandex.practicum.comments.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.comments.dto.CommentDto;
import ru.yandex.practicum.comments.dto.NewCommentDto;
import ru.yandex.practicum.comments.service.CommentService;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/user/comments")
public class CommentPrivateController {

    private final CommentService commentService;

    @PostMapping("/{eventId}/{userId}")
    @ResponseStatus(value = HttpStatus.CREATED)
    public CommentDto createComment(@RequestBody @Valid NewCommentDto newCommentDto,
                                    @PathVariable Long eventId,
                                    @PathVariable Long userId) {
        log.info("Получен запрос на создание комментария");
        return commentService.createComment(newCommentDto, eventId, userId);
    }

    @DeleteMapping("/{comId}/{userId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long comId, @PathVariable Long userId) {
        log.info("Получен запрос на удаление комментария");
        commentService.deleteComment(comId, userId);
    }

    @PatchMapping("/{comId}/{userId}")
    public CommentDto updateComment(@RequestBody @Valid NewCommentDto newCommentDto,
                                        @PathVariable Long comId,
                                        @PathVariable Long userId) {
        log.info("Получен запрос на обновление комментария");
        return commentService.updateComment(newCommentDto, comId, userId);
    }

    @GetMapping("/{eventId}")
    public List<CommentDto> getCommentsForEvent(@RequestParam(value = "from", defaultValue = "0") @PositiveOrZero int from,
                                            @RequestParam(value = "size", defaultValue = "10") @Positive int size,
                                            @PathVariable Long eventId) {
        log.info("Получен запрос на получение событий");
        return commentService.getCommentsForEvent(from, size, eventId);
    }
}