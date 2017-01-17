package com.youtravel;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import java.util.ArrayList;

/**
 * Created by Andrew-PC on 23.09.2016.
 */
public class CustomizedItemSelectedListener implements AdapterView.OnItemSelectedListener {

    public final Spinner controller;
    ArrayList<String> id_data;
    ArrayList<Integer> id_cur;

    public CustomizedItemSelectedListener(ArrayList<String> id, Spinner controller){
        this.id_data = id;
        this.controller = controller;
    }

    public CustomizedItemSelectedListener(ArrayList<Integer> id){
        this.id_cur = id;
        this.controller = null;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view,
                               int position, long id) {
    }
    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

}
