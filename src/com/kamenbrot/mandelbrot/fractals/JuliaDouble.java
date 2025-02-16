package com.kamenbrot.mandelbrot.fractals;

import com.kamenbrot.mandelbrot.complex.Complex;
import com.kamenbrot.mandelbrot.state.MandelState;

public class JuliaDouble implements Julia<Double> {
  //    private static final Complex JULIA_START = new Complex(0.285, 0.01);
  //    private static final Complex JULIA_START = new Complex(-0.7381, 0.2816);
  private static final Complex JULIA_START = new Complex(-1.768778833, -0.001738996);

  //    private static final Complex JULIA_START = new Complex(-0.3092, 0.6353);
  //    private static final Complex JULIA_START = new Complex(0.2438, 0.5598);
  //    private static final Complex JULIA_START = new Complex(0.2438, 0.5598);
  @Override
  public int juliaAt(int x, int y, MandelState<Double> mandelState) {
    final double real = Mapping.mapComplex(x, mandelState.getMandelWidth(), mandelState.getMinX(), mandelState.getMaxX());
    final double imaginary = Mapping.mapComplex(y, mandelState.getMandelHeight(), mandelState.getMinY(), mandelState.getMaxY());
    return julia(new Complex(real, imaginary), mandelState.getMaxIterations());
  }

  private static int julia(Complex z, int maxIterations) {
    int n = 0;
    while (z.abs() <= 2 && n < maxIterations) {
      z = z.times(z).plus(JULIA_START);
      n++;
    }
    return n;
  }
}
