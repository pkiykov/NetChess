package com.pkiykov.netchess.figures;

import com.pkiykov.netchess.pojo.GameExtraParams;

import java.util.ArrayList;

public class Pawn extends Figure {

    private boolean enPassantCapture;

    public Pawn(int a, int b, boolean color, GameExtraParams gameExtraParams, ArrayList<String> moveList) {
        super(a, b, color, gameExtraParams, moveList);
        enPassantCapture = false;
    }

    @Override
    public boolean move(int a1, int b1, boolean recursion) {
        if (recursion) {
            if (!checkIfLocationIsFree(a1, b1)) {
                if (checkIfLocationIsOccupied(a1, b1, color) || a == a1) {
                    return false;
                }
            }
        }

        if (Math.abs(b1 - b) > 2 || Math.abs(a1 - a) > 1) {
            return false;
        }
        if (Math.abs(b1 - b) > 1 && a != a1) {
            return false;
        }
        if (color) {
            if (b1 <= b) {
                return false;
            }
        } else {
            if (b1 >= b) {
                return false;
            }
        }
        if (!firstMove && Math.abs(b1 - b) == 2) {
            return false;
        }
        if (a == a1) {
            if (color) {
                if (!checkIfLocationIsFree(a1, b + 1)) {
                    return false;
                }
                if (b1 - b == 2) {
                    if (!checkIfLocationIsFree(a1, b + 2)) {
                        return false;
                    }
                }
            } else {
                if (!checkIfLocationIsFree(a1, b - 1)) {
                    return false;
                }
                if (b - b1 == 2) {
                    if (!checkIfLocationIsFree(a1, b - 2)) {
                        return false;
                    }
                }
            }
        }

        if (a != a1 && checkIfLocationIsFree(a1, b1)) {
            String move = "";
            if (moveList.size() != 0) {
                move = moveList.get(moveList.size()-1);
            }
            if (move.contains("P")) {
                move = move.substring(1);
                if (move.charAt(0) != move.charAt(2) || (char) (96 + a1) != move.charAt(0)
                        || Math.abs(Character.getNumericValue(move.charAt(1)) - b1) != Math
                        .abs(Character.getNumericValue(move.charAt(3)) - b1)) {
                    return false;
                }
                enPassantCapture = true;
            } else {
                return false;
            }
        }

        return !recursion || moveIsLegalIfKingIsChecked(color, a1, b1);

    }
    boolean isEnPassantCapture() {
        return enPassantCapture;
    }

    void setEnPassantCapture(boolean enPassantCapture) {
        this.enPassantCapture = enPassantCapture;
    }
}

