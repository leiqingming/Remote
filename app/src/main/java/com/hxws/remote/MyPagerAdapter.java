package com.hxws.remote;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import com.hxws.remote.R;

public class MyPagerAdapter extends PagerAdapter {

    private Context mContext;

    private ArrayList<View> viewLists;

    private String [] titles;

    MyPagerAdapter(){

    }

    MyPagerAdapter(Context context)
    {
        mContext = context;
    }


    MyPagerAdapter(String[] strings) {

        titles = strings;
    }

    public MyPagerAdapter(ArrayList<View> viewLists) {
        super();
        this.viewLists = viewLists;
    }

    void setViews(ArrayList<View> views) {
        this.viewLists = views;
    }

    @Override
    public int getCount() {
        return viewLists.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(viewLists.get(position));
        return viewLists.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(viewLists.get(position));
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        //return super.getPageTitle(position);
        return titles[position];
    }
}
