package com.kamenbrot.ui;

import com.kamenbrot.generators.JuliaBlockImageGenerator;
import com.kamenbrot.generators.MandelbrotBlockImageGenerator;
import com.kamenbrot.io.MandelOutput;
import com.kamenbrot.state.ColourState;
import com.kamenbrot.state.GenericMandelState;
import com.kamenbrot.state.MandelDoubleDoubleState;
import com.kamenbrot.state.MandelDoubleState;
import com.kamenbrot.state.MandelState;
import com.kamenbrot.state.PaletteState;
import com.kamenbrot.state.PanelState;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.CompletableFuture;

import static com.kamenbrot.MandelMain.PRECISION_SWITCH_ZOOM_LEVEL;

public class MandelKeyListener extends KeyAdapter {
    /**
     * One hundredth of a second (1/100)
     */
    private static final int GIF_FRAME_DELAY_CENTI_SECONDS = 6;
    private final PanelState panelState;
    private final ProperMandelbrotPanel parentComponent;
    private final MiniPanel miniPanel;
    private final PaletteState paletteState;

    public MandelKeyListener(PanelState panelState, ProperMandelbrotPanel parentComponent, MiniPanel miniPanel, PaletteState paletteState) {
        this.panelState = panelState;
        this.parentComponent = parentComponent;
        this.miniPanel = miniPanel;
        this.paletteState = paletteState;
    }

    private volatile boolean isGrunning = false;

