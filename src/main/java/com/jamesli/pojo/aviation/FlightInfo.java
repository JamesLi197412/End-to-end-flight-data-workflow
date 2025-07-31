package com.jamesli.pojo.aviation;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FlightInfo {
    private String airport;
    private String timezone;
    private String iata;
    private String icao;
    private String terminal;
    private String delay;
    private String gate;
    private String scheduled;
    private String estimated;
    private String actual;

    @JsonProperty("estimated_runway")
    private String estimatedRunnway;

    @JsonProperty("actual_runway")
    private String actualRunway;

    public String getAirport() {
        return airport;
    }

    public void setAirport(String airport) {
        this.airport = airport;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getIata() {
        return iata;
    }

    public void setIata(String iata) {
        this.iata = iata;
    }

    public String getIcao() {
        return icao;
    }

    public void setIcao(String icao) {
        this.icao = icao;
    }

    public String getTerminal() {
        return terminal;
    }

    public void setTerminal(String terminal) {
        this.terminal = terminal;
    }

    public String getGate() {
        return gate;
    }

    public void setGate(String gate) {
        this.gate = gate;
    }

    public String getScheduled() {
        return scheduled;
    }

    public void setScheduled(String scheduled) {
        this.scheduled = scheduled;
    }

    public String getDelay() {
        return delay;
    }

    public void setDelay(String delay) {
        this.delay = delay;
    }

    public String getEstimated() {
        return estimated;
    }

    public void setEstimated(String estimated) {
        this.estimated = estimated;
    }

    public String getActual() {
        return actual;
    }

    public void setActual(String actual) {
        this.actual = actual;
    }

    public String getEstimated_runway() {
        return estimatedRunnway;
    }

    public void setEstimated_runway(String estimated_runway) {
        this.estimatedRunnway = estimatedRunnway;
    }

    public String getActualrunway() {
        return actualRunway;
    }

    public void setActualrunway(String actual_runway) {
        this.actualRunway = actualRunway;
    }
}
