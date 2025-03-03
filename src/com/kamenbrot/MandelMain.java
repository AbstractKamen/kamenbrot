package com.kamenbrot;

import com.kamenbrot.state.MandelState;
import com.kamenbrot.ui.MandelKeyListener;
import com.kamenbrot.ui.ProperMandelbrotPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MandelMain {

    public static void main(String[] args) {
        final JFrame frame = new JFrame("Mandelbrot Set");
        final ProperMandelbrotPanel panel = new ProperMandelbrotPanel();

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
                panel.getPanelState().setWidthAndHeight(w, h);
                panel.getImageGenerator().resizeImage(w, h);
                panel.getMandelState().setMandelWidth(w);
                panel.getMandelState().setMandelHeight(h);
                panel.getImageGenerator().generateImage();
                panel.repaint();
            }

        });
        frame.setMinimumSize(new Dimension(panel.getMandelState().getMandelWidth(), panel.getMandelState().getMandelHeight()));
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
