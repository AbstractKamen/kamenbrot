package com.kamenbrot.state;

import com.kamenbrot.fractals.MutableDoubleDouble;
import com.kamenbrot.util.RingBuffer;

import java.awt.Color;
import java.util.Map;

public class MandelDoubleDoubleState extends MandelStateAbstract<MutableDoubleDouble> {

    private static final MutableDoubleDouble MIN_X = MutableDoubleDouble.valueOf(GenericMandelState.MIN_X);
    private static final MutableDoubleDouble MAX_X = MutableDoubleDouble.valueOf(GenericMandelState.MAX_X);
    private static final MutableDoubleDouble MIN_Y = MutableDoubleDouble.valueOf(GenericMandelState.MIN_Y);
    private static final MutableDoubleDouble MAX_Y = MutableDoubleDouble.valueOf(GenericMandelState.MAX_Y);

    private MutableDoubleDouble minX = MIN_X;
    private MutableDoubleDouble maxX = MAX_X;
    private MutableDoubleDouble minY = MIN_Y;
    private MutableDoubleDouble maxY = MAX_Y;
    private MutableDoubleDouble centerX = MutableDoubleDouble.valueOf(0.0);
    private MutableDoubleDouble centerY = MutableDoubleDouble.valueOf(0.0);
    private MutableDoubleDouble savedMaxX = maxX;
    private final ThreadLocal<RingBuffer<MutableDoubleDouble>> cache;

    private static RingBuffer<MutableDoubleDouble> getDoubleDoubleRingBuffer() {
        final int len = 4096;
        RingBuffer<MutableDoubleDouble> b = new RingBuffer<>(len);
        for (int i = 0; i < len; i++) {
            b.enqueue(MutableDoubleDouble.valueOf(i));
        }
        System.out.println("INFO: Init DoubleDouble buffer: " + b.capacity());
        return b;
    }

    public MandelDoubleDoubleState(int maxIterations, int mandelWidth, int mandelHeight, Map<Integer, Color> colorCache) {
        super(maxIterations, mandelWidth, mandelHeight, colorCache);
        this.cache = ThreadLocal.withInitial(MandelDoubleDoubleState::getDoubleDoubleRingBuffer);
    }

    public MandelDoubleDoubleState(MandelDoubleState other) {
        super(other);
        this.cache = ThreadLocal.withInitial(MandelDoubleDoubleState::getDoubleDoubleRingBuffer);
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
            this.minX = MutableDoubleDouble.valueOf(other.getMinX());
            this.maxX = MutableDoubleDouble.valueOf(other.getMaxX());
            this.minY = MutableDoubleDouble.valueOf(other.getMinY());
            this.maxY = MutableDoubleDouble.valueOf(other.getMaxY());
            this.centerX = MutableDoubleDouble.valueOf(other.getCenterX());
            this.centerY = MutableDoubleDouble.valueOf(other.getCenterY());
            this.savedMaxX = MutableDoubleDouble.valueOf(other.getSavedMaxX());
        }
    }

    @Override
    public MutableDoubleDouble cachedValue(int i) {
        return cache.get().dequeue().setHiLo(i, 0.0);
    }

    public MandelDoubleDoubleState(PanelState panelState, Map<Integer, Color> colorCache) {
        this(panelState.getMaxIterations(), panelState.getWidth(), panelState.getHeight(), colorCache);
    }

    @Override
    public MutableDoubleDouble getCenterX() {
        return centerX;
    }

    @Override
    public MutableDoubleDouble getCenterY() {
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
    public MutableDoubleDouble getMinX() {
        return minX;
    }

    @Override
    public MutableDoubleDouble getMinY() {
        return minY;
    }

    @Override
    public MutableDoubleDouble getMaxX() {
        return maxX;
    }

    @Override
    public MutableDoubleDouble getMaxY() {
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

    public MutableDoubleDouble getSavedMaxX() {
        return savedMaxX;
    }
}