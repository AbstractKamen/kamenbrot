package com.kamenbrot.mandelbrot.fractals;

import com.kamenbrot.mandelbrot.complex.Complex;

public class Fractal {

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
}
