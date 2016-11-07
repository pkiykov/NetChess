package com.pkiykov.netchess.logic;

import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.pkiykov.netchess.R;
import com.pkiykov.netchess.figures.Bishop;
import com.pkiykov.netchess.figures.Figure;
import com.pkiykov.netchess.figures.King;
import com.pkiykov.netchess.figures.Knight;
import com.pkiykov.netchess.figures.Pawn;
import com.pkiykov.netchess.figures.Queen;
import com.pkiykov.netchess.figures.Rook;
import com.pkiykov.netchess.fragments.Game;
import com.pkiykov.netchess.pojo.RunningGame;

import static com.pkiykov.netchess.figures.Figure.BISHOP;
import static com.pkiykov.netchess.figures.Figure.KNIGHT;
import static com.pkiykov.netchess.figures.Figure.ROOK;
import static com.pkiykov.netchess.fragments.Game.LAN_GAME;
import static com.pkiykov.netchess.fragments.Game.ONE_DEVICE_GAME;
import static com.pkiykov.netchess.fragments.Game.ONLINE_GAME;
import static com.pkiykov.netchess.logic.GameTime.resetTimeRest;
import static com.pkiykov.netchess.logic.GameTime.stopTimers;
import static com.pkiykov.netchess.pojo.Coordinates.BISHOP_ID;
import static com.pkiykov.netchess.pojo.Coordinates.COORDINATES;
import static com.pkiykov.netchess.pojo.Coordinates.KNIGHT_ID;
import static com.pkiykov.netchess.pojo.Coordinates.QUEEN_ID;
import static com.pkiykov.netchess.pojo.Coordinates.ROOK_ID;

public class GameMove {


    private Game game;
    private boolean moveOk;
    private AlertDialog pawnToQueenDialog;

    public GameMove(Game game) {
        moveOk = false;
        this.game = game;
    }

