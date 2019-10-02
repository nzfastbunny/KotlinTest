package com.nurturecloud.model;

import java.math.BigDecimal;

public class Suburb {
    private Integer Pcode;
    private String Locality;
    private String State;
    private BigDecimal Longitude;
    private BigDecimal Latitude;

    public Suburb() {

    }

    public Suburb(Integer pCode, String locality, String state, BigDecimal longitude, BigDecimal latitude) {
        this.Pcode = pCode;
        this.Locality = locality;
        this.State = state;
        this.Longitude = longitude;
        this.Latitude = latitude;
    }

    public Integer getPcode() {
        return Pcode;
    }

    public void setPcode(Integer pcode) {
        Pcode = pcode;
    }

    public String getLocality() {
        return Locality;
    }

    public void setLocality(String locality) {
        Locality = locality;
    }

    public String getState() {
        return State;
    }

    public void setState(String state) {
        State = state;
    }

    public BigDecimal getLongitude() {
        return Longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        Longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return Latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        Latitude = latitude;
    }

    @Override
    public String toString() {
        return "[Suburb: " + getLocality() + ", Post Code: " + getPcode() + "]";
    }
}
