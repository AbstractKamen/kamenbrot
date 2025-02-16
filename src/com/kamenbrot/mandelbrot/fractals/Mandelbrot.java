package com.kamenbrot.mandelbrot.fractals;

import com.kamenbrot.mandelbrot.state.MandelState;

public interface Mandelbrot<T extends Number> {

  int mandelbrotAt(int x, int y, MandelState<T> mandelState);

}
