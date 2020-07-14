/*
 * Author: Jaideep Prasad
 * CSE 476 Spring 2020 Practical Exam
 */

package edu.msu.prasadj2.examprasadj2;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Class for the 2048 game
 */
public class Game implements Serializable {

    /**
     * Percentage of the display width or height that
     * is occupied by the puzzle.
     */
    private static final float SCALE_IN_VIEW = 0.95f;

    // Tile count for rows and columns
    private static final int NUM_TILES = 4;

    // Possible fling directions
    private static final int RIGHT = 0, UP = 1, LEFT = 2, DOWN = 3;

    // Random number generator
    private Random random = new Random();

    // The game score
    private int score;

    // Movement ability flags
    private boolean canMoveLeft, canMoveRight, canMoveUp, canMoveDown;

    // The game board
    private int[][] board;

    // List of available tile positions
    private ArrayList<Integer> openTiles = new ArrayList<Integer>();

    // List of merged tile values
    private ArrayList<Integer> mergedValues = new ArrayList<Integer>();

    // Tile number value and color map
    private HashMap<Integer, Integer> tileColors;

    /**
     * Paint for filling the area the board is in
     */
    private transient Paint fillPaint;

    /**
     * Paint for drawing text
     */
    private transient Paint textPaint;

    /**
     * Paint for outlining the area the board is in
     */
    private transient Paint outlinePaint;

    /**
     * The size of the board in pixels
     */
    private int boardSize;

    /**
     * Left margin in pixels
     */
    private int marginX;

    /**
     * Top margin in pixels
     */
    private int marginY;

    /**
     * Game constructor
     */
    public Game() {
        score = 0;
        initializeBoard();
        determineValidMoves();
        randomizeColors();
        initializePaints();
    }