    void cancelLastMove() {
        game.getGameExtraParams().setColor(!game.getGameExtraParams().isColor());
        game.getGameExtraParams().setCountMoves(game.getGameExtraParams().getCountMoves() - 1);
        for (int i = 0; i < game.getHighlight().length; i++) {
            for (int j = 0; j < game.getHighlight()[i].length; j++) {
                game.getHighlight()[i][j].setImageDrawable(null);
                game.getSelect()[i][j].setImageDrawable(null);
            }
        }

        if (game.getGameExtraParams().getMoveWhenAddedFiguresList().size() > 0) {
            if (game.getGameExtraParams().getCountMoves() == game.getGameExtraParams()
                    .getMoveWhenAddedFiguresList().getLast()) {
                int oldX = game.getGameExtraParams().getAddedFigureList().getLast().getOldX();
                int oldY = game.getGameExtraParams().getAddedFigureList().getLast().getOldY();
                game.getGameExtraParams().getFigures().remove(game.getGameExtraParams()
                        .getAddedFigureList().getLast());
                game.getPiece()[8 - oldY][oldX - 1].setImageDrawable(null);
                game.getPiece()[8 - oldY][oldX - 1].setTag(null);

                game.getGameExtraParams().getMoveWhenAddedFiguresList().removeLast();
                game.getGameExtraParams().getAddedFigureList().removeLast();

                int oldXprevious = game.getGameExtraParams().getRemovedFigureList().getLast().getOldX();
                int oldYprevious = game.getGameExtraParams().getRemovedFigureList().getLast().getOldY();
                game.getGameExtraParams().getFigures().add(game.getGameExtraParams().getRemovedFigureList().getLast());
                game.getPiece()[8 - oldYprevious][oldXprevious - 1].setImageResource(game.getGameExtraParams().getRemovedFiguresImageList().getLast());
                game.getPiece()[8 - oldYprevious][oldXprevious - 1].setTag(game.getGameExtraParams().getRemovedFiguresImageList().getLast());


                game.getGameExtraParams().getMoveWhenRemovedFiguresList().removeLast();
                game.getGameExtraParams().getRemovedFiguresImageList().removeLast();
                game.getGameExtraParams().getRemovedFigureList().removeLast();
                if (game.getGameExtraParams().getMoveWhenRemovedFiguresList().size() > 0) {
                    if (game.getGameExtraParams().getMoveWhenRemovedFiguresList().getLast()
                            == game.getGameExtraParams().getCountMoves()) {
                        game.getGameExtraParams().getFigures()
                                .add(game.getGameExtraParams().getRemovedFigureList().getLast());
                        game.getPiece()[8 - oldY][oldX - 1].setImageResource(game.getGameExtraParams().getRemovedFiguresImageList().getLast());
                        game.getPiece()[8 - oldY][oldX - 1].setTag(game.getGameExtraParams().getRemovedFiguresImageList().getLast());
                        game.getGameExtraParams().getMoveWhenRemovedFiguresList().removeLast();
                        game.getGameExtraParams().getRemovedFiguresImageList().removeLast();
                        game.getGameExtraParams().getRemovedFigureList().removeLast();
                    }
                }
            }
        }
        if (game.getGameExtraParams().getMoveWhenRemovedFiguresList().size() > 0) {
            if (game.getGameExtraParams().getCountMoves() == game.getGameExtraParams().getMoveWhenRemovedFiguresList().getLast()) {

                int oldX = game.getGameExtraParams().getRemovedFigureList().getLast().getOldX();
                int oldY = game.getGameExtraParams().getRemovedFigureList().getLast().getOldY();
                game.getGameExtraParams().getFigures().add(game.getGameExtraParams().getRemovedFigureList().getLast());

                int lastImage = game.getGameExtraParams().getRemovedFiguresImageList().getLast();

                game.getPiece()[8 - oldY][oldX - 1].setImageResource(lastImage);
                game.getPiece()[8 - oldY][oldX - 1].setTag(lastImage);
                game.getGameExtraParams().getMoveWhenRemovedFiguresList().removeLast();
                game.getGameExtraParams().getRemovedFigureList().removeLast();
                game.getGameExtraParams().getRemovedFiguresImageList().removeLast();
            }
        }
        int k = 0;
        do {
            k++;

            int from = game.getGameExtraParams().getMoveFromList().getLast();
            int to = game.getGameExtraParams().getMoveToList().getLast();

            game.getRunningGame().getCoordinates().setOldX(from % 8 + 1);
            game.getRunningGame().getCoordinates().setOldY(8 - from / 8);

            game.getRunningGame().getCoordinates().setNewX(to % 8 + 1);
            game.getRunningGame().getCoordinates().setNewY(8 - to / 8);

            game.getGameExtraParams().getFigureMoved().getLast().setOldX(game.getRunningGame().getCoordinates().getOldX());
            game.getGameExtraParams().getFigureMoved().getLast().setOldY(game.getRunningGame().getCoordinates().getOldY());
            game.getGameExtraParams().getFigureMoved().getLast().setFirstMove(game.getGameExtraParams().getFirstMoveFiguresList().getLast());

            game.getPiece()[8 - game.getRunningGame().getCoordinates().getOldY()][game.getRunningGame().getCoordinates().getOldX() - 1].setImageResource(game.getGameExtraParams().getMovedFiguresImageList().getLast());
            game.getPiece()[8 - game.getRunningGame().getCoordinates().getOldY()][game.getRunningGame().getCoordinates().getOldX() - 1].setTag(game.getGameExtraParams().getMovedFiguresImageList().getLast());

            if (game.getPiece()[8 - game.getRunningGame().getCoordinates().getNewY()][game.getRunningGame().getCoordinates().getNewX() - 1].getTag() != null) {
                String s1 = game.getPiece()[8 - game.getRunningGame().getCoordinates().getNewY()][game.getRunningGame().getCoordinates().getNewX() - 1].getTag().toString();
                String s2 = String.valueOf(game.getGameExtraParams().getMovedFiguresImageList().getLast());
                if (s1.equals(s2)) {
                    game.getPiece()[8 - game.getRunningGame().getCoordinates().getNewY()]
                            [game.getRunningGame().getCoordinates().getNewX() - 1].setImageDrawable(null);
                    game.getPiece()[8 - game.getRunningGame().getCoordinates().getNewY()]
                            [game.getRunningGame().getCoordinates().getNewX() - 1].setTag(null);
                }
            }
            game.getGameExtraParams().getMovedFiguresImageList().removeLast();
            game.getGameExtraParams().getFigureMoved().removeLast();
            game.getGameExtraParams().getMoveFromList().removeLast();
            game.getGameExtraParams().getMoveToList().removeLast();
            game.getGameExtraParams().getFirstMoveFiguresList().removeLast();
            if (game.getGameExtraParams().getFigureMoved().size() > 0) {
                if (!(game.getGameExtraParams().getFigureMoved().getLast() instanceof King)
                        || Math.abs(game.getGameExtraParams().getMoveFromList().getLast() - game.getGameExtraParams().getMoveToList().getLast()) != 2) {
                    k++;
                }
            } else {
                k++;
            }

        } while (k < 2);

        game.getRunningGame().getMoveList().remove(game.getRunningGame().getMoveList().size() - 1);
        game.getGameExtraParams().getPosition().removeLast();

        game.getGameExtraParams().getKingIsCheckedList().removeLast();
        game.getGameExtraParams().getCountNoProgressMovesList().removeLast();
        game.getGameExtraParams().setCountNoProgressMoves(game.getGameExtraParams().getCountNoProgressMovesList().getLast());
        if (game.getGameExtraParams().getKingIsCheckedList().getLast()) {
            if (game.getGameExtraParams().isColor()) {
                game.getKingIsCheckedText1().setVisibility(View.VISIBLE);
            } else {
                game.getKingIsCheckedText2().setVisibility(View.VISIBLE);
            }
        } else {
            game.getKingIsCheckedText1().setVisibility(View.INVISIBLE);
            game.getKingIsCheckedText2().setVisibility(View.INVISIBLE);
        }

        if (game.getGameExtraParams().getMoveFromList().size() > 0) {
            int from = game.getGameExtraParams().getMoveFromList().getLast();
            int to = game.getGameExtraParams().getMoveToList().getLast();

            game.getSelect()[from / 8][from % 8].setImageResource(R.drawable.red);
            game.getSelect()[to / 8][to % 8].setImageResource(R.drawable.red);
        }
        stopTimers(game.getCountDownTimer1(), game.getCountDownTimer2());
        game.getGameTime().setTimeRest();
        game.getGameTime().changeTimers(game.getGameExtraParams().isColor());
    }


