package com.cosmepics.susa.cosmepics;

import android.graphics.Rect;

/**
 * Created by Amir on 28/03/2015.
 */
public class magTile {
    Rect tile;
    int size, color;
    boolean isSelected=false;

    public magTile(Rect tile, int size, int color) {
        this.tile = tile;
        this.size = size;
        this.color = color;
    }

    public magTile(int size, int color) {
        this.size = size;
        this.color = color;
        this.tile = new Rect(0,0,size,size);
    }

    public magTile(int left, int top, int right, int bottom, int color) {
        this.tile = new Rect(left,top,right,bottom);
        this.size = Math.abs(right-left)==Math.abs(bottom-top)? Math.abs(bottom-top):0;
        this.color = color;
    }

    public magTile(int left, int top, int size, int color) {
        this.tile = new Rect(left,top,left+size,top+size);
        this.size = size;
        this.color = color;
    }

    public magTile(Rect tile, int size) {
        this.tile = tile;
        this.size = size;
    }

    public magTile(int size) {
        this.size = size;
        this.tile = new Rect(0,0,size,size);
    }

/*    public magTile(int left, int top, int right, int bottom) {
        this.tile = new Rect(left,top,right,bottom);
        this.size = Math.abs(right-left)==Math.abs(bottom-top)? Math.abs(bottom-top):0;
    }*/

    public magTile(int left, int top, int size) {
        this.tile = new Rect(left,top,left+size,top+size);
        this.size = size;
    }

    public void setTile(Rect tile) {
        this.tile = tile;
    }


    public void setSize(int size) {
        this.size = size;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public Rect getTile() {
        return tile;
    }

    public int getSize() {
        return size;
    }

    public int getColor() {
        return color;
    }

    public boolean getIsSelected(){return isSelected;}

    public void setSelected(){isSelected = true;}

    public void setDeselected(){isSelected = false;}

}
