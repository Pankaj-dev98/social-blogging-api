package com.geekster.blog.uitls.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class NewPostDto {
    private AuthDto authDto;

    @Size(min = 1, max = 50, message = "Length of title should be in the range 1-50")
    private String title;

    @NotEmpty
    private String blogText;
}
