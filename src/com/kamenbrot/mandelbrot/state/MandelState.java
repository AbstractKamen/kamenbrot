package com.kamenbrot.mandelbrot.state;

import java.awt.*;
import java.util.Map;

public interface MandelState {
  void incrementZoomFactor();
  void decrementZoomFactor();

  void zoomIn( int units);
  void zoomOut(int units);

  double getCurrentZoom();
  double getZoomFactor();

  void saveCurrentZoom();
  boolean isZoomInReached();
  boolean isZoomOutReached();

  void setCenter(int x, int y);

  boolean isJuliaToggled();
  boolean isSaveToggled();
  boolean isPerformanceToggled();
  boolean isSmoothToggled();

  default int  maxSkipped() {return 2;}

  void toggleSmooth();
  void toggleJulia();
  void toggleSave();
  void togglePerformance();

  int getMandelHeight();
  int getMandelWidth();

  void setMandelWidth(int mandelWidth);
  void setMandelHeight(int mandelHeight);

  void resetCoordinates();
  int getMaxIterations();

  void clearColorCache();
  Map<Integer, Color> getColorCache();
}
