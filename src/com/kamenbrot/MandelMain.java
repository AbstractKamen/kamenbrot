package com.kamenbrot;

import com.kamenbrot.fractals.ComplexMapping;
import com.kamenbrot.fractals.DoubleDouble;
import com.kamenbrot.generators.PanelRenderer;
import com.kamenbrot.state.*;
import com.kamenbrot.ui.MandelKeyListener;
import com.kamenbrot.ui.MiniPanel;
import com.kamenbrot.ui.ProperMandelbrotPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public class MandelMain {

  public static void main(String[] args) {
    final JFrame frame = new JFrame("Mandelbrot Set");
    final JLayeredPane lpane = new JLayeredPane();
    frame.setLayout(new BorderLayout());
    frame.add(lpane, BorderLayout.CENTER);
    lpane.setLayout(null);

    final ForkJoinPool pool = (ForkJoinPool) Executors.newWorkStealingPool();
    final PanelRenderer renderer = new PanelRenderer();
    final PaletteState paletteState = new PaletteState();
    final Color[] palette = paletteState.getNextPalette();
    final ProperMandelbrotPanel panel = new ProperMandelbrotPanel(pool, renderer, paletteState);

    final int mandelWidth = panel.getMandelState().getMandelWidth();
    final int mandelHeight = panel.getMandelState().getMandelHeight();
    final int miniWidth = (int) (mandelWidth * 0.25);
    final int miniHeight = (int) (mandelHeight * 0.25);

    final MiniPanel miniPanel = new MiniPanel(panel.getPanelState(), pool, miniWidth, miniHeight, new ColourState(palette), renderer);
    panel.setOpaque(true);
    panel.setBounds(0, 0, mandelWidth, mandelHeight);
    miniPanel.setOpaque(true);
    miniPanel.setBounds(mandelWidth - miniWidth, mandelHeight - (int) (miniHeight * 1.2), miniWidth, miniHeight);

    lpane.add(panel, 0, 0);
    lpane.add(miniPanel, 1, 0);

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
    frame.setMinimumSize(new Dimension(mandelWidth, mandelHeight));
    frame.setMaximumSize(new Dimension(1600, 1200));
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
    frame.addKeyListener(new MandelKeyListener(panel.getMandelState(), panel.getPanelState(), panel, miniPanel, paletteState));

    final MouseAdapter mouseAdapter = new MouseAdapter() {

      @Override
      public void mouseMoved(MouseEvent e) {
        if (panel.getMandelState().isJuliaToggled() || panel.getMandelState().isSaveToggled()) return;
        final MandelDoubleDoubleState mandelState = (MandelDoubleDoubleState) panel.getMandelState();
        final DoubleDouble re = ComplexMapping.mapComplex(mandelState.cachedValue(e.getX()), mandelState.cachedValue(mandelState.getMandelWidth()), mandelState.getMinX(), mandelState.getMaxX());
        final DoubleDouble imag = ComplexMapping.mapComplex(mandelState.cachedValue(e.getY()), mandelState.cachedValue(mandelState.getMandelHeight()), mandelState.getMinY(), mandelState.getMaxY());
        miniPanel.setPos(re, imag);
        miniPanel.setNeedsRender();
      }

      @Override
      public void mousePressed(MouseEvent e) {
        panel.getMandelState().setCenter(e.getX(), e.getY());
        panel.setNeedsRender();
      }

      @Override
      public void mouseWheelMoved(MouseWheelEvent e) {
        final MandelState state = panel.getMandelState();
        if (e.getPreciseWheelRotation() < 0) {
          panel.getMandelState().zoomIn(panel.getPanelState().getJourneyUnits());
        } else {
          state.zoomOut(panel.getPanelState().getZoomUnits());
        }
        panel.setNeedsRender();
      }
    };
    panel.addMouseListener(mouseAdapter);
    panel.addMouseMotionListener(mouseAdapter);
    panel.addMouseWheelListener(mouseAdapter);
  }
}
