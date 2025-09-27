package com.kamenbrot.fractals.mandelbrot;

import com.kamenbrot.fractals.ComplexMapping;
import com.kamenbrot.fractals.DoubleDouble;
import com.kamenbrot.state.MandelDoubleDoubleState;
import com.kamenbrot.state.MandelDoubleState;
import com.kamenbrot.state.MandelState;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class CpuMandelbrot {
    private static final BigDecimal FOUR = new BigDecimal(4, new MathContext(50, RoundingMode.HALF_DOWN));
    public static final double EPSILON = 1e-28;

    public static int fractalIteration(double zRe, double zIm, int maxIterations, double cRe, double cIm, MandelState mandelState) {
        double savedRe = 0;
        double savedIm = 0;
        for (int i = 0; i < maxIterations; ++i) {
            final double zReSq = zRe * zRe;
            final double zImSq = zIm * zIm;
            if (i % 20 == 0) {
                savedRe = zRe;
                savedIm = zIm;
            } else if (Math.abs(zRe - savedRe) < EPSILON &&
                    Math.abs(zIm - savedIm) < EPSILON) {
                return maxIterations;
            }
            if (zReSq + zImSq > 4) return i;
            double t = zRe * zIm;
            zRe = zReSq - zImSq + cRe;
            zIm = t + t + cIm;
            // z = z.times(z).plus(c);
        }
        return maxIterations;
    }

    public static int fractalIteration(DoubleDouble zRe, DoubleDouble zIm, int maxIterations, DoubleDouble cRe, DoubleDouble cIm, MandelState mandelState) {
        DoubleDouble savedRe = null;
        DoubleDouble savedIm = null;
        for (int i = 0; i < maxIterations; ++i) {
            final DoubleDouble zReSq = zRe.mul(zRe);
            final DoubleDouble zImSq = zIm.mul(zIm);

            if (i % 20 == 0) {
                savedRe = zRe;
                savedIm = zIm;
            } else if (zRe.epsilonGreaterThanDifference(savedRe) &&
                    zIm.epsilonGreaterThanDifference(savedIm)) {
                return maxIterations;
            }

            if (zReSq.addGreaterThanFour(zImSq)) return i;
            DoubleDouble zReCSq = zReSq.sub(zImSq);
            DoubleDouble t = zRe.mul(zIm);
            DoubleDouble zImCSq = t.add(t);
            zRe = zReCSq.add(cRe);
            zIm = zImCSq.add(cIm);
            // z = z.times(z).plus(c);
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
                yield fractalIteration(0.0d, 0.0d, mandelState.getMaxIterations(), real, imaginary, mandelState);
            }
            case MandelDoubleDoubleState doubleState -> {
                final DoubleDouble real = ComplexMapping.mapComplexOptimised(doubleState.cachedValue(x), doubleState.cachedValue(mandelState.getMandelWidth()), doubleState.getMinX(), doubleState.getMaxX());
                final DoubleDouble imaginary = ComplexMapping.mapComplexOptimised(doubleState.cachedValue(y), doubleState.cachedValue(mandelState.getMandelHeight()), doubleState.getMinY(), doubleState.getMaxY());
                yield fractalIteration(DoubleDouble.ZERO, DoubleDouble.ZERO, mandelState.getMaxIterations(), real, imaginary, mandelState);
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
