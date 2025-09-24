package com.kamenbrot.fractals.mandelbrot;

import com.kamenbrot.fractals.ComplexMapping;
import com.kamenbrot.fractals.DoubleDouble;
import com.kamenbrot.state.MandelDoubleDoubleState;
import com.kamenbrot.state.MandelDoubleState;
import com.kamenbrot.state.MandelState;

import java.math.BigDecimal;
import java.math.MathContext;

import static com.kamenbrot.generators.JuliaBlockImageGenerator.JULIA_IMAG;
import static com.kamenbrot.generators.JuliaBlockImageGenerator.JULIA_REAL;

public class CpuJulia {
  private static final BigDecimal FOUR = new BigDecimal(4);

  public static <T extends Number> int juliaAt(int x, int y, MandelState mandelState, T re, T imag) {
    return switch (mandelState) {
      case MandelState s when s.isPerformanceToggled() && (x % s.maxSkipped() == 0 && y % s.maxSkipped() == 0) -> {
        yield mandelState.getMaxIterations();
      }
      case MandelDoubleState doubleState -> {
        final double real = ComplexMapping.mapComplex(x, mandelState.getMandelWidth(), doubleState.getMinX(), doubleState.getMaxX());
        final double imaginary = ComplexMapping.mapComplex(y, mandelState.getMandelHeight(), doubleState.getMinY(), doubleState.getMaxY());
        yield CpuMandelbrot.fractalIteration(real, imaginary, mandelState.getMaxIterations(), re.doubleValue(), imag.doubleValue(), mandelState);
      }
      case MandelDoubleDoubleState doubleState -> {
        final DoubleDouble real = ComplexMapping.mapComplexOptimised(doubleState.cachedValue(x), doubleState.cachedValue(mandelState.getMandelWidth()), doubleState.getMinX(), doubleState.getMaxX());
        final DoubleDouble imaginary = ComplexMapping.mapComplexOptimised(doubleState.cachedValue(y), doubleState.cachedValue(mandelState.getMandelHeight()), doubleState.getMinY(), doubleState.getMaxY());
        yield CpuMandelbrot.fractalIteration(real, imaginary, mandelState.getMaxIterations(), (DoubleDouble) re, (DoubleDouble) imag, mandelState);
      }
      default -> throw new UnsupportedOperationException("State Not Implemented");
    };
  }

  private int julia(BigDecimal real, BigDecimal imaginary, int maxIterations, MathContext mc) {
    BigDecimal zRe = real;
    BigDecimal zIm = imaginary;

    int n = 0;
    while (zRe.multiply(zRe, mc).add(zIm.multiply(zIm, mc), mc)
      .compareTo(FOUR)
      <= 0 && n < maxIterations) {
      final BigDecimal newRe = zRe.multiply(zRe, mc).subtract(zIm.multiply(zIm, mc), mc).add(JULIA_REAL, mc);
      final BigDecimal newIm = zRe.multiply(zIm, mc).multiply(BigDecimal.TWO, mc).add(JULIA_IMAG, mc);

      zRe = newRe;
      zIm = newIm;
      n++;
    }
    return n;
  }

}
