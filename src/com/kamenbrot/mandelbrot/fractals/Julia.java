package com.kamenbrot.mandelbrot.fractals;

import com.kamenbrot.mandelbrot.state.MandelState;

public interface Julia<T extends Number> {

  int juliaAt(int x, int y, MandelState<T> mandelState);

}
