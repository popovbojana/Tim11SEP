package com.sep.webshop.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 6, max = 64)
    private String password;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

}
