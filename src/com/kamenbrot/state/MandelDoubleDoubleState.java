package com.kamenbrot.state;

import com.kamenbrot.fractals.DoubleDouble;
import com.kamenbrot.util.RingBuffer;

import java.awt.Color;
import java.util.Map;

public class MandelDoubleDoubleState extends MandelStateAbstract<DoubleDouble> {

    private static final DoubleDouble MIN_X = new DoubleDouble(GenericMandelState.MIN_X);
    private static final DoubleDouble MAX_X = new DoubleDouble(GenericMandelState.MAX_X);
    private static final DoubleDouble MIN_Y = new DoubleDouble(GenericMandelState.MIN_Y);
    private static final DoubleDouble MAX_Y = new DoubleDouble(GenericMandelState.MAX_Y);

    private DoubleDouble minX = MIN_X;
    private DoubleDouble maxX = MAX_X;
    private DoubleDouble minY = MIN_Y;
    private DoubleDouble maxY = MAX_Y;
    private DoubleDouble centerX = DoubleDouble.valueOf(0.0);
    private DoubleDouble centerY = DoubleDouble.valueOf(0.0);
    private DoubleDouble savedMaxX = maxX;
    private final ThreadLocal<RingBuffer<DoubleDouble>> cache;

    public MandelDoubleDoubleState(int maxIterations, int mandelWidth, int mandelHeight, Map<Integer, Color> colorCache) {
        super(maxIterations, mandelWidth, mandelHeight, colorCache);
        this.cache = ThreadLocal.withInitial(() -> {
            final int len = 4096;
            RingBuffer<DoubleDouble> b = new RingBuffer<>(len);
            for (int i = 0; i < len; i++) {
                b.enqueue(DoubleDouble.valueOf(i));
            }
            return b;
        });
    }

    public MandelDoubleDoubleState(MandelDoubleState other) {
        super(other);
        this.cache = ThreadLocal.withInitial(() -> {
            final int len = 4096;
            RingBuffer<DoubleDouble> b = new RingBuffer<>(len);
            for (int i = 0; i < len; i++) {
                b.enqueue(DoubleDouble.valueOf(i));
            }
            return b;
        });
        final MandelDoubleState.SavedDoubleDoubleState savedDoubleDoubleState = other.getSavedDoubleDoubleState();
        if (savedDoubleDoubleState != null) {
            this.minX = savedDoubleDoubleState.minX();
            this.maxX = savedDoubleDoubleState.maxX();
            this.minY = savedDoubleDoubleState.minY();
            this.maxY = savedDoubleDoubleState.maxY();
            this.centerX = savedDoubleDoubleState.centerX();
            this.centerY = savedDoubleDoubleState.centerY();
            this.savedMaxX = savedDoubleDoubleState.savedMaxX();
        } else {
            this.minX = DoubleDouble.valueOf(other.getMinX());
            this.maxX = DoubleDouble.valueOf(other.getMaxX());
            this.minY = DoubleDouble.valueOf(other.getMinY());
            this.maxY = DoubleDouble.valueOf(other.getMaxY());
            this.centerX = DoubleDouble.valueOf(other.getCenterX());
            this.centerY = DoubleDouble.valueOf(other.getCenterY());
            this.savedMaxX = DoubleDouble.valueOf(other.getSavedMaxX());
        }
    }

    @Override
    public DoubleDouble cachedValue(int i) {
        return cache.get().dequeue().setHiLo(i, 0.0);
    }

    public MandelDoubleDoubleState(PanelState panelState, Map<Integer, Color> colorCache) {
        this(panelState.getMaxIterations(), panelState.getWidth(), panelState.getHeight(), colorCache);
    }

    @Override
    public DoubleDouble getCenterX() {
        return centerX;
    }

    @Override
    public DoubleDouble getCenterY() {
        return centerY;
    }

    @Override
    public String getCenterXString() {
        return centerX.hi() + "...";
    }

    @Override
    public String getCenterYString() {
        return centerY.hi() + "...";
    }

    @Override
    public DoubleDouble getMinX() {
        return minX;
    }

    @Override
    public DoubleDouble getMinY() {
        return minY;
    }

    @Override
    public DoubleDouble getMaxX() {
        return maxX;
    }

    @Override
    public DoubleDouble getMaxY() {
        return maxY;
    }

    @Override
    public void setCenter(int x, int y) {
        this.centerX.setHiLo(x, 0.0).mapComplex(getMandelWidth(), 0.0, minX, maxX);
        this.centerY.setHiLo(y, 0.0).mapComplex(getMandelHeight(), 0.0, minY, maxY);
        calcZoom(1);
    }

    @Override
    public void resetCoordinates() {
        this.minX = MIN_X;
        this.maxX = MAX_X;
        this.minY = MIN_Y;
        this.maxY = MAX_Y;
        calcZoom(super.setZoom(1));
    }

    @Override
    public void saveCurrentZoom() {
        this.savedMaxX = maxX;
    }

    @Override
    public boolean isZoomInReached() {
        return savedMaxX.compareTo(maxX) >= 0;
    }

    @Override
    public boolean isZoomOutReached() {
        return savedMaxX.compareTo(maxX) <= 0;
    }

    @Override
    protected void calcZoom(double z) {

        double minXhi = minX.hi();
        double minXlo = minX.lo();

        double minYhi = minY.hi();
        double minYlo = minY.lo();

        maxX.sub(minXhi, minXlo).mul(z, 0.0).div(2.0, 0.0);
        double rangeXhi = maxX.hi();
        double rangeXlo = maxX.lo();

        maxY.sub(minYhi, minYlo).mul(z, 0.0).div(2.0, 0.0);
        double rangeYhi = maxY.hi();
        double rangeYlo = maxY.lo();

        this.minX.setHiLo(centerX).sub(rangeXhi, rangeXlo);
        this.maxX.setHiLo(centerX).add(rangeXhi, rangeXlo);
        this.minY.setHiLo(centerY).sub(rangeYhi, rangeYlo);
        this.maxY.setHiLo(centerY).add(rangeYhi, rangeYlo);
    }

    public DoubleDouble getSavedMaxX() {
        return savedMaxX;
    }
}