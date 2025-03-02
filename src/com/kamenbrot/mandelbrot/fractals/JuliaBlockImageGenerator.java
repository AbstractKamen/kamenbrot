package com.kamenbrot.mandelbrot.fractals;

import com.kamenbrot.mandelbrot.state.MandelState;
import com.kamenbrot.mandelbrot.state.PanelState;

import java.util.concurrent.ForkJoinPool;

public class JuliaBlockImageGenerator extends MandelbrotBlockImageGenerator {

    private final MandelState mandelState;

    public JuliaBlockImageGenerator(ForkJoinPool pool, MandelState mandelState, PanelState panelState) {
        super(pool, mandelState, panelState);
        this.mandelState = mandelState;
    }

    @Override
    protected int mandelbrotAt(int x, int y) {
        return CpuJulia.juliaAt(x, y, mandelState);
    }
}
