package com.kamenbrot.fractals.mandelbrot;

import com.kamenbrot.fractals.Complex;
import com.kamenbrot.fractals.ComplexMapping;
import com.kamenbrot.state.MandelDoubleState;
import com.kamenbrot.state.MandelState;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class CpuMandelbrot {
    private static final BigDecimal FOUR = new BigDecimal(4, new MathContext(50, RoundingMode.HALF_DOWN));

    public static final double LOG_4 = Math.log(4);

    public static int fractalIteration(Complex z, int maxIterations, Complex c) {
        for (int i = 0; i < maxIterations; i++) {
            z = z.times(z).plus(c);
            if (z.re() * z.re() + z.im() * z.im() > 4) return i;
        }
        return maxIterations;
    }

    public static int fractalIteration_smooth(Complex z, int maxIterations, Complex c) {
        for (int i = 0; i < maxIterations; i++) {
            z = z.times(z).plus(c);
            if (z.re() * z.re() + z.im() * z.im() > 4)
                return (int) (i + 1 - Math.log(Math.log(z.re() * z.re() + z.im() * z.im())) / LOG_4);
        }
        return maxIterations;
    }

    public static int mandelbrotAt(int x, int y, MandelState mandelState) {
        return switch (mandelState) {
            case MandelState s when s.isPerformanceToggled() && (x % s.maxSkipped() == 0 && y % s.maxSkipped() == 0) -> {
                yield mandelState.getMaxIterations();
            }
            case MandelDoubleState doubleState -> {
                final double real = ComplexMapping.mapComplex(x, mandelState.getMandelWidth(), doubleState.getMinX(), doubleState.getMaxX());
                final double imaginary = ComplexMapping.mapComplex(y, mandelState.getMandelHeight(), doubleState.getMinY(), doubleState.getMaxY());
                if (mandelState.isSmoothToggled()) {
                    yield fractalIteration_smooth(new Complex(0, 0), mandelState.getMaxIterations(), new Complex(real, imaginary));
                } else {
                    yield fractalIteration(new Complex(0, 0), mandelState.getMaxIterations(), new Complex(real, imaginary));
                }
            }
            default -> throw new UnsupportedOperationException("State Not Implemented");
        };
    }

    private int mandelbrot(BigDecimal real, BigDecimal imaginary, int maxIterations, MathContext mc) {
        BigDecimal zRe = BigDecimal.ZERO;
        BigDecimal zIm = BigDecimal.ZERO;

        int n = 0;
        while (zRe.multiply(zRe, mc).add(zIm.multiply(zIm, mc), mc)
                .compareTo(FOUR)
                <= 0 && n < maxIterations) {
            final BigDecimal newRe = zRe.multiply(zRe, mc).subtract(zIm.multiply(zIm, mc), mc).add(real, mc);
            final BigDecimal newIm = zRe.multiply(zIm, mc).multiply(BigDecimal.TWO, mc).add(imaginary, mc);

            zRe = newRe;
            zIm = newIm;
            n++;
        }
        return n;
    }
}
