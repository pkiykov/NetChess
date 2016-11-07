package com.pkiykov.netchess.figures;

import com.pkiykov.netchess.pojo.GameExtraParams;

import java.util.ArrayList;

public class Knight extends Figure {

    public Knight(int oldX, int oldY, boolean color, GameExtraParams gameExtraParams, ArrayList<String> moveList) {
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
        return Math.abs(newX - oldX) <= 2 && Math.abs(newX - oldX) >= 1
                && Math.abs(newY - oldY) <= 2 && Math.abs(newY - oldY) >= 1
                && Math.abs(Math.abs(newX - oldX) - Math.abs(newY - oldY)) == 1
                && (!recursion || moveIsLegalIfKingIsChecked(color, newX, newY));

    }
}

