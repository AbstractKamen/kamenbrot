package com.kamenbrot.state;

import com.kamenbrot.palette.ColorPalette;
import com.kamenbrot.palette.CoolColors;
import com.kamenbrot.palette.PaletteGenerator;

import java.awt.Color;

public class PaletteState {

  private static final int MIN_PALETTE_SIZE = 16;
  private static final int MAX_PALETTE_SIZE = 1024 * 16;

  private volatile int paletteIndex;
  private volatile int paletteSize = MIN_PALETTE_SIZE;

  private final ColorPalette[] rootPalettes;
  private final Color[][] palettes;

  public PaletteState() {
	this.rootPalettes = ColorPalette.values();
	this.palettes = new Color[rootPalettes.length][];
	regeneratePalettes();
  }

  public synchronized Color[] getNextPalette() {
	return palettes[(paletteIndex = (paletteIndex + 1) % palettes.length)];
  }

  public synchronized Color[] getCurrentPalette() {
	return palettes[paletteIndex];
  }

  public synchronized String getCurrentPaletteLabel() {
	return rootPalettes[paletteIndex].getLabel();
  }

  public int getPaletteSize() {
	return paletteSize;
  }

  private synchronized void regeneratePalettes() {
	for (int i = 0; i < rootPalettes.length; i++) {
	  this.palettes[i] = PaletteGenerator.generatePalette(rootPalettes[i].getColors(), paletteSize);
	}
  }

  public synchronized Color[] incrementPaletteSize() {
	this.paletteSize = Math.min(MAX_PALETTE_SIZE, paletteSize << 1);
	regeneratePalettes();
	return getCurrentPalette();
  }

  public synchronized Color[] decrementPaletteSize() {
	this.paletteSize = Math.max(MIN_PALETTE_SIZE, paletteSize >> 1);
	regeneratePalettes();
	return getCurrentPalette();
  }
}