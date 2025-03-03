package com.kamenbrot.ui;

import com.kamenbrot.generators.ImageGenerator;
import com.kamenbrot.generators.MandelbrotBlockImageGenerator;
import com.kamenbrot.io.MandelOutput;
import com.kamenbrot.palette.CoolColors;
import com.kamenbrot.palette.PaletteGenerator;
import com.kamenbrot.state.MandelDoubleState;
import com.kamenbrot.state.MandelState;
import com.kamenbrot.state.PanelState;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ConcurrentHashMap;

public class ProperMandelbrotPanel extends JPanel {

    private PanelState panelState;
    private MandelState mandelState;
    private ImageGenerator imageGenerator;
    private boolean showInfo = false;

    public ProperMandelbrotPanel() {
//        final Color[] layer1 = PaletteGenerator.generatePalette(CoolColors.getFireColors(), 16);
//        final Color[] layer2 = PaletteGenerator.generatePalette(CoolColors.getOceanColors(), 16);
//        final Color[] layer3 = PaletteGenerator.generatePalette(CoolColors.getFireIceColors(), 16);
//        final Color[] layer4 = PaletteGenerator.generatePalette(CoolColors.getToxicColors(), 16);
//        final Color[] palette = PaletteGenerator.getLayeredColors(layer1, layer2, layer3, layer4);
        final Color[] palette = PaletteGenerator.getLayeredColors(PaletteGenerator.generatePalette(CoolColors.getCoolColors1(), 16), 4);
        this.panelState = new PanelState(800, 600, palette);
        this.mandelState = new MandelDoubleState(panelState, new ConcurrentHashMap<>());
        this.imageGenerator = new MandelbrotBlockImageGenerator(mandelState, panelState);
        final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
        generateAndSaveImageIfToggled();
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
}