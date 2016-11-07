package com.pkiykov.netchess.pojo;

import java.io.Serializable;

public class Coordinates implements Serializable {
    public static final String COORDINATES = "coordinates";
    public static final int QUEEN_ID = 1;
    public static final int ROOK_ID = 2;
    public static final int KNIGHT_ID = 3;
    public static final int BISHOP_ID = 4;

    private int oldX, newX, oldY, newY, figureType;

    @Override
    public boolean equals(Object obj) {
        return ((Coordinates) obj).getClass().equals(obj.getClass()) && ((Coordinates) obj).getOldX() == this.getOldX()
                && ((Coordinates) obj).getOldY() == this.getOldY() && ((Coordinates) obj).getNewX() == this.getNewX()
                && ((Coordinates) obj).getNewY() == this.getNewY();
    }

    public int getOldX() {
        return oldX;
    }

    public void setOldX(int oldX) {
        this.oldX = oldX;
    }

    public int getNewX() {
        return newX;
    }

    public void setNewX(int newX) {
        this.newX = newX;
    }

    public int getOldY() {
        return oldY;
    }

    public void setOldY(int oldY) {
        this.oldY = oldY;
    }

    public int getNewY() {
        return newY;
    }

    public void setNewY(int newY) {
        this.newY = newY;
    }

    public int getFigureType() {
        return figureType;
    }

    public void setFigureType(int figureType) {
        this.figureType = figureType;
    }
}
