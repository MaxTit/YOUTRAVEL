package com.youtravel;

import android.view.View;

import java.util.ArrayList;

/**
 * Created by Andrew-PC on 11.09.2016.
 */
public class FilteringListener implements View.OnClickListener {
    ArrayList<String> country_filter = new ArrayList<>();
    ArrayList<String> kind_filter = new ArrayList<>();
    ArrayList<String> city_filter = new ArrayList<>();

    public FilteringListener(ArrayList<String> country_filter, ArrayList<String> kind_filter,
                             ArrayList<String> city_filter) {
        this.country_filter = country_filter;
        this.kind_filter = kind_filter;
        this.city_filter = city_filter;
    }

    public static ArrayList<String> make_array(String... a){
        ArrayList<String> filter = new ArrayList<>();
        for (String i : a) {
            filter.add(i);
        }
       return filter;
    }

    @Override
    public void onClick(View v)
    {

    }
}
