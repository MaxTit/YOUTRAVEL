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
            String extra_info;
            String schedule;
            String program;
            String composition;
            String transport;
            String transfer;
            String residence;
            String excursions;
            String services;
            int id_status;
            String img;
            Double price;
            String currency;
            Date period_from;
            Date period_to;
            String id_cities;
            String id_comment;
            String days;


    public Tour(Cursor c, Context a, SharedPreferences settings){
        id = Integer.parseInt(c.getString(0));
        id_country = Integer.parseInt(c.getString(1));
        id_city = Integer.parseInt(c.getString(2));
        id_kind = c.getString(3);
        duration = Integer.parseInt(c.getString(4));
        name = c.getString(5);
        annotation = c.getString(6);
        extra_info = c.getString(7);
        schedule = c.getString(8);
        program = c.getString(9);
        composition = c.getString(10);
        transport = c.getString(11);
        transfer = c.getString(12);
        residence = c.getString(13);
        excursions = c.getString(14);
        services = c.getString(15);
        id_status = Integer.parseInt(c.getString(16));
        img = c.getString(17);
        try {
            price =  SettingsActivity.convert(c.getString(19), settings.getString("currency_short_name", null), new DBHelper(a))
                    * Double.parseDouble(c.getString(18));
            currency = settings.getString("currency_short_name", null);
            if (price == 0 && c.getDouble(18)!=0) throw new Exception();
        }
        catch (Exception e){
            price = Double.parseDouble(c.getString(18));
            currency = c.getString(19);
        }
        period_from = make_date(c.getString(20));
        period_to = make_date(c.getString(21));
        id_cities = c.getString(22);
        id_comment = c.getString(23);
        days = c.getString(24);

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
