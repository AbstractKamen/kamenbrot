package com.kamenbrot.state;

import java.awt.*;

public class ColourState {

  private Color[] colours;

  public ColourState(Color[] colours) {
    this.colours = colours;
  }

  public Color getColour(int iterations, int maxIterations) {
    if (iterations == maxIterations) return Color.BLACK;
    return colours[iterations % colours.length];
  }

  public Color getColour_smooth(int iterations, int maxIterations) {
    if (iterations == maxIterations) return Color.BLACK; // Escape case

    double normalized = (double) iterations / maxIterations; // Normalize to [0,1]
    double exponent = 1.8; // S value (adjust for different effects)
    int paletteSize = colours.length; // N in the formula

    double v = Math.pow(normalized, exponent) * paletteSize; // Exponentially scale
    int index = (int) v % paletteSize; // Keep within palette bounds

    return colours[index];
  }

  public void setColours(Color[] colours) {
    this.colours = colours;
  }

  public Color[] getColours() {
    return colours;
  }

}