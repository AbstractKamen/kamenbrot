package com.kamenbrot.ui;

import com.kamenbrot.generators.JuliaBlockImageGenerator;
import com.kamenbrot.generators.MandelbrotBlockImageGenerator;
import com.kamenbrot.io.MandelOutput;
import com.kamenbrot.palette.PaletteState;
import com.kamenbrot.state.ColourState;
import com.kamenbrot.state.GenericMandelState;
import com.kamenbrot.state.MandelState;
import com.kamenbrot.state.PanelState;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class MandelKeyListener extends KeyAdapter {
  /**
   * One hundredth of a second (1/100)
   */
  private static final int GIF_FRAME_DELAY_CENTI_SECONDS = 6;
  private final MandelState mandelState;
  private final PanelState panelState;
  private final ProperMandelbrotPanel parentComponent;
  private final MiniPanel miniPanel;
  private final PaletteState paletteState;

  public MandelKeyListener(MandelState mandelState, PanelState panelState, ProperMandelbrotPanel parentComponent, MiniPanel miniPanel, PaletteState paletteState) {
	this.mandelState = mandelState;
	this.panelState = panelState;
	this.parentComponent = parentComponent;
	this.miniPanel = miniPanel;
	this.paletteState = paletteState;
  }

  @Override
  public void keyPressed(KeyEvent e) {
	switch (e.getKeyChar()) {
	  case 's':
		mandelState.toggleSave();
		miniPanel.setVisible(false);
		parentComponent.setNeedsRender();
		break;
	  case 'j':
		if (mandelState.isJuliaToggled() || mandelState.isSaveToggled()) return;
		miniPanel.setVisible(!miniPanel.isVisible());
		if (miniPanel.isVisible()) miniPanel.setNeedsRender();
		break;
	  case 'J':
		mandelState.toggleJulia();
		mandelState.resetCoordinates();
		final ColourState colourState = parentComponent.getColorState();
		if (mandelState.isJuliaToggled()) {
		  parentComponent.setImageGenerator(new JuliaBlockImageGenerator<>(parentComponent.getImageGenerator(), (GenericMandelState<?>) mandelState, panelState, colourState));
		  miniPanel.setVisible(false);
		} else {
		  parentComponent.setImageGenerator(new MandelbrotBlockImageGenerator(parentComponent.getImageGenerator(), mandelState, panelState, colourState));
		}
		parentComponent.setNeedsRender();
		break;
	  case 'G':
		MandelOutput.makeGif(panelState.getOutputDir(), panelState.getIdentifier(), GIF_FRAME_DELAY_CENTI_SECONDS);
		break;
	  case 'i':
		parentComponent.setShowInfo(!parentComponent.getShowInfo());
		parentComponent.setNeedsRender();
		break;
	  case 'S':
		mandelState.toggleSmooth();
		mandelState.clearColorCache();
		parentComponent.setNeedsRender();

		miniPanel.getMiniMandelState().toggleSmooth();
		miniPanel.getMiniMandelState().clearColorCache();
		miniPanel.setNeedsRender();
		break;
	  case 'g':
		miniPanel.setVisible(false);
		while (!mandelState.isZoomInReached()) {
		  mandelState.zoomIn(1);
		  parentComponent.setNeedsRender();
		}
		break;
	  case 'h':
		mandelState.saveCurrentZoom();
		mandelState.resetCoordinates();
		parentComponent.setNeedsRender();
		break;
	  case '+':
		mandelState.incrementZoomFactor();
		parentComponent.setNeedsRender();
		break;
	  case '-':
		mandelState.decrementZoomFactor();
		parentComponent.setNeedsRender();
		break;
	  case 'c': {
		final Color[] palette = paletteState.getNextPalette();
		parentComponent.getColorState().setColours(palette);
		miniPanel.getColorState().setColours(palette);

		mandelState.clearColorCache();
		parentComponent.setNeedsRender();

		miniPanel.getMiniMandelState().clearColorCache();
		miniPanel.setNeedsRender();
		break;
	  }
	  case 'v': {
		final Color[] palette = paletteState.incrementPaletteSize();
		parentComponent.getColorState().setColours(palette);
		miniPanel.getColorState().setColours(palette);

		mandelState.clearColorCache();
		parentComponent.setNeedsRender();

		miniPanel.getMiniMandelState().clearColorCache();
		miniPanel.setNeedsRender();
		break;
	  }
	  case 'b': {
		final Color[] palette = paletteState.decrementPaletteSize();
		parentComponent.getColorState().setColours(palette);
		miniPanel.getColorState().setColours(palette);

		mandelState.clearColorCache();
		parentComponent.setNeedsRender();

		miniPanel.getMiniMandelState().clearColorCache();
		miniPanel.setNeedsRender();
		break;
	  }
	}
  }
}