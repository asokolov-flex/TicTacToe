package com.xowin;

public class TicTacToeGame extends Game {

    private final int[][] model = new int[3][3];

    private int currentPlayer;

    private boolean isGameStopped;

    @Override
    public void initialize() {
        setScreenSize(3, 3);

        startGame();

        updateView();
    }

    public void startGame() {
        isGameStopped = false;
        currentPlayer = 1;

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                model[x][y] = 0;
            }
        }
    }

    /**
     * Updates the visual representation of the entire game board.
     * This method iterates through the 3x3 grid of the Tic Tac Toe game
     * and updates the visual display of each cell by calling the updateCellView method.
     * The updateCellView method is used to render specific cell values
     * based on the game model.
     */
    public void updateView() {
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                updateCellView(x, y, model[x][y]);
            }
        }
    }

    /**
     * Updates the visual representation of a cell in the game board based on its coordinates
     * and the provided value. Displays a specific symbol and text color for different values.
     *
     * @param x the x-coordinate of the cell
     * @param y the y-coordinate of the cell
     * @param value the value of the cell; expected values include:
     *              1 for "X" with red text,
     *              2 for "O" with blue text,
     *              or any other value for an empty cell with white text
     */
    public void updateCellView(int x, int y, int value) {
        if (value == 1) {
            setCellValueEx(x, y, Color.WHITE, "X", Color.RED);
        } else if (value == 2) {
            setCellValueEx(x, y, Color.WHITE, "O", Color.BLUE);
        } else {
            setCellValueEx(x, y, Color.WHITE, " ", Color.WHITE);
        }
    }

    /**
     * Sets the current player's sign at the specified cell in the game board and checks for
     * a win condition or draw. Updates the game view accordingly and stops the game if necessary.
     *
     * @param x the x-coordinate of the cell to mark
     * @param y the y-coordinate of the cell to mark
     */
    public void setSignAndCheck(int x, int y) {
        model[x][y] = currentPlayer;

        updateView();

        if (checkWin(x, y, currentPlayer)) {
            isGameStopped = true;

            if (currentPlayer == 1) {
                showMessageDialog(Color.NONE, "You Win!", Color.GREEN, 75);
            }

            if (currentPlayer == 2) {
                showMessageDialog(Color.NONE, "Game Over", Color.RED, 75);
            }

            return;
        }

        if (!hasEmptyCell()) {
            isGameStopped = true;

            showMessageDialog(Color.NONE, " Draw!", Color.BLUE, 75);

            return;
        }
    }

    /**
     * Handles the left mouse click event on the game board. Updates the game state
     * based on the clicked cell, switches the player, and triggers the computer's turn.
     * The method ensures the game stops processing interactions once the game is over
     * or if the clicked cell is already occupied.
     *
     * @param x the x-coordinate of the clicked cell on the game board
     * @param y the y-coordinate of the clicked cell on the game board
     */
    @Override
    public void onMouseLeftClick(int x, int y) {
        if (isGameStopped) {
            return;
        }

        if (model[x][y] != 0) {
            return;
        }

        setSignAndCheck(x, y);

        currentPlayer = 3 - currentPlayer; // 2 <--> 1

        computerTurn();

        currentPlayer = 3 - currentPlayer; // 2 <--> 1

    }

    /**
     * Checks whether the game board contains at least one empty cell.
     * An empty cell is represented by the value 0 in the board model.
     *
     * @return true if there is at least one empty cell in the game board, false otherwise
     */
    public boolean hasEmptyCell() {
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                if (model[x][y] == 0) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks whether a player has won the game by evaluating the state of the game board.
     * The method verifies win conditions based on the most recent move's row, column,
     * main diagonal, or anti-diagonal.
     *
     * @param x the x-coordinate (row index) of the last move
     * @param y the y-coordinate (column index) of the last move
     * @param n the player's value (e.g., 1 or 2) corresponding to the last move
     * @return true if the player's move results in a winning condition, false otherwise
     */
    public boolean checkWin(int x, int y, int n) {
        boolean rowWin = model[x][0] == n && model[x][1] == n && model[x][2] == n;

        boolean columnWin = model[0][y] == n && model[1][y] == n && model[2][y] == n;

        boolean mainDiagonalWin = (x == y) && model[0][0] == n && model[1][1] == n && model[2][2] == n;

        boolean antiDiagonalWin = (x + y == 2) && model[0][2] == n && model[1][1] == n && model[2][0] == n;

        return rowWin || columnWin || mainDiagonalWin || antiDiagonalWin;
    }

    /**
     * Handles the keyboard press event during the game. If the SPACE key is pressed
     * while the game is stopped, or if the ESCAPE key is pressed at any time,
     * the game is restarted, and the game board view is updated.
     *
     * @param key the key that was pressed; possible values include keys from the Key enum
     *            such as SPACE, ESCAPE, and others
     */
    @Override
    public void onKeyPress(Key key) {
        if ((key == Key.SPACE && isGameStopped) || key == Key.ESCAPE) {
            startGame();

            updateView();
        }
    }

    /**
     * Executes the computer's turn in the Tic Tac Toe game, following a prioritized strategy.
     * The method attempts to make the most advantageous move for the computer player.
     *
     * The strategy consists of the following steps, executed in order:
     * 1. Attempts to claim the center cell of the board if it is available.
     * 2. Checks if the computer can make a move that results in an immediate win,
     *    and performs that move if possible.
     * 3. Prevents the opponent from winning by blocking their potential winning move.
     * 4. If no winning or blocking moves are possible, selects the first available empty cell
     *    and makes a move there.
     *
     * Each step is executed sequentially, stopping as soon as a move is made.
     */
    public void computerTurn() {

        // Move to the center
        if (model[1][1] == 0) {
            setSignAndCheck(1, 1);

            return;
        }


        // find the winner turn
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                if (checkFutureWin(x, y, currentPlayer)) {
                    setSignAndCheck(x, y);

                    return;
                }
            }
        }
        
        // We prevent the opponent from winning.
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                if (checkFutureWin(x, y, 3 - currentPlayer)) {
                    setSignAndCheck(x, y);
                    return;
                }
            }
        }

        // tunr in first free cell
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                if (model[x][y] == 0) {
                    setSignAndCheck(x, y);

                    return;
                }
            }
        }

    }

    /**
     * Simulates a move at the specified position on the game board to determine if it would result in a win
     * for the specified player. The method temporarily updates the game board with the player's value
     * at the given position, checks if it results in a winning condition, and then restores the original state of the board.
     *
     * @param x the x-coordinate (row index) of the position to check
     * @param y the y-coordinate (column index) of the position to check
     * @param n the player's value (e.g., 1 for "X" or 2 for "O") to place at the specified position
     * @return true if the simulated move results in a winning condition, false otherwise
     */
    public boolean checkFutureWin(int x, int y, int n) {
        if (model[x][y] != 0 ) {
            return false;
        }

        model[x][y] = n;

        boolean isWin = checkWin(x, y, n);

        model[x][y] = 0;

        return isWin;
    }

}
