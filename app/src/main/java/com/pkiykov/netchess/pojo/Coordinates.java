package com.pkiykov.netchess.pojo;

import java.io.Serializable;

public class Coordinates implements Serializable {
    public static final String COORDINATES = "coordinates";
    public static final int QUEEN_ID = 1;
    public static final int ROOK_ID = 2;
    public static final int KNIGHT_ID = 3;
    public static final int BISHOP_ID = 4;

    private int a, a1, b, b1, figureType;

    @Override
    public boolean equals(Object obj) {
        return ((Coordinates) obj).getClass().equals(obj.getClass()) && ((Coordinates) obj).getA() == this.getA()
                && ((Coordinates) obj).getB() == this.getB() && ((Coordinates) obj).getA1() == this.getA1()
                && ((Coordinates) obj).getB1() == this.getB1();
    }

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public int getA1() {
        return a1;
    }

    public void setA1(int a1) {
        this.a1 = a1;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    public int getB1() {
        return b1;
    }

    public void setB1(int b1) {
        this.b1 = b1;
    }

    public int getFigureType() {
        return figureType;
    }

    public void setFigureType(int figureType) {
        this.figureType = figureType;
    }
}
