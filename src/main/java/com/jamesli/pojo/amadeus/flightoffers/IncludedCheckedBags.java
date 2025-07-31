package com.jamesli.pojo.amadeus.flightoffers;

public class IncludedCheckedBags {
    private Integer weight; // Can be null, hence Integer
    private String weightUnit;
    private Integer quantity; // Can be null, hence Integer

    // Getters and Setters
    public Integer getWeight() { return weight; }
    public void setWeight(Integer weight) { this.weight = weight; }

    public String getWeightUnit() { return weightUnit; }
    public void setWeightUnit(String weightUnit) { this.weightUnit = weightUnit; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
