package com.geekster.blog.uitls.dto;

import com.geekster.blog.domainmodel.UserAuthenticationToken;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
public class PostUpdationDto {
    @NotNull
    private AuthDto authDto;

    @Min(value = 1)
    @Max(value = Long.MAX_VALUE)
    private long postId;

    @Size(min = 1, max = 50, message = "Title length should be in the range 1-50")
    private String title;

    @NotEmpty
    private String blogText;
}
