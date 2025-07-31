package com.jamesli.pojo.aviation;

import java.util.List;

public class FlightApiResponse {
    private Pagination pagination;
    private List<Flight> data;

    public Pagination getPagination() { return pagination;}

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public List<Flight> getData() {
        return data;
    }

    public void setData(List<Flight> data) {
        this.data = data;
    }
}