    /**
     * Draws this view
     * @param canvas The canvas
     */
    public void draw(@NonNull Canvas canvas) {
        int wid = canvas.getWidth();
        int hit = canvas.getHeight();

        // Determine the minimum of the two dimensions
        int minDim = wid < hit ? wid : hit;

        boardSize = (int)(minDim * SCALE_IN_VIEW);

        // Compute the margins so we center the board
        marginX = (wid - boardSize) / 2;
        marginY = (hit - boardSize) / 2;

        // Board background
        fillPaint.setColor(Color.LTGRAY);
        canvas.drawRect(marginX, marginY,
                marginX + boardSize, marginY + boardSize, fillPaint);

        // Board tiles
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                int value = board[row][col];
                if (value < 2) {
                    continue;
                }

                String valueString = "" + value;

                float left = marginX + col * (float)boardSize / NUM_TILES;
                float top = marginY + row * (float)boardSize / NUM_TILES;
                float right = marginX + (col + 1) * (float)boardSize / NUM_TILES;
                float bottom = marginY + (row + 1) * (float)boardSize / NUM_TILES;

                float textX = (marginX + (col + 0.5f) * (float)boardSize / NUM_TILES)
                        - (textPaint.measureText(valueString)/2);
                float textY = (marginY + (row + 0.5f) * (float)boardSize / NUM_TILES) -
                        ((textPaint.descent() + textPaint.ascent())/2);

                Integer color = tileColors.get(value);
                fillPaint.setColor(color != null ? color : Color.LTGRAY);
                canvas.drawRect(left, top, right, bottom, fillPaint);
                canvas.drawText(valueString, textX, textY, textPaint);
                canvas.drawRect(left, top, right, bottom, outlinePaint);
            }
        }
    }

    /**
     * Fling touch event handler
     * @param view The game view
     * @param e1 First event
     * @param e2 Second event
     * @param velocityX X velocity
     * @param velocityY Y velocity
     * @return true if the fling event was handled
     */
    public boolean onFlingEvent(View view, MotionEvent e1, MotionEvent e2,
                                float velocityX, float velocityY) {

        if (!canMoveRight && !canMoveLeft && !canMoveUp && !canMoveDown) {
            Toast.makeText(((GameView)view).getGameActivity(),
                    R.string.lost, Toast.LENGTH_SHORT).show();
            return false;
        }

        float x1 = e1.getX();
        float y1 = e1.getY();

        float x2 = e2.getX();
        float y2 = e2.getY();

        float deltaX = x2 - x1;
        float deltaY = y2 - y1;
        float angle = (float) Math.toDegrees(Math.atan2(deltaY, deltaX));

        int flingDirection;
        if (angle > 0) {
            if (angle < 45) { flingDirection = RIGHT; }
            else if (angle > 45 && angle < 135) { flingDirection = DOWN; }
            else { flingDirection = LEFT; }
        }
        else {
            if (angle > -45) { flingDirection = RIGHT; }
            else if (angle < -45 && angle > -135) { flingDirection = UP; }
            else { flingDirection = LEFT; }
        }

        switch (flingDirection) {
            case RIGHT:
                if (canMoveRight) {
                    moveRight();
                    break;
                }
                return false;
            case UP:
                if (canMoveUp) {
                    moveUp();
                    break;
                }
                return false;
            case LEFT:
                if (canMoveLeft) {
                    moveLeft();
                    break;
                }
                return false;
            case DOWN:
                if (canMoveDown) {
                    moveDown();
                    break;
                }
                return false;
            default:
                return false;
        }

        if (mergedValues.size() > 0) {
            for (int deltaScore : mergedValues) {
                score += deltaScore;
            }
            updateScore((GameView) view);
            mergedValues.clear();
        }

        updateOpenTiles();
        placeRandomTile();
        determineValidMoves();
        
        if (!canMoveRight && !canMoveLeft && !canMoveUp && !canMoveDown) {
            Toast.makeText(((GameView)view).getGameActivity(),
                    R.string.lost, Toast.LENGTH_SHORT).show();
        }

        view.invalidate();
        return true;
    }

    /**
     * Updates the game score
     * @param gameView The game view
     */
    public void updateScore(@NonNull GameView gameView) {
        gameView.updateScoreView(score);
    }

    /**
     * Starts a new game
     * @param view The game view
     */
    public void startNewGame(@NonNull View view) {
        score = 0;
        updateScore((GameView)view);
        initializeBoard();
        determineValidMoves();
        randomizeColors();
        view.invalidate();
    }

    /**
     * Reloads transient variables and refreshes the screen
     */
    public void reload(GameView gameView) {
        initializePaints();
        updateScore(gameView);
    }

    /**
     * Updates the list of open tiles
     */
    private void updateOpenTiles() {
        openTiles.clear();
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (board[row][col] == 0) {
                    openTiles.add(row);
                    openTiles.add(col);
                }
            }
        }
    }

    /**
     * Places a random tile with a value of 2 on an open spot on the board
     */
    private void placeRandomTile() {
        int index;
        do {
            index = random.nextInt(openTiles.size() - 1);
        } while (index % 2 != 0);
        board[openTiles.get(index)][openTiles.get(index + 1)] = 2;
    }

    /**
     * Moves the tiles right
     */
    private void moveRight() {
        for (int[] row : board) {

            for (int i = row.length - 1; i >= 0; i--) {
                int currentNum = row[i];
                if (currentNum == 0) {
                    continue;
                }
                for (int j = i - 1; j >= 0; j--) {
                    if (row[j] == 0) {
                        continue;
                    }
                    else if (row[j] == currentNum) {
                        row[i] += currentNum;
                        row[j] = 0;
                        i = j;
                        mergedValues.add(currentNum * 2);
                        break;
                    }
                    else {
                        break;
                    }
                }
            }

            int zeroCount = 0;
            ArrayList<Integer> nonZeroValues = new ArrayList<Integer>();
            for (int value : row) {
                if (value == 0) {
                    zeroCount++;
                }
                else {
                    nonZeroValues.add(value);
                }
            }

            for (int i = 0; i < row.length; i++) {
                if (i < zeroCount) {
                    row[i] = 0;
                }
                else {
                    row[i] = nonZeroValues.get(i - zeroCount);
                }
            }
        }
    }

    /**
     * Moves the tiles left
     */
    private void moveLeft() {
        for (int[] row : board) {

            for (int i = 0; i < row.length; i++) {
                int currentNum = row[i];
                if (currentNum == 0) {
                    continue;
                }
                for (int j = i + 1; j < row.length; j++) {
                    if (row[j] == 0) {
                        continue;
                    }
                    else if (row[j] == currentNum) {
                        row[i] += currentNum;
                        row[j] = 0;
                        i = j;
                        mergedValues.add(currentNum * 2);
                        break;
                    }
                    else {
                        break;
                    }
                }
            }

            ArrayList<Integer> nonZeroValues = new ArrayList<Integer>();
            for (int value : row) {
                if (value != 0) {
                    nonZeroValues.add(value);
                }
            }

            for (int i = 0; i < row.length; i++) {
                if (i < nonZeroValues.size()) {
                    row[i] = nonZeroValues.get(i);
                }
                else {
                    row[i] = 0;
                }
            }
        }
    }

    /**
     * Moves the tiles up
     */
    private void moveUp() {
        for (int col = 0; col < board.length; col++) {

            for (int row = 0; row < board.length; row++) {
                int currentNum = board[row][col];
                if (currentNum == 0) {
                    continue;
                }
                for (int j = row + 1; j < board.length; j++) {
                    if (board[j][col] == 0) {
                        continue;
                    }
                    else if (board[j][col] == currentNum) {
                        board[row][col] += currentNum;
                        board[j][col] = 0;
                        row = j;
                        mergedValues.add(currentNum * 2);
                        break;
                    }
                    else {
                        break;
                    }
                }
            }

            ArrayList<Integer> nonZeroValues = new ArrayList<Integer>();
            for (int row = 0; row < board.length; row++) {
                int value = board[row][col];
                if (value != 0) {
                    nonZeroValues.add(value);
                }
            }

            for (int row = 0; row < board.length; row++) {
                if (row < nonZeroValues.size()) {
                    board[row][col] = nonZeroValues.get(row);
                }
                else {
                    board[row][col] = 0;
                }
            }

        }
    }

    /**
     * Moves the tiles down
     */
    private void moveDown() {
        for (int col = 0; col < board.length; col++) {

            for (int row = board.length - 1; row >= 0; row--) {
                int currentNum = board[row][col];
                if (currentNum == 0) {
                    continue;
                }
                for (int j = row - 1; j >= 0; j--) {
                    if (board[j][col] == 0) {
                        continue;
                    }
                    else if (board[j][col] == currentNum) {
                        board[row][col] += currentNum;
                        board[j][col] = 0;
                        row = j;
                        mergedValues.add(currentNum * 2);
                        break;
                    }
                    else {
                        break;
                    }
                }
            }

            int zeroCount = 0;
            ArrayList<Integer> nonZeroValues = new ArrayList<Integer>();
            for (int row = 0; row < board.length; row++) {
                int value = board[row][col];
                if (value == 0) {
                    zeroCount++;
                }
                else {
                    nonZeroValues.add(value);
                }
            }

            for (int row = 0; row < board.length; row++) {
                if (row < zeroCount) {
                    board[row][col] = 0;
                }
                else {
                    board[row][col] = nonZeroValues.get(row - zeroCount);
                }
            }

        }
    }

    /**
     * Determines the valid flings that can currently occur
     */
    private void determineValidMoves() {
        canMoveRight = determineCanMoveRight();
        canMoveLeft = determineCanMoveLeft();
        canMoveUp = determineCanMoveUp();
        canMoveDown = determineCanMoveDown();
    }

    /**
     * Determines if a right fling is a valid move
     * @return true if it is
     */
    private boolean determineCanMoveRight() {
        for (int[] row : board) {

            int startOccupiedCol = -1;
            for (int col = 0; col < row.length; col++) {
                if (row[col] != 0) {
                    startOccupiedCol = col;
                    break;
                }
            }
            if (startOccupiedCol < 0) {
                continue;
            }

            for (int col = startOccupiedCol; col < row.length - 1; col++) {
                if (row[col] == row[col+1] || (col > startOccupiedCol && row[col] == 0) ||
                        (row[col+1] == 0)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if a left fling is a valid move
     * @return true if it is
     */
    private boolean determineCanMoveLeft() {
        for (int[] row : board) {

            int startOccupiedCol = -1;
            for (int col = row.length - 1; col >= 0; col--) {
                if (row[col] != 0) {
                    startOccupiedCol = col;
                    break;
                }
            }
            if (startOccupiedCol < 0) {
                continue;
            }

            for (int col = startOccupiedCol; col > 0; col--) {
                if (row[col] == row[col-1] || (col < startOccupiedCol && row[col] == 0) ||
                        (row[col-1] == 0)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if an up fling is a valid move
     * @return true if it is
     */
    private boolean determineCanMoveUp() {
        for (int col = 0; col < board.length; col++) {
            int startOccupiedRow = -1;
            for (int row = board.length - 1; row >= 0; row--) {
                if (board[row][col] != 0) {
                    startOccupiedRow = row;
                    break;
                }
            }
            if (startOccupiedRow < 0) {
                continue;
            }

            for (int row = startOccupiedRow; row > 0; row--) {
                if (board[row][col] == board[row-1][col] ||
                        (row < startOccupiedRow && board[row][col] == 0) ||
                        (board[row-1][col] == 0)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if a down fling is a valid move
     * @return true if it is
     */
    private boolean determineCanMoveDown() {
        for (int col = 0; col < board.length; col++) {
            int startOccupiedRow = -1;
            for (int row = 0; row < board.length; row++) {
                if (board[row][col] != 0) {
                    startOccupiedRow = row;
                    break;
                }
            }
            if (startOccupiedRow < 0) {
                continue;
            }

            for (int row = startOccupiedRow; row < board.length - 1; row++) {
                if (board[row][col] == board[row+1][col] ||
                        (row > startOccupiedRow && board[row][col] == 0) ||
                        (board[row+1][col] == 0)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Initializes the game board
     */
    private void initializeBoard() {
        board = new int[NUM_TILES][NUM_TILES];
        int[] randStartTileOne = new int[2];
        int[] randStartTileTwo = new int[2];

        do {
            randStartTileOne[0] = random.nextInt(NUM_TILES);
            randStartTileOne[1] = random.nextInt(NUM_TILES);
            randStartTileTwo[0] = random.nextInt(NUM_TILES);
            randStartTileTwo[1] = random.nextInt(NUM_TILES);
        } while (randStartTileOne[0] == randStartTileTwo[0] &&
                randStartTileOne[1] == randStartTileTwo[1]);

        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (row == randStartTileOne[0] && col == randStartTileOne[1] ||
                        row == randStartTileTwo[0] && col == randStartTileTwo[1]) {
                    board[row][col] = 2;
                }
                else {
                    board[row][col] = 0;
                }
            }
        }
    }

    /**
     * Randomizes tile number colors
     */
    @SuppressLint("UseSparseArrays")
    private void randomizeColors() {
        tileColors = new HashMap<Integer, Integer>();
        int r, g, b;
        for (int i = 2; i <= 2048; i *= 2) {
            r = random.nextInt(155) + 100;
            g = random.nextInt(155) + 100;
            b = random.nextInt(155) + 100;
            tileColors.put(i, Color.argb(0xff, r, g, b));
        }
    }

    /**
     * Initializes the paints
     */
    private void initializePaints() {
        // Gray background paint
        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setColor(Color.LTGRAY);

        // Black text paint
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(40f);

        // Black outline paint
        outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outlinePaint.setColor(Color.BLACK);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(3f);
    }

}
