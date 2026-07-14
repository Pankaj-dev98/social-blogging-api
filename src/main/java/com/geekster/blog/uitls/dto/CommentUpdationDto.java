package com.geekster.blog.uitls.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class CommentUpdationDto {
    private AuthDto authDto;
    private long commentId;

    @NotEmpty
    private String updatedComment;
}