    void tryToMove(View v) {
        int to = Integer.parseInt(v.getTag().toString());
        game.getRunningGame().getCoordinates().setNewX(to % 8 + 1);
        game.getRunningGame().getCoordinates().setNewY(8 - to / 8);
        boolean flag = true;
        if (game.getPressed() != null) {
            if (game.getGameType() == ONLINE_GAME) {
                game.getGameDatabase().getReceivedFromOpponent().setMoveFromOpponent(false);
            } else if (game.getGameType() == LAN_GAME) {
                game.getPeer2Peer().getReceivedFromOpponent().setMoveFromOpponent(false);
            }
            int from = Integer.parseInt(game.getPressed().getTag().toString());
            game.getRunningGame().getCoordinates().setOldX(from % 8 + 1);
            game.getRunningGame().getCoordinates().setOldY(8 - from / 8);
            int n = findFigureByCoordinates();
            if (n != -1) {
                flag = moveFull(n, from, to, true);
            }
            if (flag) {
                if (!moveOk) {
                    if (game.getRunningGame().getMoveList().size() > 0) {
                        int oldXfromLastMove = game.getRunningGame().getMoveList().get(game.getRunningGame().getMoveList().size() - 1).charAt(1) - 96;
                        int newXfromLastMove = game.getRunningGame().getMoveList().get(game.getRunningGame().getMoveList().size() - 1).charAt(3) - 96;
                        int oldYfromLastMove = Character.getNumericValue(game.getRunningGame().getMoveList().get(game.getRunningGame().getMoveList().size() - 1).charAt(2));
                        int newYfromLastMove = Character.getNumericValue(game.getRunningGame().getMoveList().get(game.getRunningGame().getMoveList().size() - 1).charAt(4));

                        for (int ii = 1; ii < 9; ii++) {
                            for (int jj = 1; jj < 9; jj++) {
                                if ((oldXfromLastMove == ii && oldYfromLastMove == jj) || (newXfromLastMove == ii && newYfromLastMove == jj)) {
                                    game.getSelect()[8 - jj][ii - 1].setImageResource(R.drawable.red);
                                    continue;
                                }
                                game.getSelect()[8 - jj][ii - 1].setImageDrawable(null);
                            }
                        }
                    } else {
                        clearSelect(game.getSelect());
                    }

                }
            }
        }
        if (game.getRunningGame() != null) {
            fieldSelection(v);
        }
    }

    private void fieldSelection(View v) {
        if (canMove()) {
            ((ImageView) ((FrameLayout) v).getChildAt(1)).setImageResource(R.drawable.reeed);
        }
        moveOk = false;
        game.setPressed(((FrameLayout) v));
    }

    private static void clearSelect(ImageView[][] view) {
        for (ImageView[] aSelect : view) {
            for (ImageView anASelect : aSelect) {
                anASelect.setImageDrawable(null);
            }
        }
    }

    private boolean canMove() {
        // if (game.isHighlightMoves()) {
        clearSelect(game.getHighlight());
        // }
        boolean flag = false;
        for (int k = 0; k < game.getGameExtraParams().getFigures().size(); k++) {
            Figure f = game.getGameExtraParams().getFigures().get(k);
            if (f.getOldX() == game.getRunningGame().getCoordinates().getNewX() && f.getOldY()
                    == game.getRunningGame().getCoordinates().getNewY()) {
                if (f.isColor() == game.getGameExtraParams().isColor())
                    for (int i = 1; i <= game.getImages().length; i++) {
                        for (int j = 1; j <= game.getImages().length; j++) {
                            game.getRunningGame().getCoordinates().setNewX(i);
                            game.getRunningGame().getCoordinates().setNewY(j);
                            if (f.move(game.getRunningGame().getCoordinates().getNewX(), game.getRunningGame().getCoordinates().getNewY(), true)) {
                                flag = true;
                                game.getHighlight()[8 - j][i - 1].setImageResource(R.drawable.highlight_moves);
                            }
                        }
                    }
                else {
                    return flag;
                }
            }
        }
        return flag;
    }

