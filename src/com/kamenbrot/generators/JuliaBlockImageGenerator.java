package com.kamenbrot.generators;

import com.kamenbrot.fractals.mandelbrot.CpuJulia;
import com.kamenbrot.state.MandelState;
import com.kamenbrot.state.PanelState;

public class JuliaBlockImageGenerator extends MandelbrotBlockImageGenerator {

    public JuliaBlockImageGenerator(MandelbrotBlockImageGenerator imageGenerator, MandelState mandelState, PanelState panelState) {
        super(imageGenerator, mandelState, panelState);
    }

    public JuliaBlockImageGenerator(ImageGenerator imageGenerator, MandelState mandelState, PanelState panelState) {
        super(imageGenerator, mandelState, panelState);
    }

    public JuliaBlockImageGenerator(MandelState mandelState, PanelState panelState) {
        super(mandelState, panelState);
    }


    @Override
    protected int mandelbrotAt(int x, int y) {
        return CpuJulia.juliaAt(x, y, getMandelState());
    }
}
