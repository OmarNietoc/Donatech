package com.donatech.shipping.enums;

public enum DeliveryStatus {
    PENDING(1),
    ASSIGNED(2),
    DISPATCHED(3),
    DELIVERED(4),
    FAILED(5),
    CANCELLED(6);

    private final int step;

    DeliveryStatus(int step) {
        this.step = step;
    }

    public int getStep() {
        return step;
    }

    public boolean canTransitionTo(DeliveryStatus newStatus) {
        if (newStatus == FAILED || newStatus == CANCELLED) {
            return true;
        }
        return newStatus.getStep() > this.step;
    }
}

