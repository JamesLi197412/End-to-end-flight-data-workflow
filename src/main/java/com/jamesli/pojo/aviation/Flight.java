package com.jamesli.pojo.aviation;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Flight {
    @JsonProperty("flight_date")
    private String flightDate;

    @JsonProperty("flight_status")
    private String flightStatus;

    private Departure departure;
    private Arrival arrival;
    private Airline airline;
    private FlightDetails flightDetails;

    private AirCraft aircraft;
    private LiveData live;

    public String getFlightDate() {
        return flightDate;
    }

    public void setFlightDate(String flightDate) {
        this.flightDate = flightDate;
    }

    public String getFlightStatus() {
        return flightStatus;
    }

    public void setFlightStatus(String flightStatus) {
        this.flightStatus = flightStatus;
    }

    public Departure getDeparture() {
        return departure;
    }

    public void setDeparture(Departure departure) {
        this.departure = departure;
    }

    public Arrival getArrival() {
        return arrival;
    }

    public void setArrival(Arrival arrival) {
        this.arrival = arrival;
    }

    public Airline getAirline() {
        return airline;
    }

    public void setAirline(Airline airline) {
        this.airline = airline;
    }

    public FlightDetails getFlight() {
        return flightDetails;
    }

    public void setFlight(FlightDetails flightDetails) {
        this.flightDetails = flightDetails;
    }

    public AirCraft getAircraft() {
        return aircraft;
    }

    public void setAircraft(AirCraft aircraft) {
        this.aircraft = aircraft;
    }

    public LiveData getLive() {
        return live;
    }

    public void setLive(LiveData live) {
        this.live = live;
    }
}
