package com.kamenbrot.fractals;

import java.math.BigDecimal;
import java.math.MathContext;

public class ComplexMapping {

  public static double mapComplex(int value, int limit, double min, double max) {
    return min + (double) value / limit * (max - min);
  }

  public static DoubleDouble mapComplex(DoubleDouble value, DoubleDouble limit, DoubleDouble min, DoubleDouble max) {
    final DoubleDouble fraction = value.div(limit);
    return min.add(fraction.mul(max.sub(min)));
  }

  public static BigDecimal mapComplex(BigDecimal value, BigDecimal limit, BigDecimal min, BigDecimal max, MathContext mc) {
    return min.add(value
                     .divide(limit, mc)
                     .multiply(max.subtract(min, mc), mc),
                   mc);
  }
}