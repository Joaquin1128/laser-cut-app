package com.example.lasercut.laser_cut_back.shared.util;

public class DxfAnalysis {

    public final double width;
    public final double height;
    public final double cutLengthMm;

    public DxfAnalysis(double width, double height, double cutLengthMm) {
        this.width = width;
        this.height = height;
        this.cutLengthMm = cutLengthMm;
    }

}
