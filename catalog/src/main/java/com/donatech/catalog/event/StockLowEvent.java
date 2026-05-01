package com.donatech.catalog.event;

public record StockLowEvent(
        String productId,
        String productName,
        Integer currentStock,
        Integer stockMinimo
) {}
