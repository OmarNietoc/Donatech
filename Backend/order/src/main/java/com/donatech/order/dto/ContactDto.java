package com.donatech.order.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContactDto {
    private Long id;
    private String name;
    private String apellido;
    private String email;
    private String phone;
    private String direccion;
    private String comuna;
    private String region;
}
