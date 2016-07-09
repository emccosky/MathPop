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

         long secs = (long)(time/1000);
        long mins = (long)((time/1000)/60);
        long hrs = (long)(((time/1000)/60)/60);

		/* Convert the seconds to String
		 * and format to ensure it has
		 * a leading zero when required
		 */
        secs = secs % 60;
        String seconds=String.valueOf(secs);
        if(secs == 0){
            seconds = "00";
        }
        if(secs <10 && secs > 0){
            seconds = "0"+seconds;
        }

		/* Convert the minutes to String and format the String */

        mins = mins % 60;
        String minutes=String.valueOf(mins);
        if(mins == 0){
            minutes = "00";
        }
        if(mins <10 && mins > 0){
            minutes = "0"+minutes;
        }

    	/* Convert the hours to String and format the String */

        String hours=String.valueOf(hrs);
        if(hrs == 0){
            hours = "00";
        }
        if(hrs <10 && hrs > 0){
            hours = "0"+hours;
        }

        String milliseconds = String.valueOf((long)time/100);
        if(milliseconds.length()==2){
            milliseconds = "0"+milliseconds;
        }
        if(milliseconds.length()<=1){
            milliseconds = "00";
        }

		/* Setting the timer text to the elapsed time */
        ((TextView)findViewById(R.id.scoreBox)).setText(hours + ":" + minutes + ":" + seconds + "." + milliseconds);
        ((TextView)findViewById(R.id.correctField)).setText("" + correctCount + " out of " + (correctCount + wrongCount));
    }

    public void playAgain(View view){
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
    }

}
