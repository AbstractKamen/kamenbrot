package com.kamenbrot.mandelbrot.fractals;

public class Mapping {

    public static double mapComplex(int value, int limit, double min, double max) {
        return min + (double) value / limit * (max - min);
    }

}
