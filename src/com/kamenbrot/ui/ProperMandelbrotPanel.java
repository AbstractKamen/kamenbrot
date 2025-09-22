package com.kamenbrot.ui;

import com.kamenbrot.generators.ImageGenerator;
import com.kamenbrot.generators.MandelbrotBlockImageGenerator;
import com.kamenbrot.generators.PanelRenderer;
import com.kamenbrot.io.MandelOutput;
import com.kamenbrot.state.PaletteState;
import com.kamenbrot.state.ColourState;
import com.kamenbrot.state.MandelDoubleState;
import com.kamenbrot.state.MandelState;
import com.kamenbrot.state.PanelState;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

public class ProperMandelbrotPanel extends JPanel {

  private PanelState panelState;
  private MandelState mandelState;
  private ColourState colourState;
  private ImageGenerator imageGenerator;
  private boolean showInfo;
  private PanelRenderer.RenderTask renderTask;
  private final PaletteState paletteState;

  public ProperMandelbrotPanel(ForkJoinPool pool, PanelRenderer renderer, PaletteState paletteState) {
	this.panelState = new PanelState(800, 600);
	this.mandelState = new MandelDoubleState(panelState, new ConcurrentHashMap<>());
	this.colourState = new ColourState(paletteState.getCurrentPalette());
	this.imageGenerator = new MandelbrotBlockImageGenerator(mandelState, panelState, pool, colourState);
	final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
	this.paletteState = paletteState;
	generateAndSaveImageIfToggled();
	this.renderTask = renderer.addRenderTask(() -> {
	  generateAndSaveImageIfToggled();
	  repaint();
	});
  }

  public void setNeedsRender() {
	renderTask.setNeedsRender(true);
  }

  @Override
  protected void paintComponent(Graphics g) {
	((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	super.paintComponent(g);
	g.drawImage(imageGenerator.getImage(), 0, 0, null);
	g.setColor(Color.GREEN);
	final int textSpacing = 15;
	g.drawString("Press 'i' to show more info", textSpacing, textSpacing);
	int i = 2;
	if (showInfo) {
	  g.drawString(String.format("Press '+' or '-' to adjust zoom factor. Currently %.2f. Current mandelbrot: %s", mandelState.getZoomFactor(), mandelState.getClass().getSimpleName()), textSpacing, textSpacing * i++);
	  g.drawString(String.format("Press 'H' to reset zoom and mouse wheel to adjust zoom. Current zoom %.2fx.", mandelState.getCurrentZoom()), textSpacing, textSpacing * i++);
	  g.drawString("Press 'S' to save on zoom. Currently " + (mandelState.isSaveToggled() ? "active" : "inactive"), textSpacing, textSpacing * i++);
	  g.drawString("Press 'c' to cycle colours", textSpacing, textSpacing * i++);
	  g.drawString(String.format("Press 'v' to increase and 'b' to decrease palette size. Current palette size %d", paletteState.getPaletteSize()), textSpacing, textSpacing * i++);
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

  public ColourState getColorState() {
	return colourState;
  }
}