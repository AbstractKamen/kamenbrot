package com.kamenbrot.state;

import com.kamenbrot.fractals.ComplexMapping;

import java.awt.*;
import java.util.Map;

public class MandelDoubleState extends MandelStateAbstract<Double> {

    private double minX = MIN_X;
    private double maxX = MAX_X;
    private double minY = MIN_Y;
    private double maxY = MAX_Y;
    private double centerX;
    private double centerY;
    private double savedMaxX = maxX;

    public MandelDoubleState(int maxIterations, int mandelWidth, int mandelHeight, Map<Integer, Color> colorCache) {
        super(maxIterations, mandelWidth, mandelHeight, colorCache);
    }

    public MandelDoubleState(PanelState panelState, Map<Integer, Color> colorCache) {
        this(panelState.getMaxIterations(), panelState.getWidth(), panelState.getHeight(), colorCache);
    }

    @Override
    public Double getCenterX() {
        return centerX;
    }

    @Override
    public Double getCenterY() {
        return centerY;
    }

    @Override
    public Double getMinX() {
        return minX;
    }

    @Override
    public Double getMinY() {
        return minY;
    }

    @Override
    public Double getMaxX() {
        return maxX;
    }

    @Override
    public Double getMaxY() {
        return maxY;
    }

    @Override
    public void setCenter(int x, int y) {
        this.centerX = ComplexMapping.mapComplex(x, getMandelWidth(), minX, maxX);
        this.centerY = ComplexMapping.mapComplex(y, getMandelHeight(), minY, maxY);
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
        return savedMaxX >= maxX;
    }

    @Override
    public boolean isZoomOutReached() {
        return savedMaxX <= maxX;
    }

    @Override
    protected void calcZoom(double z) {
        // Adjust bounds while keeping clicked point as the new center
        double rangeX = (maxX - minX) * z;
        double rangeY = (maxY - minY) * z;
        // Adjust new min and max
        this.minX = centerX - rangeX / 2;
        this.maxX = centerX + rangeX / 2;
        this.minY = centerY - rangeY / 2;
        this.maxY = centerY + rangeY / 2;
    }
}
