package com.kamenbrot.state;

import java.awt.*;
import java.util.Map;

public abstract class MandelStateAbstract<T extends Number> implements GenericMandelState<T> {

  private static final double ZOOM_FACTOR_UNIT = 0.05;
  private double zoomFactor = 0.1;
  private double zoom = 1;
  private boolean juliaToggle = false;
  private boolean saveToggle = false;
  private boolean performanceToggle = false;
  private boolean smoothToggle = false;
  private boolean zoomBundled = false;
  private int mandelWidth;
  private int mandelHeight;
  private int maxIterations;
  private Map<Integer, Color> colorCache;
  private final int[] iterationSteps;
  private double iterationSkipFactor = 0.3;

  public MandelStateAbstract(int maxIterations, int mandelWidth, int mandelHeight, Map<Integer, Color> colorCache) {
	this.maxIterations = maxIterations;
	this.mandelHeight = mandelHeight;
	this.mandelWidth = mandelWidth;
	this.colorCache = colorCache;
	this.iterationSteps = new int[maxIterations];
	recomputeIterationSteps();
  }

  @Override
  public void clearColorCache() {
	colorCache.clear();
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
  public boolean isSmoothToggled() {
	return smoothToggle;
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
  public void toggleSmooth() {
	this.smoothToggle = !smoothToggle;
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
  public Map<Integer, Color> getColorCache() {
	return colorCache;
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
	this.zoomFactor = Math.min(0.95, zoomFactor + ZOOM_FACTOR_UNIT);
  }

  @Override
  public void decrementZoomFactor() {
	this.zoomFactor = Math.max(0.05, zoomFactor - ZOOM_FACTOR_UNIT);
  }

  @Override
  public void zoomIn(int units) {
	double factor = Math.pow(1 + zoomFactor, units);
	this.zoom += units * zoomFactor;
	calcZoom(1.0 / factor);
  }

  @Override
  public void zoomOut(int units) {
	double factor = Math.pow(1 + zoomFactor, units);
	this.zoom -= units * zoomFactor;
	calcZoom(factor);
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
  public int getNextIteration(int iteration) {
	return iteration + iterationSteps[iteration];
  }

  /**
   * Computes each iteration step using "Exponential Threshold-Based Growth" in order to reduce the total number of iterations
   */
  public void recomputeIterationSteps() {
	for (int i = 0; i < iterationSteps.length; i++) {
	  // f(x)=x+(e^x/T) - smallest step being 1
	  iterationSteps[i] = Math.max(1, (int) (Math.pow(Math.E, ((double) i) / maxIterations * 10 * iterationSkipFactor)));
	  // System.out.println(iterationSteps[i]);
	}
  }

}