package com.kamenbrot.mandelbrot.fractals;

import java.awt.image.BufferedImage;
import java.util.concurrent.ForkJoinPool;

public abstract class BlockImageGeneratorAbstract implements ImageGenerator {

    private final ForkJoinPool pool;
    private int blockSize;

    public BlockImageGeneratorAbstract(ForkJoinPool pool, int blockSize) {
        this.pool = pool;
        this.blockSize = blockSize;
    }

    protected abstract void generateBlock(int x, int y);
    protected abstract void beforeGenerate();

    protected void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    @Override
    public void generateImage() {
        beforeGenerate();
        final BufferedImage image = getImage();
        for (int x = 0; x < image.getWidth(); x += blockSize) {
            for (int y = 0; y < image.getHeight(); y += blockSize) {
                final int X = x, Y = y;
                pool.execute(() -> generateBlock(X, Y));
            }
        }
    }

    protected int getBlockSize() {
        return blockSize;
    }

}
