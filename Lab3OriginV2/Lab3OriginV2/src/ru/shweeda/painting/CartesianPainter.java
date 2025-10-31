package ru.shweeda.painting;

import ru.shweeda.Converter;

import java.awt.*;

public class CartesianPainter implements Painter{

    private final Converter conv;
    private Color mainColor = Color.BLACK;
    private Color accentColor = Color.RED;

    public CartesianPainter(Converter conv){
        this.conv = conv;
    }

    public Color getMainColor() {
        return mainColor;
    }

    public void setMainColor(Color mainColor) {
        this.mainColor = mainColor;
    }

    public Color getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(Color accentColor) {
        this.accentColor = accentColor;
    }

    @Override
    public void setWidth(double width) {
        conv.setWidth(width);
    }

    @Override
    public double getWidth() {
        return conv.getWidth();
    }

    @Override
    public void setHeight(double height) {
        conv.setHeight(height);
    }

    @Override
    public double getHeight() {
        return conv.getHeight();
    }

    @Override
    public void paint(Graphics g) {
        var x0 = conv.xCrt2Scr( 0.0);
        var y0 = conv.yCrt2Scr( 0.0);
        g.setColor(mainColor);
        g.drawLine(0, (int)y0, (int)getWidth(), (int)y0);
        g.drawLine((int)x0, 0, (int)x0, (int)getHeight());
    }
}
