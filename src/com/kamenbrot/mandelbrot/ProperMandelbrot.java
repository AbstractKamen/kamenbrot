package com.kamenbrot.mandelbrot;

import com.kamenbrot.mandelbrot.colors.CoolColors;
import com.kamenbrot.mandelbrot.colors.PaletteGenerator;
import com.kamenbrot.mandelbrot.fractals.CpuJulia;
import com.kamenbrot.mandelbrot.fractals.CpuJuliaImpl;
import com.kamenbrot.mandelbrot.fractals.CpuMandelbrot;
import com.kamenbrot.mandelbrot.fractals.CpuMandelbrotImpl;
import com.kamenbrot.mandelbrot.state.MandelBigDecimalState;
import com.kamenbrot.mandelbrot.state.MandelDoubleState;
import com.kamenbrot.mandelbrot.state.MandelState;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class ProperMandelbrot extends JPanel {
  public static final double SWITCH_STATE = 9999999;

  private static final String OUTPUT_PATH = "out/mandelbrot";
  private static final int MAX_ITERATIONS = 2400;
  private static final int BLOCK_SIZE = 128;
  private static final int OPTIMIZATION_BLOCK_SIZE = 8;

  private static Color[] PALETTE = CoolColors.getCoolColors69();

  private final BufferedImage image;
  private final Color[] colors;
  private final String identifier;
  private final int[] mandelCache;
  private MandelState mandelState;
  private final CpuMandelbrot mandelbrot;
  private final CpuJulia julia;


  public ProperMandelbrot() {
    this.mandelState = new MandelDoubleState(MAX_ITERATIONS, 800, 600);
    this.mandelbrot = new CpuMandelbrotImpl();
    this.julia = new CpuJuliaImpl();
    this.colors = PaletteGenerator.generatePalette(PALETTE, 512);
    final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    this.mandelCache = new int[mandelState.getMandelWidth() * mandelState.getMandelHeight()];
    this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
    this.image = new BufferedImage(mandelState.getMandelWidth(), mandelState.getMandelHeight(), BufferedImage.TYPE_INT_RGB);
    this.identifier = LocalDateTime.now().toString().replace(':', '-');
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
          case 'g':
            final double savedZoom = panel.mandelState.getSavedZoom();
            while (Double.compare(panel.mandelState.getCurrentZoom(), savedZoom) <= 0) {
              zoomIn(1, panel);
              panel.generateImage();
              panel.repaint();
            }
            break;
          case 'h':
            panel.mandelState.saveCurrentZoom();
            panel.mandelState.resetCoordinates();
            if (panel.mandelState.getSavedZoom() > SWITCH_STATE && panel.mandelState instanceof MandelBigDecimalState) {
              panel.mandelState = panel.mandelState.translate();
            }
            panel.generateImage();
            panel.repaint();
            break;
          case '+':
            panel.mandelState.incrementZoomFactor();
            panel.repaint();
            break;
          case 't':
            panel.mandelState = panel.mandelState.translate();
            panel.generateImage();
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
          zoomIn(e.getScrollAmount(), panel);
        } else {
          if (state.getCurrentZoom() < SWITCH_STATE && state instanceof MandelBigDecimalState) {
            panel.mandelState = state.translate();
          }
          state.zoomOut(e.getScrollAmount());
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
    if (state.getCurrentZoom() > SWITCH_STATE && state instanceof MandelDoubleState) {
      panel.mandelState = state.translate();
    }
  }

  private void generateImage() {
    if (mandelState.isJuliaToggled()) {
      generateJulia();
    } else {
      generateMandelbrot();
    }
    ForkJoinPool.commonPool().awaitQuiescence(10, TimeUnit.SECONDS);
    saveImage();
  }

  private void generateMandelbrot() {
    Arrays.fill(mandelCache, -1);
    for (int x = 0; x < mandelState.getMandelWidth(); x += BLOCK_SIZE) {
      for (int y = 0; y < mandelState.getMandelHeight(); y += BLOCK_SIZE) {
        final int X = x, Y = y;
        ForkJoinPool.commonPool().execute(() -> generateFractal(X, Y, this::mandelbrotAt));
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
        ForkJoinPool.commonPool().execute(() -> generateFractal(X, Y, this::juliaAt));
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
            it = mandelCache[index] = atFunc.apply(px, py);
          }
          image.setRGB(px, py, getColor(it).getRGB());
        }
      }
    }
  }

  @Override
  protected void paintComponent(Graphics g) {
    ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    super.paintComponent(g);
    g.drawImage(image, 0, 0, null);
    g.setColor(Color.GREEN);
    g.drawString(String.format("Press '+' or '-' to adjust zoom factor. Currently %.2f. Current mandelbrot: %s", mandelState.getZoomFactor(), mandelState.getClass().getSimpleName()), 15, 15);
    g.drawString(String.format("Press 'H' to reset zoom and mouse wheel to adjust zoom. Current zoom %.2fx. Saved zoom %.2fx", mandelState.getCurrentZoom(), mandelState.getSavedZoom()), 15, 30);
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

  private Color getColor(int iterations) {
    if (iterations == MAX_ITERATIONS) return Color.BLACK;
    return colors[iterations % colors.length];
  }
}

interface BinaryIntFunc {
  int apply(int a, int b);
}
