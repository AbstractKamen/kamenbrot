package com.kamenbrot.state;

public interface GenericMandelState<T extends Number> extends MandelState {

    double MIN_X = -2.0;
    double MAX_X = 1.0;
    double MIN_Y = -1.5;
    double MAX_Y = 1.5;

    T cachedValue(int i);

    T getCenterX();
    T getCenterY();

    default String getCenterXString(){return getCenterX().toString();}
    default String getCenterYString(){return getCenterY().toString();}

    T getMinX();
    T getMinY();
    T getMaxX();
    T getMaxY();
}