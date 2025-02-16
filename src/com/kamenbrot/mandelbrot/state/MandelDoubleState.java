package com.kamenbrot.mandelbrot.state;

import com.kamenbrot.mandelbrot.fractals.Mapping;

public class MandelDoubleState implements MandelState<Double> {

  private double zoomFactor = 0.2;
  private double minX = MIN_X;
  private double maxX = MAX_X;
  private double minY = MIN_Y;
  private double maxY = MAX_Y;
  private double centerX;
  private double centerY;
  private double zoom = 1;
  private double savedZoom = zoom;
  private boolean juliaToggle = false;
  private boolean saveToggle = false;
  private int mandelWidth;
  private int mandelHeight;
  private int maxIterations;

  public MandelDoubleState(int maxIterations, int mandelWidth, int mandelHeight) {
    this.maxIterations = maxIterations;
    this.mandelHeight = mandelHeight;
    this.mandelWidth = mandelWidth;
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
  public void incrementZoomFactor() {
    this.zoomFactor = Math.min(1, zoomFactor + 0.1);
  }

  @Override
  public void decrementZoomFactor() {
    this.zoomFactor = Math.max(0.1, zoomFactor - 0.1);
  }

  @Override
  public void zoomIn(int units) {
    double z = 1.0 - units * zoomFactor;
    this.zoom += units * zoomFactor;
    calcZoom(z);
  }

  @Override
  public void zoomOut(int units) {
    double z = 1.0 + units * zoomFactor;
    this.zoom -= units * zoomFactor;
    calcZoom(z);
  }

  @Override
  public double getCurrentZoom() {
    return zoom;
  }

  @Override
  public double getZoomFactor() {
    return zoomFactor;
  }

  @Override
  public void saveCurrentZoom() {
    this.savedZoom = zoom;
  }

  @Override
  public double getSavedZoom() {
    return savedZoom;
  }

  @Override
  public void setCenter(int x, int y) {
    this.centerX = Mapping.mapComplex(x, mandelWidth, minX, maxX);
    this.centerY = Mapping.mapComplex(y, mandelHeight, minY, maxY);
    calcZoom(1);
  }

  @Override
  public boolean isJuliaToggled() {
    return juliaToggle;
  }

  @Override
  public boolean isSaveToggled() {
    return saveToggle;
  }

  @Override
  public void toggleJulia() {
    this.juliaToggle = !juliaToggle;
  }

  @Override
  public void toggleSave() {
    this.saveToggle = !saveToggle;
  }

  @Override
  public int getMandelHeight() {
    return mandelHeight;
  }

  @Override
  public int getMandelWidth() {
    return mandelWidth;
  }

  @Override
  public void setMandelWidth(int mandelWidth) {
    this.mandelWidth = mandelWidth;
  }

  @Override
  public void setMandelHeight(int mandelHeight) {
    this.mandelHeight = mandelHeight;
  }

  @Override
  public void resetCoordinates() {
    this.minX = MIN_X;
    this.maxX = MAX_X;
    this.minY = MIN_Y;
    this.maxY = MAX_Y;
    calcZoom(this.zoom = 1);
  }

  @Override
  public int getMaxIterations() {
    return maxIterations;
  }

  /*
   * HELPERS
   */

  private void calcZoom(double z) {
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
