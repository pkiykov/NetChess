package com.pkiykov.netchess.figures;

interface Actions {

    boolean move(int newX, int newY, boolean recursion);

    boolean checkIfLocationIsFree(int newX, int newY);

    boolean moveIsLegalIfKingIsChecked(boolean color, int newX, int newY);

    boolean kingIsNotChecked(boolean sideToMove);

    void overwriteCoordinates(int newX, int newY);

    boolean checkIfLocationIsOccupied(int newX, int newY, boolean sideToMove);
}
