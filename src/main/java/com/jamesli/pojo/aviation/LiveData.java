package com.jamesli.pojo.aviation;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LiveData {
    private String updated;
    private double latitude;
    private double longitude;
    private double altitude;
    private double direction;

    @JsonProperty("speed_horizontal")
    private double speedHorizontal;

    @JsonProperty("speed_vertical")
    private double speedVertical;

    @JsonProperty("is_ground")
    private boolean isGround;

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getDirection() {
        return direction;
    }

    public void setDirection(double direction) {
        this.direction = direction;
    }

    public double getSpeedHorizontal() {
        return speedHorizontal;
    }

    public void setSpeedHorizontal(double speedHorizontal) {
        this.speedHorizontal = speedHorizontal;
    }

    public double getSpeedVertical() {
        return speedVertical;
    }

    public void setSpeedVertical(double speedVertical) {
        this.speedVertical = speedVertical;
    }

    public boolean isGround() {
        return isGround;
    }

    public void setGround(boolean ground) {
        isGround = ground;
    }
}