    private boolean moveFull(int n, int from, int to, boolean flag) {
        Figure f = game.getGameExtraParams().getFigures().get(n);
        if (f.move(game.getRunningGame().getCoordinates().getNewX(), game.getRunningGame().getCoordinates().getNewY(), true)) {
            boolean firstMove = f.isFirstMove();
            if (game.getRunningGame().getPlayer1().getPlayerGameParams().isCancelableMoves() || game.getRunningGame().getPlayer2().getPlayerGameParams().isCancelableMoves()) {
                game.getGameExtraParams().getFigureMoved().add(f);
                game.getGameExtraParams().getMoveFromList().add(from);
                game.getGameExtraParams().getMoveToList().add(to);
                game.getGameExtraParams().getFirstMoveFiguresList().add(firstMove);
                game.getGameExtraParams().getMovedFiguresImageList().add
                        (Integer.parseInt(game.getPiece()[8 - game.getRunningGame().getCoordinates().getOldY()]
                                [game.getRunningGame().getCoordinates().getOldX() - 1].getTag().toString()));
            }
            coordinatesUpdated(f);
            flag = false;
        }
        return flag;
    }


    private void moveContinue(Figure f) {
        game.getPiece()[8 - game.getRunningGame().getCoordinates().getOldY()][game.getRunningGame().getCoordinates().getOldX() - 1].setImageDrawable(null);
        game.getPiece()[8 - game.getRunningGame().getCoordinates().getOldY()][game.getRunningGame().getCoordinates().getOldX() - 1].setTag(null);
        game.getGameExtraParams().setColor(!game.getGameExtraParams().isColor());
        f.writePosition();
        game.getRunningGame().writeMove(f);
        game.getGameExtraParams().setCountMoves(game.getGameExtraParams().getCountMoves() + 1);
        moveOk = true;
        game.getGameGo().setDrawOfferWasSent(false);
        if (!game.getGameEnd().checkEndGame()) {
            setUpFieldChanges();
            switchPlayersToMove();
        } else {
            setUpFieldChanges();
            switchPlayersToMove();
            game.getGameEnd().recordResult();
        }
    }

    private void setUpFieldChanges() {
        game.getGameGo().changeKingIsCheckedTextView();
        clearSelect(game.getSelect());
        game.getSelect()[8 - game.getRunningGame().getCoordinates().getOldY()][game.getRunningGame().getCoordinates().getOldX() - 1].setImageResource(R.drawable.red);
        game.getSelect()[8 - game.getRunningGame().getCoordinates().getNewY()][game.getRunningGame().getCoordinates().getNewX() - 1].setImageResource(R.drawable.red);
        clearSelect(game.getHighlight());
    }

    private void switchPlayersToMove() {
        switch (game.getGameType()) {
            case ONLINE_GAME:
                game.getGameGo().playerInactive();
                if (!game.getGameDatabase().getReceivedFromOpponent().isMoveFromOpponent()) {
                    resetTimeRest(game.getRunningGame().isThisPlayerPlaysWhite(), game.getGameDatabase().getGameRef());
                    stopTimers(game.getCountDownTimer1(), game.getCountDownTimer2());
                    game.getGameTime().setTimeFromTimer();
                    game.getGameTime().setOnlineTimeRest();
                    game.getGameDatabase().getGameRef().child(COORDINATES).setValue(game.getRunningGame().getCoordinates());
                    game.getGameDatabase().getGameRef().child(RunningGame.MOVE_LIST).setValue(game.getRunningGame().getMoveList());
                }
                break;
            case LAN_GAME:
                game.getGameGo().playerInactive();
                if (!game.getPeer2Peer().getReceivedFromOpponent().isMoveFromOpponent()) {
                    stopTimers(game.getCountDownTimer1(), game.getCountDownTimer2());
                    game.getGameTime().setTimeFromTimer();
                    game.getGameTime().setPeer2PeerTimeRest();
                    game.getPeer2Peer().sendAction(COORDINATES, game.getRunningGame().getCoordinates());
                }
                break;
            default:
                if (game.getRunningGame().getPlayer1().getPlayerGameParams().isCancelableMoves() || game.getRunningGame().getPlayer2().getPlayerGameParams().isCancelableMoves()) {
                    game.getGameGo().recordListsForCancelableMoves();
                }
                stopTimers(game.getCountDownTimer1(), game.getCountDownTimer2());
                game.getGameTime().setTimeRest();
                game.getGameTime().changeTimers(game.getGameExtraParams().isColor());
                break;
        }
    }

