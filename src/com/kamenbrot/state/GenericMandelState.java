package com.kamenbrot.state;

public interface GenericMandelState<T extends Number> extends MandelState {

    double MIN_X = -2.0;
    double MAX_X = 1.0;
    double MIN_Y = -1.5;
    double MAX_Y = 1.5;

    T cachedValue(int i);

    T getCenterX();
    T getCenterY();

    T getMinX();
    T getMinY();
    T getMaxX();
    T getMaxY();
}