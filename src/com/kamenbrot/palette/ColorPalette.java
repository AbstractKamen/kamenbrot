package com.kamenbrot.palette;

import java.awt.Color;

public enum ColorPalette {
    COOL_COLORS_1("Cool Colors 1", CoolColors.getCoolColors1()),
    COOL_COLORS_420("Cool Colors 420", CoolColors.getCoolColors420()),
    COOL_COLORS_69("Cool Colors 69", CoolColors.getCoolColors69()),
    COOL_COLORS_2("Cool Colors 2", CoolColors.getCoolColors2()),
    COOL_COLORS_3("Cool Colors 3", CoolColors.getCoolColors3()),
    COOL_COLORS_4("Cool Colors 4", CoolColors.getCoolColors4()),
    COOL_COLORS_5("Cool Colors 5", CoolColors.getCoolColors5()),
    FIRE("Fire", CoolColors.getFireColors()),
    OCEAN("Ocean", CoolColors.getOceanColors()),
    TOXIC("Toxic", CoolColors.getToxicColors()),
    FIRE_ICE("Fire & Ice", CoolColors.getFireIceColors()),
    CYBERPUNK("Cyberpunk", CoolColors.getCyberpunkColors()),
    AURORA("Aurora", CoolColors.getAuroraColors()),
    SUNSET("Sunset", CoolColors.getSunsetColors()),
    CRYSTAL("Crystal", CoolColors.getCrystalColors()),
    NEON("Neon", CoolColors.getNeonColors()),
    MONOCHROME_STEEL("Monochrome Steel", CoolColors.getMonochromeSteelColors());
    private final String label;
    private final Color[] colors;

    ColorPalette(String label, Color[] colors) {
        this.label = label;
        this.colors = colors;
    }

    public String getLabel() {
        return label;
    }

    public Color[] getColors() {
        return colors;
    }

    @Override
    public String toString() {
        return label;
    }
}