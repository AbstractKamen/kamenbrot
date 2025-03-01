package com.kamenbrot.mandelbrot.fractals;

import com.kamenbrot.mandelbrot.complex.Complex;
import com.kamenbrot.mandelbrot.state.MandelDoubleState;
import com.kamenbrot.mandelbrot.state.MandelState;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class CpuMandelbrotImpl implements CpuMandelbrot {
  private static final BigDecimal FOUR = new BigDecimal(4, new MathContext(50, RoundingMode.HALF_DOWN));

  @Override
  public int mandelbrotAt(int x, int y, MandelState mandelState) {
    return switch (mandelState) {
      case MandelState s when s.isPerformanceToggled() && (x % s.maxSkipped() == 0 && y % s.maxSkipped() == 0) -> {
        yield mandelState.getMaxIterations();
      }
      case MandelDoubleState doubleState -> {
        final double real = Mapping.mapComplex(x, mandelState.getMandelWidth(), doubleState.getMinX(), doubleState.getMaxX());
        final double imaginary = Mapping.mapComplex(y, mandelState.getMandelHeight(), doubleState.getMinY(), doubleState.getMaxY());
        if (mandelState.isSaveToggled()) {
          yield Fractal.fractalIteration_smooth(new Complex(0, 0), mandelState.getMaxIterations(), new Complex(real, imaginary));
        } else {
          yield Fractal.fractalIteration(new Complex(0, 0), mandelState.getMaxIterations(), new Complex(real, imaginary));
        }
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
