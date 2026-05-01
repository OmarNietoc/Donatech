package com.donatech.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QRScanDto {

    @NotNull(message = "El ID de la donación es obligatorio")
    private Long donationId;

    private String location;

    private String scannedByEmail;

    private String notes;
}
