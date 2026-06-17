package com.donatech.shipping.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ContactDto(Long id, String name, String email, String phone, String direccion) {}
