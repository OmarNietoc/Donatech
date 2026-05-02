package com.donatech.users.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateUserInternalDto {
    @NotBlank @Size(min = 4, max = 100)
    private String name;

    @NotBlank @Email
    private String email;

    @NotBlank
    private String password;

    @NotNull
    private Long roleId;

    private String phone;
    private Long regionId;
    private Long comunaId;
}
