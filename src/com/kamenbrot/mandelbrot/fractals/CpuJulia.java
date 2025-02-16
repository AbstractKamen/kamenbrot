package com.kamenbrot.mandelbrot.fractals;

import com.kamenbrot.mandelbrot.state.MandelState;

public interface CpuJulia {

  int juliaAt(int x, int y, MandelState mandelState);

}
