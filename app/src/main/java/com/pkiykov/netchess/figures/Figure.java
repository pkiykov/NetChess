package com.pkiykov.netchess.figures;

import com.pkiykov.netchess.pojo.GameExtraParams;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class Figure implements Actions, Serializable {
    protected int oldX;
    protected int oldY;
    protected boolean color;
    boolean firstMove;
    GameExtraParams gameExtraParams;
    protected ArrayList<String> moveList;
    public static final String PAWN = "Pawn";
    public static final String KNIGHT = "Knight";
    public static final String BISHOP = "Bishop";
    public static final String ROOK = "Rook";
    public static final String KING = "King";

    public Figure(int oldX, int oldY, boolean color, GameExtraParams gameExtraParams, ArrayList<String> moveList) {
        this.oldX = oldX;
        this.oldY = oldY;
        this.color = color;
        this.firstMove = true;
        this.gameExtraParams = gameExtraParams;
        this.moveList = moveList;
    }

    public boolean isFirstMove() {
        return firstMove;
    }

    public void setFirstMove(boolean firstMove) {
        this.firstMove = firstMove;
    }

    public int getOldX() {
        return oldX;
    }

    public void setOldX(int oldX) {
        this.oldX = oldX;
    }

    public int getOldY() {
        return oldY;
    }

    public void setOldY(int oldY) {
        this.oldY = oldY;
    }

    public boolean isColor() {
        return color;
    }

    @Override
    public boolean checkIfLocationIsOccupied(int newX, int newY, boolean sideToMove) {
        for (Figure f : gameExtraParams.getFigures()) {
            if (f.getOldX() == newX && f.getOldY() == newY)
                return f.isColor() == sideToMove;
        }
        return false;
    }

    @Override
    public boolean kingIsNotChecked(boolean sideToMove) {
        int newX = 0, newY = 0;
        for (Figure f : gameExtraParams.getFigures()) {
            if (f instanceof King && f.isColor() == sideToMove) {
                newX = f.getOldX();
                newY = f.getOldY();
                break;
            }
        }

        for (Figure f : gameExtraParams.getFigures()) {
            if (f.isColor() != sideToMove) {
                if (f.move(newX, newY, false)) {
                    return false;
                }
            }
        }

        return true;
    }


    @Override
    public void overwriteCoordinates(int newX, int newY) {
        oldX = newX;
        oldY = newY;
    }


    @Override
    public boolean checkIfLocationIsFree(int newX, int newY) {
        for (Figure f : gameExtraParams.getFigures()) {
            if (f.getOldX() == newX && f.getOldY() == newY) {
                return false;
            }
        }
        return true;
    }

    public boolean moveIsLegalIfKingIsChecked(boolean color, int newX, int newY) {

        Figure backup = null;
        for (Figure f : gameExtraParams.getFigures()) {
            if (f.getOldX() == newX && f.getOldY() == newY) {
                backup = f;
                gameExtraParams.getFigures().remove(f);
                break;
            }
        }
        if (backup == null && moveList.size() > 0 && this instanceof Pawn) {
            backup = enpassantCapture(newX, newY);
        }
        int tmpX = oldX;
        int tmpY = oldY;
        overwriteCoordinates(newX, newY);

        if (!kingIsNotChecked(color)) {
            if (backup != null) {
                gameExtraParams.getFigures().add(backup);
            }
            overwriteCoordinates(tmpX, tmpY);
            return false;
        } else {
            if (backup != null) {
                gameExtraParams.getFigures().add(backup);
            }
            overwriteCoordinates(tmpX, tmpY);
            return true;
        }
    }

    private Figure enpassantCapture(int newX, int newY) {
        Figure backup = null;
        String lastMove = moveList.get(moveList.size() - 1);
        int newXfromLastMove = lastMove.charAt(3) - 96;
        int newYfromLastMove = Character.getNumericValue(lastMove.charAt(4));
        int oldYfromLastMove = Character.getNumericValue(lastMove.charAt(2));
        if (lastMove.contains("P") && newX == newXfromLastMove && Math.abs(oldYfromLastMove - newY)
                == Math.abs(newYfromLastMove - newY)) {
            for (Figure f : gameExtraParams.getFigures()) {
                if (f.getOldX() == newXfromLastMove && f.getOldY() == newYfromLastMove) {
                    backup = f;
                    gameExtraParams.getFigures().remove(f);
                    break;
                }
            }
        }
        return backup;
    }

    public void writePosition() {
        StringBuilder sb = new StringBuilder();
        for (Figure f : gameExtraParams.getFigures()) {
            sb.append(f.getClass().getSimpleName()).append(f.getOldX()).append(f.getOldY()).append(f.isColor()).append("\n");
            if (f instanceof Pawn) {
                sb.append(((Pawn) f).isEnPassantCapture());
            } else if (f instanceof King && f.isFirstMove()) {
                for (Figure ff : gameExtraParams.getFigures()) {
                    if (ff instanceof Rook && ff.isColor() == f.isColor() && ff.isFirstMove()) {
                        sb.append(ff.isColor());
                        sb.append(true);
                    }
                }
            }
        }
        if (this instanceof Pawn) {
            ((Pawn) this).setEnPassantCapture(false);
        }
        sb.append(gameExtraParams.isColor());
        gameExtraParams.getPosition().add(sb.toString());
        sb.setLength(0);
    }
}
