package com.youtravel;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
      // ����������� �����������
      super(context.getApplicationContext(), "myDB", null, 16); // !! increase last argument value to perform update of data base on devices !!
    }
    @Override
    public void onOpen(SQLiteDatabase database) {
    if(!database.isOpen()) {
    SQLiteDatabase.openDatabase(database.getPath(), null, SQLiteDatabase.NO_LOCALIZED_COLLATORS |
    SQLiteDatabase.CREATE_IF_NECESSARY);
    }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    	try
    	{
    		db.execSQL("create TABLE IF NOT EXISTS countries ("
    	             + "id integer,"
    	             + "name text,"
    	             + "annotation text,"
    	             + "description text,"
    	             + "html text,"
    	             + "latitude text,"
    	             + "longitude text,"
    	             + "id_status integer,"
    	             + "link text,"
    	             + "img text,"
					 + "date_update DATETIME" + ");");

			db.execSQL("create TABLE IF NOT EXISTS currency ("
					 + "id integer,"
					 + "name text,"
					 + "short_name text,"
					 + "date_update DATETIME" + ");");

			db.execSQL("create TABLE IF NOT EXISTS currency_data ("
					+ "id integer,"
					+ "cur_from text,"
					+ "cur_to text,"
					+ "multiplier real,"
					+ "date_update DATETIME" + ");");

    		db.execSQL("create TABLE IF NOT EXISTS cities ("
    	             + "id integer,"
    	             + "id_country integer,"
    	             + "name text,"
    	             + "annotation text,"
    	             + "description text,"
    	             + "html text,"
    	             + "latitude text,"
    	             + "longitude text,"
    	             + "id_status integer,"
    	             + "link text,"
    	             + "img text,"
    	             + "id_comment text,"
					 + "date_update DATETIME" + ");");

    		db.execSQL("create TABLE IF NOT EXISTS objects ("
    	             + "id integer,"
    	             + "id_country integer,"
    	             + "id_city integer,"
    	             + "name text,"
    	             + "annotation text,"
    	             + "description text,"
    	             + "html text,"
    	             + "latitude text,"
    	             + "longitude text,"
    	             + "id_status integer,"
    	             + "link text,"
    	             + "img text,"
    	             + "extra_info text,"
    	             + "mail text,"
    	             + "t_number text,"
    	             + "web_site text,"
    	             + "object_type text,"
    	             + "children_info text,"
    	             + "id_comment text,"
					 + "date_update DATETIME" + ");");

    		db.execSQL("create TABLE IF NOT EXISTS events ("
    	             + "id integer,"
    	             + "event_type text,"
					 + "name text,"
    	             + "annotation text,"
    	             + "description text,"
    	             + "extra_info text,"
    	             + "html text,"
    	             + "id_status integer,"
    	             + "link text,"
    	             + "img text,"
    	             + "price real,"
    	             + "currency text,"
    	             + "location_and_time text,"
					 + "id_country text,"
					 + "id_city text,"
    	             + "id_comment text,"
					 + "date_update DATETIME" + ");");

    		db.execSQL("create TABLE IF NOT EXISTS journey_kinds ("
    	             + "id integer,"
    	             + "name text,"
    	             + "annotation text,"
    	             + "description text,"
    	             + "html text,"
    	             + "id_status integer,"
    	             + "link text,"
    	             + "img text,"
					 + "date_update DATETIME" + ");");

    		db.execSQL("create TABLE IF NOT EXISTS services ("
    	             + "id integer,"
    	             + "name text,"
    	             + "annotation text,"
    	             + "description text,"
    	             + "extra_info text,"
    	             + "html text,"
    	             + "id_status integer,"
    	             + "link text,"
    	             + "img text,"
    	             + "price real,"
    	             + "currency text,"
    	             + "id_comment text,"
					 + "date_update DATETIME" + ");");

    		db.execSQL("create TABLE IF NOT EXISTS excursions ("
    	             + "id integer,"
    	             + "id_country integer,"
    	             + "id_city integer,"
    	             + "excursion_type text,"
    	             + "individual bool,"
    	             + "duration integer,"
    	             + "name text,"
    	             + "annotation text,"
    	             + "description text,"
    	             + "extra_info text,"
    	             + "html text,"
    	             + "id_status integer,"
    	             + "link text,"
    	             + "img text,"
    	             + "price real,"
    	             + "currency text,"
    	             + "location text,"
    	             + "latitude text,"
    	             + "longitude text,"
    	             + "date DATE,"
    	             + "id_comment text,"
					 + "date_update DATETIME" + ");");

			db.execSQL("create TABLE IF NOT EXISTS tours ("
    	             + "id integer,"
    	             + "id_country integer,"
    	             + "id_city integer,"
					 + "id_kind text,"
    	             + "duration integer,"
    	             + "name text,"
    	             + "annotation text,"
    	             + "description text,"
    	             + "extra_info text,"
    	             + "html text,"
    	             + "id_status integer,"
    	             + "link text,"
    	             + "img text,"
    	             + "price real,"
    	             + "currency text,"
    	             + "date DATE,"
    	             + "id_cities text,"
    	             + "start_point text,"
    	             + "id_comment text,"
					 + "date_update DATETIME" + ");");

    		db.execSQL("create TABLE IF NOT EXISTS comments ("
    	             + "id integer,"
    	             + "author text,"
    	             + "date DATETIME,"
    	             + "description text,"
    	             + "rate integer,"
    	             + "id_status integer,"
    	             + "link text,"
    	             + "img text,"
					 + "date_update DATETIME" + ");");

    		db.execSQL("create TABLE IF NOT EXISTS article ("
    	             + "id integer,"
    	             + "date DATETIME,"
    	             + "annotation text,"
    	             + "id_status integer,"
    	             + "link text,"
    	             + "img text,"
					 + "date_update DATETIME" + ");");

    		db.execSQL("create TABLE IF NOT EXISTS chat ("
	                 + "id integer,"
	                 + "id_order integer,"
	                 + "type text,"
	                 + "id_source integer,"
	                 + "author text,"
	                 + "date_update DATETIME" +");");

			db.execSQL("create TABLE IF NOT EXISTS chat_mes ("
					+ "id integer,"
					+ "id_chat integer,"
					+ "id_member integer,"
					+ "author text,"
					+ "message text,"
					+ "date_update DATETIME" +");");


    		Log.d("Endcreate", "end");
    	} catch(Exception e){e.printStackTrace();}
    	
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	// Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS countries");
        db.execSQL("DROP TABLE IF EXISTS cities");
        db.execSQL("DROP TABLE IF EXISTS objects");
        db.execSQL("DROP TABLE IF EXISTS events");
        db.execSQL("DROP TABLE IF EXISTS services");
        db.execSQL("DROP TABLE IF EXISTS excursions");
        db.execSQL("DROP TABLE IF EXISTS tours");
        db.execSQL("DROP TABLE IF EXISTS comments");
        db.execSQL("DROP TABLE IF EXISTS article");
        db.execSQL("DROP TABLE IF EXISTS chat");
		db.execSQL("DROP TABLE IF EXISTS chat_mes");
        db.execSQL("DROP TABLE IF EXISTS currency");
		db.execSQL("DROP TABLE IF EXISTS currency_data");
		db.execSQL("DROP TABLE IF EXISTS journey_kinds");
        // Create tables again
        onCreate(db);

    }

	
  }
