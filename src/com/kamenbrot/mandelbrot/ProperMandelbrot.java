package com.kamenbrot.mandelbrot;

import com.kamenbrot.mandelbrot.colors.CoolColors;
import com.kamenbrot.mandelbrot.colors.PaletteGenerator;
import com.kamenbrot.mandelbrot.fractals.ImageGenerator;
import com.kamenbrot.mandelbrot.fractals.MandelbrotBlockImageGenerator;
import com.kamenbrot.mandelbrot.state.MandelDoubleState;
import com.kamenbrot.mandelbrot.state.MandelState;
import com.kamenbrot.mandelbrot.state.PanelState;
import com.kamenbrot.mandelbrot.ui.MandelKeyListener;
import com.kamenbrot.mandelbrot.ui.MandelOutput;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class ProperMandelbrot extends JPanel {

    private PanelState panelState;
    private MandelState mandelState;
    private ImageGenerator imageGenerator;
    private final ForkJoinPool pool;


    public ProperMandelbrot() {
        final Color[] layer1 = PaletteGenerator.generatePalette(CoolColors.getFireColors(), 16);
        final Color[] layer2 = PaletteGenerator.generatePalette(CoolColors.getOceanColors(), 16);
        final Color[] layer3 = PaletteGenerator.generatePalette(CoolColors.getFireIceColors(), 16);
        final Color[] layer4 = PaletteGenerator.generatePalette(CoolColors.getToxicColors(), 16);
        final Color[] palette = PaletteGenerator.getLayeredColors(layer1, layer2, layer3, layer4);
//        final Color[] palette = PaletteGenerator.getLayeredColors(PaletteGenerator.generatePalette(CoolColors.getCoolColors5(), 64), 2);
        this.panelState = new PanelState(800, 600, palette);
        this.mandelState = new MandelDoubleState(panelState, new ConcurrentHashMap<>());
        this.pool = (ForkJoinPool) Executors.newWorkStealingPool();
        this.imageGenerator = new MandelbrotBlockImageGenerator(pool, mandelState, panelState);
        final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
        generateAndSaveImageIfToggled();
    }

    @Override
    protected void paintComponent(Graphics g) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        super.paintComponent(g);
        while (!pool.awaitQuiescence(5, TimeUnit.SECONDS)) ;
        g.drawImage(imageGenerator.getImage(), 0, 0, null);
        g.setColor(Color.GREEN);
        g.drawString(String.format("Press '+' or '-' to adjust zoom factor. Currently %.2f. Current mandelbrot: %s", mandelState.getZoomFactor(), mandelState.getClass().getSimpleName()), 15, 15);
        g.drawString(String.format("Press 'H' to reset zoom and mouse wheel to adjust zoom. Current zoom %.2fx.", mandelState.getCurrentZoom()), 15, 30);
        g.drawString("Press 'S' to save on zoom. Currently " + (mandelState.isSaveToggled() ? "active" : "inactive"), 15, 45);
    }

    public void generateAndSaveImageIfToggled() {
        imageGenerator.generateImage();
        while (!pool.awaitQuiescence(5, TimeUnit.SECONDS)) ;
        if (mandelState.isSaveToggled()) {
            MandelOutput.saveImage(panelState.getOutputDir(), imageGenerator.getImage());
        }
    }

    public ImageGenerator getImageGenerator() {
        return imageGenerator;
    }

    public ForkJoinPool getPool() {
        return pool;
    }

    public PanelState getPanelState() {
        return panelState;
    }

    public MandelState getMandelState() {
        return mandelState;
    }

    public void setImageGenerator(ImageGenerator imageGenerator) {
        this.imageGenerator = imageGenerator;
    }

    public static void main(String[] args) {
        final JFrame frame = new JFrame("Mandelbrot Set");
        final ProperMandelbrot panel = new ProperMandelbrot();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addComponentListener(new ComponentAdapter() {


            @Override
            public void componentResized(ComponentEvent e) {
                final int w = frame.getWidth();
                final int h = frame.getHeight();
                final int aspectW = 16;
                final int aspectH = 9;
                if (aspectW * h > aspectH * w) {
                    panel.setPreferredSize(new Dimension(w, aspectH * w / aspectW));
                } else {
                    panel.setPreferredSize(new Dimension(aspectW * h / aspectH, h));
                }
                frame.validate();
                panel.panelState.setWidthAndHeight(w, h);
                panel.imageGenerator.resizeImage(w, h);
                panel.mandelState.setMandelWidth(w);
                panel.mandelState.setMandelHeight(h);
                panel.imageGenerator.generateImage();
                panel.repaint();
            }

        });
        frame.setMinimumSize(new Dimension(panel.mandelState.getMandelWidth(), panel.mandelState.getMandelHeight()));
        frame.setMaximumSize(new Dimension(1600, 1200));
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.addKeyListener(new MandelKeyListener(panel.getMandelState(), panel.getPanelState(), panel));
        final MouseAdapter mouseAdapter = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                panel.getMandelState().setCenter(e.getX(), e.getY());
                panel.generateAndSaveImageIfToggled();
                panel.repaint();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                final MandelState state = panel.getMandelState();
                if (e.getPreciseWheelRotation() < 0) {
                    panel.getMandelState().zoomIn(panel.getPanelState().getJourneyUnits());
                } else {
                    state.zoomOut(panel.getPanelState().getZoomUnits());
                }
                panel.generateAndSaveImageIfToggled();
                panel.repaint();
            }
        };
        panel.addMouseListener(mouseAdapter);
        panel.addMouseWheelListener(mouseAdapter);
    }

}