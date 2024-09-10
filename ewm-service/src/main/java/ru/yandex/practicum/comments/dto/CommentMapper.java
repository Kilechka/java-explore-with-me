package ru.yandex.practicum.comments.dto;

import ru.yandex.practicum.comments.Comment;

public class CommentMapper {

    private CommentMapper() {
    }

    public static Comment toComment(NewCommentDto newCommentDto) {
        return Comment.builder()
                .text(newCommentDto.getText())
                .isModified(false)
                .build();
    }

    public static CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .text(comment.getText())
                .isModified(comment.isModified())
                .eventId(comment.getEvent().getId())
                .changedOn(comment.getChangedOn())
                .id(comment.getId())
                .creatorId(comment.getCreator().getId())
                .build();
    }


}