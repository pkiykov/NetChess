package com.pkiykov.netchess.pojo;

import com.pkiykov.netchess.figures.Bishop;
import com.pkiykov.netchess.figures.Figure;
import com.pkiykov.netchess.figures.King;
import com.pkiykov.netchess.figures.Knight;
import com.pkiykov.netchess.figures.Pawn;
import com.pkiykov.netchess.figures.Queen;
import com.pkiykov.netchess.figures.Rook;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;

public class GameExtraParams implements Serializable {

    private int countMoves, countNoProgressMoves, reason;
    private boolean kingIsChecked, color;

    private LinkedList<Figure> figures;
    private LinkedList<String> position;

    private LinkedList<Integer> countNoProgressMovesList;
    private LinkedList<Figure> figureMoved;
    private LinkedList<Figure> removedFigureList;
    private LinkedList<Figure> addedFigureList;
    private LinkedList<Integer> moveWhenRemovedFiguresList;
    private LinkedList<Integer> moveWhenAddedFiguresList;
    private LinkedList<Integer> moveToList;
    private LinkedList<Integer> moveFromList;
    private LinkedList<Boolean> firstMoveFiguresList;
    private LinkedList<Boolean> kingIsCheckedList;
    private LinkedList<Integer> removedFiguresImageList;
    private LinkedList<Integer> movedFiguresImageList;

    public GameExtraParams(boolean cancelableMoves) {

        this.figures = new LinkedList<>();
        this.position = new LinkedList<>();

        kingIsChecked = false;
        color = true;

        if (cancelableMoves) {
            this.countNoProgressMovesList = new LinkedList<>();
            this.figureMoved = new LinkedList<>();
            this.removedFigureList = new LinkedList<>();
            this.addedFigureList = new LinkedList<>();
            this.moveWhenRemovedFiguresList = new LinkedList<>();
            this.moveWhenAddedFiguresList = new LinkedList<>();
            this.moveToList = new LinkedList<>();
            this.moveFromList = new LinkedList<>();
            this.firstMoveFiguresList = new LinkedList<>();
            this.kingIsCheckedList = new LinkedList<>();
            this.removedFiguresImageList = new LinkedList<>();
            this.movedFiguresImageList = new LinkedList<>();
        }
    }

    public void createFigures(GameExtraParams gameExtraParams, ArrayList<String> moveList) {
        figures.add(new Bishop(3, 1, true, gameExtraParams, moveList));
        figures.add(new Bishop(6, 1, true, gameExtraParams, moveList));
        figures.add(new Bishop(3, 8, false, gameExtraParams, moveList));
        figures.add(new Bishop(6, 8, false, gameExtraParams, moveList));
        figures.add(new Knight(2, 8, false, gameExtraParams, moveList));
        figures.add(new Knight(7, 8, false, gameExtraParams, moveList));
        figures.add(new Knight(2, 1, true, gameExtraParams, moveList));
        figures.add(new Knight(7, 1, true, gameExtraParams, moveList));
        figures.add(new Rook(1, 8, false, gameExtraParams, moveList));
        figures.add(new Rook(8, 8, false, gameExtraParams, moveList));
        figures.add(new Rook(8, 1, true, gameExtraParams, moveList));
        figures.add(new Rook(1, 1, true, gameExtraParams, moveList));
        figures.add(new King(5, 8, false, gameExtraParams, moveList));
        figures.add(new King(5, 1, true, gameExtraParams, moveList));
        figures.add(new Queen(4, 8, false, gameExtraParams, moveList));
        figures.add(new Queen(4, 1, true, gameExtraParams, moveList));
        figures.add(new Pawn(1, 7, false, gameExtraParams, moveList));
        figures.add(new Pawn(2, 7, false, gameExtraParams, moveList));
        figures.add(new Pawn(3, 7, false, gameExtraParams, moveList));
        figures.add(new Pawn(4, 7, false, gameExtraParams, moveList));
        figures.add(new Pawn(5, 7, false, gameExtraParams, moveList));
        figures.add(new Pawn(6, 7, false, gameExtraParams, moveList));
        figures.add(new Pawn(7, 7, false, gameExtraParams, moveList));
        figures.add(new Pawn(8, 7, false, gameExtraParams, moveList));
        figures.add(new Pawn(1, 2, true, gameExtraParams, moveList));
        figures.add(new Pawn(2, 2, true, gameExtraParams, moveList));
        figures.add(new Pawn(3, 2, true, gameExtraParams, moveList));
        figures.add(new Pawn(4, 2, true, gameExtraParams, moveList));
        figures.add(new Pawn(5, 2, true, gameExtraParams, moveList));
        figures.add(new Pawn(6, 2, true, gameExtraParams, moveList));
        figures.add(new Pawn(7, 2, true, gameExtraParams, moveList));
        figures.add(new Pawn(8, 2, true, gameExtraParams, moveList));
    }

    public boolean isKingIsChecked() {
        return kingIsChecked;
    }

    public void setKingIsChecked(boolean kingIsChecked) {
        this.kingIsChecked = kingIsChecked;
    }

    public LinkedList<Figure> getFigures() {
        return figures;
    }

    public void setFigures(LinkedList<Figure> figures) {
        this.figures = figures;
    }

    public LinkedList<String> getPosition() {
        return position;
    }

    public int getCountMoves() {
        return countMoves;
    }

    public void setCountMoves(int countMoves) {
        this.countMoves = countMoves;
    }

    public int getCountNoProgressMoves() {
        return countNoProgressMoves;
    }

    public void setCountNoProgressMoves(int countNoProgressMoves) {
        this.countNoProgressMoves = countNoProgressMoves;
    }

    public int getReason() {
        return reason;
    }

    public void setReason(int reason) {
        this.reason = reason;
    }

    public boolean isColor() {
        return color;
    }

    public void setColor(boolean color) {
        this.color = color;
    }

    public LinkedList<Figure> getFigureMoved() {
        return figureMoved;
    }

    public LinkedList<Integer> getCountNoProgressMovesList() {
        return countNoProgressMovesList;
    }

    public LinkedList<Figure> getRemovedFigureList() {
        return removedFigureList;
    }

    public LinkedList<Figure> getAddedFigureList() {
        return addedFigureList;
    }

    public LinkedList<Integer> getMoveWhenRemovedFiguresList() {
        return moveWhenRemovedFiguresList;
    }

    public LinkedList<Integer> getMoveWhenAddedFiguresList() {
        return moveWhenAddedFiguresList;
    }

    public LinkedList<Integer> getMoveToList() {
        return moveToList;
    }

    public LinkedList<Integer> getMoveFromList() {
        return moveFromList;
    }

    public LinkedList<Boolean> getFirstMoveFiguresList() {
        return firstMoveFiguresList;
    }

    public LinkedList<Boolean> getKingIsCheckedList() {
        return kingIsCheckedList;
    }

    public LinkedList<Integer> getRemovedFiguresImageList() {
        return removedFiguresImageList;
    }

    public LinkedList<Integer> getMovedFiguresImageList() {
        return movedFiguresImageList;
    }

}
