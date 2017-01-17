package com.youtravel;


import android.database.Cursor;

public class Country {

    int id;
    String name;
    String annotation;
    String description;
    String html;
    String latitude;
    String longitude;
    int id_status;
    String link;
    String img;

    public Country(Cursor c){
        id = Integer.parseInt(c.getString(0));
        name = c.getString(1);
        annotation = c.getString(2);
        description = c.getString(3);
        html = c.getString(4);
        latitude = c.getString(5);
        longitude = c.getString(6);
        id_status = Integer.parseInt(c.getString(7));
        link = c.getString(8);
        img = c.getString(9);
    }

}
