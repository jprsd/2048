/*
 * Author: Jaideep Prasad
 * CSE 476 Spring 2020 Practical Exam
 */

package edu.msu.prasadj2.examprasadj2;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

/**
 * Custom view for the 2048 game
 */
public class GameView extends View {

    /**
     * The actual game
     */
    private Game game;

    // The score text view
    private TextView scoreView;

    // The activity this view is a part of
    private GameActivity gameActivity;

    /**
     * GameView constructor
     * @param context Application context
     */
    public GameView(Context context) {
        super(context);
        init(null, 0);
    }

    /**
     * GameView constructor
     * @param context Application context
     * @param attrs Attributes
     */
    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    /**
     * GameView constructor
     * @param context Application context
     * @param attrs Attributes
     * @param defStyle Style
     */
    public GameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    /**
     * Initializes the view
     * @param attrs Attributes
     * @param defStyle Style
     */
    private void init(AttributeSet attrs, int defStyle) {
        game = new Game();
    }

    /**
     * Saves the game state
     * @param key Key
     * @param bundle Bundle to save to
     */
    public void putToBundle(@NonNull String key, @NonNull Bundle bundle) {
        bundle.putSerializable(key, game);
    }

    /**
     * Reloads the game state
     */
    public void reload(@NonNull String key, @NonNull Bundle bundle) {
        game = (Game)bundle.getSerializable(key);
        if (game != null) {
            game.reload(this);
        }
        else {
            game = new Game();
        }
    }

    /**
     * Notifies this view of the activity it is a part of
     * @param gameActivity The game activity
     */
    public void setGameActivity(GameActivity gameActivity) {
        this.gameActivity = gameActivity;
    }

    /**
     * Gets the game activity
     * @return The game activity
     */
    public GameActivity getGameActivity() {
        return gameActivity;
    }

    /**
     * Notifies this game view of the score view
     * @param scoreView The score view
     */
    public void setScoreView(TextView scoreView) {
        this.scoreView = scoreView;
        this.scoreView.setText("0");
    }

    /**
     * Updates the score view value
     * @param score The game score
     */
    public void updateScoreView(int score) {
        String scoreString = "" + score;
        scoreView.setText(scoreString);
        invalidate();
    }

    /**
     * Draws the view
     * @param canvas The canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        game.draw(canvas);
    }

    /**
     * Fling touch event handler
     * @param e1 First event
     * @param e2 Second event
     * @param velocityX X velocity
     * @param velocityY Y velocity
     * @return true if the fling event was handled
     */
    public boolean onFlingEvent(MotionEvent e1, MotionEvent e2,
                                float velocityX, float velocityY) {
        return game.onFlingEvent(this, e1, e2, velocityX, velocityY);
    }

    /**
     * Starts a new game
     */
    public void startNewGame() {
        game.startNewGame(this);
    }

}
