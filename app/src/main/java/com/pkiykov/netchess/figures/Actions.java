package com.pkiykov.netchess.figures;

interface Actions {

    boolean move(int a1, int b1, boolean recusion);

    boolean checkIfLocationIsFree(int a1, int b1);

    boolean moveIsLegalIfKingIsChecked(boolean color, int a1, int b1);

    boolean kingIsNotChecked(boolean sideToMove);

    void overwriteCoordinates(int a1, int b1);

    boolean checkIfLocationIsOccupied(int a1, int b1, boolean sideToMove);
}
