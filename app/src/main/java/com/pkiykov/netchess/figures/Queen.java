package com.pkiykov.netchess.figures;

import com.pkiykov.netchess.pojo.GameExtraParams;

import java.util.ArrayList;

public class Queen extends Figure {
    public Queen(int a, int b, boolean color, GameExtraParams gameExtraParams, ArrayList<String> moveList) {
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
        if (a1 != a && b1 != b) {
            if (Math.abs(a - a1) != Math.abs(b - b1)) {
                return false;
            }
        }

        if (a1 == a ^ b1 == b) {
            int k = 1;
            while (k != Math.max(Math.abs(a1 - a), Math.abs(b1 - b))) {
                if (a1 == a) {
                    if (checkIfLocationIsFree(a, Math.min(b1, b) + k)) {
                        k++;
                    } else {
                        return false;
                    }
                } else {
                    if (checkIfLocationIsFree(Math.min(a1, a) + k, b)) {
                        k++;
                    } else {
                        return false;
                    }
                }
            }
        } else {
            int k = 1;
            while (k < Math.abs(a - a1)) {
                if (b1 > b && a1 > a) {
                    if (!checkIfLocationIsFree(a + k, b + k)) {
                        return false;
                    }
                } else if (b1 > b && a > a1) {
                    if (!checkIfLocationIsFree(a - k, b + k)) {
                        return false;
                    }
                } else if (b > b1 && a > a1) {
                    if (!checkIfLocationIsFree(a - k, b - k)) {
                        return false;
                    }
                } else if (b > b1 && a1 > a) {
                    if (!checkIfLocationIsFree(a + k, b - k)) {
                        return false;
                    }
                }
                k++;
            }
        }

        return !recursion || moveIsLegalIfKingIsChecked(color, a1, b1);
    }

}
