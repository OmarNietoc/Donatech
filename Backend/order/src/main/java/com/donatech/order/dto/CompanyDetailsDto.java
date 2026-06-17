package com.donatech.order.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyDetailsDto {
    private String rut;
    private String razonSocial;
    private String giro;
    private String direccionLegal;
}
