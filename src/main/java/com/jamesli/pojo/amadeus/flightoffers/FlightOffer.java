package com.jamesli.pojo.amadeus.flightoffers;

import java.util.List;

public class FlightOffer {
    private String type;
    private String id;
    private String source;
    private boolean instantTicketingRequired;
    private boolean nonHomogeneous;
    private boolean oneWay;
    private boolean isUpsellOffer;
    private String lastTicketingDate;
    private String lastTicketingDateTime;
    private int numberOfBookableSeats;
    private List<Itinerary> itineraries;
    private Price price;
    private PricingOptions pricingOptions;
    private List<String> validatingAirlineCodes;
    private List<TravelerPricing> travelerPricings;

    // Getters and Setters for all fields
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public boolean isInstantTicketingRequired() { return instantTicketingRequired; }
    public void setInstantTicketingRequired(boolean instantTicketingRequired) { this.instantTicketingRequired = instantTicketingRequired; }

    public boolean isNonHomogeneous() { return nonHomogeneous; }
    public void setNonHomogeneous(boolean nonHomogeneous) { this.nonHomogeneous = nonHomogeneous; }

    public boolean isOneWay() { return oneWay; }
    public void setOneWay(boolean oneWay) { this.oneWay = oneWay; }

    public boolean getIsUpsellOffer() { return isUpsellOffer; }
    public void setIsUpsellOffer(boolean isUpsellOffer) { this.isUpsellOffer = isUpsellOffer; }

    public String getLastTicketingDate() { return lastTicketingDate; }
    public void setLastTicketingDate(String lastTicketingDate) { this.lastTicketingDate = lastTicketingDate; }

    public String getLastTicketingDateTime() { return lastTicketingDateTime; }
    public void setLastTicketingDateTime(String lastTicketingDateTime) { this.lastTicketingDateTime = lastTicketingDateTime; }

    public int getNumberOfBookableSeats() { return numberOfBookableSeats; }
    public void setNumberOfBookableSeats(int numberOfBookableSeats) { this.numberOfBookableSeats = numberOfBookableSeats; }

    public List<Itinerary> getItineraries() { return itineraries; }
    public void setItineraries(List<Itinerary> itineraries) { this.itineraries = itineraries; }

    public Price getPrice() { return price; }
    public void setPrice(Price price) { this.price = price; }

    public PricingOptions getPricingOptions() { return pricingOptions; }
    public void setPricingOptions(PricingOptions pricingOptions) { this.pricingOptions = pricingOptions; }

    public List<String> getValidatingAirlineCodes() { return validatingAirlineCodes; }
    public void setValidatingAirlineCodes(List<String> validatingAirlineCodes) { this.validatingAirlineCodes = validatingAirlineCodes; }

    public List<TravelerPricing> getTravelerPricings() { return travelerPricings; }
    public void setTravelerPricings(List<TravelerPricing> travelerPricings) { this.travelerPricings = travelerPricings; }
}
