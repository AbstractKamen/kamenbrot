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
                final DoubleDouble real = doubleState.cachedValue(x).mapComplex(mandelState.getMandelWidth(), 0.0d, doubleState.getMinX(), doubleState.getMaxX());
                final DoubleDouble imaginary = doubleState.cachedValue(y).mapComplex(mandelState.getMandelHeight(), 0.0d, doubleState.getMinY(), doubleState.getMaxY());
                double cReHi = real.hi();
                double cReLo = real.lo();
                double cImHi = imaginary.hi();
                double cImLo = imaginary.lo();
                yield fractalIteration(real.setHiLo(0.0, 0.0), imaginary.setHiLo(0.0, 0.0), mandelState.getMaxIterations(), cReHi, cReLo, cImHi, cImLo, mandelState);
            }
            default -> throw new UnsupportedOperationException("State Not Implemented");
        };
    }

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

    public static int fractalIteration(DoubleDouble zRe, DoubleDouble zIm, int maxIterations, double cReHi, double cReLo, double cImHi, double cImLo, MandelState mandelState) {
        double savedZreHi = zRe.hi();
        double savedZreLo = zRe.lo();
        double savedZimHi = zRe.hi();
        double savedZimLo = zRe.lo();
        for (int i = 0; i < maxIterations; ++i) {
            double zreHi = zRe.hi();
            double zreLo = zRe.lo();
            zRe.mul(zreHi, zreLo);
            double zimHi = zIm.hi();
            double zimLo = zIm.lo();
            zIm.mul(zimHi, zimLo);

            if (i % 20 == 0) {
                savedZreHi = zreHi;
                savedZreLo = zreLo;
                savedZimHi = zimHi;
                savedZimLo = zimLo;
            } else if (zRe.epsilonGreaterThanDifference(savedZreHi, savedZreLo) &&
                    zIm.epsilonGreaterThanDifference(savedZimHi, savedZimLo)) {
                return maxIterations;
            }

            if (zRe.addGreaterThanFour(zIm)) return i;
            zRe.sub(zIm.hi(), zIm.lo())
                    .add(cReHi, cReLo);
            zIm.setHiLo(zreHi, zreLo)
                    .mul(zimHi, zimLo)
                    .timesTwo()
                    .add(cImHi, cImLo);
        }
        return maxIterations;
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
