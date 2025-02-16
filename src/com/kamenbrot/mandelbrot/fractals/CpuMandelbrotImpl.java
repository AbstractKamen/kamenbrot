package com.kamenbrot.mandelbrot.fractals;

import com.kamenbrot.mandelbrot.complex.Complex;
import com.kamenbrot.mandelbrot.state.MandelBigDecimalState;
import com.kamenbrot.mandelbrot.state.MandelDoubleState;
import com.kamenbrot.mandelbrot.state.MandelState;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class CpuMandelbrotImpl implements CpuMandelbrot {
  private static final BigDecimal FOUR = new BigDecimal(4, new MathContext(50, RoundingMode.HALF_DOWN));

  @Override
  public int mandelbrotAt(int x, int y, MandelState mandelState) {
    if (mandelState.isPerformanceToggled() && (x % mandelState.maxSkipped() == 0 || y % mandelState.maxSkipped() == 0)) {
      return mandelState.getMaxIterations();
    } else if (mandelState instanceof MandelDoubleState doubleState) {
      final double real = Mapping.mapComplex(x, mandelState.getMandelWidth(), doubleState.getMinX(), doubleState.getMaxX());
      final double imaginary = Mapping.mapComplex(y, mandelState.getMandelHeight(), doubleState.getMinY(), doubleState.getMaxY());
      return mandelbrot(new Complex(real, imaginary), mandelState.getMaxIterations());

    } else if (mandelState instanceof MandelBigDecimalState decimalState) {
      final BigDecimal real = Mapping.mapComplex(decimalState.getDecimalCache()[x], decimalState.getDecimalCache()[mandelState.getMandelWidth()], decimalState.getMinX(), decimalState.getMaxX(), decimalState.getMathContext());
      final BigDecimal imaginary = Mapping.mapComplex(decimalState.getDecimalCache()[y], decimalState.getDecimalCache()[mandelState.getMandelHeight()], decimalState.getMinY(), decimalState.getMaxY(), decimalState.getMathContext());
      return mandelbrot(real, imaginary, decimalState.getMaxIterations(), decimalState.getMathContext());

    } else {
      throw new UnsupportedOperationException("State Not Implemented");
    }
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


  private int mandelbrot(Complex c, int maxIterations) {
    Complex z = new Complex(0, 0);
    int n = 0;
    while (z.abs() <= 2 && n < maxIterations) {
      z = z.times(z).plus(c);
      n++;
    }
    return n;

  }
}