    int findFigureByCoordinates() {
        for (int n = 0; n < game.getGameExtraParams().getFigures().size(); n++) {
            if (game.getGameExtraParams().getFigures().get(n).getOldX() == game.getRunningGame().getCoordinates().getOldX()
                    && game.getGameExtraParams().getFigures().get(n).getOldY() == game.getRunningGame().getCoordinates().getOldY()
                    && game.getGameExtraParams().getFigures().get(n).isColor() == game.getGameExtraParams().isColor()) {
                return n;
            }
        }
        return -1;
    }

    private void changeFigureImage() {
        if (game.getPiece()[8 - game.getRunningGame().getCoordinates().getOldY()]
                [game.getRunningGame().getCoordinates().getOldX() - 1].getTag() != null) {
            game.getPiece()[8 - game.getRunningGame().getCoordinates().getNewY()]
                    [game.getRunningGame().getCoordinates().getNewX() - 1].setImageResource
                    (Integer.parseInt(game.getPiece()[8 - game.getRunningGame().getCoordinates().getOldY()]
                            [game.getRunningGame().getCoordinates().getOldX() - 1].getTag().toString()));
            game.getPiece()[8 - game.getRunningGame().getCoordinates().getNewY()]
                    [game.getRunningGame().getCoordinates().getNewX() - 1].setTag
                    (Integer.parseInt(game.getPiece()[8 - game.getRunningGame().getCoordinates().getOldY()]
                            [game.getRunningGame().getCoordinates().getOldX() - 1].getTag().toString()));
        }
    }

    private void overwriteAndRemove(Figure f) {
        game.getGameExtraParams().setCountNoProgressMoves(game.getGameExtraParams().getCountNoProgressMoves() + 1);
        f.setFirstMove(false);
        if (f instanceof King && Math.abs(game.getRunningGame().getCoordinates().getOldX()
                - game.getRunningGame().getCoordinates().getNewX()) == 2) {
            overwriteCastle();
        } else if (game.getRunningGame().getMoveList().size() > 0 && f instanceof Pawn) {
            overwriteEnPassantCapture();
        } else {
            for (Figure fff : game.getGameExtraParams().getFigures()) {
                if (fff.getOldX() == game.getRunningGame().getCoordinates().getNewX() && fff.getOldY() == game.getRunningGame().getCoordinates().getNewY()) {
                    if (game.getRunningGame().getPlayer1().getPlayerGameParams().isCancelableMoves()
                            || game.getRunningGame().getPlayer2().getPlayerGameParams().isCancelableMoves()) {
                        game.getGameExtraParams().getMoveWhenRemovedFiguresList().add(game.getGameExtraParams().getCountMoves());
                        game.getGameExtraParams().getRemovedFigureList().add(fff);
                        game.getGameExtraParams().getRemovedFiguresImageList()
                                .add(Integer.parseInt(game.getPiece()[8 - game.getRunningGame().getCoordinates().getNewY()]
                                        [game.getRunningGame().getCoordinates().getNewX() - 1].getTag().toString()));
                    }
                    game.getGameExtraParams().getFigures().remove(fff);
                    game.getGameExtraParams().setCountNoProgressMoves(0);
                    break;
                }
            }
        }
    }

    private boolean isPawnToQueen(Figure f) {
        if (f instanceof Pawn && (game.getRunningGame().getCoordinates().getNewY() == 8
                || game.getRunningGame().getCoordinates().getNewY() == 1)) {
            if (game.getRunningGame().getPlayer1().getPlayerGameParams().isCancelableMoves() || game.getRunningGame().getPlayer2().getPlayerGameParams().isCancelableMoves()) {
                game.getGameExtraParams().getMoveWhenRemovedFiguresList().add(game.getGameExtraParams().getCountMoves());
                game.getGameExtraParams().getRemovedFigureList().add(f);
                if (game.getPiece()[8 - game.getRunningGame().getCoordinates().getNewY()][game.getRunningGame().getCoordinates().getNewX() - 1].getTag() != null) {
                    game.getGameExtraParams().getRemovedFiguresImageList().add(Integer.parseInt(game.getPiece()[8 - game.getRunningGame().getCoordinates().getNewY()]
                            [game.getRunningGame().getCoordinates().getNewX() - 1].getTag().toString()));
                }
            }
            game.getGameExtraParams().getFigures().remove(f);
            game.getPiece()[8 - game.getRunningGame().getCoordinates().getOldY()][game.getRunningGame().getCoordinates().getOldX() - 1].setImageDrawable(null);
            game.getPiece()[8 - game.getRunningGame().getCoordinates().getOldY()][game.getRunningGame().getCoordinates().getOldX() - 1].setTag(null);
            return true;
        }
        return false;
    }

