package com.geekster.blog.uitls.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthDto {
    private String email;
    private String token;

    public void setEmail(String email) {
        this.email = email.toLowerCase();
    }
}
