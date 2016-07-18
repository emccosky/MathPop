package com.mccosky.mathpop;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
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
public class MultiplicationActivity extends AppCompatActivity {
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

    //Set default value just to be safe
    private int state = 0;
    //STATE CODES:
    //  0 - # out of # mode
    //  1 - countdown based on time mode
    //  2 - infinite until certain # of wrong answers mode

    //SET THESE VARIABLES FOR DIFFERENT GAMEPLAYS
    private int numChoices;
    private int numQuestions;
    private int allowedWrongs= 10;


    private int[][] answerPattern;
    private int[][] currModel = new int[3][3];

    private int currAnswer;
    private int currQuestion = 0;
    private int[] choices;
    private int[] changedAnswers;
    private ArrayList<Integer> choosenAnswers = new ArrayList<>();
    private ArrayList<Integer> poppedIndices = new ArrayList<>();

    private int correctCount = 0;
    private int wrongCount = 0;
    private int max;

    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        mediaPlayer = MediaPlayer.create(this, R.raw.song);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        mVisible = true;
        mContentView = findViewById(R.id.questionView);

        Intent intent = getIntent();
        state = (int)intent.getExtras().get("state");
        numChoices = (int)intent.getExtras().get("numChoices");
        choices = new int[numChoices];
        numQuestions = (int)intent.getExtras().get("qCount");
        max = numQuestions;
        allowedWrongs = (int)intent.getExtras().get("allowedWrongs");

        Log.d("NumChoices", "" + numChoices);

        genModel();
        updateProgress();
        nextQuestion();

        switch (state){
            case 0:
                startTime = System.currentTimeMillis();
                startTimer.run();
                break;
            case 1:
                remainTime = (long)intent.getExtras().get("timeTotal");
                downTimer.start();
                break;
            case 2:
                startTime = System.currentTimeMillis();
                startTimer.run();
                TextView time = (TextView)findViewById(R.id.timer);
                time.setVisibility(View.GONE);
                break;
        }

