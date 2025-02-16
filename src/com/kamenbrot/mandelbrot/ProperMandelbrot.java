package com.kamenbrot.mandelbrot;

import com.kamenbrot.mandelbrot.colors.CoolColors;
import com.kamenbrot.mandelbrot.colors.PaletteGenerator;
import com.kamenbrot.mandelbrot.fractals.Julia;
import com.kamenbrot.mandelbrot.fractals.JuliaDouble;
import com.kamenbrot.mandelbrot.fractals.Mandelbrot;
import com.kamenbrot.mandelbrot.fractals.MandelbrotDouble;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ProperMandelbrot extends JPanel {

  private static String OUTPUT_PATH = "out/mandelbrot";
  private static final int MAX_ITERATIONS = 1400;
  private static final int BLOCK_SIZE = 8;

  private static Color[] PALETTE = CoolColors.getCoolColors2();

  private final BufferedImage image;
  private final Color[] colors;
  private final int[] mandelCache;
  private final String identifier;
  private final MandelState<Double> mandelState;
  private final Mandelbrot<Double> mandelbrot;
  private final Julia<Double> julia;

  public ProperMandelbrot() {
    this.mandelState = new MandelDoubleState(MAX_ITERATIONS, 800, 600);
    this.mandelbrot = new MandelbrotDouble();
    this.julia = new JuliaDouble();
    this.colors = PaletteGenerator.generatePalette(PALETTE, 256);
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
          case 't':
            panel.mandelState.toggleJulia();
            panel.mandelState.resetCoordinates();
            panel.generateImage();
            panel.repaint();
            break;
          case 'g':
            while (panel.mandelState.getCurrentZoom() < panel.mandelState.getSavedZoom()) {
              try {
                CompletableFuture.runAsync(() -> {
                    panel.mandelState.zoomIn(1);
                  })
                  .thenAccept(v -> panel.generateImage())
                  .thenAccept(v -> panel.repaint())
                  .get(10, TimeUnit.SECONDS);
              } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
              } catch (ExecutionException ex) {
                throw new RuntimeException(ex);
              } catch (TimeoutException ex) {
                throw new RuntimeException(ex);
              }
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
            panel.generateImage();
            panel.repaint();
            break;
          case '-':
            panel.mandelState.decrementZoomFactor();
            panel.generateImage();
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
        if (e.getPreciseWheelRotation() < 0) {
          panel.mandelState.zoomIn(e.getScrollAmount());
        } else {
          panel.mandelState.zoomOut(e.getScrollAmount());
        }
        panel.generateImage();
        panel.repaint();
      }
    };
    panel.addMouseListener(mouseAdapter);
    panel.addMouseWheelListener(mouseAdapter);

    frame.addKeyListener(saveKey);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setMaximumSize(new Dimension(panel.mandelState.getMandelWidth(), panel.mandelState.getMandelWidth()));
    frame.setMinimumSize(new Dimension(panel.mandelState.getMandelHeight(), panel.mandelState.getMandelHeight()));
    frame.add(panel);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
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
    int blockSize = BLOCK_SIZE;
    while (blockSize > 2) {
      // Ensure we don't go out of bounds
      int x1 = Math.min(x, mandelState.getMandelWidth() - 1);
      int y1 = Math.min(y, mandelState.getMandelHeight() - 1);
      int x2 = Math.min(x + blockSize, mandelState.getMandelWidth() - 1);
      int y2 = Math.min(y + blockSize, mandelState.getMandelHeight() - 1);

      int c1 = mandelCache[x1 + mandelState.getMandelWidth() * y1] = atFunc.apply(x1, y1);
      int c2 = mandelCache[x2 + mandelState.getMandelWidth() * y1] = atFunc.apply(x2, y1);
      int c3 = mandelCache[x1 + mandelState.getMandelWidth() * y2] = atFunc.apply(x1, y2);
      int c4 = mandelCache[x2 + mandelState.getMandelWidth() * y2] = atFunc.apply(x2, y2);

      // If all corners are black, fill the entire block as black
      if (c1 == MAX_ITERATIONS && c2 == MAX_ITERATIONS &&
        c3 == MAX_ITERATIONS && c4 == MAX_ITERATIONS) {
        for (int i = 0; i < blockSize; i++) {
          for (int j = 0; j < blockSize; j++) {
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
        blockSize = blockSize >> 1;
      }
    }
    for (int i = 0; i < BLOCK_SIZE; i++) {
      for (int j = 0; j < BLOCK_SIZE; j++) {
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
    g.drawString(String.format("Press '+' or '-' to adjust zoom factor. Currently %.2f", mandelState.getZoomFactor()), 25, 50);
    g.drawString(String.format("Press 'H' to reset zoom and mouse wheel to adjust zoom. Current zoom %.2fx", mandelState.getCurrentZoom()), 25, 25);
    g.drawString("Press 'S' to save on zoom. Currently " + (mandelState.isSaveToggled() ? "active" : "inactive"), mandelState.getMandelWidth() >> 1, 50);
  }

  private void saveImage() {
    if (mandelState.isSaveToggled()) {
      try {
        final File output = Paths.get(OUTPUT_PATH + "/" + identifier + "/" + System.currentTimeMillis() + ".jpg").toFile();
        output.mkdirs();
        output.createNewFile();
        ImageIO.write(image, "jpg", output);
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
