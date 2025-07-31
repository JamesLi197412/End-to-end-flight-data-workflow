package com.jamesli.pojo.amadeus.flightoffers;

import java.util.List;

public class Price {
    private String currency;
    private String total;
    private String base;
    private List<Fee> fees;
    private String grandTotal;
    private List<AdditionalService> additionalServices; // This field is optional

    // Getters and Setters
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getTotal() { return total; }
    public void setTotal(String total) { this.total = total; }

    public String getBase() { return base; }
    public void setBase(String base) { this.base = base; }

    public List<Fee> getFees() { return fees; }
    public void setFees(List<Fee> fees) { this.fees = fees; }

    public String getGrandTotal() { return grandTotal; }
    public void setGrandTotal(String grandTotal) { this.grandTotal = grandTotal; }

    public List<AdditionalService> getAdditionalServices() { return additionalServices; }
    public void setAdditionalServices(List<AdditionalService> additionalServices) { this.additionalServices = additionalServices; }
}
