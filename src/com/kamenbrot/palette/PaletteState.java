package com.kamenbrot.palette;

import java.awt.*;

public class PaletteState {

  private static final int MIN_PALETTE_SIZE = 16;
  private static final int MAX_PALETTE_SIZE = 1024 * 16;

  private volatile int paletteIndex;
  private volatile int paletteSize = MIN_PALETTE_SIZE;

  private final Color[][] rootPalettes;
  private final Color[][] palettes;

  public PaletteState() {
	this.rootPalettes = new Color[][]{
			CoolColors.getCoolColors1(),
			CoolColors.getCoolColors2(),
			CoolColors.getCoolColors3(),
			CoolColors.getCoolColors4(),
			CoolColors.getCoolColors5(),
			CoolColors.getFireColors(),
			CoolColors.getOceanColors(),
			CoolColors.getFireIceColors(),
			CoolColors.getToxicColors(),
			CoolColors.getCyberpunkColors(),
			CoolColors.getCoolColors69(),
			CoolColors.getCoolColors420()
	};
	this.palettes = new Color[rootPalettes.length][];
	regeneratePalettes();
  }

  public synchronized Color[] getNextPalette() {
	return palettes[(paletteIndex = (paletteIndex + 1) % palettes.length)];
  }

  public synchronized Color[] getCurrentPalette() {
	return palettes[paletteIndex];
  }

  public int getPaletteSize() {
	return paletteSize;
  }

  private synchronized void regeneratePalettes() {
	for (int i = 0; i < rootPalettes.length; i++) {
	  this.palettes[i] = PaletteGenerator.generatePalette(rootPalettes[i], paletteSize);
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