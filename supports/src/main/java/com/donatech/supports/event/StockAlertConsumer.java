package com.donatech.supports.event;

import com.donatech.supports.dto.SoporteRequestDTO;
import com.donatech.supports.model.PrioridadSoporte;
import com.donatech.supports.model.TipoSoporte;
import com.donatech.supports.service.SoporteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockAlertConsumer {

    private final SoporteService soporteService;

    @RabbitListener(queues = "supports.stock.alert")
    public void handleStockLow(StockLowEvent event) {
        log.warn("Alerta stock recibida para producto id={}, stock={}", event.productId(), event.currentStock());

        SoporteRequestDTO dto = new SoporteRequestDTO();
        dto.setDescripcion("ALERTA STOCK: Producto '" + event.productName() +
                "' (ID: " + event.productId() + ") tiene stock " + event.currentStock() +
                ", por debajo del mínimo " + event.stockMinimo());
        dto.setUsuarioId(0L);
        dto.setPrioridad(PrioridadSoporte.CRITICO);
        dto.setTipo(TipoSoporte.PRODUCTO);

        soporteService.crear(dto);
    }
}
