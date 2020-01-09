package com.dan.app.daily;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.dan.lib.media.SmartPlayer;
import com.dan.lib.media.entity.PlayerBean;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SmartPlayer smartMedia;

    private String resources = "https://img.tukuppt.com/newpreview_music/08/99/75/5c8994da1484642590.mp3";
    private String bgPath = "http://img02.tooopen.com/images/20131212/sy_51552288528.jpg";
    private String resources2 = "https://img.tukuppt.com/newpreview_music/08/99/75/5c8994da6362432738.mp3";
    private String bgPath2 = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1578567871689&di=7b9bd83aa369be1e917e04419907a614&imgtype=0&src=http%3A%2F%2Fdl.ppt123.net%2Fpptbj%2F51%2F20181115%2Fmzj0ghw2xo2.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        smartMedia = findViewById(R.id.smart_media);

        List<PlayerBean> datas = new ArrayList<>();
        datas.add(new PlayerBean(resources, bgPath));
        datas.add(new PlayerBean(resources2, bgPath2));
        smartMedia.setDatas(datas);

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
