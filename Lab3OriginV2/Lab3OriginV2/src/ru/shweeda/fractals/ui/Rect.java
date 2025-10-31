package ru.shweeda.fractals.ui;

import java.awt.*;

public class Rect {
    Point p1 = null;
    Point p2 = null;

    public void addPoint(Point p){
        if (p1 == null){
            p1 = p;
        } else {
            p2 = p;
        }
    }

    public void clear(){
        p1 = null;
        p2 = null;
    }

    public int getX() throws InvalidRectException{
        if (isValid())
            return Math.min(p1.x, p2.x);
        throw new InvalidRectException();
    }

    public int getY() throws InvalidRectException{
        if (isValid())
            return Math.min(p1.y, p2.y);
        throw new InvalidRectException();
    }

    public int getWidth() throws InvalidRectException{
        if (isValid()){
            return Math.max(p1.x, p2.x) - Math.min(p1.x, p2.x);
        }
        throw new InvalidRectException();
    }

    public int getHeight() throws InvalidRectException{
        if (isValid()){
            return Math.max(p1.y, p2.y) - Math.min(p1.y, p2.y);
        }
        throw new InvalidRectException();
    }

    public boolean isValid(){
        return p1 != null && p2 != null;
    }
}
