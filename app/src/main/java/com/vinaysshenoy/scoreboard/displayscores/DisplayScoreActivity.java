package com.vinaysshenoy.scoreboard.displayscores;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.vinaysshenoy.scoreboard.R;

/**
 * Created by vinaysshenoy on 14/1/17.
 */

public class DisplayScoreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_displayscore);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.actionBar);
        setSupportActionBar(toolbar);
    }
}
