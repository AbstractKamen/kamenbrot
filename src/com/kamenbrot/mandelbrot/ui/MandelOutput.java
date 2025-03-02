package com.kamenbrot.mandelbrot.ui;

import com.kamenbrot.mandelbrot.io.GifSequenceWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class MandelOutput {

    public static void saveImage(String outputPath, BufferedImage image) {
        saveImage(Paths.get(outputPath).toFile(), image);
    }

    public static void saveImage(File dirFile, BufferedImage image) {
        try {
            if (!dirFile.exists()) dirFile.mkdir();
            final File outputImage = new File(dirFile, System.currentTimeMillis() + ".jpg");
            outputImage.createNewFile();
            ImageIO.write(image, "jpg", outputImage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void makeGif(String outputPath, String gifName) {
        makeGif(Paths.get(outputPath).toFile(), gifName);
    }

    public static void makeGif(File dirFile, String gifName) {
        // list the files before making the output gif
        final File[] frames = Objects.requireNonNull(dirFile.listFiles(), "no saved frames found");

        try (final GifSequenceWriter writer = new GifSequenceWriter(7, dirFile, gifName)) {

            final List<File> sortedFrames = Arrays.stream(frames)
                    .sorted(Comparator.comparingLong(f -> {
                        final String name = f.getName();
                        return Long.parseLong(name.substring(0, name.length() - 4));
                    }))
                    .toList();
            for (File frameFile : sortedFrames) {
                writer.writeToSequence(ImageIO.read(frameFile));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
