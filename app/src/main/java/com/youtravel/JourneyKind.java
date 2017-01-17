package com.youtravel;

import android.database.Cursor;

/**
 * Created by Andrew-PC on 15.09.2016.
 */
public class JourneyKind {

    int id;
    String name;
    String annotation;
    String description;
    String html;
    int id_status;
    String link;
    String img;

    public JourneyKind(Cursor c){
        id = Integer.parseInt(c.getString(0));
        name = c.getString(1);
        annotation = c.getString(2);
        description = c.getString(3);
        html = c.getString(4);
        id_status = Integer.parseInt(c.getString(5));
        link = c.getString(6);
        img = c.getString(7);
    }

}
