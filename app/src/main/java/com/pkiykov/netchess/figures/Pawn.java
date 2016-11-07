package com.pkiykov.netchess.figures;

import com.pkiykov.netchess.pojo.GameExtraParams;

import java.util.ArrayList;

public class Pawn extends Figure {

    private boolean enPassantCapture;

    public Pawn(int oldX, int oldY, boolean color, GameExtraParams gameExtraParams, ArrayList<String> moveList) {
        super(oldX, oldY, color, gameExtraParams, moveList);
        enPassantCapture = false;
    }

    @Override
    public boolean move(int newX, int newY, boolean recursion) {
        if (recursion) {
            if (!checkIfLocationIsFree(newX, newY)) {
                if (checkIfLocationIsOccupied(newX, newY, color) || oldX == newX) {
                    return false;
                }
            }
        }

        if (Math.abs(newY - oldY) > 2 || Math.abs(newX - oldX) > 1) {
            return false;
        }
        if (Math.abs(newY - oldY) > 1 && oldX != newX) {
            return false;
        }
        if (color) {
            if (newY <= oldY) {
                return false;
            }
        } else {
            if (newY >= oldY) {
                return false;
            }
        }
        if (!firstMove && Math.abs(newY - oldY) == 2) {
            return false;
        }
        if (oldX == newX) {
            if (color) {
                if (!checkIfLocationIsFree(newX, oldY + 1)) {
                    return false;
                }
                if (newY - oldY == 2) {
                    if (!checkIfLocationIsFree(newX, oldY + 2)) {
                        return false;
                    }
                }
            } else {
                if (!checkIfLocationIsFree(newX, oldY - 1)) {
                    return false;
                }
                if (oldY - newY == 2) {
                    if (!checkIfLocationIsFree(newX, oldY - 2)) {
                        return false;
                    }
                }
            }
        }

        if (oldX != newX && checkIfLocationIsFree(newX, newY)) {
           if(!enPassantCapture(newX, newY)){
               return false;
           }
        }

        return !recursion || moveIsLegalIfKingIsChecked(color, newX, newY);

    }

    private boolean enPassantCapture(int newX, int newY) {
        String move = "";
        if (moveList.size() != 0) {
            move = moveList.get(moveList.size()-1);
        }
        if (move.contains("P")) {
            move = move.substring(1);
            if (move.charAt(0) != move.charAt(2) || (char) (96 + newX) != move.charAt(0)
                    || Math.abs(Character.getNumericValue(move.charAt(1)) - newY) != Math
                    .abs(Character.getNumericValue(move.charAt(3)) - newY)) {
                return false;
            }
            enPassantCapture = true;
        } else {
            return false;
        }
        return true;
    }

    boolean isEnPassantCapture() {
        return enPassantCapture;
    }

    void setEnPassantCapture(boolean enPassantCapture) {
        this.enPassantCapture = enPassantCapture;
    }
}

