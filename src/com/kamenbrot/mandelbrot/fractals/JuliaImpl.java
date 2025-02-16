package com.kamenbrot.mandelbrot.fractals;

import com.kamenbrot.mandelbrot.complex.Complex;
import com.kamenbrot.mandelbrot.state.MandelBigDecimalState;
import com.kamenbrot.mandelbrot.state.MandelDoubleState;
import com.kamenbrot.mandelbrot.state.MandelState;

import java.math.BigDecimal;
import java.math.MathContext;

public class JuliaImpl implements Julia {
  private static final BigDecimal FOUR = new BigDecimal(4);

  //    private static final Complex JULIA_START = new Complex(0.285, 0.01);
  //    private static final Complex JULIA_START = new Complex(-0.7381, 0.2816);
  private static final Complex JULIA_START = new Complex(-1.768778833, -0.001738996);

  //    private static final Complex JULIA_START = new Complex(-0.3092, 0.6353);
  //    private static final Complex JULIA_START = new Complex(0.2438, 0.5598);
  //    private static final Complex JULIA_START = new Complex(0.2438, 0.5598);
  @Override
  public int juliaAt(int x, int y, MandelState mandelState) {

    if (mandelState instanceof MandelDoubleState doubleState) {
      final double real = Mapping.mapComplex(x, mandelState.getMandelWidth(), doubleState.getMinX(), doubleState.getMaxX());
      final double imaginary = Mapping.mapComplex(y, mandelState.getMandelHeight(), doubleState.getMinY(), doubleState.getMaxY());
      return julia(new Complex(real, imaginary), mandelState.getMaxIterations());

    } else if (mandelState instanceof MandelBigDecimalState decimalState) {
      final BigDecimal real = Mapping.mapComplex(decimalState.getDecimalCache()[x], decimalState.getDecimalCache()[mandelState.getMandelWidth()], decimalState.getMinX(), decimalState.getMaxX(), decimalState.getMathContext());
      final BigDecimal imaginary = Mapping.mapComplex(decimalState.getDecimalCache()[y], decimalState.getDecimalCache()[mandelState.getMandelHeight()], decimalState.getMinY(), decimalState.getMaxY(), decimalState.getMathContext());
      return julia(real, imaginary, decimalState.getMaxIterations(), decimalState.getMathContext());

    } else {
      throw new UnsupportedOperationException("State Not Implemented");
    }
  }

  private static int julia(Complex z, int maxIterations) {
    int n = 0;
    while (z.abs() <= 2 && n < maxIterations) {
      z = z.times(z).plus(JULIA_START);
      n++;
    }
    return n;
  }

  private int julia(BigDecimal real, BigDecimal imaginary, int maxIterations, MathContext mc) {
    //    BigDecimal zRe = BigDecimal.ZERO;
    //    BigDecimal zIm = BigDecimal.ZERO;

    BigDecimal zRe = new BigDecimal(JULIA_START.re(), mc);
    BigDecimal zIm = new BigDecimal(JULIA_START.im(), mc);

    int n = 0;
    while (zRe.multiply(zRe, mc).add(zIm.multiply(zIm, mc), mc)
      .compareTo(FOUR)
      <= 0 && n < maxIterations) {
      //      final BigDecimal newRe = zRe.multiply(zRe, mc).subtract(zIm.multiply(zIm, mc), mc).add(real, mc);
      final BigDecimal newRe = real.multiply(real, mc).subtract(imaginary.multiply(imaginary, mc), mc).add(zRe, mc);
      //      final BigDecimal newIm = zRe.multiply(zIm, mc).multiply(BigDecimal.TWO, mc).add(imaginary, mc);
      final BigDecimal newIm = real.multiply(imaginary, mc).multiply(BigDecimal.TWO, mc).add(zIm, mc);

      zRe = newRe;
      zIm = newIm;
      n++;
    }
    return n;
  }
}
