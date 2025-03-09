package com.kamenbrot.ui;

import com.kamenbrot.generators.JuliaBlockImageGenerator;
import com.kamenbrot.generators.MandelbrotBlockImageGenerator;
import com.kamenbrot.io.MandelOutput;
import com.kamenbrot.state.ColorState;
import com.kamenbrot.state.GenericMandelState;
import com.kamenbrot.state.MandelState;
import com.kamenbrot.state.PanelState;

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

  public MandelKeyListener(MandelState mandelState, PanelState panelState, ProperMandelbrotPanel parentComponent, MiniPanel miniPanel) {
    this.mandelState = mandelState;
    this.panelState = panelState;
    this.parentComponent = parentComponent;
    this.miniPanel = miniPanel;
  }

  @Override
  public void keyPressed(KeyEvent e) {
    switch (e.getKeyChar()) {
      case 's':
        mandelState.toggleSave();
        miniPanel.setVisible(false);
        parentComponent.repaint();
        break;
      case 'j':
        if (mandelState.isJuliaToggled() || mandelState.isSaveToggled()) return;
        miniPanel.setVisible(!miniPanel.isVisible());
        if (miniPanel.isVisible()) miniPanel.repaint();
        break;
      case 'J':
        mandelState.toggleJulia();
        mandelState.resetCoordinates();
        final ColorState colorState = parentComponent.getColorState();
        if (mandelState.isJuliaToggled()) {
          parentComponent.setImageGenerator(new JuliaBlockImageGenerator<>(parentComponent.getImageGenerator(), (GenericMandelState<?>) mandelState, panelState, colorState));
          miniPanel.setVisible(false);
        } else {
          parentComponent.setImageGenerator(new MandelbrotBlockImageGenerator(parentComponent.getImageGenerator(), mandelState, panelState, colorState));
        }
        parentComponent.getImageGenerator().generateImage();
        parentComponent.repaint();
        break;
      case 'G':
        MandelOutput.makeGif(panelState.getOutputDir(), panelState.getIdentifier(), GIF_FRAME_DELAY_CENTI_SECONDS);
        break;
      case 'i':
        parentComponent.setShowInfo(!parentComponent.getShowInfo());
        parentComponent.repaint();
        break;
      case 'S':
        mandelState.toggleSmooth();
        mandelState.clearColorCache();
        parentComponent.getImageGenerator().generateImage();
        parentComponent.repaint();

        miniPanel.getMiniMandelState().toggleSmooth();
        miniPanel.getMiniMandelState().clearColorCache();
        miniPanel.getMiniImageGen().generateImage();
        miniPanel.repaint();
        break;
      case 'g':
        miniPanel.setVisible(false);
        while (!mandelState.isZoomInReached()) {
          mandelState.zoomIn(panelState.getJourneyUnits());
          parentComponent.generateAndSaveImageIfToggled();
          parentComponent.repaint();
        }
        break;
      case 'h':
        mandelState.saveCurrentZoom();
        mandelState.resetCoordinates();
        parentComponent.getImageGenerator().generateImage();
        parentComponent.repaint();
        break;
      case '+':
        mandelState.incrementZoomFactor();
        parentComponent.repaint();
        break;
      case '-':
        mandelState.decrementZoomFactor();
        parentComponent.repaint();
        break;
    }
  }
}
