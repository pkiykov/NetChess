package com.pkiykov.netchess.figures;

import com.pkiykov.netchess.pojo.GameExtraParams;

import java.util.ArrayList;

public class King extends Figure {
    
    public King(int oldX, int oldY, boolean color, GameExtraParams gameExtraParams, ArrayList<String> moveList) {
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
        if (Math.abs(newX - oldX) > 1 || Math.abs(newY - oldY) > 1) {
            if ((newX != 7 && newX != 3) || !firstMove) {
                return false;
            }
            if (color) {
                if (newY != 1) {
                    return false;
                }
            } else {
                if (newY != 8) {
                    return false;
                }
            }
            if (!canCastle(newX, newY)) {
                return false;
            }
        }

        return !recursion || moveIsLegalIfKingIsChecked(color, newX, newY);

    }

    private boolean canCastle(int a1, int b1) {
        if (!checkIfLocationIsFree(a1 - 1, b1)) {
            return false;
        }
        int tmpX = oldX, tmpY = oldY;
        if (a1 == 3) {
            int k = 0;
            while (k < 2) {
                if (!kingIsNotChecked(color)) {
                    overwriteCoordinates(tmpX, tmpY);
                    return false;
                }
                overwriteCoordinates(oldX - k, b1);
                k++;
            }
            overwriteCoordinates(tmpX, tmpY);
            for (Figure f : gameExtraParams.getFigures()) {
                if (f.getOldX() == 1 && f.getOldY() == b1) {
                    return f.isFirstMove();
                }
            }
            return false;
        } else {
            int k = 0;
            while (k < 2) {
                if (!kingIsNotChecked(color)) {
                    overwriteCoordinates(tmpX, tmpY);
                    return false;
                }
                overwriteCoordinates(oldX + k, b1);
                k++;
            }
            overwriteCoordinates(tmpX, tmpY);
            for (Figure f : gameExtraParams.getFigures()) {
                if (f.getOldX() == 8 && f.getOldY() == b1) {
                    return f.isFirstMove();
                }
            }
            return false;
        }
    }

}

