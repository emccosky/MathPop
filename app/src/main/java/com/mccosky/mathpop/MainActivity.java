package com.mccosky.mathpop;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    int[] params = new int[3];
    //Spots for each param
    //  0 - Type
    //  1 - Difficulty
    //  2 - Mode

    //Keep track of how many param categories are complete
    int selectCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void setAdd(View view){
        selectCount++;
        params[0] = 0;
        if (selectCount == 3){
            startGame();
        }
        ((Button)findViewById(R.id.multButton)).setVisibility(View.GONE);
    }

    public void setMult(View view){
        selectCount++;
        params[0] = 1;
        if (selectCount == 3){
            startGame();
        }
        ((Button)findViewById(R.id.addButton)).setVisibility(View.GONE);
    }

    public void setEasy(View view){
        selectCount++;
        params[1] = 0;
        if (selectCount == 3){
            startGame();
        }
        ((Button)findViewById(R.id.medButton)).setVisibility(View.GONE);
        ((Button)findViewById(R.id.hardButton)).setVisibility(View.GONE);
    }

    public void setMed(View view){
        selectCount++;
        params[1] = 1;
        if (selectCount == 3){
            startGame();
        }
        ((Button)findViewById(R.id.easyButton)).setVisibility(View.GONE);
        ((Button)findViewById(R.id.hardButton)).setVisibility(View.GONE);
    }

    public void setHard(View view){
        selectCount++;
        params[1] = 2;
        if (selectCount == 3){
            startGame();
        }
        ((Button)findViewById(R.id.easyButton)).setVisibility(View.GONE);
        ((Button)findViewById(R.id.medButton)).setVisibility(View.GONE);
    }

    public void setMode1(View view){
        selectCount++;
        params[2] = 0;
        if (selectCount == 3){
            startGame();
        }
        ((Button)findViewById(R.id.mode2Button)).setVisibility(View.GONE);
        ((Button)findViewById(R.id.mode3Button)).setVisibility(View.GONE);
    }

    public void setMode2(View view){
        selectCount++;
        params[2] = 1;
        if (selectCount == 3){
            startGame();
        }
        ((Button)findViewById(R.id.mode1Button)).setVisibility(View.GONE);
        ((Button)findViewById(R.id.mode3Button)).setVisibility(View.GONE);
    }

    public void setMode3(View view){
        selectCount++;
        params[2] = 2;
        if (selectCount == 3){
            startGame();
        }
        ((Button)findViewById(R.id.mode1Button)).setVisibility(View.GONE);
        ((Button)findViewById(R.id.mode2Button)).setVisibility(View.GONE);
    }

    public void startGame(){
        Intent i;
        if (params[0] == 0){
            i = new Intent(getApplicationContext(), FullscreenActivity.class);
        } else {
            i = new Intent(getApplicationContext(), MultiplicationActivity.class);
        }
        switch (params[1]){
            case 0:
                i.putExtra("numChoices", 4);
                i.putExtra("qCount", 10);
                i.putExtra("allowedWrongs", 10);
                i.putExtra("timeTotal", (long)60000);
                break;
            case 1:
                i.putExtra("numChoices", 5);
                i.putExtra("qCount", 15);
                i.putExtra("allowedWrongs", 5);
                i.putExtra("timeTotal", (long)60000);
                break;
            case 2:
                i.putExtra("numChoices", 7);
                i.putExtra("qCount", 20);
                i.putExtra("allowedWrongs", 3);
                i.putExtra("timeTotal", (long)60000);
                break;
        }
        i.putExtra("state", params[2]);
        startActivity(i);
    }

}
