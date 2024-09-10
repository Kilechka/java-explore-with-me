package ru.yandex.practicum.comments.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.comments.service.CommentService;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/comments")
public class CommentAdminController {

    private final CommentService commentService;

    @DeleteMapping("/{comId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteCommentForAdmin(@PathVariable Long comId) {
        log.info("Получен запрос на удаление комментария админом");
        commentService.deleteCommentForAdmin(comId);
    }
}