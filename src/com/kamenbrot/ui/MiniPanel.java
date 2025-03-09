package com.kamenbrot.ui;

import com.kamenbrot.fractals.ComplexMapping;
import com.kamenbrot.generators.ImageGenerator;
import com.kamenbrot.generators.JuliaBlockImageGenerator;
import com.kamenbrot.state.ColorState;
import com.kamenbrot.state.MandelDoubleState;
import com.kamenbrot.state.MandelState;
import com.kamenbrot.state.PanelState;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.concurrent.ForkJoinPool;

public class MiniPanel extends JPanel {

  private JuliaBlockImageGenerator miniImageGen;
  private MandelDoubleState miniMandelState;
  private ColorState colorState;

  public MiniPanel(PanelState panelState, ForkJoinPool pool, int width, int height, ColorState colorState) {
    final int[] miniCache = new int[width * height];
    final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    this.miniMandelState = new MandelDoubleState(240, width, height, new HashMap<>());
    miniMandelState.setCenter((int) ((width * 1.2) / 2), height >> 1);
    this.miniImageGen = new JuliaBlockImageGenerator<>(miniMandelState, panelState, colorState, pool, panelState.getBlockSize(), miniCache, bufferedImage, 0.0d, 0.0d);
    this.colorState = colorState;
    miniImageGen.generateImage();
  }

  @Override
  protected void paintComponent(Graphics g) {
    ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    super.paintComponent(g);
    g.drawImage(miniImageGen.getImage(), 0, 0, null);
    g.setColor(Color.GREEN);
    final int textSpacing = 15;
    g.drawString("real: " + miniImageGen.getRe(), textSpacing, getHeight() - 2 * textSpacing);
    g.drawString("imaginary: " + miniImageGen.getImag(), textSpacing, getHeight() - textSpacing);
  }

  public ImageGenerator getMiniImageGen() {
    return miniImageGen;
  }

  public MandelState getMiniMandelState() {
    return miniMandelState;
  }

  public ColorState getColorState() {
    return colorState;
  }

  public void setPos(double re, double imag) {
    miniImageGen.setPos(re, imag);
  }
}
