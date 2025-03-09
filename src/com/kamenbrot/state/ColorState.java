package com.kamenbrot.state;

import com.kamenbrot.palette.PaletteGenerator;

import java.awt.*;

public class ColorState {

  private Color[] palette;
  private Color[] colors;

  public ColorState(Color[] palette) {
    this.palette = palette;
    this.colors = PaletteGenerator.generatePalette(palette, palette.length);
  }

  public Color getColor(int iterations, int maxIterations) {
    if (iterations == maxIterations) return Color.BLACK;
    return colors[iterations % colors.length];
  }

  public Color getColor_smooth(int iterations, int maxIterations) {
    if (iterations == maxIterations) return Color.BLACK; // Escape case

    double normalized = (double) iterations / maxIterations; // Normalize to [0,1]
    double exponent = 1.8; // S value (adjust for different effects)
    int paletteSize = colors.length; // N in the formula

    double v = Math.pow(normalized, exponent) * paletteSize; // Exponentially scale
    int index = (int) v % paletteSize; // Keep within palette bounds

    return colors[index];
  }

  public void doubleColors() {
    this.colors = PaletteGenerator.generatePalette(palette, colors.length << 1);
  }

  public void halveColors() {
    this.colors = PaletteGenerator.generatePalette(palette, colors.length >> 1);
  }

  public void setPalette(Color[] palette) {
    this.palette = palette;
    this.colors = PaletteGenerator.generatePalette(palette, colors.length);
  }

  public Color[] getColors() {
    return colors;
  }

  public Color[] getPalette() {
    return palette;
  }
}
