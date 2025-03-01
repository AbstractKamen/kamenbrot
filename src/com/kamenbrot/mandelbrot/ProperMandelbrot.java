package com.kamenbrot.mandelbrot;

import com.kamenbrot.mandelbrot.colors.CoolColors;
import com.kamenbrot.mandelbrot.colors.PaletteGenerator;
import com.kamenbrot.mandelbrot.fractals.CpuJulia;
import com.kamenbrot.mandelbrot.fractals.CpuJuliaImpl;
import com.kamenbrot.mandelbrot.fractals.CpuMandelbrot;
import com.kamenbrot.mandelbrot.fractals.CpuMandelbrotImpl;
import com.kamenbrot.mandelbrot.state.MandelDoubleState;
import com.kamenbrot.mandelbrot.state.MandelState;

import javax.imageio.*;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class ProperMandelbrot extends JPanel implements AutoCloseable {

    private static final String OUTPUT_PATH = "out/mandelbrot";
    private static final int MAX_ITERATIONS = 1200;
    private static final int BLOCK_SIZE = 128;
    private static final int OPTIMIZATION_BLOCK_SIZE = 2;
    private static final int ZOOM_UNITS = 1;
    private static final int JOURNEY_UNITS = 1;

    private static Color[] PALETTE = CoolColors.getCoolColors2();

    private final BufferedImage image;
    private final Color[] colors;
    private final String identifier;
    private final int[] mandelCache;
    private MandelState mandelState;
    private final CpuMandelbrot mandelbrot;
    private final CpuJulia julia;
    private final ForkJoinPool pool;


    public ProperMandelbrot() {
        this.mandelState = new MandelDoubleState(MAX_ITERATIONS, 800, 600);
        this.mandelbrot = new CpuMandelbrotImpl();
        this.julia = new CpuJuliaImpl();
        this.colors = PaletteGenerator.generatePalette(PALETTE, 2 << 8);
        final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.mandelCache = new int[mandelState.getMandelWidth() * mandelState.getMandelHeight()];
        this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
        this.image = new BufferedImage(mandelState.getMandelWidth(), mandelState.getMandelHeight(), BufferedImage.TYPE_INT_RGB);
        this.identifier = LocalDateTime.now().toString().replace(':', '-');
        this.pool = (ForkJoinPool) Executors.newWorkStealingPool();
        generateImage();
    }
    public static void main(String[] args) {
        final JFrame frame = new JFrame("Mandelbrot Set");
        final ProperMandelbrot panel = new ProperMandelbrot();
        final KeyListener saveKey = new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyChar()) {
                    case 's':
                        panel.mandelState.toggleSave();
                        panel.repaint();
                        break;
                    case 'p':
                        panel.mandelState.togglePerformance();
                        panel.generateImage();
                        panel.repaint();
                        break;
                    case 'j':
                        panel.mandelState.toggleJulia();
                        panel.mandelState.resetCoordinates();
                        panel.generateImage();
                        panel.repaint();
                        break;
                    case 'G':
                        panel.makeGif();
                        break;
                    case 'g':
                        while (!panel.mandelState.isZoomInReached()) {
                            zoomIn(JOURNEY_UNITS, panel);
                            panel.generateImage();
                            panel.repaint();
                        }
                        break;
                    case 'h':
                        panel.mandelState.saveCurrentZoom();
                        panel.mandelState.resetCoordinates();
                        panel.generateImage();
                        panel.repaint();
                        break;
                    case '+':
                        panel.mandelState.incrementZoomFactor();
                        panel.repaint();
                        break;
                    case '-':
                        panel.mandelState.decrementZoomFactor();
                        panel.repaint();
                        break;
                }
            }

        };
        final MouseAdapter mouseAdapter = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                panel.mandelState.setCenter(e.getX(), e.getY());
                panel.generateImage();
                panel.repaint();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                final MandelState state = panel.mandelState;
                if (e.getPreciseWheelRotation() < 0) {
                    zoomIn(ZOOM_UNITS, panel);
                } else {
                    state.zoomOut(ZOOM_UNITS);
                }
                panel.generateImage();
                panel.repaint();
            }
        };
        panel.addMouseListener(mouseAdapter);
        panel.addMouseWheelListener(mouseAdapter);

        frame.addKeyListener(saveKey);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMaximumSize(new Dimension(panel.mandelState.getMandelWidth(), panel.mandelState.getMandelHeight()));
        frame.setMinimumSize(new Dimension(panel.mandelState.getMandelWidth(), panel.mandelState.getMandelHeight()));
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void zoomIn(int units, ProperMandelbrot panel) {
        MandelState state = panel.mandelState;
        state.zoomIn(units);
    }

    private void generateImage() {
        if (mandelState.isJuliaToggled()) {
            generateJulia();
        } else {
            generateMandelbrot();
        }
        while (!pool.awaitQuiescence(5, TimeUnit.SECONDS)) ;
        saveImage();
    }

    private void generateMandelbrot() {
        Arrays.fill(mandelCache, -1);
        for (int x = 0; x < mandelState.getMandelWidth(); x += BLOCK_SIZE) {
            for (int y = 0; y < mandelState.getMandelHeight(); y += BLOCK_SIZE) {
                final int X = x, Y = y;
                pool.execute(() -> generateFractal(X, Y, this::mandelbrotAt));
            }
        }
    }

    private int mandelbrotAt(int x, int y) {
        return mandelbrot.mandelbrotAt(x, y, mandelState);
    }

    private void generateJulia() {
        Arrays.fill(mandelCache, -1);
        for (int x = 0; x < mandelState.getMandelWidth(); x += BLOCK_SIZE) {
            for (int y = 0; y < mandelState.getMandelHeight(); y += BLOCK_SIZE) {
                final int X = x, Y = y;
                pool.execute(() -> generateFractal(X, Y, this::juliaAt));
            }
        }
    }

    private int juliaAt(int x, int y) {
        return julia.juliaAt(x, y, mandelState);
    }

    private void generateFractal(int x, int y, BinaryIntFunc atFunc) {
        for (int i = 0; i < BLOCK_SIZE; i += OPTIMIZATION_BLOCK_SIZE) {
            for (int j = 0; j < BLOCK_SIZE; j += OPTIMIZATION_BLOCK_SIZE) {
                generateFractalBlock(x + j, y + i, atFunc, OPTIMIZATION_BLOCK_SIZE);
            }
        }
    }

    private void generateFractalBlock(int x, int y, BinaryIntFunc atFunc, int blockSize) {
        int optimizationBlockSize = blockSize;

        while (optimizationBlockSize > 2) {
            // Ensure we don't go out of bounds
            int x1 = Math.min(x, mandelState.getMandelWidth() - 1);
            int y1 = Math.min(y, mandelState.getMandelHeight() - 1);
            int x2 = Math.min(x + optimizationBlockSize, mandelState.getMandelWidth() - 1);
            int y2 = Math.min(y + optimizationBlockSize, mandelState.getMandelHeight() - 1);

            int c1 = mandelCache[x1 + mandelState.getMandelWidth() * y1] = atFunc.apply(x1, y1);
            int c2 = mandelCache[x2 + mandelState.getMandelWidth() * y1] = atFunc.apply(x2, y1);
            int c3 = mandelCache[x1 + mandelState.getMandelWidth() * y2] = atFunc.apply(x1, y2);
            int c4 = mandelCache[x2 + mandelState.getMandelWidth() * y2] = atFunc.apply(x2, y2);

            // If all corners are black, fill the entire block as black
            if (c1 == MAX_ITERATIONS && c2 == MAX_ITERATIONS &&
                    c3 == MAX_ITERATIONS && c4 == MAX_ITERATIONS) {
                for (int i = 0; i < optimizationBlockSize; i++) {
                    for (int j = 0; j < optimizationBlockSize; j++) {
                        int px = x + i;
                        int py = y + j;
                        if (px < mandelState.getMandelWidth() && py < mandelState.getMandelHeight()) {
                            image.setRGB(px, py, Color.BLACK.getRGB());
                            mandelCache[px + mandelState.getMandelWidth() * py] = MAX_ITERATIONS;
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
                        it = mandelCache[index] = atFunc.apply(px, py) ;
                    }
                    image.setRGB(px, py, getColor(it).getRGB());
                }
            }
        }
    }

    @Override
    public void close() throws Exception {
        this.pool.close();
    }

    @Override
    protected void paintComponent(Graphics g) {
//        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null);
        g.setColor(Color.GREEN);
        g.drawString(String.format("Press '+' or '-' to adjust zoom factor. Currently %.2f. Current mandelbrot: %s", mandelState.getZoomFactor(), mandelState.getClass().getSimpleName()), 15, 15);
        g.drawString(String.format("Press 'H' to reset zoom and mouse wheel to adjust zoom. Current zoom %.2fx.", mandelState.getCurrentZoom()), 15, 30);
        g.drawString("Press 'S' to save on zoom. Currently " + (mandelState.isSaveToggled() ? "active" : "inactive"), 15, 45);
    }

    private void saveImage() {
        if (mandelState.isSaveToggled()) {
            try {
                final File dirFile = Paths.get(OUTPUT_PATH + "/" + identifier).toFile();
                dirFile.mkdirs();
                final File outputImage = new File(dirFile, System.currentTimeMillis() + ".jpg");
                outputImage.createNewFile();
                ImageIO.write(image, "jpg", outputImage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void makeGif() {
        final File dirFile = Paths.get(OUTPUT_PATH + "/" + identifier).toFile();

        final ImageTypeSpecifier spec = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
        final ImageWriter wr = ImageIO.getImageWriters(spec, "GIF").next();
        final ImageWriteParam param = wr.getDefaultWriteParam();

        final IIOMetadata metadata = wr.getDefaultImageMetadata(spec, param);
        configureRootMetadata(metadata, 5, true);
        // list the files before making the output gif
        final File[] frames = Objects.requireNonNull(dirFile.listFiles(), "no saved frames found");

        try {
            final ImageOutputStream os = ImageIO.createImageOutputStream(new File(dirFile, identifier + ".gif"));
            wr.setOutput(os);
            wr.prepareWriteSequence(null);

            final List<File> sortedFrames = Arrays.stream(frames)
                    .sorted(Comparator.comparingLong(f -> {
                        final String name = f.getName();
                        return Long.parseLong(name.substring(0, name.length() - 4));
                    }))
                    .toList();
            for (File frameFile : sortedFrames) {
                wr.writeToSequence(new IIOImage(ImageIO.read(frameFile), null, metadata), param);
            }
            wr.endWriteSequence();
            os.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // https://memorynotfound.com/generate-gif-image-java-delay-infinite-loop-example/
    private void configureRootMetadata(IIOMetadata metadata, int delay, boolean loopContinuously) {
        final String metaFormatName = metadata.getNativeMetadataFormatName();
        final IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormatName);

        final IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");
        graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
        graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute("delayTime", delay + "");
        graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");

        // IIOMetadataNode commentsNode = getNode(root, "CommentExtensions");
        // commentsNode.setAttribute("CommentExtension", "Created by: https://memorynotfound.com");

        final IIOMetadataNode appExtensionsNode = getNode(root, "ApplicationExtensions");
        final IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");
        child.setAttribute("applicationID", "NETSCAPE");
        child.setAttribute("authenticationCode", "2.0");

        final byte loop = (byte) (loopContinuously ? 0 : 1);

        child.setUserObject(new byte[]{1, loop, 0});
        appExtensionsNode.appendChild(child);

        try {
            metadata.setFromTree(metaFormatName, root);
        } catch (IIOInvalidTreeException e) {
            throw new RuntimeException(e);
        }
    }

    private static IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
        for (int i = 0; i < rootNode.getLength(); i++) {
            if (rootNode.item(i).getNodeName().equalsIgnoreCase(nodeName)) {
                return (IIOMetadataNode) rootNode.item(i);
            }
        }
        final IIOMetadataNode node = new IIOMetadataNode(nodeName);
        rootNode.appendChild(node);
        return (node);
    }

    private Color getColor(int iterations) {
        if (iterations == MAX_ITERATIONS) return Color.BLACK;
        return colors[iterations % colors.length];
    }
}

interface BinaryIntFunc {
    int apply(int a, int b);
}
