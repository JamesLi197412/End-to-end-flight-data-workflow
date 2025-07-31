package com.jamesli.pojo.amadeus.flightoffers;

import java.util.List;

public class TravelerPricing {
    private String travelerId;
    private String fareOption;
    private String travelerType;
    private PriceDetail price; // Reusing the Price class for inner price object
    private List<FareDetailsBySegment> fareDetailsBySegment;

    // Getters and Setters
    public String getTravelerId() { return travelerId; }
    public void setTravelerId(String travelerId) { this.travelerId = travelerId; }

    public String getFareOption() { return fareOption; }
    public void setFareOption(String fareOption) { this.fareOption = fareOption; }

    public String getTravelerType() { return travelerType; }
    public void setTravelerType(String travelerType) { this.travelerType = travelerType; }

    public PriceDetail getPrice() { return price; } // This is a different "price" structure,
    // renamed to avoid conflict.
    // Could make a generic Price class.
    public void setPrice(PriceDetail price) { this.price = price; }

    public List<FareDetailsBySegment> getFareDetailsBySegment() { return fareDetailsBySegment; }
    public void setFareDetailsBySegment(List<FareDetailsBySegment> fareDetailsBySegment) { this.fareDetailsBySegment = fareDetailsBySegment; }
}
