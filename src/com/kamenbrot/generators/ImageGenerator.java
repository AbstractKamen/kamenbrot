package com.kamenbrot.generators;

import java.awt.image.BufferedImage;

public interface ImageGenerator {

    BufferedImage getImage();
    void generateImage();
    void resizeImage(int width, int height);
}
