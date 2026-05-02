package com.donatech.shipping.mapper;

import com.donatech.shipping.dto.RouteDTO;
import com.donatech.shipping.model.Route;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ShipmentMapper.class})
public interface RouteMapper {

    RouteDTO toDto(Route route);

    Route toEntity(RouteDTO dto);
}

