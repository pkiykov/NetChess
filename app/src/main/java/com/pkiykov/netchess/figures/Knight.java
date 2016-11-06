package com.pkiykov.netchess.figures;

import com.pkiykov.netchess.pojo.GameExtraParams;

import java.util.ArrayList;

public class Knight extends Figure {

    public Knight(int a, int b, boolean color, GameExtraParams gameExtraParams, ArrayList<String> moveList) {
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
        return Math.abs(a1 - a) <= 2 && Math.abs(a1 - a) >= 1
                && Math.abs(b1 - b) <= 2 && Math.abs(b1 - b) >= 1
                && Math.abs(Math.abs(a1 - a) - Math.abs(b1 - b)) == 1
                && (!recursion || moveIsLegalIfKingIsChecked(color, a1, b1));

    }
}

