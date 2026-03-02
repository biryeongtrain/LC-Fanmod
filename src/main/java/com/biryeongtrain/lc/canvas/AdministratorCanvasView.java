package com.biryeongtrain.lc.canvas;

import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.util.math.random.Random;

import javax.xml.transform.Source;


public class AdministratorCanvasView implements DrawableCanvas {
    private DrawableCanvas canvas;
    private final Random localRandom = new LocalRandom(0);

    public AdministratorCanvasView(DrawableCanvas canvas) {
        this.canvas = canvas;
    }
    
    public void setCanvas(DrawableCanvas canvas) {
        this.canvas = canvas;
    }
    
    

    @Override
    public byte getRaw(int x, int y) {
        return canvas.getRaw(x, y);
    }

    @Override
    public void setRaw(int x, int y, byte color) {
        canvas.setRaw(x, y, color);
    }

    @Override
    public int getHeight() {
        return canvas.getWidth();
    }

    @Override
    public int getWidth() {
        return canvas.getHeight();
    }
}
