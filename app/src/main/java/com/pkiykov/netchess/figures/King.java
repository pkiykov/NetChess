package com.pkiykov.netchess.figures;

import com.pkiykov.netchess.pojo.GameExtraParams;

import java.util.ArrayList;

public class King extends Figure {

    public King(int a, int b, boolean color, GameExtraParams gameExtraParams, ArrayList<String> moveList) {
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
        if (Math.abs(a1 - a) > 1 || Math.abs(b1 - b) > 1) {
            if ((a1 != 7 && a1 != 3) || !firstMove) {
                return false;
            }
            if (color) {
                if (b1 != 1) {
                    return false;
                }
            } else {
                if (b1 != 8) {
                    return false;
                }
            }
            if (!canCastle(a1, b1)) {
                return false;
            }
        }

        return !recursion || moveIsLegalIfKingIsChecked(color, a1, b1);

    }

    private boolean canCastle(int a1, int b1) {
        if (!checkIfLocationIsFree(a1 - 1, b1)) {
            return false;
        }
        int tmp1 = a, tmp2 = b;
        if (a1 == 3) {
            int k = 0;
            while (k < 2) {
                if (!kingIsNotChecked(color)) {
                    overwriteCoordinates(tmp1, tmp2);
                    return false;
                }
                overwriteCoordinates(a - k, b1);
                k++;
            }
            overwriteCoordinates(tmp1, tmp2);
            for (Figure f : gameExtraParams.getFigures()) {
                if (f.getA() == 1 && f.getB() == b1) {
                    return f.isFirstMove();
                }
            }
            return false;
        } else {
            int k = 0;
            while (k < 2) {
                if (!kingIsNotChecked(color)) {
                    overwriteCoordinates(tmp1, tmp2);
                    return false;
                }
                overwriteCoordinates(a + k, b1);
                k++;
            }
            overwriteCoordinates(tmp1, tmp2);
            for (Figure f : gameExtraParams.getFigures()) {
                if (f.getA() == 8 && f.getB() == b1) {
                    return f.isFirstMove();
                }
            }
            return false;
        }
    }

}

