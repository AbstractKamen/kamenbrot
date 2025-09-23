package com.kamenbrot.fractals;

import java.math.BigDecimal;
import java.math.MathContext;

public final class DoubleDouble extends Number implements Comparable<DoubleDouble> {

    public static final DoubleDouble ZERO = DoubleDouble.valueOf(0.0f);
    public static final DoubleDouble EPSILON = DoubleDouble.valueOf(1e-28);
    public static final DoubleDouble TWO = DoubleDouble.valueOf(2.0f);
    public static final DoubleDouble FOUR = DoubleDouble.valueOf(4.0f);

    private final double hi;
    private final double lo;

    public DoubleDouble(double value) {
        this.hi = value;
        this.lo = 0.0;
    }

    private DoubleDouble(double hi, double lo) {
        this.hi = hi;
        this.lo = lo;
    }

    public static DoubleDouble valueOf(double value) {
        return new DoubleDouble(value);
    }

    // cool arithmetic
    public DoubleDouble add(DoubleDouble b) {
        double s = this.hi + b.hi;
        double v = s - this.hi;
        double t = (b.hi - v) + (this.hi - (s - v)) + this.lo + b.lo;
        double newHi = s + t;
        double newLo = t - (newHi - s);
        return new DoubleDouble(newHi, newLo);
    }

    public DoubleDouble sub(DoubleDouble b) {
        return this.add(new DoubleDouble(-b.hi, -b.lo));
    }

    private static final double SPLIT = (1L << 27) + 1; // 2^27+1, works for IEEE double
    public DoubleDouble mul(DoubleDouble b) {
        double p = this.hi * b.hi;

        // Dekker split
        double a1 = this.hi * SPLIT;
        double aBig = a1 - (a1 - this.hi);
        double aSmall = this.hi - aBig;

        double b1 = b.hi * SPLIT;
        double bBig = b1 - (b1 - b.hi);
        double bSmall = b.hi - bBig;

        double err = ((aBig * bBig - p) + aBig * bSmall + aSmall * bBig) + aSmall * bSmall;
        double q = this.hi * b.lo + this.lo * b.hi;

        double newHi = p + (err + q);
        double newLo = (p - newHi) + (err + q) + this.lo * b.lo;

        return new DoubleDouble(newHi, newLo);
    }

    public DoubleDouble div(DoubleDouble b) {
        double q1 = this.hi / b.hi;
        DoubleDouble approx = new DoubleDouble(q1);
        DoubleDouble prod = b.mul(approx);
        DoubleDouble diff = this.sub(prod);
        double q2 = diff.hi / b.hi;
        return approx.add(new DoubleDouble(q2));
    }

    public boolean epsilonGreaterThanDifference(DoubleDouble other) {
        double s = this.hi - other.hi;
        double v = s - this.hi;
        double t = ((-other.hi) - v) + (this.hi - (s - v)) + this.lo - other.lo;

        double diffHi = s + t;
        double diffLo = t - (diffHi - s);

        if (diffHi < 0) {
            diffHi = -diffHi;
            diffLo = -diffLo;
        }

        if (diffHi < EPSILON.hi) return true;
        return diffHi == EPSILON.hi && diffLo <= EPSILON.lo;
    }

    @Override
    public int compareTo(DoubleDouble b) {
        if (this.hi < b.hi) return -1;
        if (this.hi > b.hi) return 1;
        return Double.compare(this.lo, b.lo);
    }

    @Override
    public int intValue() {
        return (int) (hi + lo);
    }

    @Override
    public long longValue() {
        return (long) (hi + lo);
    }

    @Override
    public float floatValue() {
        return (float) (hi + lo);
    }

    @Override
    public double doubleValue() {
        return hi + lo;
    }

    @Override
    public String toString() {
        BigDecimal bd = BigDecimal.valueOf(hi).add(BigDecimal.valueOf(lo));
        return bd.round(new MathContext(34)).toPlainString();
    }
}