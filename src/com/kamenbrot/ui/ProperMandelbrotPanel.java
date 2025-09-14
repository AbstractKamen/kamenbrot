package com.kamenbrot.ui;

import com.kamenbrot.generators.ImageGenerator;
import com.kamenbrot.generators.MandelbrotBlockImageGenerator;
import com.kamenbrot.io.MandelOutput;
import com.kamenbrot.palette.CoolColors;
import com.kamenbrot.palette.PaletteGenerator;
import com.kamenbrot.state.ColorState;
import com.kamenbrot.state.MandelDoubleState;
import com.kamenbrot.state.MandelState;
import com.kamenbrot.state.PanelState;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.*;

public class ProperMandelbrotPanel extends JPanel implements AutoCloseable {

  private PanelState panelState;
  private MandelState mandelState;
  private ColorState colorState;
  private ImageGenerator imageGenerator;
  private boolean showInfo;
  private volatile boolean needsRender;
  private volatile int paleteIndex;
  private final ScheduledExecutorService exec;

  public Color[][] palettes = new Color[][]{
		  PaletteGenerator.generatePalette(CoolColors.getCoolColors1(), 32),
		  PaletteGenerator.generatePalette(CoolColors.getCoolColors2(), 32),
		  PaletteGenerator.generatePalette(CoolColors.getCoolColors3(), 32),
		  PaletteGenerator.generatePalette(CoolColors.getCoolColors4(), 32),
		  PaletteGenerator.generatePalette(CoolColors.getCoolColors5(), 32),
		  PaletteGenerator.generatePalette(CoolColors.getFireColors(), 32),
		  PaletteGenerator.generatePalette(CoolColors.getOceanColors(), 32),
		  PaletteGenerator.generatePalette(CoolColors.getFireIceColors(), 32),
		  PaletteGenerator.generatePalette(CoolColors.getToxicColors(), 32),
		  PaletteGenerator.generatePalette(CoolColors.getCyberpunkColors(), 32),
		  PaletteGenerator.generatePalette(CoolColors.getCoolColors69(), 32),
		  PaletteGenerator.generatePalette(CoolColors.getCoolColors420(), 32)
  };

  public synchronized Color[] getNextPalette() {
	return palettes[(paleteIndex = (paleteIndex + 1) % palettes.length)];
  }

  public ProperMandelbrotPanel(ForkJoinPool pool) {
	this.panelState = new PanelState(800, 600);
	this.mandelState = new MandelDoubleState(panelState, new ConcurrentHashMap<>());
	this.colorState = new ColorState(getNextPalette());
	this.imageGenerator = new MandelbrotBlockImageGenerator(mandelState, panelState, pool, colorState);
	final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
	generateAndSaveImageIfToggled();
	this.exec = Executors.newSingleThreadScheduledExecutor();
	this.exec.scheduleAtFixedRate(() -> {
	  if (needsRender) {
		needsRender = false;
		SwingUtilities.invokeLater(() -> {
		  generateAndSaveImageIfToggled();
		  repaint();
		});
	  }
	}, 0, 16, TimeUnit.MILLISECONDS);
  }

  public void setNeedsRender() {
	this.needsRender = true;
  }

  @Override
  protected void paintComponent(Graphics g) {
	((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	super.paintComponent(g);
	g.drawImage(imageGenerator.getImage(), 0, 0, null);
	g.setColor(Color.GREEN);
	final int textSpacing = 15;
	g.drawString("Press 'i' to show more info", textSpacing, textSpacing);
	if (showInfo) {
	  g.drawString(String.format("Press '+' or '-' to adjust zoom factor. Currently %.2f. Current mandelbrot: %s", mandelState.getZoomFactor(), mandelState.getClass().getSimpleName()), textSpacing, textSpacing * 2);
	  g.drawString(String.format("Press 'H' to reset zoom and mouse wheel to adjust zoom. Current zoom %.2fx.", mandelState.getCurrentZoom()), textSpacing, textSpacing * 3);
	  g.drawString("Press 'S' to save on zoom. Currently " + (mandelState.isSaveToggled() ? "active" : "inactive"), textSpacing, textSpacing * 4);
	  g.drawString("Press 'c' to cycle colours", textSpacing, textSpacing * 5);
	}
  }

  public void generateAndSaveImageIfToggled() {
	imageGenerator.generateImage();
	if (mandelState.isSaveToggled()) {
	  MandelOutput.saveImage(panelState.getOutputDir(), imageGenerator.getImage());
	}
  }

  public ImageGenerator getImageGenerator() {
	return imageGenerator;
  }

  public PanelState getPanelState() {
	return panelState;
  }

  public MandelState getMandelState() {
	return mandelState;
  }

  public boolean getShowInfo() {
	return showInfo;
  }

  public void setImageGenerator(ImageGenerator imageGenerator) {
	this.imageGenerator = imageGenerator;
  }

  public void setShowInfo(boolean showInfo) {
	this.showInfo = showInfo;
  }

  public ColorState getColorState() {
	return colorState;
  }

  @Override
  public void close() {
	this.exec.shutdownNow();
  }
}