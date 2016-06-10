package com.mccosky.mathpop;

import android.content.Context;
import android.view.ContextMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ethan on 6/9/2016.
 */
public class AnswerAdapter extends BaseAdapter{
    private Context mContext;
    private HashMap<Integer, Integer> itemMap;

    public AnswerAdapter(Context c){
        mContext = c;
    }

    @Override
    public int getCount() {
        return mThumbIds.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    //Make a new imageView for the items specified by the arrayList
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(110,110));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(5,5,5,5);
        } else {
            imageView = (ImageView) convertView;
        }
        imageView.setImageResource(mThumbIds[position]);
        return imageView;
    }

    public void setAnswerChoices(ArrayList<Integer> answers){
        int[] spots = {1,0,1,0,1,0,1,0,1};
        int k = 0;
        for (int i = 0; i < spots.length; i++) {
            if (spots[i] != 0) {
                itemMap.put(i, answers.get(k++));
            } else {
                itemMap.put(i, 0);
            }
        }

        for (int i = 0; i < spots.length; i++){
            if(itemMap.get(i) == 0) {
                mThumbIds[i] = itemMap.get(i);
            } else {
                Integer id;
                int mapSpot;
            }
        }
    }

    public Integer[] mThumbIds = {
        0,R.drawable.bubble
    };
}
