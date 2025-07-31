package com.jamesli.pojo.amadeus.flightoffers;

import java.util.List;

public class Itinerary {
    private String duration;
    private List<Segment> segments;

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public List<Segment> getSegments() { return segments; }
    public void setSegments(List<Segment> segments) { this.segments = segments; }
}
