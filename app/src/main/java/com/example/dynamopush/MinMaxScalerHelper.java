package com.example.dynamopush;

public class MinMaxScalerHelper {
    private final float minX, maxX;
    private final float minY, maxY;

    public MinMaxScalerHelper(float minX, float maxX, float minY, float maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }
    public float normalizeX(float x) {
        return (x - minX) / (maxX - minX);
    }
    public float inverseTransformY(float y) {
        return (y * (maxY - minY)) + minY;
    }
}
