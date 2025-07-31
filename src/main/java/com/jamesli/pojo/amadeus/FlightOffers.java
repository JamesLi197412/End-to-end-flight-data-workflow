package com.jamesli.pojo.amadeus;

import com.jamesli.pojo.amadeus.flightoffers.FlightOffer;
import com.jamesli.pojo.amadeus.flightoffers.Meta;
import com.jamesli.pojo.amadeus.flightoffers.Dictionary;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Main class to hold the entire JSON structure
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlightOffers {
    private Meta meta;
    private List<FlightOffer> data;

    @JsonProperty("dictionaries")
    private Dictionary dictionary;

    public Meta getMeta() { return meta;}

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public List<FlightOffer> getData() {
        return data;
    }

    public void setData(List<FlightOffer> data) {
        this.data = data;
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }
}
