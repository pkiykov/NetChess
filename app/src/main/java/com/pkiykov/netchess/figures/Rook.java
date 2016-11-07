package com.pkiykov.netchess.figures;

import com.pkiykov.netchess.pojo.GameExtraParams;

import java.util.ArrayList;

public class Rook extends Figure {
    public Rook(int oldX, int oldY, boolean color, GameExtraParams gameExtraParams, ArrayList<String> moveList) {
        super(oldX, oldY, color, gameExtraParams, moveList);
    }

    @Override
    public boolean move(int newX, int newY, boolean recursion) {
        if (recursion) {
            if (!checkIfLocationIsFree(newX, newY)) {
                if (checkIfLocationIsOccupied(newX, newY, color)) {
                    return false;
                }
            }
        }
        if (newX != oldX && newY != oldY) {
            return false;
        }

        int k = 1;
        while (k != Math.max(Math.abs(newX - oldX), Math.abs(newY - oldY))) {
            if (newX == oldX) {
                if (checkIfLocationIsFree(newX, Math.min(newY, oldY) + k)) {
                    k++;
                } else {
                    return false;
                }
            } else {
                if (checkIfLocationIsFree(Math.min(newX, oldX) + k, oldY)) {
                    k++;
                } else {
                    return false;
                }
            }
        }
        return !recursion || moveIsLegalIfKingIsChecked(color, newX, newY);
    }

}
