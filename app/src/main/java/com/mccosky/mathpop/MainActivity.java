package com.mccosky.mathpop;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void playAdd(View v) {
        Intent i = new Intent(getApplicationContext(), FullscreenActivity.class);6
        startActivity(i);
    }

    public void playMult(View v) {
        Intent i = new Intent(getApplicationContext(), MultiplicationActivity.class);
        startActivity(i);
    }

}