    @Override
    public void keyPressed(KeyEvent e) {
        if (isGrunning) return;
        switch (e.getKeyChar()) {
            case 's':
                parentComponent.getMandelState().toggleSave();
                miniPanel.setVisible(false);
                parentComponent.setNeedsRender();
                break;
            case 'j':
                if (parentComponent.getMandelState().isJuliaToggled() || parentComponent.getMandelState().isSaveToggled()) return;
                miniPanel.setVisible(!miniPanel.isVisible());
                if (miniPanel.isVisible()) miniPanel.setNeedsRender();
                break;
            case 'J': {
                parentComponent.getMandelState().toggleJulia();
                parentComponent.getMandelState().resetCoordinates();
                final ColourState colourState = parentComponent.getColorState();
                if (parentComponent.getMandelState().isJuliaToggled()) {
                    parentComponent.setImageGenerator(new JuliaBlockImageGenerator<>(parentComponent.getImageGenerator(), (GenericMandelState<?>) parentComponent.getMandelState(), panelState, colourState));
                    miniPanel.setVisible(false);
                } else {
                    parentComponent.setImageGenerator(new MandelbrotBlockImageGenerator(parentComponent.getImageGenerator(), parentComponent.getMandelState(), panelState, colourState));
                }
                parentComponent.setNeedsRender();
                break;
            }
            case 'G':
                MandelOutput.makeGif(panelState.getOutputDir(), panelState.getIdentifier(), GIF_FRAME_DELAY_CENTI_SECONDS);
                break;
            case 'i':
                parentComponent.setShowInfo(!parentComponent.getShowInfo());
                parentComponent.setNeedsRender();
                break;
            case 'S':
                parentComponent.getMandelState().toggleSmooth();
                parentComponent.getMandelState().clearColorCache();
                parentComponent.setNeedsRender();

                miniPanel.getMiniMandelState().toggleSmooth();
                miniPanel.getMiniMandelState().clearColorCache();
                miniPanel.setNeedsRender();
                break;
            case 'g':
                if (miniPanel.isVisible()) miniPanel.setVisible(false);
                CompletableFuture.runAsync(() -> {
                    while (!parentComponent.getMandelState().isZoomInReached()) {
                        isGrunning = true;
                        final MandelState state = parentComponent.getMandelState();
                        state.zoomIn(1);
                        if (state instanceof MandelDoubleState cast && state.getCurrentZoom() > PRECISION_SWITCH_ZOOM_LEVEL) {
                            final MandelState newMandelState = new MandelDoubleDoubleState(cast);
                            parentComponent.setMandelState(newMandelState);
                            parentComponent.setImageGenerator(new MandelbrotBlockImageGenerator(parentComponent.getImageGenerator(), newMandelState, parentComponent.getPanelState(), parentComponent.getColorState()));
                        } else if (state instanceof MandelDoubleDoubleState cast && state.getCurrentZoom() <= PRECISION_SWITCH_ZOOM_LEVEL) {
                            final MandelState newMandelState = new MandelDoubleState(cast);
                            parentComponent.setMandelState(newMandelState);
                            parentComponent.setImageGenerator(new MandelbrotBlockImageGenerator(parentComponent.getImageGenerator(), newMandelState, parentComponent.getPanelState(), parentComponent.getColorState()));
                        }
                        parentComponent.setNeedsRender();
                    }
                    isGrunning = false;
                });
                break;
            case 'q': {
                final ColourState colourState = parentComponent.getColorState();
                final Color[] colours = colourState.getColours();
                final Color[] shifted = new Color[colours.length];
                int shift = Math.max(colours.length / 100, 1);
                for (int i = 0; i < colours.length; i++) {
                    shifted[((i - shift) % colours.length + colours.length) % colours.length] = colours[i];
                }
                parentComponent.getColorState().setColours(shifted);
                miniPanel.getColorState().setColours(shifted);

                parentComponent.getMandelState().clearColorCache();
                parentComponent.setNeedsRender();

                miniPanel.getMiniMandelState().clearColorCache();
                miniPanel.setNeedsRender();
                parentComponent.setNeedsRender();
            }
            break;
            case 'r':
                final ColourState colourState = parentComponent.getColorState();
                final Color[] colours = colourState.getColours();
                final Color[] shifted = new Color[colours.length];
                int shift = Math.max(colours.length / 250, 1);
                for (int i = 0; i < colours.length; i++) {
                    shifted[(i + shift) % colours.length] = colours[i];
                }
                parentComponent.getColorState().setColours(shifted);
                miniPanel.getColorState().setColours(shifted);

                parentComponent.getMandelState().clearColorCache();
                parentComponent.setNeedsRender();

                miniPanel.getMiniMandelState().clearColorCache();
                miniPanel.setNeedsRender();
                parentComponent.setNeedsRender();
                break;
            case 'h':
                parentComponent.getMandelState().saveCurrentZoom();
                parentComponent.getMandelState().resetCoordinates();
                parentComponent.setNeedsRender();
                break;
            case '+':
                parentComponent.getMandelState().incrementZoomFactor();
                parentComponent.setNeedsRender();
                break;
            case '-':
                parentComponent.getMandelState().decrementZoomFactor();
                parentComponent.setNeedsRender();
                break;
            case 'c': {
                final Color[] palette = paletteState.getNextPalette();
                parentComponent.getColorState().setColours(palette);
                miniPanel.getColorState().setColours(palette);

                parentComponent.getMandelState().clearColorCache();
                parentComponent.setNeedsRender();

                miniPanel.getMiniMandelState().clearColorCache();
                miniPanel.setNeedsRender();
                break;
            }
            case 'v': {
                final Color[] palette = paletteState.incrementPaletteSize();
                parentComponent.getColorState().setColours(palette);
                miniPanel.getColorState().setColours(palette);

                parentComponent.getMandelState().clearColorCache();
                parentComponent.setNeedsRender();

                miniPanel.getMiniMandelState().clearColorCache();
                miniPanel.setNeedsRender();
                break;
            }
            case 'b': {
                final Color[] palette = paletteState.decrementPaletteSize();
                parentComponent.getColorState().setColours(palette);
                miniPanel.getColorState().setColours(palette);

                parentComponent.getMandelState().clearColorCache();
                parentComponent.setNeedsRender();

                miniPanel.getMiniMandelState().clearColorCache();
                miniPanel.setNeedsRender();
                break;
            }
        }
    }
}