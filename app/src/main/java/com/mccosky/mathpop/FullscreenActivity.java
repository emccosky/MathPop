package com.mccosky.mathpop;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.annotation.DrawableRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.text.AndroidCharacter;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

import github.nisrulz.stackedhorizontalprogressbar.StackedHorizontalProgressBar;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    //======================================== CLASS VARIABLES ========================================

    private int[] results = new int[10];

    private boolean questionGen = false;
    private int[] questions = new int[10];
    private int[][] answerPattern = {{1, 0, 1},
            {0, 1, 0},
            {1, 1, 0}};
    private int[][] currModel = new int[3][3];

    private int currQuestion = 0;
    private final int numChoices = 5;
    private int[] choices = new int[numChoices];
    private ArrayList<Integer> choosenAnswers = new ArrayList<>();

    private int correctCount = 9;
    private int wrongCount = 1;
    private int max = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        mVisible = true;
        mContentView = findViewById(R.id.fullscreen_content);

        StackedHorizontalProgressBar stackedHorizontalProgressBar;
        stackedHorizontalProgressBar = (StackedHorizontalProgressBar) findViewById(R.id.progressBar);
        stackedHorizontalProgressBar.setMax(max);
        stackedHorizontalProgressBar.setProgress(correctCount);
        stackedHorizontalProgressBar.setSecondaryProgress(wrongCount);

        /*GridView gridview = (GridView) findViewById(R.id.gridView);
        AnswerAdapter answerAdapter = new AnswerAdapter(this);
        gridview.setAdapter(answerAdapter);*/
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        //mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    //check to see if there are 2 answers that have been selected yet
    private boolean checkAnswers() {
        return false;
    }

    private void addAnswer(int answer) {
        choosenAnswers.add(answer);
        for (int i = 0; i < choices.length; i++) {
            if (choices[i] == answer) {
                choices[i] = 0;
            }
        }
        if (choosenAnswers.size() == 2) {
            nextQuestion();
        }
    }

    private void checkQuestion() {
        int ans1 = choosenAnswers.get(0);
        int ans2 = choosenAnswers.get(1);

        if ((ans1 + ans2) == questions[currQuestion]) {
            correctCount++;
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            wrongCount++;
            Toast.makeText(this, "Wrong Answer!", Toast.LENGTH_SHORT).show();
        }
    }

    private void nextQuestion() {
        checkQuestion();
        if (currQuestion < 9) {
            currQuestion++;
            updateChoices(choices);
        } else {
            //END GAME
        }
    }

    //update the choices array
    private void updateChoices(int[] leftovers) {
        int qNum = questions[currQuestion];
        Random r = new Random();
        choices = new int[numChoices];

        //only runs the for the first time the answers are generated
        if (leftovers.length - zeroCount(leftovers) == 0) {
            int spot = 0;

            //generate a random number for the first correct answer
            int num1 = r.nextInt(9) + 1;
            choices[spot++] = num1;

            //get the second correct answer
            int num2 = qNum - num1;
            choices[spot++] = num2;

            boolean isNumAcceptable = false;

            for (int i = 0; i < (numChoices - 4); i++) {
                int tryNum = 0;
                while (!isNumAcceptable) {
                    //give the number the benefit of the doubt
                    isNumAcceptable = true;
                    tryNum = r.nextInt(9) + 1;

                    boolean contains = false;
                    boolean conflicts = false;
                    for (int num : choices) {
                        if (num == tryNum) {
                            contains = true;
                        }
                        if ((num + tryNum) == qNum) {
                            conflicts = true;
                        }
                    }

                    //check to see if the number is not acceptable
                    if (contains) {
                        isNumAcceptable = false;
                    }
                    if (conflicts) {
                        isNumAcceptable = false;
                    }
                }

                choices[spot++] = tryNum;
            }
        } else {
            if (hasAnswer(leftovers, qNum)) {
                int spot = 0;
                boolean isNumAcceptable = false;
                for (int i = 0; i < leftovers.length; i++) {
                    if (leftovers[i] != 0) {
                        choices[spot++] = leftovers[i];
                    } else {
                        int tryNum = 0;
                        while (!isNumAcceptable) {
                            //give the number the benefit of the doubt
                            isNumAcceptable = true;
                            tryNum = r.nextInt(9) + 1;

                            boolean contains = false;
                            boolean conflicts = false;
                            for (int num : choices) {
                                if (num == tryNum) {
                                    contains = true;
                                }
                                if ((num + tryNum) == qNum) {
                                    conflicts = true;
                                }
                            }

                            //check to see if the number is not acceptable
                            if (contains) {
                                isNumAcceptable = false;
                            }
                            if (conflicts) {
                                isNumAcceptable = false;
                            }
                        }

                        choices[spot++] = tryNum;
                    }
                }
            } else {
                choices = leftovers;
                //pick a random choice that exists
                int num1 = leftovers[r.nextInt(leftovers.length) + 1];
                //get the second correct answer
                int num2 = qNum - num1;
                //find a 0 and set the new num2 to it
                for (int i = 0; i < leftovers.length; i++) {
                    if (leftovers[i] == 0) {
                        leftovers[i] = num2;
                        break;
                    }
                }

                int spot = 0;
                boolean isNumAcceptable = false;
                for (int i = 0; i < leftovers.length; i++) {
                    if (leftovers[i] != 0) {
                        choices[spot++] = leftovers[i];
                    } else {
                        int tryNum = 0;
                        while (!isNumAcceptable) {
                            //give the number the benefit of the doubt
                            isNumAcceptable = true;
                            tryNum = r.nextInt(9) + 1;

                            boolean contains = false;
                            boolean conflicts = false;
                            for (int num : choices) {
                                if (num == tryNum) {
                                    contains = true;
                                }
                                if ((num + tryNum) == qNum) {
                                    conflicts = true;
                                }
                            }

                            //check to see if the number is unacceptable
                            if (contains) {
                                isNumAcceptable = false;
                            }
                            if (conflicts) {
                                isNumAcceptable = false;
                            }
                        }

                        choices[spot++] = tryNum;
                    }
                }

            }
        }

        setBubbles();
    }

    private boolean hasAnswer(int[] currChoices, int question) {
        for (int i = 0; i < currChoices.length; i++) {
            int reqNum = question - currChoices[i];
            for (int j = 0; j < currChoices.length && j != i; j++) {
                if (currChoices[j] == reqNum) {
                    return true;
                }
            }
        }
        return false;
    }

    private int zeroCount(int[] checkArray) {
        int count = 0;
        for (int item : checkArray) {
            if (item == 0) {
                count++;
            }
        }
        return count;
    }

    private void setBubbles() {
        Random r = new Random();
        ArrayList<Integer> tempChoices = new ArrayList<>();
        for (int item : choices) {
            tempChoices.add(item);
        }

        //ASSIGN VALUES TO THE MODEL BASE DON THE PATTERN

        //for the very first question do a clean gen of the bubble field
        if (currQuestion == 0) {
            //TODO: Generate a random pattern for the beginning
            for (int i = 0; i < answerPattern.length; i++) {
                for (int j = 0; j < answerPattern[i].length; j++) {
                    if (answerPattern[i][j] == 1) {
                        currModel[i][j] = tempChoices.remove(r.nextInt(tempChoices.size()));
                    } else {
                        currModel[i][j] = 0;
                    }
                }
            }

            for (int i = 0; i < currModel.length; i++) {
                for (int j = 0; j < currModel[i].length; j++) {
                    final int choice = currModel[i][j];
                    if (choice != 0) {
                        int id = 0;
                        switch ((i * 3) + (j + 1)){
                            case 1:
                                id = R.id.bubble1;
                                break;
                            case 2:
                                id = R.id.bubble2;
                                break;
                            case 3:
                                id = R.id.bubble3;
                                break;
                            case 4:
                                id = R.id.bubble4;
                                break;
                            case 5:
                                id = R.id.bubble5;
                                break;
                            case 6:
                                id = R.id.bubble6;
                                break;
                            case 7:
                                id = R.id.bubble7;
                                break;
                            case 8:
                                id = R.id.bubble8;
                                break;
                            case 9:
                                id = R.id.bubble9;
                                break;
                        }

                        final ImageView currBubble = (ImageView)findViewById(id);
                        switch (choice){
                            case 1:
                                currBubble.setBackground(getDrawable(R.drawable.bubble1));
                                break;
                            case 2:
                                currBubble.setBackground(getDrawable(R.drawable.bubble2));
                                break;
                            case 3:
                                currBubble.setBackground(getDrawable(R.drawable.bubble3));
                                break;
                            case 4:
                                currBubble.setBackground(getDrawable(R.drawable.bubble4));
                                break;
                            case 5:
                                currBubble.setBackground(getDrawable(R.drawable.bubble5));
                                break;
                            case 6:
                                currBubble.setBackground(getDrawable(R.drawable.bubble6));
                                break;
                            case 7:
                                currBubble.setBackground(getDrawable(R.drawable.bubble7));
                                break;
                            case 8:
                                currBubble.setBackground(getDrawable(R.drawable.bubble8));
                                break;
                            case 9:
                                currBubble.setBackground(getDrawable(R.drawable.bubble9));
                                break;
                        }

                        currBubble.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                currBubble.setBackground(getDrawable(R.drawable.popped_bubble));
                                addAnswer(choice);
                            }
                        });
                    }
                }
            }
        }


    }
}
