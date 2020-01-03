package com.dan.app.daily;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.dan.lib.media.SmartMediaPlayer;
import com.dan.lib.media.entity.PlayerBean;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SmartMediaPlayer smartMedia;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        smartMedia = findViewById(R.id.smart_media);
//        smartMedia.setPlayerData(new PlayerBean());
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {

    }


}
