package com.kamenbrot;

import com.kamenbrot.fractals.ComplexMapping;
import com.kamenbrot.state.ColorState;
import com.kamenbrot.state.MandelDoubleState;
import com.kamenbrot.state.MandelState;
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
    final ProperMandelbrotPanel panel = new ProperMandelbrotPanel(pool);

    final int mandelWidth = panel.getMandelState().getMandelWidth();
    final int mandelHeight = panel.getMandelState().getMandelHeight();
    final int miniWidth = (int) (mandelWidth * 0.25);
    final int miniHeight = (int) (mandelHeight * 0.25);

    final MiniPanel miniPanel = new MiniPanel(panel.getPanelState(), pool, miniWidth, miniHeight, new ColorState(panel.getColorState().getPalette()));
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
    frame.addKeyListener(new MandelKeyListener(panel.getMandelState(), panel.getPanelState(), panel, miniPanel));

    final MouseAdapter mouseAdapter = new MouseAdapter() {

      @Override
      public void mouseMoved(MouseEvent e) {
        if (panel.getMandelState().isJuliaToggled() || panel.getMandelState().isSaveToggled()) return;
        final MandelDoubleState mandelState = (MandelDoubleState) panel.getMandelState();
        final double re = ComplexMapping.mapComplex(e.getX(), mandelState.getMandelWidth(), mandelState.getMinX(), mandelState.getMaxX());
        final double imag = ComplexMapping.mapComplex(e.getY(), mandelState.getMandelHeight(), mandelState.getMinY(), mandelState.getMaxY());
        miniPanel.setPos(re, imag);
        miniPanel.getMiniImageGen().generateImage();
        miniPanel.repaint();
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
