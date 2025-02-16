package com.kamenbrot.mandelbrot.state;

import com.kamenbrot.mandelbrot.fractals.Mapping;

public class MandelDoubleState extends MandelStateAbstract<Double> {

  private double minX = MIN_X;
  private double maxX = MAX_X;
  private double minY = MIN_Y;
  private double maxY = MAX_Y;
  private double centerX;
  private double centerY;

  public MandelDoubleState(int maxIterations, int mandelWidth, int mandelHeight) {
    super(maxIterations, mandelWidth, mandelHeight);
  }

  public MandelDoubleState(MandelBigDecimalState mandelState) {
    super(mandelState);
    this.minX = mandelState.getMinX().doubleValue();
    this.maxX = mandelState.getMaxX().doubleValue();
    this.minY = mandelState.getMinY().doubleValue();
    this.maxY = mandelState.getMaxY().doubleValue();
    this.centerX = mandelState.getCenterX().doubleValue();
    this.centerY = mandelState.getCenterY().doubleValue();
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
    this.centerX = Mapping.mapComplex(x, getMandelWidth(), minX, maxX);
    this.centerY = Mapping.mapComplex(y, getMandelHeight(), minY, maxY);
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
  public MandelState translate() {
    return new MandelBigDecimalState(this);
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
