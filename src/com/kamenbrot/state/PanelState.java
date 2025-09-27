package com.kamenbrot.state;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDateTime;

public class PanelState {

    private static final String OUTPUT_PATH = "out/mandelbrot";
    private static final int MAX_ITERATIONS = 6900;
    private static final int BLOCK_SIZE = 256;
    private static final int OPTIMIZATION_BLOCK_SIZE = 16;
    private static final int ZOOM_UNITS = 1;
    private static final int JOURNEY_UNITS = 1;

    private final String identifier;

    private int maxIterations;
    private int zoomUnits;
    private int journeyUnits;
    private int optimizationBlockSize;
    private int blockSize;
    private int width;
    private int height;

    public PanelState(int width, int height) {
        this.identifier = LocalDateTime.now().toString().replace(':', '-');
        this.maxIterations = MAX_ITERATIONS;
        this.zoomUnits = ZOOM_UNITS;
        this.journeyUnits = JOURNEY_UNITS;
        this.optimizationBlockSize = OPTIMIZATION_BLOCK_SIZE;
        this.blockSize = BLOCK_SIZE;
        this.width = width;
        this.height = height;
    }

    public File getOutputDir() {
        return Paths.get(OUTPUT_PATH + "/" + identifier).toFile();
    }

    public String getIdentifier() {
        return identifier;
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
    }
}