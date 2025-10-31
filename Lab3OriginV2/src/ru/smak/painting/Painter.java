package ru.smak.painting;

import java.awt.*;

public interface Painter {
    void setWidth(double width);
    double  getWidth();
    void setHeight(double height);
    double  getHeight();

    void paint(Graphics g);
}
