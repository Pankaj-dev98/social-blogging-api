package com.geekster.blog.uitls.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class NewCommentDto {
    private AuthDto authDto;
    private long postId;

    @NotEmpty
    private String CommentText;
}
