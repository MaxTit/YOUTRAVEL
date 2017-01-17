package com.youtravel;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;

import java.io.Serializable;

/**
 * Created by Andrew-PC on 24.10.2016.
 */
public class Service implements Serializable {
    int id;
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
    String id_comment;

    public Service(Cursor c, Context a, SharedPreferences settings){
        id = Integer.parseInt(c.getString(0));
        name = c.getString(1);
        annotation = c.getString(2);
        description = c.getString(3);
        extra_info = c.getString(4);
        html = c.getString(5);
        id_status = Integer.parseInt(c.getString(6));
        link = c.getString(7);
        img = c.getString(8);
        try {
            price =  SettingsActivity.convert(c.getString(10), settings.getString("currency_short_name", null), new DBHelper(a))
                    * Double.parseDouble(c.getString(9));
            currency = settings.getString("currency_short_name", null);
            if (price == 0 && c.getDouble(9)!=0) throw new Exception();
        }
        catch (Exception e){
            price = Double.parseDouble(c.getString(9));
            currency = c.getString(10);
        }
        id_comment = c.getString(11);
    }
}
