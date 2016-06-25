package com.mccosky.mathpop;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {

    long time;
    int correctCount;
    int wrongCount;
    int score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent i = getIntent();
        time = (long)i.getExtras().get("time");
        correctCount = (int)i.getExtras().get("correct");
        wrongCount = (int)i.getExtras().get("wrong");

        score = (int)((((double)correctCount / (double)time) / 0.005) * 9999);
        ((TextView)findViewById(R.id.scoreBox)).setText("" + score);
        ((TextView)findViewById(R.id.correctField)).setText("" + correctCount + " out of " + (correctCount + wrongCount));
    }

    public void playAgain(View view){
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
    }

}
