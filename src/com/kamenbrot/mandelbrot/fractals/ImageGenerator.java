package com.kamenbrot.mandelbrot.fractals;

import java.awt.image.BufferedImage;

public interface ImageGenerator {

    BufferedImage getImage();
    void generateImage();
    void resizeImage(int width, int height);
}
