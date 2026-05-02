package com.donatech.shipping.mapper;

import com.donatech.shipping.dto.ShipmentDTO;
import com.donatech.shipping.model.Shipment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShipmentMapper {

    @Mapping(target = "routeId", source = "route.id")
    ShipmentDTO toDto(Shipment shipment);

    @Mapping(target = "route", ignore = true)
    Shipment toEntity(ShipmentDTO dto);
}

