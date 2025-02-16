package com.kamenbrot.mandelbrot.colors;

import java.awt.*;
import java.util.Arrays;

public class PaletteGenerator {

    public static Color[] generatePalette(Color[] baseColors, int newSize) {
        if (newSize <= baseColors.length) return Arrays.copyOf(baseColors, baseColors.length);
        final Color[] expandedPalette = new Color[newSize];

        final int steps = newSize / (baseColors.length);

        int expandedI = 0;
        for (int i = 1; i < baseColors.length && expandedI < newSize; i++) {
            final Color start = baseColors[i - 1];
            final Color end = baseColors[i];

            for (int j = 0; j < steps; j++) {
                final float t = (float) j / steps;
                expandedPalette[expandedI++] = interpolateColor(start, end, t);
            }
        }

        if (expandedI < newSize - 1) {
            final Color start = baseColors[baseColors.length - 2];
            final Color end = baseColors[baseColors.length - 1];
            for (int j = 0; expandedI < newSize; j++) {
                float t = (float) j / steps;
                expandedPalette[expandedI++] = interpolateColor(start, end, t);
            }
        } else {
            expandedPalette[newSize - 1] = baseColors[baseColors.length - 1];
        }
        return expandedPalette;
    }

    private static Color interpolateColor(Color start, Color end, float t) {
        final int r = (int) (start.getRed() + t * (end.getRed() - start.getRed()));
        final int g = (int) (start.getGreen() + t * (end.getGreen() - start.getGreen()));
        final int b = (int) (start.getBlue() + t * (end.getBlue() - start.getBlue()));
        return new Color(r, g, b);
    }

    public static void main(String[] args) {
        final Color[] colors = generatePalette(CoolColors.getCoolColors1(), 777);

        for (Color color : colors) {
            System.out.println(color);
        }
    }
}