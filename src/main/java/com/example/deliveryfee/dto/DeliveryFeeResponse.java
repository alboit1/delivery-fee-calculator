package com.example.deliveryfee.dto;

public class DeliveryFeeResponse {
    private double deliveryFee;

    public DeliveryFeeResponse(double deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public double getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(double deliveryFee) {
        this.deliveryFee = deliveryFee;
    }
}