package com.kamenbrot.state;

import com.kamenbrot.fractals.ComplexMapping;
import com.kamenbrot.fractals.DoubleDouble;

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
    private DoubleDouble centerX = DoubleDouble.ZERO;
    private DoubleDouble centerY = DoubleDouble.ZERO;
    private DoubleDouble savedMaxX = maxX;
    private final DoubleDouble[] cache;

    public MandelDoubleDoubleState(int maxIterations, int mandelWidth, int mandelHeight, Map<Integer, Color> colorCache) {
        super(maxIterations, mandelWidth, mandelHeight, colorCache);
        final int len = mandelWidth * mandelHeight;
        cache = new DoubleDouble[len];
        for (int i = 0; i < len; i++) {
            cache[i] = DoubleDouble.valueOf(i);
        }
    }

    public MandelDoubleDoubleState(MandelDoubleState other) {
        super(other);
        final int len = getMandelWidth() * getMandelHeight();
        cache = new DoubleDouble[len];
        for (int i = 0; i < len; i++) {
            cache[i] = DoubleDouble.valueOf(i);
        }

        this.minX = DoubleDouble.valueOf(other.getMinX());
        this.maxX = DoubleDouble.valueOf(other.getMaxX());
        this.minY = DoubleDouble.valueOf(other.getMinY());
        this.maxY = DoubleDouble.valueOf(other.getMaxY());
        this.centerX = DoubleDouble.valueOf(other.getCenterX());
        this.centerY = DoubleDouble.valueOf(other.getCenterY());
        this.savedMaxX = DoubleDouble.valueOf(other.getSavedMaxX());
    }

    @Override
    public DoubleDouble cachedValue(int i) {
        return cache[i];
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
        this.centerX = ComplexMapping.mapComplexOptimised(cache[x], cache[getMandelWidth()], minX, maxX);
        this.centerY = ComplexMapping.mapComplexOptimised(cache[y], cache[getMandelHeight()], minY, maxY);
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
        DoubleDouble rangeX = maxX.sub(minX).mul(DoubleDouble.valueOf(z));
        DoubleDouble rangeY = maxY.sub(minY).mul(DoubleDouble.valueOf(z));

        this.minX = centerX.sub(rangeX.div(DoubleDouble.TWO));
        this.maxX = centerX.add(rangeX.div(DoubleDouble.TWO));
        this.minY = centerY.sub(rangeY.div(DoubleDouble.TWO));
        this.maxY = centerY.add(rangeY.div(DoubleDouble.TWO));
    }

    public DoubleDouble getSavedMaxX() {
        return savedMaxX;
    }
}