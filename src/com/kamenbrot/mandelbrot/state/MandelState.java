package com.kamenbrot.mandelbrot.state;

public interface MandelState {
  void incrementZoomFactor();
  void decrementZoomFactor();

  void zoomIn( int units);
  void zoomOut(int units);

  double getCurrentZoom();
  double getZoomFactor();

  void saveCurrentZoom();
  double getSavedZoom();

  void setCenter(int x, int y);

  boolean isJuliaToggled();
  boolean isSaveToggled();

  void toggleJulia();
  void toggleSave();

  int getMandelHeight();
  int getMandelWidth();

  void setMandelWidth(int mandelWidth);
  void setMandelHeight(int mandelHeight);

  void resetCoordinates();
  int getMaxIterations();

  MandelState translate();
}
