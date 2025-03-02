package com.kamenbrot.generators;

import java.awt.image.BufferedImage;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public abstract class BlockImageGeneratorAbstract implements ImageGenerator {

    private final ForkJoinPool pool;
    private int blockSize;

    protected BlockImageGeneratorAbstract(BlockImageGeneratorAbstract imageGenerator) {
        this(imageGenerator.getPool(), imageGenerator.getBlockSize());
    }

    protected BlockImageGeneratorAbstract(ForkJoinPool pool, int blockSize) {
        this.pool = pool;
        this.blockSize = blockSize;
    }

    protected abstract void generateBlock(int x, int y);
    protected abstract void beforeGenerate();

    protected void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    protected ForkJoinPool getPool() {
        return pool;
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
        while (!pool.awaitQuiescence(10, TimeUnit.SECONDS)) ;
    }

    protected int getBlockSize() {
        return blockSize;
    }

}
