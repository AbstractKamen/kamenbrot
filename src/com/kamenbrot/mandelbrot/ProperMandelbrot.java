package com.kamenbrot.mandelbrot;

import com.kamenbrot.mandelbrot.colors.CoolColors;
import com.kamenbrot.mandelbrot.colors.PaletteGenerator;
import com.kamenbrot.mandelbrot.fractals.CpuJulia;
import com.kamenbrot.mandelbrot.fractals.CpuJuliaImpl;
import com.kamenbrot.mandelbrot.fractals.CpuMandelbrot;
import com.kamenbrot.mandelbrot.fractals.CpuMandelbrotImpl;
import com.kamenbrot.mandelbrot.io.GifSequenceWriter;
import com.kamenbrot.mandelbrot.state.MandelDoubleState;
import com.kamenbrot.mandelbrot.state.MandelState;
import com.kamenbrot.mandelbrot.state.PanelState;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class ProperMandelbrot extends JPanel implements AutoCloseable {

    private int[] mandelCache;
    private PanelState panelState;
    private MandelState mandelState;
    private final CpuMandelbrot mandelbrot;
    private final CpuJulia julia;
    private final ForkJoinPool pool;


    public ProperMandelbrot() {
        final Color[] layer1 = PaletteGenerator.generatePalette(CoolColors.getCoolColors1(), 16);
        final Color[] layer2 = PaletteGenerator.generatePalette(CoolColors.getCoolColors69(), 16);
        final Color[] layer3 = PaletteGenerator.generatePalette(CoolColors.getCoolColors2(), 16);
        final Color[] layer4 = PaletteGenerator.generatePalette(CoolColors.getCoolColors420(), 16);
        final Color[] palette = PaletteGenerator.getLayeredColors(layer1, layer2, layer3, layer4);
        this.panelState = new PanelState(800, 600, palette);
        this.mandelState = new MandelDoubleState(panelState);
        this.mandelbrot = new CpuMandelbrotImpl();
        this.julia = new CpuJuliaImpl();
        final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.mandelCache = new int[mandelState.getMandelWidth() * mandelState.getMandelHeight()];
        this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
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
                    case 'S':
                        panel.mandelState.toggleSmooth();
                        panel.generateImage();
                        panel.repaint();
                        break;
                    case 'g':
                        while (!panel.mandelState.isZoomInReached()) {
                            zoomIn(panel.panelState.getJourneyUnits(), panel);
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
                    zoomIn(panel.panelState.getZoomUnits(), panel);
                } else {
                    state.zoomOut(panel.panelState.getZoomUnits());
                }
                panel.generateImage();
                panel.repaint();
            }
        };
        panel.addMouseListener(mouseAdapter);
        panel.addMouseWheelListener(mouseAdapter);

        frame.addKeyListener(saveKey);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addComponentListener(new ComponentAdapter() {


            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                Component c = e.getComponent();
                panel.panelState.setWidthAndHeight(c.getWidth(), c.getHeight());
                panel.mandelState.setMandelWidth(c.getWidth());
                panel.mandelState.setMandelHeight(c.getHeight());
                panel.mandelCache = new int[c.getWidth() * c.getHeight()];
                panel.generateImage();
                panel.repaint();
            }

        });
        frame.setMinimumSize(new Dimension(panel.mandelState.getMandelWidth(), panel.mandelState.getMandelHeight()));
        frame.setMaximumSize(new Dimension(1600, 1200));
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
        final int blockSize = panelState.getBlockSize();
        Arrays.fill(mandelCache, -1);
        for (int x = 0; x < mandelState.getMandelWidth(); x += blockSize) {
            for (int y = 0; y < mandelState.getMandelHeight(); y += blockSize) {
                final int X = x, Y = y;
                pool.execute(() -> generateFractal(X, Y, this::mandelbrotAt));
            }
        }
    }

    private int mandelbrotAt(int x, int y) {
        return mandelbrot.mandelbrotAt(x, y, mandelState);
    }

    private void generateJulia() {
        final int blockSize = panelState.getBlockSize();
        Arrays.fill(mandelCache, -1);
        for (int x = 0; x < mandelState.getMandelWidth(); x += blockSize) {
            for (int y = 0; y < mandelState.getMandelHeight(); y += blockSize) {
                final int X = x, Y = y;
                pool.execute(() -> generateFractal(X, Y, this::juliaAt));
            }
        }
    }

    private int juliaAt(int x, int y) {
        return julia.juliaAt(x, y, mandelState);
    }

    private void generateFractal(int x, int y, BinaryIntFunc atFunc) {
        final int optimizationBlockSize = panelState.getOptimizationBlockSize();
        final int blockSize = panelState.getBlockSize();
        for (int i = 0; i < blockSize; i += optimizationBlockSize) {
            for (int j = 0; j < blockSize; j += optimizationBlockSize) {
                generateFractalBlock(x + j, y + i, atFunc, optimizationBlockSize);
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
            int maxIterations = panelState.getMaxIterations();
            if (c1 == maxIterations && c2 == maxIterations &&
                    c3 == maxIterations && c4 == maxIterations) {
                for (int i = 0; i < optimizationBlockSize; i++) {
                    for (int j = 0; j < optimizationBlockSize; j++) {
                        int px = x + i;
                        int py = y + j;
                        if (px < mandelState.getMandelWidth() && py < mandelState.getMandelHeight()) {
                            panelState.getImage().setRGB(px, py, Color.BLACK.getRGB());
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
                        it = mandelCache[index] = atFunc.apply(px, py);
                    }
                    if (mandelState.isSmoothToggled()) {
                        panelState.getImage().setRGB(px, py, panelState.getColor_smooth(it).getRGB());
                    } else {
                        panelState.getImage().setRGB(px, py, panelState.getColor(it).getRGB());
                    }
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
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        super.paintComponent(g);
        g.drawImage(panelState.getImage(), 0, 0, null);
        g.setColor(Color.GREEN);
        g.drawString(String.format("Press '+' or '-' to adjust zoom factor. Currently %.2f. Current mandelbrot: %s", mandelState.getZoomFactor(), mandelState.getClass().getSimpleName()), 15, 15);
        g.drawString(String.format("Press 'H' to reset zoom and mouse wheel to adjust zoom. Current zoom %.2fx.", mandelState.getCurrentZoom()), 15, 30);
        g.drawString("Press 'S' to save on zoom. Currently " + (mandelState.isSaveToggled() ? "active" : "inactive"), 15, 45);
    }

    private void saveImage() {
        if (mandelState.isSaveToggled()) {
            panelState.saveImage();
        }
    }

    private void makeGif() {
        final File dirFile = panelState.getOutputDir();
        // list the files before making the output gif
        final File[] frames = Objects.requireNonNull(dirFile.listFiles(), "no saved frames found");

        try (final GifSequenceWriter writer = new GifSequenceWriter(7, dirFile, panelState.getIdentifier())) {

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

interface BinaryIntFunc {
    int apply(int a, int b);
}
