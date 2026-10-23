package com.kamenbrot.fractals;

import java.math.BigDecimal;
import java.math.MathContext;

public final class DoubleDouble extends Number implements Comparable<DoubleDouble> {

    private static final DoubleDouble ZERO = DoubleDouble.valueOf(0.0f);
    private static final DoubleDouble EPSILON = DoubleDouble.valueOf(1e-28);
    private static final DoubleDouble TWO = DoubleDouble.valueOf(2.0f);
    private static final DoubleDouble FOUR = DoubleDouble.valueOf(4.0f);

    private double hi;
    private double lo;

    public double hi() {
        return hi;
    }

    public double lo() {
        return lo;
    }

    public DoubleDouble setHiLo(double hi, double lo) {
        this.hi = hi;
        this.lo = lo;
        return this;
    }

    public DoubleDouble setHiLo(DoubleDouble other) {
        return setHiLo(other.hi, other.lo);
    }

    public DoubleDouble(double value) {
        this.hi = value;
        this.lo = 0.0;
    }

    public DoubleDouble timesTwo() {
        return add(this.hi, this.lo);
    }

    private DoubleDouble(double hi, double lo) {
        this.hi = hi;
        this.lo = lo;
    }

    public static DoubleDouble valueOf(double value) {
        return new DoubleDouble(value);
    }

    // cooler arithmetic
    public DoubleDouble add(double hi, double lo) {
        // Knuth twosum
        double s = this.hi + hi;
        double v = s - this.hi;
        double e = (this.hi - (s - v)) + (hi - v);

        double t = e + this.lo + lo;

        this.hi = s + t;
        this.lo = t - (this.hi - s);
        return this;
    }

    public DoubleDouble sub(double hi, double lo) {
        return add(-hi, -lo);
    }

    public static final double SPLIT = (1L << 27) + 1; // 2^27+1, works for IEEE double 134_217_729

    public DoubleDouble mul(double hi, double lo) {
        double p = this.hi * hi;

        // Dekker split
        double a1 = this.hi * SPLIT;
        double aBig = a1 - (a1 - this.hi);
        double aSmall = this.hi - aBig;

        double b1 = hi * SPLIT;
        double bBig = b1 - (b1 - hi);
        double bSmall = hi - bBig;

        double err = ((aBig * bBig - p) + aBig * bSmall + aSmall * bBig) + aSmall * bSmall;
        double q = this.hi * lo + this.lo * hi;

        this.hi = p + (err + q);
        this.lo = (p - this.hi) + (err + q);
        return this;
    }

    public DoubleDouble div(double hi, double lo) {
        // Initial approximation
        double q1 = this.hi / hi;

        // approximate product: b * q1
        double p = hi * q1;

        // Dekker splitting
        double bh = hi * SPLIT;
        double bBig = bh - (bh - hi);
        double bSmall = hi - bBig;

        double qh = q1 * SPLIT;
        double qBig = qh - (qh - q1);
        double qSmall = q1 - qBig;

        double err = ((bBig * qBig - p) + bBig * qSmall + bSmall * qBig) + bSmall * qSmall;
        double prodLo = (hi * q1 - p) + err + lo * q1 + hi * 0.0; // lo component ignored here

        double dHi = this.hi - p;
        double dLo = (this.hi - (p + dHi)) + dHi + this.lo - prodLo;

        // quotient with correction term
        double q2 = (dHi + dLo) / hi;

        // refinement to initial estimate
        double s = q1 + q2;
        double v = s - q1;
        double t = (q2 - v) + (q1 - (s - v));

        this.hi = s + t;
        this.lo = t - (this.hi - s);

        return this;
    }

    public DoubleDouble mapComplex(double limitHi, double limitLo, DoubleDouble min, DoubleDouble max) {
        // value.div(limit)
        double divResHi;
        double divResLo;
        {
            double q1 = this.hi / limitHi;

            double p = limitHi * q1;

            double bh = limitHi * SPLIT;
            double bBig = bh - (bh - limitHi);
            double bSmall = limitHi - bBig;

            double qh = q1 * SPLIT;
            double qBig = qh - (qh - q1);
            double qSmall = q1 - qBig;

            double err = ((bBig * qBig - p) + bBig * qSmall + bSmall * qBig) + bSmall * qSmall;
            double prodLo = (limitHi * q1 - p) + err + limitLo * q1 + limitHi * 0.0;

            double dHi = this.hi - p;
            double dLo = (this.hi - (p + dHi)) + dHi + this.lo - prodLo;

            double q2 = (dHi + dLo) / limitHi;

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
            divResMulMaxSubMinResLo = (p - divResMulMaxSubMinResHi) + (err + q);
        }

        // min.add(divResMulMaxSubMinRes)
        {
            double s = min.hi + divResMulMaxSubMinResHi;
            double v = s - min.hi;
            double t = (divResMulMaxSubMinResHi - v) + (min.hi - (s - v)) + min.lo + divResMulMaxSubMinResLo;
            this.hi = s + t;
            this.lo = t - (this.hi - s);
        }
        return this;
    }

    public boolean epsilonGreaterThanDifference(double hi, double lo) {
        double s = this.hi - hi;
        double v = s - this.hi;
        double t = ((-hi) - v) + (this.hi - (s - v)) + this.lo - lo;

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