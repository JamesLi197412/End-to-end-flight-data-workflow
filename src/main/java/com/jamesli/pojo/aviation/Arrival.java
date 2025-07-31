package com.jamesli.pojo.aviation;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Arrival extends FlightInfo{
    @JsonProperty("baggage")
    private String baggageCarousel;

    public String getBaggage() {
        return baggageCarousel;
    }

    public void setBaggage(String baggageCarousel) {
        this.baggageCarousel = baggageCarousel;
    }
}
