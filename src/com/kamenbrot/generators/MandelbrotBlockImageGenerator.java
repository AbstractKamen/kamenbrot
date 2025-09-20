package com.kamenbrot.generators;

import com.kamenbrot.fractals.mandelbrot.CpuMandelbrot;
import com.kamenbrot.state.ColourState;
import com.kamenbrot.state.MandelState;
import com.kamenbrot.state.PanelState;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public class MandelbrotBlockImageGenerator extends BlockImageGeneratorAbstract {

    private MandelState mandelState;
    private PanelState panelState;
    private int[] mandelCache;
    private BufferedImage image;
    private ColourState colourState;

    public MandelbrotBlockImageGenerator(MandelState mandelState, PanelState panelState, ForkJoinPool pool, ColourState colourState) {
        this(mandelState, panelState, pool, panelState.getBlockSize(), new int[mandelState.getMandelWidth() * mandelState.getMandelHeight()], new BufferedImage(mandelState.getMandelWidth(), mandelState.getMandelHeight(), BufferedImage.TYPE_INT_RGB), colourState);
    }

    public MandelbrotBlockImageGenerator(MandelbrotBlockImageGenerator other, MandelState mandelState, PanelState panelState, ColourState colourState) {
        this(mandelState, panelState, other.getPool(), other.getBlockSize(), other.mandelCache, other.image, colourState);
    }

    public MandelbrotBlockImageGenerator(ImageGenerator imageGenerator, MandelState mandelState, PanelState panelState, ColourState colourState) {
        this(mandelState, panelState, (ForkJoinPool) Executors.newWorkStealingPool(), panelState.getBlockSize(), new int[mandelState.getMandelWidth() * mandelState.getMandelHeight()], imageGenerator.getImage(), colourState);
    }

    public MandelbrotBlockImageGenerator(MandelState mandelState, PanelState panelState, ForkJoinPool pool, int blockSize, int[] mandelCache, BufferedImage image, ColourState colourState) {
        super(pool, blockSize);
        this.mandelState = mandelState;
        this.panelState = panelState;
        this.mandelCache = mandelCache;
        this.image = image;
        this.colourState = colourState;
    }

    @Override
    protected void beforeGenerate() {
        // reset cache
        Arrays.fill(mandelCache, -1);
    }

    @Override
    protected void generateBlock(int x, int y) {
        final int blockSize = getBlockSize();
        final int optimizationBlockSize = panelState.getOptimizationBlockSize();
        for (int i = 0; i < blockSize; i += optimizationBlockSize) {
            for (int j = 0; j < blockSize; j += optimizationBlockSize) {
                generateFractalBlock(x + j, y + i, optimizationBlockSize);
            }
        }
    }

    @Override
    public void resizeImage(int width, int height) {
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        this.mandelCache = new int[width * height];
    }

    private void generateFractalBlock(int x, int y, int blockSize) {
        int optimizationBlockSize = blockSize;

        while (optimizationBlockSize > 2) {
            // Ensure we don't go out of bounds
            int x1 = Math.min(x, mandelState.getMandelWidth() - 1);
            int y1 = Math.min(y, mandelState.getMandelHeight() - 1);
            int x2 = Math.min(x + optimizationBlockSize, mandelState.getMandelWidth() - 1);
            int y2 = Math.min(y + optimizationBlockSize, mandelState.getMandelHeight() - 1);

            int c1 = mandelCache[x1 + mandelState.getMandelWidth() * y1] = mandelbrotAt(x1, y1);
            int c2 = mandelCache[x2 + mandelState.getMandelWidth() * y1] = mandelbrotAt(x2, y1);
            int c3 = mandelCache[x1 + mandelState.getMandelWidth() * y2] = mandelbrotAt(x1, y2);
            int c4 = mandelCache[x2 + mandelState.getMandelWidth() * y2] = mandelbrotAt(x2, y2);

            // If all corners are black, fill the entire block as black
            int maxIterations = panelState.getMaxIterations();
            if (c1 == maxIterations && c2 == maxIterations &&
                    c3 == maxIterations && c4 == maxIterations) {
                for (int i = 0; i < optimizationBlockSize; i++) {
                    for (int j = 0; j < optimizationBlockSize; j++) {
                        int px = x + i;
                        int py = y + j;
                        if (px < mandelState.getMandelWidth() && py < mandelState.getMandelHeight()) {
                            image.setRGB(px, py, Color.BLACK.getRGB());
                            mandelCache[px + mandelState.getMandelWidth() * py] = maxIterations;
                        }
                    }
                }
                break;
            } else {
                optimizationBlockSize = optimizationBlockSize >> 1;
            }
        }
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                int px = x + i;
                int py = y + j;
                if (px < mandelState.getMandelWidth() && py < mandelState.getMandelHeight()) {
                    final int index = px + mandelState.getMandelWidth() * py;

                    int it;
                    if ((it = mandelCache[index]) == -1) {
                        it = mandelCache[index] = mandelbrotAt(px, py);
                    }
                    if (mandelState.isSmoothToggled()) {
                        image.setRGB(px, py, mandelState.getColorCache().computeIfAbsent(it, k -> colourState.getColour_smooth(k, mandelState.getMaxIterations())).getRGB());
                    } else {
                        // no need to cache
                        image.setRGB(px, py, colourState.getColour(it, mandelState.getMaxIterations()).getRGB());
                    }
                }
            }
        }
    }

    protected MandelState getMandelState() {
        return mandelState;
    }

    protected int mandelbrotAt(int x, int y) {
        return CpuMandelbrot.mandelbrotAt(x, y, mandelState);
    }


    @Override
    public BufferedImage getImage() {
        return image;
    }
}
