package com.kamenbrot.mandelbrot.fractals;

import java.math.BigDecimal;
import java.math.MathContext;

public class Mapping {

  public static double mapComplex(int value, int limit, double min, double max) {
    return min + (double) value / limit * (max - min);
  }

  public static BigDecimal mapComplex(BigDecimal value, BigDecimal limit, BigDecimal min, BigDecimal max, MathContext mc) {
    return min.add(value
                     .divide(limit, mc)
                     .multiply(max.subtract(min, mc), mc),
                   mc);
  }
}