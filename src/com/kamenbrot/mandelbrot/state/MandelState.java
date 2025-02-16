package com.kamenbrot.mandelbrot.state;

public interface MandelState<T extends Number> {

    double MIN_X = -2.0;
    double MAX_X = 1.0;
    double MIN_Y = -1.5;
    double MAX_Y = 1.5;

    T getCenterX();
    T getCenterY();

    T getMinX();
    T getMinY();
    T getMaxX();
    T getMaxY();

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
}
