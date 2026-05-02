package com.donatech.users.dto;

import com.donatech.users.model.EstadoVerificacion;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyBeneficiaryDto {

    @NotNull
    private EstadoVerificacion estado;

    @NotNull
    private Long verificadorId;

    private String motivoRechazo;
}
