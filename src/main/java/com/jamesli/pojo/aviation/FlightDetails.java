package com.jamesli.pojo.aviation;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FlightDetails {
    private String number;
    private String iata;
    private String icao;

    @JsonProperty("codeshared")
    private Object codeShared;

    public void setCodeShared(Object codeShared) {
        this.codeShared = codeShared;
    }

    public String getIata() {
        return iata;
    }

    public void setIata(String iata) {
        this.iata = iata;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getIcao() {
        return icao;
    }

    public void setIcao(String icao) {
        this.icao = icao;
    }

    public Object getCodeShared() {
        return codeShared;
    }

}
