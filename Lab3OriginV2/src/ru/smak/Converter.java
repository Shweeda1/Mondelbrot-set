//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//
package ru.smak;
public class Converter {
    private Double xMin;
    private Double xMax;
    private Double yMin;
    private Double yMax;
    private Double width;
    private Double height;

    public Converter(double xMin, double xMax, double yMin, double yMax) {
        this.setxMin(xMin);
        this.setxMax(xMax);
        this.setyMin(yMin);
        this.setyMax(yMax);
    }

    public double getxMin() {
        return this.xMin;
    }

    public void setxMin(Double xMin) {
        this.xMin = xMin;
    }

    public double getxMax() {
        return this.xMax;
    }

    public void setxMax(double xMax) {
        this.xMax = xMax;
    }

    public double getyMin() {
        return this.yMin;
    }

    public void setyMin(double yMin) {
        this.yMin = yMin;
    }

    public double getyMax() {
        return this.yMax;
    }

    public void setyMax(double yMax) {
        this.yMax = yMax;
    }

    public double getWidth() {
        return this.width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return this.height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getxDen() {
        return (this.width / (this.xMax - this.xMin));
    }

    public double getyDen() {
        return (this.height / (this.yMax - this.yMin));
    }

    public double xCrt2Scr(double x) {
        return ((double)this.getxDen() * (x - this.xMin));
    }

    public double yCrt2Scr(double y) {
        return (this.getyDen() * (this.yMax - y));
    }

    public double xScr2Crt(double x) {
        double worldX = this.xMin + x / this.getxDen();
        return worldX;
    }

    public double yScr2Crt(double y) {
        double worldY = this.yMax - y / this.getyDen();
        return worldY;
    }
}
