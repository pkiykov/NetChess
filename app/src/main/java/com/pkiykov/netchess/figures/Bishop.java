package com.pkiykov.netchess.figures;

import com.pkiykov.netchess.pojo.GameExtraParams;

import java.util.ArrayList;

public class Bishop extends Figure {

    public Bishop(int a, int b, boolean color, GameExtraParams gameExtraParams, ArrayList<String> moveList) {
        super(a, b, color, gameExtraParams, moveList);

    }

    @Override
    public boolean move(int a1, int b1, boolean recursion) {
        if (recursion) {
            if (!checkIfLocationIsFree(a1, b1)) {
                if (checkIfLocationIsOccupied(a1, b1, color)) {
                    return false;
                }
            }
        }

        if (Math.abs(oldX - a1) != Math.abs(oldY - b1)) {
            return false;
        }

        int k = 1;
        while (k < Math.abs(oldX - a1)) {
            if (b1 > oldY && a1 > oldX) {
                if (!checkIfLocationIsFree(oldX + k, oldY + k)) {
                    return false;
                }
            } else if (b1 > oldY && oldX > a1) {
                if (!checkIfLocationIsFree(oldX - k, oldY + k)) {
                    return false;
                }
            } else if (oldY > b1 && oldX > a1) {
                if (!checkIfLocationIsFree(oldX - k, oldY - k)) {
                    return false;
                }
            } else if (oldY > b1 && a1 > oldX) {
                if (!checkIfLocationIsFree(oldX + k, oldY - k)) {
                    return false;
                }
            }
            k++;
        }
        return !recursion || moveIsLegalIfKingIsChecked(color, a1, b1);

    }

}
