package com.youtravel;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class ExcursionFiltersActivity extends AppCompatActivity {

    private class Record{
        String[] date;
        ArrayList<String> id_date;
        public Record(){
            id_date = new ArrayList<>();
        }
    }

    static ArrayList<String> country_filter;
    static ArrayList<String> city_filter;
    static ArrayList<String> kind_filter;
    static ArrayList<Integer> duration_filter = new ArrayList<>();
    static ArrayList<Integer> price_filter = new ArrayList<>();
    static ArrayList<Date> date_filter = new ArrayList<>();
    static int country_last_pos;
    static int kind_last_pos;
    static int city_last_pos;
    static boolean first_time;

    enum Table{
        JOURNEY_KINDS, CITIES, COUNTRIES
    }

    Spinner spinner_country;
    Spinner spinner_city;
    Spinner spinner_kind;
    static DBHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_excursion_filters);
        new MainMenu(this);
        dbHelper = new DBHelper(this);
        spinner_country = (Spinner) findViewById(R.id.spinner);
        spinner_city = (Spinner) findViewById(R.id.spinner_city);



        first_time = true;
        make_country_filter(get_array(Table.COUNTRIES, null));
        make_price_filter();



        make_ok();
        make_cancel();
        make_show_all();
/////////////////////////

    }

    private Record get_array(Table table, ArrayList<String> temp){
        ArrayList<String> list = new ArrayList<>();
        Cursor c = null;
        int index = 0;
        Record elements = new Record();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch (table) {
            case COUNTRIES:
                c = db.rawQuery("SELECT * FROM countries" +
                        " WHERE id = (SELECT id_country FROM excursions WHERE countries.id = id_country) ORDER BY name ASC", new String[]{});
                index = 1;
                break;
            case CITIES:
                index = 2;
                if (temp != null) {
                    c = db.rawQuery("SELECT * FROM cities WHERE (id_country = -1 " + put_country_or(temp)+") ORDER BY name ASC", new String[]{});
                    Log.d("","SELECT * FROM cities WHERE (id_country = -1 " + put_country_or(temp)+")");
                }
                else {
                    c = db.rawQuery("SELECT * FROM cities " +
                            " WHERE id = (SELECT id_city FROM excursions WHERE cities.id = id_city) ORDER BY name ASC", new String[]{});
                }
                break;
            case JOURNEY_KINDS:
                c = db.rawQuery("SELECT * FROM journey_kinds " +
                        " WHERE id = (SELECT id_kind FROM excursions WHERE journey_kinds.id = id_kind) ORDER BY name ASC", new String[]{});
                index = 1; // index of name (KOSTYLIK :))
                break;
        }
        list.add("Все");
        elements.id_date.add("-1");
        if (c!=null && c.moveToFirst() && c.getCount()>0 ) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                list.add(c.getString(index));
                elements.id_date.add(c.getString(0));
                c.moveToNext();
            }
        }

        if (c != null) c.close();
        db.close();
        elements.date = new String[list.size()];
        for (int i = 0; i<list.size(); i++){
            elements.date[i] = list.get(i);
        }
        return elements;

    }

    private String put_country_or(ArrayList<String> list){
        String result = "";
        for (String i: list) {
            result += "OR id_country = " + i + " ";
        }
        return result;
    }

    private void make_country_filter(Record record){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, record.date);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_country.setAdapter(adapter);
        spinner_country.setPrompt("Выберите страну");
        spinner_country.setOnItemSelectedListener(new CustomizedItemSelectedListener(record.id_date, spinner_city) {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                if (position != 0) {
                    ExcursionFiltersActivity.country_filter = new ArrayList<>();
                    ExcursionFiltersActivity.country_filter.add(id_data.get(position));
                }
                else{
                    ExcursionFiltersActivity.country_filter = null;
                }
                country_last_pos = position;
                if (ExcursionFiltersActivity.first_time) {
                    make_city_filter(get_array(Table.CITIES, ExcursionFiltersActivity.country_filter), false);
                    first_time = false;
                }
                else
                    make_city_filter(get_array(Table.CITIES, ExcursionFiltersActivity.country_filter), true);

            }
        });
        first_time = true;
        spinner_country.setSelection(country_last_pos);
    }

    private void make_city_filter(Record record, boolean update){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, record.date);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_city.setAdapter(adapter);
        spinner_city.setPrompt("Выберите город");
        spinner_city.setOnItemSelectedListener(new CustomizedItemSelectedListener(record.id_date, null) {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                if (position!=0) {
                    ExcursionFiltersActivity.city_filter = new ArrayList<>();
                    ExcursionFiltersActivity.city_filter.add(id_data.get(position));
                }
                else ExcursionFiltersActivity.city_filter = null;
                city_last_pos = position;
            }
        });
        if (update) spinner_city.setSelection(0);
        else spinner_city.setSelection(city_last_pos);
    }


    private void make_price_filter(){
        final EditText tvMin = (EditText) findViewById(R.id.textMin2);
        final EditText tvMax = (EditText) findViewById(R.id.textMax2);
        if (price_filter != null && !price_filter.isEmpty()) {
            try{
                tvMin.setText(price_filter.get(0)+"");
                tvMax.setText(price_filter.get(1)+"");
            }
            catch (NullPointerException n) {}
        }
        else{
            price_filter = new ArrayList<>();
            price_filter.add(0,0);
            price_filter.add(1,50000);
            tvMin.setText(price_filter.get(0)+"");
            tvMax.setText(price_filter.get(1)+"");
        }
        //rangeSeekbar.setMinValue(0);
        Log.d("","__price");
        //8rangeSeekbar.setMaxValue(50);
//        rangeSeekbar.setMinStartValue(20).setMaxStartValue(80).apply();
//
//        // set listener
//        rangeSeekbar.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
//            @Override
//            public void valueChanged(Number minValue, Number maxValue) {
//                tvMin.setText(String.valueOf(minValue));
//                tvMax.setText(String.valueOf(maxValue));
//            }
//        });
    }



    public final void cancel(){
        ExcursionFiltersActivity.country_filter = null;
        ExcursionFiltersActivity.city_filter = null;
        ExcursionFiltersActivity.price_filter = null;
        ExcursionsActivity.country_filter = null;
        ExcursionsActivity.city_filter = null;
        ExcursionsActivity.price_filter = null;
        country_last_pos = 0;
        city_last_pos = 0;
        first_time = true;
        make_country_filter(get_array(Table.COUNTRIES, null));
        make_price_filter();

    }


    final void get_price_filter(){
        price_filter.set(0,Integer.parseInt(((EditText) findViewById(R.id.textMin2)).getText().toString()));
        price_filter.set(1,Integer.parseInt(((EditText) findViewById(R.id.textMax2)).getText().toString()));
    }


    private void make_ok(){
        Button ok = (Button)findViewById(R.id.ok_button);
        ok.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                get_price_filter();
                Intent intent = new Intent(ExcursionFiltersActivity.this, ExcursionsActivity.class);
                ExcursionsActivity.country_filter = ExcursionFiltersActivity.country_filter;
                ExcursionsActivity.city_filter = ExcursionFiltersActivity.city_filter;
                ExcursionsActivity.price_filter = price_filter;
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }

    private void make_cancel(){
        Button ok = (Button)findViewById(R.id.cancel_button);
        ok.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
    }

    private void make_show_all() {
        Button show_all = (Button) findViewById(R.id.show_all_button);
        show_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
                Intent intent = new Intent(ExcursionFiltersActivity.this, ExcursionsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }


}
