package com.allenxuan.xuanyihuang.xuanimageviewproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.allenxuan.xuanyihuang.xuanimageview.XuanImageView;
import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

public class WithPicassoActivity extends AppCompatActivity {
    private XuanImageView xuanImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_picasso);

        xuanImageView = (XuanImageView) findViewById(R.id.withGlide_ImageView);
    }

    @Override
    protected void onStart() {
        super.onStart();


        Picasso.with(this).load("http://i.imgur.com/DvpvklR.png").into(xuanImageView);
    }
}
