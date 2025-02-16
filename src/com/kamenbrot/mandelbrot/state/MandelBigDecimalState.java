package com.kamenbrot.mandelbrot.state;

import com.kamenbrot.mandelbrot.fractals.Mapping;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class MandelBigDecimalState extends MandelStateAbstract<BigDecimal> {

  private static final BigDecimal MIN_X = BigDecimal.valueOf(GenericMandelState.MIN_X);
  private static final BigDecimal MAX_X = BigDecimal.valueOf(GenericMandelState.MAX_X);
  private static final BigDecimal MIN_Y = BigDecimal.valueOf(GenericMandelState.MIN_Y);
  private static final BigDecimal MAX_Y = BigDecimal.valueOf(GenericMandelState.MAX_Y);
  private static BigDecimal[] decimalCache;
  private static final Map<Double, BigDecimal> decimalMapCache = new HashMap<>();

  private BigDecimal centerX = BigDecimal.ZERO;
  private BigDecimal centerY = BigDecimal.ZERO;
  private MathContext mathContext = new MathContext(20, RoundingMode.HALF_UP);
  private BigDecimal minX = MIN_X;
  private BigDecimal maxX = MAX_X;
  private BigDecimal minY = MIN_Y;
  private BigDecimal maxY = MAX_Y;

  public MandelBigDecimalState(int maxIterations, int mandelWidth, int mandelHeight) {
    super(maxIterations, mandelWidth, mandelHeight);
    if (decimalCache == null) {
      decimalCache = initCache(mandelWidth, mandelHeight);
    }
  }

  public MandelBigDecimalState(MandelDoubleState mandelState) {
    super(mandelState);
    this.minX = BigDecimal.valueOf(mandelState.getMinX()).setScale(mathContext.getPrecision(), mathContext.getRoundingMode());
    this.maxX = BigDecimal.valueOf(mandelState.getMaxX()).setScale(mathContext.getPrecision(), mathContext.getRoundingMode());
    this.minY = BigDecimal.valueOf(mandelState.getMinY()).setScale(mathContext.getPrecision(), mathContext.getRoundingMode());
    this.maxY = BigDecimal.valueOf(mandelState.getMaxY()).setScale(mathContext.getPrecision(), mathContext.getRoundingMode());
    this.centerX = BigDecimal.valueOf(mandelState.getCenterX()).setScale(mathContext.getPrecision(), mathContext.getRoundingMode());
    this.centerY = BigDecimal.valueOf(mandelState.getCenterY()).setScale(mathContext.getPrecision(), mathContext.getRoundingMode());
    if (decimalCache == null) {
      decimalCache = initCache(getMandelWidth(), getMandelHeight());
    }
  }

  private BigDecimal[] initCache(int mandelWidth, int mandelHeight) {
    final int len = Math.max(mandelHeight, mandelWidth) + 1;
    final BigDecimal[] decimalCache = new BigDecimal[len]; ;

    for (int i = 0; i < len; ++i) {
      decimalCache[i] = BigDecimal.valueOf(i);
    }
    return decimalCache;
  }

  public BigDecimal[] getDecimalCache() {
    return decimalCache;
  }

  @Override
  public BigDecimal getCenterX() {
    return centerX;
  }

  @Override
  public BigDecimal getCenterY() {
    return centerY;
  }

  @Override
  public BigDecimal getMinX() {
    return minX;
  }

  @Override
  public BigDecimal getMinY() {
    return minY;
  }

  @Override
  public BigDecimal getMaxX() {
    return maxX;
  }

  @Override
  public BigDecimal getMaxY() {
    return maxY;
  }

  @Override
  public void setCenter(int x, int y) {
    this.centerX = Mapping.mapComplex(decimalCache[x], decimalCache[getMandelWidth()], minX, maxX, mathContext);
    this.centerY = Mapping.mapComplex(decimalCache[y], decimalCache[getMandelHeight()], minY, maxY, mathContext);
    calcZoom(1);
  }

  @Override
  public void resetCoordinates() {
    this.minX = MIN_X;
    this.maxX = MAX_X;
    this.minY = MIN_Y;
    this.maxY = MAX_Y;
    calcZoom(super.setZoom(1));
  }


  @Override
  protected void calcZoom(double z) {
    final BigDecimal Z = decimalMapCache.computeIfAbsent(z, BigDecimal::valueOf);

    final BigDecimal rangeX = maxX.subtract(minX, mathContext)
      .multiply(Z, mathContext)
      .divide(BigDecimal.TWO, mathContext);

    final BigDecimal rangeY = maxY.subtract(minY, mathContext)
      .multiply(Z, mathContext)
      .divide(BigDecimal.TWO, mathContext);

    this.minX = centerX.subtract(rangeX, mathContext);
    this.maxX = centerX.add(rangeX, mathContext);
    this.minY = centerY.subtract(rangeY, mathContext);
    this.maxY = centerY.add(rangeY, mathContext);
  }


  @Override
  public MathContext getMathContext() {
    return mathContext;
  }

  @Override
  public MandelState translate() {
    return new MandelDoubleState(this);
  }
}
