package com.kamenbrot.fractals.mandelbrot;

import com.kamenbrot.fractals.Complex;
import com.kamenbrot.fractals.ComplexMapping;
import com.kamenbrot.state.MandelDoubleState;
import com.kamenbrot.state.MandelState;

import java.math.BigDecimal;
import java.math.MathContext;

public class CpuJulia {
    private static final BigDecimal FOUR = new BigDecimal(4);

    //    private static final Complex JULIA_START = new Complex(0.285, 0.01);
    //    private static final Complex JULIA_START = new Complex(-0.7381, 0.2816);
    private static final Complex JULIA_START = new Complex(-1.768778833, -0.001738996);
    //    private static final Complex JULIA_START = new Complex(-0.3092, 0.6353);
    //    private static final Complex JULIA_START = new Complex(0.2438, 0.5598);
//      private static final Complex JULIA_START = new Complex(0.2438, 0.5598);
    public static final BigDecimal JULIA_IMAG = BigDecimal.valueOf(JULIA_START.im());
    public static final BigDecimal JULIA_REAL = BigDecimal.valueOf(JULIA_START.re());

    public static int juliaAt(int x, int y, MandelState mandelState) {
        return switch (mandelState) {
            case MandelState s when s.isPerformanceToggled() && (x % s.maxSkipped() == 0 && y % s.maxSkipped() == 0) -> {
                yield mandelState.getMaxIterations();
            }
            case MandelDoubleState doubleState -> {
                final double real = ComplexMapping.mapComplex(x, mandelState.getMandelWidth(), doubleState.getMinX(), doubleState.getMaxX());
                final double imaginary = ComplexMapping.mapComplex(y, mandelState.getMandelHeight(), doubleState.getMinY(), doubleState.getMaxY());
                if (mandelState.isSmoothToggled()) {
                    yield CpuMandelbrot.fractalIteration_smooth(new Complex(real, imaginary), mandelState.getMaxIterations(), JULIA_START);
                } else {
                    yield CpuMandelbrot.fractalIteration(new Complex(real, imaginary), mandelState.getMaxIterations(), JULIA_START);
                }
            }
            default -> throw new UnsupportedOperationException("State Not Implemented");
        };
    }

    private int julia(BigDecimal real, BigDecimal imaginary, int maxIterations, MathContext mc) {
        BigDecimal zRe = real;
        BigDecimal zIm = imaginary;

        int n = 0;
        while (zRe.multiply(zRe, mc).add(zIm.multiply(zIm, mc), mc)
                .compareTo(FOUR)
                <= 0 && n < maxIterations) {
            final BigDecimal newRe = zRe.multiply(zRe, mc).subtract(zIm.multiply(zIm, mc), mc).add(JULIA_REAL, mc);
            final BigDecimal newIm = zRe.multiply(zIm, mc).multiply(BigDecimal.TWO, mc).add(JULIA_IMAG, mc);

            zRe = newRe;
            zIm = newIm;
            n++;
        }
        return n;
    }

}
