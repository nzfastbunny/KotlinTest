package com.nurturecloud.model;

import java.util.Comparator;

public class DistanceComparator implements Comparator<Result> {
    public int compare(Result c1, Result c2) {
        return c1.getDistance().compareTo(c2.getDistance());
    }
}