        ((TextView)findViewById(R.id.typeView)).setText("Multiplication");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    protected void onStop(){
        super.onStop();
        mediaPlayer.stop();
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


    //================= MAIN GAME CODE =================

    private void updateProgress(){
        if (state == 0) {
            StackedHorizontalProgressBar stackedHorizontalProgressBar;
            stackedHorizontalProgressBar = (StackedHorizontalProgressBar) findViewById(R.id.progressBar);
            stackedHorizontalProgressBar.setMax(max);
            stackedHorizontalProgressBar.setProgress(correctCount);
            stackedHorizontalProgressBar.setSecondaryProgress(wrongCount);

            TextView progressText = (TextView)findViewById(R.id.progressText);
            progressText.setText("Correct:" + correctCount + " Incorrect:" + wrongCount);
        } else if (state == 1) {
            StackedHorizontalProgressBar stackedHorizontalProgressBar;
            stackedHorizontalProgressBar = (StackedHorizontalProgressBar) findViewById(R.id.progressBar);
            stackedHorizontalProgressBar.setMax(correctCount + wrongCount);
            stackedHorizontalProgressBar.setProgress(correctCount);
            stackedHorizontalProgressBar.setSecondaryProgress(wrongCount);

            TextView progressText = (TextView)findViewById(R.id.progressText);
            progressText.setText("Correct:" + correctCount + " Incorrect:" + wrongCount);
        } else {
            StackedHorizontalProgressBar stackedHorizontalProgressBar;
            stackedHorizontalProgressBar = (StackedHorizontalProgressBar) findViewById(R.id.progressBar);
            stackedHorizontalProgressBar.setMax(allowedWrongs);
            stackedHorizontalProgressBar.setSecondaryProgress(wrongCount);

            TextView progressText = (TextView)findViewById(R.id.progressText);
            progressText.setText("Strikes Remaining: " + (allowedWrongs - wrongCount) + "/" + allowedWrongs);
        }
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
    }

    private void checkQuestion() {
        int ans1 = choosenAnswers.get(0);
        int ans2 = choosenAnswers.get(1);

        if ((ans1 * ans2) == currAnswer) {
            correctCount++;
            //Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            wrongCount++;
            //Toast.makeText(this, "Wrong Answer!", Toast.LENGTH_SHORT).show();
        }
    }

    private void nextQuestion() {
        if (currQuestion > 0) {
            checkQuestion();
            updateProgress();
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

        switch (state) {
            //# out of mode
            case 0:
                if (currQuestion < numQuestions) {
                    currQuestion++;
                    repopulateChoices();
                    genAnswer();
                    setViews();
                } else {
                    elapsedTime = System.currentTimeMillis() - startTime;
                    Intent i = new Intent(getApplicationContext(), ResultActivity.class);
                    i.putExtra("time", elapsedTime);
                    i.putExtra("correct", correctCount);
                    i.putExtra("wrong", wrongCount);
                    startActivity(i);
                }
                break;
            // countdown mode
            case 1:
                currQuestion++;
                repopulateChoices();
                genAnswer();
                setViews();
                break;
            //infinite mode
            case 2:
                if (wrongCount < allowedWrongs) {
                    currQuestion++;
                    repopulateChoices();
                    genAnswer();
                    setViews();
                } else {
                    Intent i = new Intent(getApplicationContext(), ResultActivity.class);
                    i.putExtra("time", elapsedTime);
                    i.putExtra("correct", correctCount);
                    i.putExtra("wrong", wrongCount);
                    startActivity(i);
                }
                break;
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
        currAnswer = choices[spot1] * choices[spot2];
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

    private void genModel(){
        Random r = new Random();
        answerPattern = new int[3][3];
        int[] randoSpots = new int[numChoices];

        for (int i = 0; i < numChoices; i++){
            boolean acceptable = false;
            int test = 0;
            while (!acceptable){
                acceptable = true;
                test = r.nextInt(9);
                for (int c : randoSpots){
                    if (c == test){
                        acceptable = false;
                    }
                }
            }
            randoSpots[i] = test;
        }

        for (int spot : randoSpots){
            answerPattern[spot / 3][spot % 3] = 1;
        }
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
                            currBubble.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    currBubble.setBackground(getDrawable(R.drawable.popped_bubble1));
                                    poppedIndices.add(gridIndex);
                                    addAnswer(choice);
                                }
                            });
                            break;
                        case 2:
                            currBubble.setBackground(getDrawable(R.drawable.bubble2));
                            currBubble.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    currBubble.setBackground(getDrawable(R.drawable.popped_bubble2));
                                    poppedIndices.add(gridIndex);
                                    addAnswer(choice);
                                }
                            });
                            break;
                        case 3:
                            currBubble.setBackground(getDrawable(R.drawable.bubble3));
                            currBubble.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    currBubble.setBackground(getDrawable(R.drawable.popped_bubble3));
                                    poppedIndices.add(gridIndex);
                                    addAnswer(choice);
                                }
                            });
                            break;
                        case 4:
                            currBubble.setBackground(getDrawable(R.drawable.bubble4));
                            currBubble.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    currBubble.setBackground(getDrawable(R.drawable.popped_bubble4));
                                    poppedIndices.add(gridIndex);
                                    addAnswer(choice);
                                }
                            });
                            break;
                        case 5:
                            currBubble.setBackground(getDrawable(R.drawable.bubble5));
                            currBubble.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    currBubble.setBackground(getDrawable(R.drawable.popped_bubble5));
                                    poppedIndices.add(gridIndex);
                                    addAnswer(choice);
                                }
                            });
                            break;
                        case 6:
                            currBubble.setBackground(getDrawable(R.drawable.bubble6));
                            currBubble.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    currBubble.setBackground(getDrawable(R.drawable.popped_bubble6));
                                    poppedIndices.add(gridIndex);
                                    addAnswer(choice);
                                }
                            });
                            break;
                        case 7:
                            currBubble.setBackground(getDrawable(R.drawable.bubble7));
                            currBubble.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    currBubble.setBackground(getDrawable(R.drawable.popped_bubble7));
                                    poppedIndices.add(gridIndex);
                                    addAnswer(choice);
                                }
                            });
                            break;
                        case 8:
                            currBubble.setBackground(getDrawable(R.drawable.bubble8));
                            currBubble.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    currBubble.setBackground(getDrawable(R.drawable.popped_bubble8));
                                    poppedIndices.add(gridIndex);
                                    addAnswer(choice);
                                }
                            });
                            break;
                        case 9:
                            currBubble.setBackground(getDrawable(R.drawable.bubble9));
                            currBubble.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    currBubble.setBackground(getDrawable(R.drawable.popped_bubble9));
                                    poppedIndices.add(gridIndex);
                                    addAnswer(choice);
                                }
                            });
                            break;
                    }
                } else {
                    final ImageView currBubble = (ImageView) findViewById(id);
                    currBubble.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    private Handler mHandler = new Handler();
    private long startTime;
    private long elapsedTime;
    private long remainTime;
    private final int REFRESH_RATE = 100;
    private String hours,minutes,seconds,milliseconds;
    private long secs,mins,hrs,msecs;

    private void updateTimer (float time){

        secs = (long)(time/1000);
        mins = (long)((time/1000)/60);
        hrs = (long)(((time/1000)/60)/60);

		/* Convert the seconds to String
		 * and format to ensure it has
		 * a leading zero when required
		 */
        secs = secs % 60;
        seconds=String.valueOf(secs);
        if(secs == 0){
            seconds = "00";
        }
        if(secs <10 && secs > 0){
            seconds = "0"+seconds;
        }

		/* Convert the minutes to String and format the String */

        mins = mins % 60;
        minutes=String.valueOf(mins);
        if(mins == 0){
            minutes = "00";
        }
        if(mins <10 && mins > 0){
            minutes = "0"+minutes;
        }

    	/* Convert the hours to String and format the String */

        hours=String.valueOf(hrs);
        if(hrs == 0){
            hours = "00";
        }
        if(hrs <10 && hrs > 0){
            hours = "0"+hours;
        }

        milliseconds = String.valueOf((long)time/100);
        if(milliseconds.length()==2){
            milliseconds = "0"+milliseconds;
        }
        if(milliseconds.length()<=1){
            milliseconds = "00";
        }

		/* Setting the timer text to the elapsed time */
        ((TextView)findViewById(R.id.timer)).setText(hours + ":" + minutes + ":" + seconds + "." + milliseconds);
    }

    private Runnable startTimer = new Runnable() {
        public void run() {
            elapsedTime = System.currentTimeMillis() - startTime;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateTimer(elapsedTime);
                }
            });
            mHandler.postDelayed(this,REFRESH_RATE);
        }
    };

    CountDownTimer downTimer = new CountDownTimer(60000, 1) {

        public void onTick(long millisUntilFinished) {
            updateTimer(millisUntilFinished);
        }

        public void onFinish() {
            Intent i = new Intent(getApplicationContext(), ResultActivity.class);
            i.putExtra("time", remainTime);
            i.putExtra("correct", correctCount);
            i.putExtra("wrong", wrongCount);
            startActivity(i);
            System.exit(0);
        }
    };
}