    private void overwriteCastle() {
        if (game.getRunningGame().getCoordinates().getNewX() == 7) {
            for (Figure f : game.getGameExtraParams().getFigures()) {
                if (f.getOldX() == 8 && f instanceof Rook && f.isColor() == game.getGameExtraParams().isColor()) {
                    if (game.getRunningGame().getPlayer1().getPlayerGameParams().isCancelableMoves()
                            || game.getRunningGame().getPlayer2().getPlayerGameParams().isCancelableMoves()) {
                        game.getGameExtraParams().getFirstMoveFiguresList().add(f.isFirstMove());
                        game.getGameExtraParams().getMoveFromList().add((8 - f.getOldY()) * 8 + f.getOldX() - 1);
                        game.getGameExtraParams().getMovedFiguresImageList().add(Integer.parseInt(game.getPiece()[8 - f.getOldY()][f.getOldX() - 1].getTag().toString()));
                    }

                    game.getPiece()[8 - f.getOldY()][f.getOldX() - 1].setImageDrawable(null);
                    game.getPiece()[8 - f.getOldY()][f.getOldX() - 1].setTag(null);
                    f.setOldX(6);
                    f.setFirstMove(false);

                    if (game.getGameExtraParams().isColor()) {
                        game.getPiece()[8 - f.getOldY()][5].setImageResource(R.drawable.rook_white);
                        game.getPiece()[8 - f.getOldY()][5].setTag(R.drawable.rook_white);

                    } else {
                        game.getPiece()[8 - f.getOldY()][5].setImageResource(R.drawable.rook_black);
                        game.getPiece()[8 - f.getOldY()][5].setTag(R.drawable.rook_black);
                    }
                    if (game.getRunningGame().getPlayer1().getPlayerGameParams().isCancelableMoves()
                            || game.getRunningGame().getPlayer2().getPlayerGameParams().isCancelableMoves()) {
                        game.getGameExtraParams().getFigureMoved().add(f);
                        game.getGameExtraParams().getMoveToList().add((8 - f.getOldY()) * 8 + f.getOldX() - 1);
                    }

                    break;
                }
            }
        } else {
            for (Figure f : game.getGameExtraParams().getFigures()) {
                if (f.getOldX() == 1 && f instanceof Rook && f.isColor() == game.getGameExtraParams().isColor()) {
                    if (game.getRunningGame().getPlayer1().getPlayerGameParams().isCancelableMoves()
                            || game.getRunningGame().getPlayer2().getPlayerGameParams().isCancelableMoves()) {
                        game.getGameExtraParams().getFirstMoveFiguresList().add(f.isFirstMove());
                        game.getGameExtraParams().getMoveFromList().add((8 - f.getOldY()) * 8 + f.getOldX() - 1);
                        game.getGameExtraParams().getMovedFiguresImageList()
                                .add(Integer.parseInt(game.getPiece()[8 - f.getOldY()][f.getOldX() - 1].getTag().toString()));
                    }
                    game.getPiece()[8 - f.getOldY()][f.getOldX() - 1].setImageDrawable(null);
                    game.getPiece()[8 - f.getOldY()][f.getOldX() - 1].setTag(null);
                    f.setOldX(4);
                    f.setFirstMove(false);
                    if (game.getGameExtraParams().isColor()) {
                        game.getPiece()[8 - f.getOldY()][3].setImageResource(R.drawable.rook_white);
                        game.getPiece()[8 - f.getOldY()][3].setTag(R.drawable.rook_white);
                    } else {
                        game.getPiece()[8 - f.getOldY()][3].setImageResource(R.drawable.rook_black);
                        game.getPiece()[8 - f.getOldY()][3].setTag(R.drawable.rook_black);
                    }
                    if (game.getRunningGame().getPlayer1().getPlayerGameParams().isCancelableMoves()
                            || game.getRunningGame().getPlayer2().getPlayerGameParams().isCancelableMoves()) {
                        game.getGameExtraParams().getFigureMoved().add(f);
                        game.getGameExtraParams().getMoveToList().add((8 - f.getOldY()) * 8 + f.getOldX() - 1);
                    }
                    break;
                }
            }
        }
    }

