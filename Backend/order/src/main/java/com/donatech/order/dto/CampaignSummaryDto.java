package com.donatech.order.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CampaignSummaryDto {
    private Long id;
    private Long beneficiaryId;
    private String titulo;
    private String motivo;
}
