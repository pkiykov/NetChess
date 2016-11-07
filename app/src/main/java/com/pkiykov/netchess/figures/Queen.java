package com.pkiykov.netchess.figures;

import com.pkiykov.netchess.pojo.GameExtraParams;

import java.util.ArrayList;

public class Queen extends Figure {
    public Queen(int oldX, int oldY, boolean color, GameExtraParams gameExtraParams, ArrayList<String> moveList) {
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
            if (Math.abs(oldX - newX) != Math.abs(oldY - newY)) {
                return false;
            }
        }

        if (newX == oldX ^ newY == oldY) {
            int k = 1;
            while (k != Math.max(Math.abs(newX - oldX), Math.abs(newY - oldY))) {
                if (newX == oldX) {
                    if (checkIfLocationIsFree(oldX, Math.min(newY, oldY) + k)) {
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
        } else {
            int k = 1;
            while (k < Math.abs(oldX - newX)) {
                if (newY > oldY && newX > oldX) {
                    if (!checkIfLocationIsFree(oldX + k, oldY + k)) {
                        return false;
                    }
                } else if (newY > oldY && oldX > newX) {
                    if (!checkIfLocationIsFree(oldX - k, oldY + k)) {
                        return false;
                    }
                } else if (oldY > newY && oldX > newX) {
                    if (!checkIfLocationIsFree(oldX - k, oldY - k)) {
                        return false;
                    }
                } else if (oldY > newY && newX > oldX) {
                    if (!checkIfLocationIsFree(oldX + k, oldY - k)) {
                        return false;
                    }
                }
                k++;
            }
        }

        return !recursion || moveIsLegalIfKingIsChecked(color, newX, newY);
    }

}
