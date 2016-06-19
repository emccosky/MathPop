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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
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

    //SET THESE VARIABLES FOR DIFFERENT GAMEPLAY
    private final int numChoices = 5;
    private final int numQuestions = 10;


    private int[][] answerPattern = {   {1, 0, 1},
                                        {0, 1, 0},
                                        {1, 1, 0}};
    private int[][] currModel = new int[3][3];

    private int currAnswer;
    private int currQuestion = 0;
    private int[] choices = new int[numChoices];
    private int[] changedAnswers;
    private ArrayList<Integer> choosenAnswers = new ArrayList<>();
    private ArrayList<Integer> poppedIndices = new ArrayList<>();

    private int correctCount = 0;
    private int wrongCount = 0;
    private int max = numQuestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        mVisible = true;
        mContentView = findViewById(R.id.questionView);

        updateProgress();
        nextQuestion();
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

    private void updateProgress(){
        StackedHorizontalProgressBar stackedHorizontalProgressBar;
        stackedHorizontalProgressBar = (StackedHorizontalProgressBar) findViewById(R.id.progressBar);
        stackedHorizontalProgressBar.setMax(max);
        stackedHorizontalProgressBar.setProgress(correctCount);
        stackedHorizontalProgressBar.setSecondaryProgress(wrongCount);
    }

    private void addAnswer(int answer) {
        choosenAnswers.add(answer);
        for (int i = 0; i < choices.length; i++) {
            if (choices[i] == answer) {
                choices[i] = 0;
                break;
            }
        }

        Log.d("Selected", "" + answer);
        String out = "";
        for (int i : choices){
            out += i + " ";
        }
        Log.d("New Choices", out);

        for (int index : poppedIndices){
            int temp = index - 1;
            int temp2 = temp / 3;
            int temp3 = temp % 3;
            currModel[temp2][temp3] = 0;
        }
        poppedIndices = new ArrayList<>();

        if (choosenAnswers.size() == 2) {
            nextQuestion();
        }
        updateProgress();
    }

    private void checkQuestion() {
        int ans1 = choosenAnswers.get(0);
        int ans2 = choosenAnswers.get(1);

        if ((ans1 + ans2) == currAnswer) {
            correctCount++;
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            wrongCount++;
            Toast.makeText(this, "Wrong Answer!", Toast.LENGTH_SHORT).show();
        }
    }

    private void nextQuestion() {
        if (currQuestion > 0) {
            checkQuestion();
        }
        choosenAnswers = new ArrayList<>();

        String output = "";
        for (int i = 0; i < currModel.length; i++){
            for (int j = 0; j < currModel[i].length; j++){
                output += currModel[i][j];
            }
            output += "\n";
        }
        Log.d("currModel", output);

        if (currQuestion < 10) {
            currQuestion++;
            repopulateChoices();
            genAnswer();
            setViews();
        } else {
            //END GAME
            System.exit(0);
        }
    }

    private void repopulateChoices(){
        changedAnswers = zeroLocations(choices);
        Random r = new Random();

        for (int zLoc : changedAnswers){
            choices[zLoc] = r.nextInt(9) + 1;
        }
    }

    private void genAnswer(){
        Random r = new Random();

        int spot1 = r.nextInt(choices.length);
        Log.d("Answer Spot", "" + spot1);
        int spot2 = 0;

        boolean isNumAcceptable = false;
        while (!isNumAcceptable){
            isNumAcceptable = true;
            spot2 = r.nextInt(choices.length);
            if(spot2 == spot1){
                isNumAcceptable = false;
            }
        }
        Log.d("Answer Spot", "" + spot2);
        currAnswer = choices[spot1] + choices[spot2];
    }

    private int zeroCount(int[] checkArray) {
        int count = 0;
        for (int item : checkArray) {
            if (item == 0) {
                count++;
            }
        }
        Log.d("Zero Count", "" + count);
        return count;
    }

    private int[] zeroLocations(int[] checkArray){
        int count = zeroCount(checkArray);
        int[] zLocs = new int[count];
        int spot = 0;

        for (int i = 0; i < checkArray.length; i++){
            if (checkArray[i] == 0){
                zLocs[spot++] = i;
            }
        }
        return zLocs;
    }

    private void setViews() {
        Random r = new Random();
        ArrayList<Integer> tempChoices = new ArrayList<>();
        if (currQuestion == 0) {
            for (int item : choices) {
                tempChoices.add(item);
            }
        } else {
            for (int i = 0; i < numChoices; i++) {
                for (int spot : changedAnswers) {
                    if (spot == i) {
                        tempChoices.add(choices[i]);
                    }
                }
            }
        }

        //Set the text in the question prompt field
        String nextQ = Integer.toString(currAnswer);
        TextView question = (TextView)findViewById(R.id.questionView);
        question.setText(nextQ);
        Log.d("Curr Question", "" + nextQ);

        //for the very first question do a clean gen of the bubble field
        String output = "\n";
        for (int i = 0; i < answerPattern.length; i++) {
            for (int j = 0; j < answerPattern[i].length; j++) {
                if (currModel[i][j] == 0) {
                    if (answerPattern[i][j] == 1) {
                        currModel[i][j] = tempChoices.remove(r.nextInt(tempChoices.size()));
                    } else {
                        currModel[i][j] = 0;
                    }
                }
                output += currModel[i][j];
            }
            output += "\n";
        }
        Log.d("Answer Model", output);
        Log.d("Question Num", "" + currQuestion);

        for (int i = 0; i < currModel.length; i++) {
            for (int j = 0; j < currModel[i].length; j++) {
                final int choice = currModel[i][j];
                int id = 0;
                final int gridIndex = (i * 3) + (j + 1);
                switch (gridIndex){
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
                if (choice != 0) {
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
                            poppedIndices.add(gridIndex);
                            addAnswer(choice);
                        }
                    });
                } else {
                    final ImageView currBubble = (ImageView) findViewById(id);
                    currBubble.setVisibility(View.INVISIBLE);
                }
            }
        }
    }
}