    private void overwriteEnPassantCapture() {
        String lastMove = game.getRunningGame().getMoveList().get(game.getRunningGame().getMoveList().size() - 1);
        int newXfromLastMove = (lastMove.charAt(3) - 96);
        int newYfromLastMove = Character.getNumericValue(lastMove.charAt(4));
        int oldYfromLastMove = Character.getNumericValue(lastMove.charAt(2));
        if (lastMove.contains("P") && game.getRunningGame().getCoordinates().getNewX() == newXfromLastMove
                && Math.abs(oldYfromLastMove - game.getRunningGame().getCoordinates().getNewY())
                == Math.abs(newYfromLastMove - game.getRunningGame().getCoordinates().getNewY())) {
            for (Figure fff : game.getGameExtraParams().getFigures()) {
                if (fff.getOldX() == newXfromLastMove && fff.getOldY() == newYfromLastMove) {
                    if (game.getRunningGame().getPlayer1().getPlayerGameParams().isCancelableMoves()
                            || game.getRunningGame().getPlayer2().getPlayerGameParams().isCancelableMoves()) {
                        game.getGameExtraParams().getMoveWhenRemovedFiguresList().add(game.getGameExtraParams().getCountMoves());
                        game.getGameExtraParams().getRemovedFigureList().add(fff);
                        int add = Integer.parseInt(game.getPiece()[8 - newYfromLastMove][newXfromLastMove - 1].getTag().toString());
                        game.getGameExtraParams().getRemovedFiguresImageList().add(add);
                    }
                    game.getGameExtraParams().getFigures().remove(fff);
                    game.getPiece()[8 - newYfromLastMove][newXfromLastMove - 1].setImageDrawable(null);
                    game.getPiece()[8 - newYfromLastMove][newXfromLastMove - 1].setTag(null);

                    break;
                }
            }
        } else {
            for (Figure fff : game.getGameExtraParams().getFigures()) {
                if (fff.getOldX() == game.getRunningGame().getCoordinates().getNewX() && fff.getOldY() == game.getRunningGame().getCoordinates().getNewY()) {
                    if (game.getRunningGame().getPlayer1().getPlayerGameParams().isCancelableMoves()
                            || game.getRunningGame().getPlayer2().getPlayerGameParams().isCancelableMoves()) {
                        game.getGameExtraParams().getMoveWhenRemovedFiguresList().add(game.getGameExtraParams().getCountMoves());
                        game.getGameExtraParams().getRemovedFigureList().add(fff);
                        int add = Integer.parseInt(game.getPiece()[8 - game.getRunningGame().getCoordinates().getNewY()]
                                [game.getRunningGame().getCoordinates().getNewX() - 1].getTag().toString());
                        game.getGameExtraParams().getRemovedFiguresImageList().add(add);
                    }
                    game.getGameExtraParams().getFigures().remove(fff);
                    break;
                }
            }
        }
        game.getGameExtraParams().setCountNoProgressMoves(0);
    }

    void coordinatesUpdated(Figure f) {
        game.getKingIsCheckedText1().setVisibility(View.INVISIBLE);
        game.getKingIsCheckedText2().setVisibility(View.INVISIBLE);
        overwriteAndRemove(f);
        if (isPawnToQueen(f)) {
            if (game.getGameType() == ONE_DEVICE_GAME
                    || (game.getGameType() == ONLINE_GAME && !game.getGameDatabase().getReceivedFromOpponent().isMoveFromOpponent())
                    || (game.getGameType() == LAN_GAME && !game.getPeer2Peer().getReceivedFromOpponent().isMoveFromOpponent())) {
                pawnToQueenDialog();
            } else {
                int figureType = game.getRunningGame().getCoordinates().getFigureType();
                Figure newFigure = createFigure(figureType);
                int figureId = getFigureId(newFigure);
                pawnToQueenExtra(newFigure, figureId);
            }
        } else {
            f.overwriteCoordinates(game.getRunningGame().getCoordinates().getNewX(), game.getRunningGame().getCoordinates().getNewY());
            changeFigureImage();
            moveContinue(f);
        }
    }

    private Figure createFigure(int figureType) {
        Figure newFigure;
        switch (figureType) {
            case ROOK_ID:
                newFigure = new Rook(game.getRunningGame().getCoordinates().getNewX(), game.getRunningGame().getCoordinates().getNewY()
                        , game.getGameExtraParams().isColor(), game.getGameExtraParams(), game.getRunningGame().getMoveList());
                break;
            case BISHOP_ID:
                newFigure = new Bishop(game.getRunningGame().getCoordinates().getNewX(), game.getRunningGame().getCoordinates().getNewY()
                        , game.getGameExtraParams().isColor(), game.getGameExtraParams(), game.getRunningGame().getMoveList());
                break;
            case KNIGHT_ID:
                newFigure = new Knight(game.getRunningGame().getCoordinates().getNewX(), game.getRunningGame().getCoordinates().getNewY()
                        , game.getGameExtraParams().isColor(), game.getGameExtraParams(), game.getRunningGame().getMoveList());
                break;
            default:
                newFigure = new Queen(game.getRunningGame().getCoordinates().getNewX(), game.getRunningGame().getCoordinates().getNewY()
                        , game.getGameExtraParams().isColor(), game.getGameExtraParams(), game.getRunningGame().getMoveList());
                break;
        }
        return newFigure;
    }


    void setMoveFromOpponent(boolean moveFromOpponent) {
        if (game.getGameType() == ONLINE_GAME) {
            game.getGameDatabase().getReceivedFromOpponent().setMoveFromOpponent(moveFromOpponent);
        } else {
            game.getPeer2Peer().getReceivedFromOpponent().setMoveFromOpponent(moveFromOpponent);
        }
    }

