package com.allenxuan.xuanyihuang.xuanimageviewproject;

import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.allenxuan.xuanyihuang.xuanimageview.XuanImageView;

public class ImagePagerActivity extends AppCompatActivity {
    private int[] images = new int[]{R.drawable.wallpaper1, R.drawable.wallpaper2, R.drawable.wallpaper3};
    private XuanImageView[] mXuanImageViews = new XuanImageView[images.length];
    private ViewPager mViewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_pager);

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(new PagerAdapter() {

            @Override
            public int getCount() {
                return images.length;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                XuanImageView xuanImageView = new XuanImageView(getBaseContext());
                xuanImageView.setImageResource(images[position]);
                container.addView(xuanImageView);

                mXuanImageViews[position] = xuanImageView;

                return  xuanImageView;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(mXuanImageViews[position]);
            }
        });
    }



//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        switch(item.getItemId()){
//            case android.R.id.home:
//                finish();
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
