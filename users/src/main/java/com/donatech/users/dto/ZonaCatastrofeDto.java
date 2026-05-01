package com.donatech.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ZonaCatastrofeDto {
    @NotNull
    private Long regionId;

    @NotNull
    private Long comunaId;

    @NotBlank
    private String nombreEvento;

    @NotNull
    private LocalDate fechaDeclaracion;

    private LocalDate fechaFin;
    private Boolean activa = true;
}
