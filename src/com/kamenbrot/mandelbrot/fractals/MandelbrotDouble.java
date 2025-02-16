package com.kamenbrot.mandelbrot.fractals;

import com.kamenbrot.mandelbrot.complex.Complex;
import com.kamenbrot.mandelbrot.state.MandelState;

public class MandelbrotDouble implements Mandelbrot<Double> {

  @Override
  public int mandelbrotAt(int x, int y, MandelState<Double> mandelState) {
    final double real = Mapping.mapComplex(x, mandelState.getMandelWidth(), mandelState.getMinX(), mandelState.getMaxX());
    final double imaginary = Mapping.mapComplex(y, mandelState.getMandelHeight(), mandelState.getMinY(), mandelState.getMaxY());
    return mandelbrot(new Complex(real, imaginary), mandelState.getMaxIterations());
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
