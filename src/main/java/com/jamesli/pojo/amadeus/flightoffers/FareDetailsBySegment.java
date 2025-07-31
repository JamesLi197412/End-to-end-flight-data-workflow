package com.jamesli.pojo.amadeus.flightoffers;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class FareDetailsBySegment {
    private String segmentId;
    private String cabin;
    private String fareBasis;
    private String brandedFare;
    private String brandedFareLabel;
    private String clazz; // "class" is a reserved keyword in Java, so use "clazz" or "@JsonProperty("class")"
    private IncludedCheckedBags includedCheckedBags;
    private IncludedCabinBags includedCabinBags;
    private List<Amenity> amenities;

    // Getters and Setters
    public String getSegmentId() { return segmentId; }
    public void setSegmentId(String segmentId) { this.segmentId = segmentId; }

    public String getCabin() { return cabin; }
    public void setCabin(String cabin) { this.cabin = cabin; }

    public String getFareBasis() { return fareBasis; }
    public void setFareBasis(String fareBasis) { this.fareBasis = fareBasis; }

    public String getBrandedFare() { return brandedFare; }
    public void setBrandedFare(String brandedFare) { this.brandedFare = brandedFare; }

    public String getBrandedFareLabel() { return brandedFareLabel; }
    public void setBrandedFareLabel(String brandedFareLabel) { this.brandedFareLabel = brandedFareLabel; }

    @JsonProperty("class") // Use this annotation to map "class" JSON field
    public String getClazz() { return clazz; }
    @JsonProperty("class")
    public void setClazz(String clazz) { this.clazz = clazz; }

    public IncludedCheckedBags getIncludedCheckedBags() { return includedCheckedBags; }
    public void setIncludedCheckedBags(IncludedCheckedBags includedCheckedBags) { this.includedCheckedBags = includedCheckedBags; }

    public IncludedCabinBags getIncludedCabinBags() { return includedCabinBags; }
    public void setIncludedCabinBags(IncludedCabinBags includedCabinBags) { this.includedCabinBags = includedCabinBags; }

    public List<Amenity> getAmenities() { return amenities; }
    public void setAmenities(List<Amenity> amenities) { this.amenities = amenities; }
}
