package com.pkiykov.netchess.figures;

import com.pkiykov.netchess.pojo.GameExtraParams;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class Figure implements Actions, Serializable{
    protected int a;
    protected int b;
    protected boolean color;
    boolean firstMove;
    GameExtraParams gameExtraParams;
    protected ArrayList<String> moveList;
    public static final String PAWN = "Pawn";
    public static final String KNIGHT = "Knight";
    public static final String BISHOP = "Bishop";
    public static final String ROOK = "Rook";
    public static final String KING = "King";

    public Figure(int a, int b, boolean color, GameExtraParams gameExtraParams, ArrayList<String> moveList) {
        this.a = a;
        this.b = b;
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

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    public boolean isColor() {
        return color;
    }

    @Override
    public boolean checkIfLocationIsOccupied(int a1, int b1, boolean sideToMove) {
        for (Figure f : gameExtraParams.getFigures()) {
            if (f.getA() == a1 && f.getB() == b1)
                return f.isColor() == sideToMove;
        }
        return false;
    }

    @Override
    public boolean kingIsNotChecked(boolean sideToMove) {
        int aa = 0, bb = 0;
        for (Figure f : gameExtraParams.getFigures()) {
            if (f.getClass().getSimpleName().equals(KING) && f.isColor() == sideToMove) {
                aa = f.getA();
                bb = f.getB();
                break;
            }
        }

        for (Figure f : gameExtraParams.getFigures()) {
            if (f.isColor() != sideToMove) {
                if (f.move(aa, bb, false)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void overwriteCoordinates(int a1, int b1) {
        a = a1;
        b = b1;
    }




    @Override
    public boolean checkIfLocationIsFree(int a1, int b1) {
        for (Figure f : gameExtraParams.getFigures()) {
            if (f.getA() == a1 && f.getB() == b1) {
                return false;
            }
        }
        return true;
    }

    public boolean moveIsLegalIfKingIsChecked(boolean color, int a1, int b1) {

        Figure backup = null;
        for (Figure f : gameExtraParams.getFigures()) {
            if (f.getA() == a1 && f.getB() == b1) {
                backup = f;
                gameExtraParams.getFigures().remove(f);
                break;
            }
        }
        if (backup == null && moveList.size() > 0 && this.getClass().getSimpleName().equals(Figure.PAWN)) {
            String lastMove = moveList.get(moveList.size()-1);
            int aa1 = lastMove.charAt(3) - 96;
            int bb1 = Character.getNumericValue(lastMove.charAt(4));
            int bb = Character.getNumericValue(lastMove.charAt(2));
            if (lastMove.contains("P") && a1 == aa1 && Math.abs(bb - b1) == Math.abs(bb1 - b1)) {
                for (Figure f : gameExtraParams.getFigures()) {
                    if (f.getA() == aa1 && f.getB() == bb1) {
                        backup = f;
                        gameExtraParams.getFigures().remove(f);
                        break;
                    }
                }
            }
        }
        int tmp1 = a;
        int tmp2 = b;
        overwriteCoordinates(a1, b1);

        if (!kingIsNotChecked(color)) {
            if (backup != null) {
                gameExtraParams.getFigures().add(backup);
            }
            overwriteCoordinates(tmp1, tmp2);
            return false;
        } else {
            if (backup != null) {
                gameExtraParams.getFigures().add(backup);
            }
            overwriteCoordinates(tmp1, tmp2);
            return true;
        }
    }

    public void writePosition() {
        StringBuilder sb = new StringBuilder();
        for (Figure f : gameExtraParams.getFigures()) {
            sb.append(f.getClass().getSimpleName()).append(f.getA()).append(f.getB()).append(f.isColor()).append("\n");
            if (f.getClass().getSimpleName().equals(Figure.PAWN)) {
                sb.append(((Pawn) f).isEnPassantCapture());
            } else if (f.getClass().getSimpleName().equals(Figure.KING) && f.isFirstMove()) {
                for (Figure ff : gameExtraParams.getFigures()) {
                    if (ff.getClass().getSimpleName().equals(Figure.ROOK) && ff.isColor() == f.isColor() && ff.isFirstMove()) {
                        sb.append(ff.isColor());
                        sb.append(true);
                    }
                }
            }
        }
        if (this.getClass().getSimpleName().equals(Figure.PAWN)) {
            ((Pawn) this).setEnPassantCapture(false);
        }
        sb.append(gameExtraParams.isColor());
        gameExtraParams.getPosition().add(sb.toString());
        sb.setLength(0);
    }
}
