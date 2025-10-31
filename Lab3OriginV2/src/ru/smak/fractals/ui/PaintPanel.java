package ru.smak.fractals.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class PaintPanel extends JPanel {

    private boolean selectionActive = false;

    public void setSelectionActive(boolean active) {
        this.selectionActive = active;
    }

    public boolean isSelectionActive() {
        return selectionActive;
    }

    class MouseButtonAndMotionListener extends MouseAdapter{

        public Rect rect = new Rect();

        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            g = getGraphics();
            // Исправление бага с XOR-Mode
            g.setXORMode(Color.WHITE);
            g.fillRect(-10, -10, 1, 1);
            g.setPaintMode();

            rect.clear();
            rect.addPoint(e.getPoint());
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            super.mouseReleased(e);
            if (selectedAction != null){
                selectedAction.accept(rect);
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            super.mouseDragged(e);
            if (rect.isValid()) {
                drawRect(rect);
            }
            rect.addPoint(e.getPoint());
            if (rect.isValid()){
                drawRect(rect);
            }
        }
    }

    private Consumer<Graphics> paintAction = null;
    private Consumer<Rect> selectedAction = null;
    private MouseButtonAndMotionListener mbl = new MouseButtonAndMotionListener();
    private Graphics g = null;
    private Color selectionColor = Color.BLACK;

    public void addSelectListener(Consumer<Rect> l) {
        selectedAction = l;
        addMouseListener(mbl);
        addMouseMotionListener(mbl);
    }

    public void removeSelectListener(Consumer<Rect> l){
        selectedAction = null;
        removeMouseListener(mbl);
        removeMouseMotionListener(mbl);
    }

    public void setPaintAction(Consumer<Graphics> action){
        paintAction = action;
    }

    public void removePaintAction(){
        paintAction = null;
    }

    @Override
    public void paint(Graphics g){
        super.paint(g);
        if (paintAction != null) paintAction.accept(g);
    }

    private void drawRect(Rect r){
        g.setXORMode(Color.WHITE);
        g.setColor(selectionColor);
        try {
            g.drawRect(r.getX(), r.getY(), r.getWidth(), r.getHeight());
        } catch (InvalidRectException _){}
        finally {
            g.setPaintMode();
        }
    }
}
