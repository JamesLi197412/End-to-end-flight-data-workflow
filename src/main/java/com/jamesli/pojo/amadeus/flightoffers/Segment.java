package com.jamesli.pojo.amadeus.flightoffers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Segment {
    private Departure departure;
    private Arrival arrival;
    private String carrierCode;
    private String number;
    private Aircraft aircraft;
    private Operating operating;
    private String duration;
    private String id;
    private int numberOfStops;
    private boolean blacklistedInEU;

    // Getters and Setters
    public Departure getDeparture() { return departure; }
    public void setDeparture(Departure departure) { this.departure = departure; }

    public Arrival getArrival() { return arrival; }
    public void setArrival(Arrival arrival) { this.arrival = arrival; }

    public String getCarrierCode() { return carrierCode; }
    public void setCarrierCode(String carrierCode) { this.carrierCode = carrierCode; }

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }

    public Aircraft getAircraft() { return aircraft; }
    public void setAircraft(Aircraft aircraft) { this.aircraft = aircraft; }

    public Operating getOperating() { return operating; }
    public void setOperating(Operating operating) { this.operating = operating; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getNumberOfStops() { return numberOfStops; }
    public void setNumberOfStops(int numberOfStops) { this.numberOfStops = numberOfStops; }

    public boolean isBlacklistedInEU() { return blacklistedInEU; }
    public void setBlacklistedInEU(boolean blacklistedInEU) { this.blacklistedInEU = blacklistedInEU; }
}
