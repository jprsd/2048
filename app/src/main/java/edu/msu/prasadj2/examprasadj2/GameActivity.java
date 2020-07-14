/*
 * Author: Jaideep Prasad
 * CSE 476 Spring 2020 Practical Exam
 */

package edu.msu.prasadj2.examprasadj2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * Main activity
 */
public class GameActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

    private static final String GAME_STATE = "GameActivity.GameState";

    private GestureDetectorCompat gestureDetector;

    /**
     * Creates the activity
     * @param savedInstanceState Any previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getGameView().setScoreView((TextView)this.findViewById(R.id.score));
        getGameView().setGameActivity(this);

        if (savedInstanceState != null) {
            getGameView().reload(GAME_STATE, savedInstanceState);
        }

        gestureDetector = new GestureDetectorCompat(this, this);
    }

    /**
     * Saves the current state
     * @param outState The bundle to save to
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        getGameView().putToBundle(GAME_STATE, outState);
    }

    /**
     * Touch event handler
     * @param event The motion event
     * @return true if the event was handled
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    /**
     * Fling touch event handler
     * @param e1 First event
     * @param e2 Second event
     * @param velocityX X velocity
     * @param velocityY Y velocity
     * @return true if the fling event was handled
     */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return getGameView().onFlingEvent(e1, e2, velocityX, velocityY);
    }

    /**
     * Handler for the new game button
     * @param view The current view
     */
    public void onNewGame(View view) {
        getGameView().startNewGame();
    }

    /**
     * Gets the GameView for this activity
     * @return The GameView
     */
    private GameView getGameView() {
        return (GameView)this.findViewById(R.id.gameView);
    }

    // Ignore below

    @Override
    public boolean onDown(MotionEvent e) { return false; }
    @Override
    public void onShowPress(MotionEvent e) {}
    @Override
    public boolean onSingleTapUp(MotionEvent e) { return false; }
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) { return false; }
    @Override
    public void onLongPress(MotionEvent e) {}
}
