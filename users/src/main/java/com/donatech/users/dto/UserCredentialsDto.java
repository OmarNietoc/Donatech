package com.donatech.users.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCredentialsDto {
    private Long id;
    private String email;
    private String password;
    private String roleName;
    private Integer status;
}
