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


public class Tour implements Serializable {
            int id;
            int id_country;
            int id_city;
            String id_kind;
            int duration;
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
            ArrayList<Date> date;
            String id_cities;
            String start_point;
            String id_comment;


    public Tour(Cursor c, Context a, SharedPreferences settings){
        id = Integer.parseInt(c.getString(0));
        id_country = Integer.parseInt(c.getString(1));
        id_city = Integer.parseInt(c.getString(2));
        id_kind = c.getString(3);
        duration = Integer.parseInt(c.getString(4));
        name = c.getString(5);
        annotation = c.getString(6);
        description = c.getString(7);
        extra_info = c.getString(8);
        html = c.getString(9);
        id_status = Integer.parseInt(c.getString(10));
        link = c.getString(11);
        img = c.getString(12);
        try {
            price =  SettingsActivity.convert(c.getString(14), settings.getString("currency_short_name", null), new DBHelper(a))
                    * Double.parseDouble(c.getString(13));
            currency = settings.getString("currency_short_name", null);
            if (price == 0 && c.getDouble(13)!=0) throw new Exception();
        }
        catch (Exception e){
            price = Double.parseDouble(c.getString(13));
            currency = c.getString(14);
        }
        date = make_date_list(c.getString(15));
        id_cities = c.getString(16);
        start_point = c.getString(17);
        id_comment = c.getString(18);

    }

    public static Date make_date(String reference){
        DateFormat format = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());  // TODO dots between dd mm yyyy
        Date date = null;
        try {
            date = format.parse(reference);
        }
        catch (ParseException e){
            Log.d("DATA_ERROR","Date parsing error!");
        }
        return date;
    }

    public static ArrayList<Date> make_date_list(String reference){
        ArrayList<Date> result = new ArrayList<>();
        for (String s : reference.split(",")) {
            DateFormat format = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());  // TODO dots between dd mm yyyy
            Date date = null;
            try {
                date = format.parse(s);
                result.add(date);
            } catch (ParseException e) {
                Log.d("DATA_ERROR", "Date parsing error!");
            }

        }
        return result;
    }


}
