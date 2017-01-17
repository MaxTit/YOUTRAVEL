package com.youtravel;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Event implements Serializable {
    int id;
    String event_type;
    String name;
    String annotation;
    String description;
    String extra_info;
    String html;
    int id_status;
    String link;
    String img;
    Double price;
    String currency;
    ArrayList<Date> location_and_time;
    ArrayList<Integer> id_country;
    ArrayList<Integer> id_city;
    String id_comment;

    public Event(Cursor c, Context a, SharedPreferences settings){
        id = Integer.parseInt(c.getString(0));
        event_type = c.getString(1);
        name = c.getString(2);
        annotation = c.getString(3);
        description = c.getString(4);
        extra_info = c.getString(5);
        html = c.getString(6);
        id_status = Integer.parseInt(c.getString(7));
        link = c.getString(8);
        img = c.getString(9);
        try {
                price = SettingsActivity.convert(c.getString(11), settings.getString("currency_short_name", null), new DBHelper(a))
                        * Double.parseDouble(c.getString(10));
                currency = settings.getString("currency_short_name", null);
                if (price == 0 && c.getDouble(10)!=0) throw new Exception();
        }
        catch (Exception e){
            price = Double.parseDouble(c.getString(10));
            currency = c.getString(11);
        }
        location_and_time = Tour.make_date_list(c.getString(12));
        id_country = make_list(c.getString(13));
        id_city = make_list(c.getString(14));
        id_comment = c.getString(15);
    }


    private ArrayList<Integer> make_list(String reference){
        ArrayList<Integer> list = new ArrayList<>();
        String[] s = reference.split(",");
        try {
            for ( String i : s) {
                if (!i.equals("NULL"))
                    list.add(Integer.parseInt(i));
                else list.add(-1);
            }
        }
        catch (NullPointerException e){
            Log.d("DATA_ERROR","Date parsing error!");
        }
        return list;
    }

}

