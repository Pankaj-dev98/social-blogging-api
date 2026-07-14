package com.geekster.blog.uitls.dto;

import com.geekster.blog.uitls.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;

@Data
@Validated
public class SignUpDto {
    @Pattern(regexp = "^[a-zA-Z]+(?: [a-zA-Z]+)*$", message = "Only English letters and single spaces allowed in a name")
    private String name;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$", message = "Password should be at least 8 characters long, must have at least" +
            "one uppercase, one lowercase alphabet and one digit")
    private String password;

    @Email
    private String email;
    private LocalDate dob;
    private Gender gender;
}
