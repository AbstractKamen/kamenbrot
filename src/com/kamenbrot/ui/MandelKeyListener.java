package com.kamenbrot.ui;

import com.kamenbrot.generators.JuliaBlockImageGenerator;
import com.kamenbrot.generators.MandelbrotBlockImageGenerator;
import com.kamenbrot.io.MandelOutput;
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

    public MandelKeyListener(MandelState mandelState, PanelState panelState, ProperMandelbrotPanel parentComponent) {
        this.mandelState = mandelState;
        this.panelState = panelState;
        this.parentComponent = parentComponent;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyChar()) {
            case 's':
                mandelState.toggleSave();
                parentComponent.repaint();
                break;
            case 'p':
                mandelState.togglePerformance();
                parentComponent.getImageGenerator().generateImage();
                parentComponent.repaint();
                break;
            case 'j':
                mandelState.toggleJulia();
                mandelState.resetCoordinates();
                if (mandelState.isJuliaToggled()) {
                    parentComponent.setImageGenerator(new JuliaBlockImageGenerator(parentComponent.getImageGenerator(), mandelState, panelState));
                } else {
                    parentComponent.setImageGenerator(new MandelbrotBlockImageGenerator(parentComponent.getImageGenerator(), mandelState, panelState));
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
                break;
            case 'g':
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
