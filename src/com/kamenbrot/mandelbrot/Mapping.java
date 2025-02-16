package com.kamenbrot.mandelbrot;

public class Mapping {

    public static double mapComplex(int value, int limit, double min, double max) {
        return min + (double) value / limit * (max - min);
    }

}
