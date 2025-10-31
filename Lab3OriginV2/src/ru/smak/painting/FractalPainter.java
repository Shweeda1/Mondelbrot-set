package ru.smak.painting;

import ru.smak.math.fractals.Fractal;
import ru.smak.Converter;
import ru.smak.math.Complex;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class FractalPainter implements Painter{

    private final Converter conv;
//    private final Mandelbrot m = new Mandelbrot();
    private final Fractal f;

    public FractalPainter(Converter conv, Fractal f){
        this.conv = conv;
        this.f = f;
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

    private BufferedImage paintVerticalLine(double i){
        BufferedImage bi = new BufferedImage(1, (int) getHeight(), BufferedImage.TYPE_INT_RGB);
        var g = bi.getGraphics();
        for (int j = 0; j < conv.getHeight(); j++) {
            double x = conv.xScr2Crt(i);
            double y = conv.yScr2Crt(j);
            var r = f.isInSet(new Complex(x, y));
            Color c;
            if (r == 1.0) {
                c = Color.BLACK;
            } else {
                var red = (float) Math.abs(Math.sin(13 * r * r));
                var green = (float) Math.abs((Math.sin(8 * r) + Math.cos(15 * r)) / 2);
                var blue = (float) Math.abs(Math.cos(23 * (1 - r)));
                c = new Color(red, green, blue);
            }
            g.setColor(c);
            g.fillRect(0, j, 1, 1);
        }
        return bi;
    }

    @Override
    public void paint(Graphics g) {
        g.fillRect(0, 0, (int)getWidth(), (int)getHeight());
        var threadCount = Runtime.getRuntime().availableProcessors();
        List<Future<BufferedImage>> futures = new ArrayList<>();
        try (ExecutorService threadPool = Executors.newFixedThreadPool(threadCount)) {
            for (int i = 0; i<getWidth(); i++){
                final int j = i;
                futures.add(CompletableFuture.supplyAsync(
                        () -> paintVerticalLine(j), threadPool
                ));
            }
            for (var i = 0; i<getWidth(); i++) {
                var bi = futures.get(i).get();
                synchronized (g) {
                    g.drawImage(bi, i, 0, null);
                }
            }
        } catch (Exception _){}
    }
}
