package com.youtravel;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Andrew-PC on 24.10.2016.
 */
public class Excursion implements Serializable {
    int id;
    int id_country;
    int id_city;
    String excursion_type;
    Boolean individual;
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
    String location;
    String latitude;
    String longitude;
    ArrayList<Date> date;
    String id_comment;

        public Excursion(Cursor c, Context a, SharedPreferences settings){
            id = Integer.parseInt(c.getString(0));
            id_country = Integer.parseInt(c.getString(1));
            id_city = Integer.parseInt(c.getString(2));
            excursion_type = c.getString(3);
            individual = Boolean.parseBoolean(c.getString(4));
            duration = Integer.parseInt(c.getString(5));
            name = c.getString(6);
            annotation = c.getString(7);
            description = c.getString(8);
            extra_info = c.getString(9);
            html = c.getString(10);
            id_status = Integer.parseInt(c.getString(11));
            link = c.getString(12);
            img = c.getString(13);
            try {
                price =  SettingsActivity.convert(c.getString(15), settings.getString("currency_short_name", null), new DBHelper(a))
                        * Double.parseDouble(c.getString(14));
                currency = settings.getString("currency_short_name", null);
                if (price == 0 && c.getDouble(14)!=0) throw new Exception();
            }
            catch (Exception e){
                price = Double.parseDouble(c.getString(14));
                currency = c.getString(15);
            }
            location = c.getString(16);
            latitude = c.getString(17);
            longitude = c.getString(18);
            date = Tour.make_date_list(c.getString(19));
            id_comment = c.getString(20);
        }



}

