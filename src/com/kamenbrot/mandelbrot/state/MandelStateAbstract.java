package com.kamenbrot.mandelbrot.state;

public abstract class MandelStateAbstract<T extends Number> implements GenericMandelState<T> {

  private double zoomFactor = 0.1;
  private double zoom = 1;
  private double savedZoom = zoom;
  private boolean juliaToggle = false;
  private boolean saveToggle = false;
  private boolean performanceToggle = false;
  private int mandelWidth;
  private int mandelHeight;
  private int maxIterations;

  public MandelStateAbstract(int maxIterations, int mandelWidth, int mandelHeight) {
    this.maxIterations = maxIterations;
    this.mandelHeight = mandelHeight;
    this.mandelWidth = mandelWidth;
  }

  public MandelStateAbstract(MandelState mandelState) {
    this.zoomFactor = mandelState.getZoomFactor();
    this.zoom = mandelState.getCurrentZoom();
    this.savedZoom = mandelState.getSavedZoom();
    this.juliaToggle = mandelState.isJuliaToggled();
    this.saveToggle = mandelState.isSaveToggled();
    this.performanceToggle = mandelState.isPerformanceToggled();
    this.mandelWidth = mandelState.getMandelWidth();
    this.mandelHeight = mandelState.getMandelHeight();
    this.maxIterations = mandelState.getMaxIterations();
  }

  protected abstract void calcZoom(double z);

  protected double setZoom(double z) {
    return this.zoom = z;
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
  public boolean isPerformanceToggled() {
    return performanceToggle;
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
  public void togglePerformance() {
    this.performanceToggle = !performanceToggle;
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
  public int getMaxIterations() {
    return maxIterations;
  }

  @Override
  public void incrementZoomFactor() {
    this.zoomFactor = Math.min(0.95, zoomFactor + 0.05);
  }

  @Override
  public void decrementZoomFactor() {
    this.zoomFactor = Math.max(0.05, zoomFactor - 0.05);
  }

  @Override
  public void zoomIn(int units) {
    double z = 1 - zoomFactor;
    this.zoom += z;
    calcZoom(z);
  }

  @Override
  public void zoomOut(int units) {
    double z = 1 + zoomFactor;
    this.zoom -= z;
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

}
