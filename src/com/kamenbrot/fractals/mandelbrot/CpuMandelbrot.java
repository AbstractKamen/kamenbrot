package com.kamenbrot.fractals.mandelbrot;

import com.kamenbrot.fractals.ComplexMapping;
import com.kamenbrot.state.MandelDoubleState;
import com.kamenbrot.state.MandelState;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class CpuMandelbrot {
  private static final BigDecimal FOUR = new BigDecimal(4, new MathContext(50, RoundingMode.HALF_DOWN));

  public static int fractalIteration(double zRe, double zIm, int maxIterations, double cRe, double cIm, MandelState mandelState) {
    for (int i = 0; i < maxIterations; i = mandelState.getNextIteration(i)) {
      final double zReSq = zRe * zRe;
      final double zImSq = zIm * zIm;
      if (zReSq + zImSq > 4) return i;
      double zReCSq = zReSq - zImSq;
      double t;
      double zImCSq = (t = zRe * zIm) + t;
      zRe = zReCSq + cRe;
      zIm = zImCSq + cIm;
      // z = z.times(z).plus(c);
    }
    return maxIterations;
  }

  public static int mandelbrotAt(int x, int y, MandelState mandelState) {
    return switch (mandelState) {
      case MandelState s when s.isPerformanceToggled() && (x % s.maxSkipped() == 0 && y % s.maxSkipped() == 0) -> {
        yield mandelState.getMaxIterations();
      }
      case MandelDoubleState doubleState -> {
        final double real = ComplexMapping.mapComplex(x, mandelState.getMandelWidth(), doubleState.getMinX(), doubleState.getMaxX());
        final double imaginary = ComplexMapping.mapComplex(y, mandelState.getMandelHeight(), doubleState.getMinY(), doubleState.getMaxY());
        yield fractalIteration(0.0d, 0.0d, mandelState.getMaxIterations(), real, imaginary, mandelState);
      }
      default -> throw new UnsupportedOperationException("State Not Implemented");
    };
  }

  private int mandelbrot(BigDecimal real, BigDecimal imaginary, int maxIterations, MathContext mc) {
    BigDecimal zRe = BigDecimal.ZERO;
    BigDecimal zIm = BigDecimal.ZERO;

    int n = 0;
    while (zRe.multiply(zRe, mc).add(zIm.multiply(zIm, mc), mc)
      .compareTo(FOUR)
      <= 0 && n < maxIterations) {
      final BigDecimal newRe = zRe.multiply(zRe, mc).subtract(zIm.multiply(zIm, mc), mc).add(real, mc);
      final BigDecimal newIm = zRe.multiply(zIm, mc).multiply(BigDecimal.TWO, mc).add(imaginary, mc);

      zRe = newRe;
      zIm = newIm;
      n++;
    }
    return n;
  }
}
