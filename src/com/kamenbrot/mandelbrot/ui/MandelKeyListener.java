package com.kamenbrot.mandelbrot.ui;

import com.kamenbrot.mandelbrot.ProperMandelbrot;
import com.kamenbrot.mandelbrot.fractals.JuliaBlockImageGenerator;
import com.kamenbrot.mandelbrot.fractals.MandelbrotBlockImageGenerator;
import com.kamenbrot.mandelbrot.state.MandelState;
import com.kamenbrot.mandelbrot.state.PanelState;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class MandelKeyListener extends KeyAdapter {

    private final MandelState mandelState;
    private final PanelState panelState;
    private final ProperMandelbrot parentComponent;

    public MandelKeyListener(MandelState mandelState, PanelState panelState, ProperMandelbrot parentComponent) {
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
                    parentComponent.setImageGenerator(new JuliaBlockImageGenerator(parentComponent.getPool(), mandelState, panelState));
                } else {
                    parentComponent.setImageGenerator(new MandelbrotBlockImageGenerator(parentComponent.getPool(), mandelState, panelState));
                }
                parentComponent.getImageGenerator().generateImage();
                parentComponent.repaint();
                break;
            case 'G':
                MandelOutput.makeGif(panelState.getOutputDir(), panelState.getIdentifier());
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
