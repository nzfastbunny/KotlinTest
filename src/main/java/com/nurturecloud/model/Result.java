package com.nurturecloud.model;

import java.math.BigDecimal;
import java.util.Comparator;

public class Result {
    private String suburb;
    private Integer postCode;
    private BigDecimal distance;

    public Result() {

    }

    public Result(String suburb, Integer postCode, BigDecimal distance) {
        this.suburb = suburb;
        this.postCode = postCode;
        this.distance = distance;
    }

    public String getSuburb() {
        return suburb;
    }

    public void setSuburb(String suburb) {
        this.suburb = suburb;
    }

    public Integer getPostCode() {
        return postCode;
    }

    public void setPostCode(Integer postCode) {
        this.postCode = postCode;
    }

    public BigDecimal getDistance() {
        return distance;
    }

    public void setDistance(BigDecimal distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return getSuburb().toUpperCase() + "  " + getPostCode();
    }
}
