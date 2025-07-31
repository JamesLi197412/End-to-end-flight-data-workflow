package com.jamesli.pojo.amadeus.flightoffers;

public class Arrival {
    private String iataCode;
    private String terminal;
    private String at; // This will parse as a String, consider java.time.OffsetDateTime for better handling

    public String getIataCode() { return iataCode; }
    public void setIataCode(String iataCode) { this.iataCode = iataCode; }

    public String getTerminal() { return terminal; }
    public void setTerminal(String terminal) { this.terminal = terminal; }

    public String getAt() { return at; }
    public void setAt(String at) { this.at = at; }
}
