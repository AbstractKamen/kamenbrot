package com.kamenbrot.generators;

import com.kamenbrot.fractals.mandelbrot.CpuJulia;
import com.kamenbrot.state.ColourState;
import com.kamenbrot.state.GenericMandelState;
import com.kamenbrot.state.MandelState;
import com.kamenbrot.state.PanelState;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.concurrent.ForkJoinPool;

public class JuliaBlockImageGenerator<T extends Number> extends MandelbrotBlockImageGenerator {

  //    private static final Complex JULIA_START = new Complex(0.285, 0.01);
  //    private static final Complex JULIA_START = new Complex(-0.7381, 0.2816);
  //    private static final Complex JULIA_START = new Complex(-1.768778833, -0.001738996);
  //    private static final Complex JULIA_START = new Complex(-0.3092, 0.6353);
  //    private static final Complex JULIA_START = new Complex(0.2438, 0.5598);
  //    private static final Complex JULIA_START = new Complex(0.2438, 0.5598);
  public static final BigDecimal JULIA_IMAG = BigDecimal.valueOf(0);
  public static final BigDecimal JULIA_REAL = BigDecimal.valueOf(0);

  private T re;
  private T imag;

  public JuliaBlockImageGenerator(ImageGenerator imageGenerator, GenericMandelState<T> mandelState, PanelState panelState, ColourState colourState) {
    super(imageGenerator, mandelState, panelState, colourState);
    this.re = mandelState.getCenterX();
    this.imag = mandelState.getCenterY();
  }

  public JuliaBlockImageGenerator(MandelState mandelState, PanelState panelState, ColourState colourState, ForkJoinPool pool, int blockSize, int[] mandelCache, BufferedImage image, T re, T imag) {
    super(mandelState, panelState, pool, blockSize, mandelCache, image, colourState);
    this.re = re;
    this.imag = imag;
  }


  @Override
  protected int mandelbrotAt(int x, int y) {
    return CpuJulia.juliaAt(x, y, getMandelState(), re, imag);
  }

  public void setPos(T re, T imag) {
    this.re = re;
    this.imag = imag;
  }

  public T getRe() {
    return re;
  }

  public T getImag() {
    return imag;
  }
}
