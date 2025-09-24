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
        // Initial approximation
        double q1 = this.hi / b.hi;

        // approximate product: b * q1
        double p = b.hi * q1;

        // Dekker splitting
        double bh = b.hi * SPLIT;
        double bBig = bh - (bh - b.hi);
        double bSmall = b.hi - bBig;

        double qh = q1 * SPLIT;
        double qBig = qh - (qh - q1);
        double qSmall = q1 - qBig;

        double err = ((bBig * qBig - p) + bBig * qSmall + bSmall * qBig) + bSmall * qSmall;
        double prodLo = (b.hi * q1 - p) + err + b.lo * q1 + b.hi * 0.0; // lo component ignored here

        double dHi = this.hi - p;
        double dLo = (this.hi - (p + dHi)) + dHi + this.lo - prodLo;

        // quotient with correction term
        double q2 = (dHi + dLo) / b.hi;

        // refinement to initial estimate
        double s = q1 + q2;
        double v = s - q1;
        double t = (q2 - v) + (q1 - (s - v));

        double newHi = s + t;
        double newLo = t - (newHi - s);

        return new DoubleDouble(newHi, newLo);
    }

    /**
     * Inlined {@link ComplexMapping#mapComplex(DoubleDouble, DoubleDouble, DoubleDouble, DoubleDouble)}
     *
     * @param limit param
     * @param min   param
     * @param max   param
     * @return mapped complex
     */
    public DoubleDouble mapComplex(DoubleDouble limit, DoubleDouble min, DoubleDouble max) {
        // value.div(limit)
        double divResHi;
        double divResLo;
        {
            double q1 = this.hi / limit.hi;

            double p = limit.hi * q1;

            double bh = limit.hi * SPLIT;
            double bBig = bh - (bh - limit.hi);
            double bSmall = limit.hi - bBig;

            double qh = q1 * SPLIT;
            double qBig = qh - (qh - q1);
            double qSmall = q1 - qBig;

            double err = ((bBig * qBig - p) + bBig * qSmall + bSmall * qBig) + bSmall * qSmall;
            double prodLo = (limit.hi * q1 - p) + err + limit.lo * q1 + limit.hi * 0.0;

            double dHi = this.hi - p;
            double dLo = (this.hi - (p + dHi)) + dHi + this.lo - prodLo;

            double q2 = (dHi + dLo) / limit.hi;

            double s = q1 + q2;
            double v = s - q1;
            double t = (q2 - v) + (q1 - (s - v));

            divResHi = s + t;
            divResLo = t - (divResHi - s);
        }

        // max.sub(min)
        double maxSubMinResHi;
        double maxSubMInResLo;
        {
            double s = max.hi - min.hi;
            double v = s - max.hi;
            double t = ((-min.hi) - v) + (max.hi - (s - v)) + max.lo - min.lo;
            maxSubMinResHi = s + t;
            maxSubMInResLo = t - (maxSubMinResHi - s);
        }

        // divRes.mul(maxSubMinRes)
        double divResMulMaxSubMinResHi;
        double divResMulMaxSubMinResLo;
        {
            double p = divResHi * maxSubMinResHi;

            double a1 = divResHi * SPLIT;
            double aBig = a1 - (a1 - divResHi);
            double aSmall = divResHi - aBig;

            double b1 = maxSubMinResHi * SPLIT;
            double bBig = b1 - (b1 - maxSubMinResHi);
            double bSmall = maxSubMinResHi - bBig;

            double err = ((aBig * bBig - p) + aBig * bSmall + aSmall * bBig) + aSmall * bSmall;
            double q = divResHi * maxSubMInResLo + divResLo * maxSubMinResHi;

            divResMulMaxSubMinResHi = p + (err + q);
            divResMulMaxSubMinResLo = (p - divResMulMaxSubMinResHi) + (err + q) + divResLo * maxSubMInResLo;
        }

        // min.add(divResMulMaxSubMinRes)
        double newHi;
        double newLo;
        {
            double s = min.hi + divResMulMaxSubMinResHi;
            double v = s - min.hi;
            double t = (divResMulMaxSubMinResHi - v) + (min.hi - (s - v)) + min.lo + divResMulMaxSubMinResLo;
            newHi = s + t;
            newLo = t - (newHi - s);
        }
        return new DoubleDouble(newHi, newLo);
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

    public boolean addGreaterThanFour(DoubleDouble b) {
        double s = this.hi + b.hi;
        double v = s - this.hi;
        double t = (b.hi - v) + (this.hi - (s - v)) + this.lo + b.lo;
        double newHi = s + t;
        double newLo = t - (newHi - s);
        if (newHi > FOUR.hi) return true;
        return newHi == FOUR.hi && newLo > FOUR.lo;
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