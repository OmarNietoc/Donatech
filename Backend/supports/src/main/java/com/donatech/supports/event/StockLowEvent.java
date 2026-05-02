package com.donatech.supports.event;

public record StockLowEvent(
        String productId,
        String productName,
        Integer currentStock,
        Integer stockMinimo
) {}