    private void pawnToQueenDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(game.getActivity());
        View layout = game.getActivity().getLayoutInflater().inflate(R.layout.dialog_pawn_to_queen, null);
        builder.setView(layout);
        builder.setCancelable(false);
        ImageButton queen = (ImageButton) layout.findViewById(R.id.queen);
        ImageButton rook = (ImageButton) layout.findViewById(R.id.rook);
        ImageButton bishop = (ImageButton) layout.findViewById(R.id.bishop);
        ImageButton knight = (ImageButton) layout.findViewById(R.id.knight);
        if (game.getGameExtraParams().isColor()) {
            queen.setImageResource(R.drawable.queen_white);
            rook.setImageResource(R.drawable.rook_white);
            bishop.setImageResource(R.drawable.bishop_white);
            knight.setImageResource(R.drawable.knight_white);
        } else {
            queen.setImageResource(R.drawable.queen_black);
            rook.setImageResource(R.drawable.rook_black);
            bishop.setImageResource(R.drawable.bishop_black);
            knight.setImageResource(R.drawable.knight_black);
        }
        queen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Figure f = new Queen(game.getRunningGame().getCoordinates().getNewX(), game.getRunningGame().getCoordinates().getNewY(),
                        game.getGameExtraParams().isColor(), game.getGameExtraParams(), game.getRunningGame().getMoveList());
                int figureId = getFigureId(f);
                game.getRunningGame().getCoordinates().setFigureType(QUEEN_ID);
                pawnToQueenExtra(f, figureId);

            }
        });
        rook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Figure f = new Rook(game.getRunningGame().getCoordinates().getNewX(), game.getRunningGame().getCoordinates().getNewY(),
                        game.getGameExtraParams().isColor(), game.getGameExtraParams(), game.getRunningGame().getMoveList());
                int figureId = getFigureId(f);
                game.getRunningGame().getCoordinates().setFigureType(ROOK_ID);
                pawnToQueenExtra(f, figureId);
            }
        });
        bishop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Figure f = new Bishop(game.getRunningGame().getCoordinates().getNewX(), game.getRunningGame().getCoordinates().getNewY(),
                        game.getGameExtraParams().isColor(), game.getGameExtraParams(), game.getRunningGame().getMoveList());
                int figureId = getFigureId(f);
                game.getRunningGame().getCoordinates().setFigureType(BISHOP_ID);
                pawnToQueenExtra(f, figureId);
            }
        });
        knight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Figure f = new Knight(game.getRunningGame().getCoordinates().getNewX(), game.getRunningGame().getCoordinates().getNewY(),
                        game.getGameExtraParams().isColor(), game.getGameExtraParams(), game.getRunningGame().getMoveList());
                int figureId = getFigureId(f);
                game.getRunningGame().getCoordinates().setFigureType(KNIGHT_ID);
                pawnToQueenExtra(f, figureId);
            }
        });
        pawnToQueenDialog = builder.create();
        pawnToQueenDialog.show();
    }

    private int getFigureId(Figure f) {
        int figureId;
        if (game.getGameExtraParams().isColor()) {
            switch (f.getClass().getSimpleName()) {
                case KNIGHT:
                    figureId = R.drawable.knight_white;
                    break;
                case BISHOP:
                    figureId = R.drawable.bishop_white;
                    break;
                case ROOK:
                    figureId = R.drawable.rook_white;
                    break;
                default:
                    figureId = R.drawable.queen_white;
                    break;
            }
        } else {
            switch (f.getClass().getSimpleName()) {
                case KNIGHT:
                    figureId = R.drawable.knight_black;
                    break;
                case BISHOP:
                    figureId = R.drawable.bishop_black;
                    break;
                case ROOK:
                    figureId = R.drawable.rook_black;
                    break;
                default:
                    figureId = R.drawable.queen_black;
                    break;
            }
        }
        return figureId;
    }

    private void pawnToQueenExtra(Figure f, int figureId) {
        game.getGameExtraParams().getFigures().add(f);
        if (game.getRunningGame().getPlayer1().getPlayerGameParams().isCancelableMoves()
                || game.getRunningGame().getPlayer1().getPlayerGameParams().isCancelableMoves()) {
            game.getGameExtraParams().getMoveWhenAddedFiguresList().add(game.getGameExtraParams().getCountMoves());
            game.getGameExtraParams().getAddedFigureList().add(f);
        }
        game.getPiece()[8 - game.getRunningGame().getCoordinates().getNewY()]
                [game.getRunningGame().getCoordinates().getNewX() - 1].setTag(figureId);
        game.getPiece()[8 - game.getRunningGame().getCoordinates().getNewY()]
                [game.getRunningGame().getCoordinates().getNewX() - 1].setImageResource(figureId);
        game.getGameMove().moveContinue(f);
        if (pawnToQueenDialog != null && pawnToQueenDialog.isShowing()) {
            pawnToQueenDialog.dismiss();
        }
    }

    AlertDialog getPawnToQueenDialog() {
        return pawnToQueenDialog;
    }
}
