package com.geekster.blog.uitls.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class GenericErrorResponse {
    private LocalDateTime timeStamp;
    private String error;
}
