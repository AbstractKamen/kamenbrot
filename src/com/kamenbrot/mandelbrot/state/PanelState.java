package com.kamenbrot.mandelbrot.state;

import com.kamenbrot.mandelbrot.colors.PaletteGenerator;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;

public class PanelState {

    private static final String OUTPUT_PATH = "out/mandelbrot";
    private static final int MAX_ITERATIONS = 1200;
    private static final int BLOCK_SIZE = 128;
    private static final int OPTIMIZATION_BLOCK_SIZE = 2;
    private static final int ZOOM_UNITS = 1;
    private static final int JOURNEY_UNITS = 1;

    private final String identifier;
    private BufferedImage image;

    private Color[] palette;
    private Color[] colors;

    private int maxIterations;
    private int zoomUnits;
    private int journeyUnits;
    private int optimizationBlockSize;
    private int blockSize;
    private int width;
    private int height;

    public PanelState(int width, int height, Color[] palette, int colorsLength) {
        this.identifier = LocalDateTime.now().toString().replace(':', '-');
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        this.palette = palette;
        this.colors = PaletteGenerator.generatePalette(palette, colorsLength);
        this.maxIterations = MAX_ITERATIONS;
        this.zoomUnits = ZOOM_UNITS;
        this.journeyUnits = JOURNEY_UNITS;
        this.optimizationBlockSize = OPTIMIZATION_BLOCK_SIZE;
        this.blockSize = BLOCK_SIZE;
        this.width = width;
        this.height = height;
    }

    public void saveImage() {
        try {
            final File dirFile = getOutputDir();
            if (!dirFile.exists()) dirFile.mkdir();
            final File outputImage = new File(dirFile, System.currentTimeMillis() + ".jpg");
            outputImage.createNewFile();
            ImageIO.write(image, "jpg", outputImage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public File getOutputDir() {
        return Paths.get(OUTPUT_PATH + "/" + identifier).toFile();
    }

    public String getIdentifier() {
        return identifier;
    }

    public Color getColor(int iterations) {
        if (iterations == maxIterations) return Color.BLACK;
        return colors[iterations % colors.length];
    }

    public void doubleColors() {
        this.colors = PaletteGenerator.generatePalette(palette, colors.length << 1);
    }

    public void halveColors() {
        this.colors = PaletteGenerator.generatePalette(palette, colors.length >> 1);
    }

    public void resizePanel(int width, int height) {
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    public void setPalette(Color[] palette) {
        this.palette = palette;
        this.colors = PaletteGenerator.generatePalette(palette, colors.length);
    }

    public BufferedImage getImage() {
        return image;
    }

    public Color[] getColors() {
        return colors;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public int getZoomUnits() {
        return zoomUnits;
    }

    public int getJourneyUnits() {
        return journeyUnits;
    }

    public int getOptimizationBlockSize() {
        return optimizationBlockSize;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public void setOptimizationBlockSize(int optimizationBlockSize) {
        this.optimizationBlockSize = optimizationBlockSize;
    }

    public void setJourneyUnits(int journeyUnits) {
        this.journeyUnits = journeyUnits;
    }

    public void setZoomUnits(int zoomUnits) {
        this.zoomUnits = zoomUnits;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public void setWidthAndHeight(int width, int height) {
        this.width = width;
        this.height = height;
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

}
