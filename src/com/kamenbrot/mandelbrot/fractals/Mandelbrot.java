package com.kamenbrot.mandelbrot.fractals;

import com.kamenbrot.mandelbrot.state.MandelState;

public interface Mandelbrot {

  int mandelbrotAt(int x, int y, MandelState mandelState);

}
