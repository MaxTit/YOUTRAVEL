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


public class CatalogueActivity extends AppCompatActivity {

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
    static ArrayList<Integer> duration_filter;
    static ArrayList<Integer> price_filter;
    static ArrayList<Date> date_filter;
    static int country_last_pos;
    static int kind_last_pos;
    static int city_last_pos;
    static boolean first_time;
    static String fromCountries = null;
    static String fromJK = null;

    enum Table{
        JOURNEY_KINDS, CITIES, COUNTRIES
    }

    Spinner spinner_country;
    Spinner spinner_city;
    Spinner spinner_kind;
    static DBHelper dbHelper;
 //   CrystalRangeSeekbar rangeSeekbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_catalogue);
        new MainMenu(this, "Каталог туров");
        dbHelper = new DBHelper(this);
    //    rangeSeekbar = (CrystalRangeSeekbar) findViewById(R.id.rangeSeekbar1);
        spinner_country = (Spinner) findViewById(R.id.spinner);
        spinner_city = (Spinner) findViewById(R.id.spinner_city);
        spinner_kind = (Spinner) findViewById(R.id.spinner_kind);
///////////////////////////



        first_time = true;
        make_country_filter(get_array(Table.COUNTRIES, null));
        make_kind_filter(get_array(Table.JOURNEY_KINDS, null));
        make_duration_filter();
        make_price_filter();
        make_date_filter();


//        if (!duration_filter.isEmpty()) {
//            rangeSeekbar.setMaxStartValue(duration_filter.get(0));
//            rangeSeekbar.setMinStartValue(duration_filter.get(1));
//            Log.d("","__dur");
//        }
//        else {
//            duration_filter.add(0,0);
//            duration_filter.add(1,50);
//        }


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
                        " WHERE id = (SELECT id_country FROM tours WHERE countries.id = id_country) ORDER BY name ASC", new String[]{});
                index = 1;
                break;
            case CITIES:
                index = 2;
                if (temp != null) {
                    c = db.rawQuery("SELECT * FROM cities WHERE (id_country = -1 " + put_country_or(temp)+"" +
                            " AND id = (SELECT id_city FROM tours WHERE cities.id = id_city)) ORDER BY name ASC", new String[]{});
                }
                else {
                    c = db.rawQuery("SELECT * FROM cities WHERE id = (SELECT id_city FROM tours WHERE cities.id = id_city) ORDER BY name ASC", new String[]{});
                }
                break;
            case JOURNEY_KINDS:
                c = db.rawQuery("SELECT * FROM journey_kinds" +
                        " WHERE id = (SELECT id_kind FROM tours WHERE journey_kinds.id = id_kind) ORDER BY name ASC", new String[]{});
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
                    CatalogueActivity.country_filter = new ArrayList<>();
                    CatalogueActivity.country_filter.add(id_data.get(position));
                }
                else{
                    CatalogueActivity.country_filter = null;
                }
                country_last_pos = position;
                if (CatalogueActivity.first_time) {
                    make_city_filter(get_array(Table.CITIES, CatalogueActivity.country_filter), false);
                    first_time = false;
                }
                else
                    make_city_filter(get_array(Table.CITIES, CatalogueActivity.country_filter), true);

            }
        });
        first_time = true;
        if (fromCountries!=null){
            spinner_country.setSelection(adapter.getPosition(fromCountries));}
        else
            spinner_country.setSelection(country_last_pos);
        fromCountries = null;
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
                    CatalogueActivity.city_filter = new ArrayList<>();
                    CatalogueActivity.city_filter.add(id_data.get(position));
                }
                else CatalogueActivity.city_filter = null;
                city_last_pos = position;
            }
        });
        if (update) spinner_city.setSelection(0);
        else spinner_city.setSelection(city_last_pos);
    }

    private void make_kind_filter(Record record){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, record.date);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_kind.setAdapter(adapter);
        spinner_kind.setPrompt("Выберите вид отдыха");
        spinner_kind.setOnItemSelectedListener(new CustomizedItemSelectedListener(record.id_date, null) {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                if (position!=0) {
                    CatalogueActivity.kind_filter = new ArrayList<>();
                    CatalogueActivity.kind_filter.add(id_data.get(position));
                }
                else CatalogueActivity.kind_filter = null;
                kind_last_pos = position;
            }
        });
        if (fromJK!=null)
            spinner_kind.setSelection(adapter.getPosition(fromJK));
        else
            spinner_kind.setSelection(kind_last_pos);
        fromJK = null;
    }

    private void make_duration_filter(){

        final EditText tvMin = (EditText) findViewById(R.id.textMin1);
        final EditText tvMax = (EditText) findViewById(R.id.textMax1);
        if ((duration_filter != null) && !duration_filter.isEmpty()) {
            try{
                tvMin.setText(duration_filter.get(0)+"");
                tvMax.setText(duration_filter.get(1)+"");
            }
            catch (NullPointerException n) {}
        }
        else{
            duration_filter = new ArrayList<>();
            duration_filter.add(0,0);
            duration_filter.add(1,50);
        }
        //rangeSeekbar.setMinValue(0);
        Log.d("","__dur");
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

    private void make_date_filter(){
        final DatePicker tvMin = (DatePicker) findViewById(R.id.textMin3);
        final DatePicker tvMax = (DatePicker) findViewById(R.id.textMax3);
        if (date_filter != null && !date_filter.isEmpty()) {
            try{
                Log.d("","+-+-+-="+(date_filter.get(1).getYear()-1900));
                Calendar cal = Calendar.getInstance();
                cal.setTime(date_filter.get(0));
                tvMin.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                cal.setTime(date_filter.get(1));
                tvMax.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            }
            catch (NullPointerException n) {}
        }
        else {
            date_filter = new ArrayList<>();
            date_filter.add(0,new Date());
            date_filter.add(1,new Date());
            Calendar cal = Calendar.getInstance();
            cal.setTime(date_filter.get(1));
            tvMin.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            tvMax.updateDate(cal.get(Calendar.YEAR) + 1, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
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
        CatalogueActivity.country_filter = null;
        CatalogueActivity.kind_filter = null;
        CatalogueActivity.city_filter = null;
        CatalogueActivity.duration_filter = null;
        CatalogueActivity.price_filter = null;
        CatalogueActivity.date_filter = null;
        TourFilteredActivity.country_filter = null;
        TourFilteredActivity.kind_filter = null;
        TourFilteredActivity.city_filter = null;
        TourFilteredActivity.duration_filter = null;
        TourFilteredActivity.price_filter = null;
        TourFilteredActivity.date_filter = null;
        country_last_pos = 0;
        kind_last_pos = 0;
        city_last_pos = 0;
        first_time = true;
        make_country_filter(get_array(Table.COUNTRIES, null));
        make_kind_filter(get_array(Table.JOURNEY_KINDS, null));
        make_duration_filter();
        make_price_filter();
        make_date_filter();
        get_date_filter();
    }

    final void get_duration_filter(){
        duration_filter.set(0,Integer.parseInt(((EditText) findViewById(R.id.textMin1)).getText().toString()));
        duration_filter.set(1,Integer.parseInt(((EditText) findViewById(R.id.textMax1)).getText().toString()));
    }

    final void get_price_filter(){
        price_filter.set(0,Integer.parseInt(((EditText) findViewById(R.id.textMin2)).getText().toString()));
        price_filter.set(1,Integer.parseInt(((EditText) findViewById(R.id.textMax2)).getText().toString()));
    }

    final void get_date_filter(){
        Date s = new Date(((DatePicker) findViewById(R.id.textMin3)).getYear()-1900,
                ((DatePicker) findViewById(R.id.textMin3)).getMonth(),
                ((DatePicker) findViewById(R.id.textMin3)).getDayOfMonth());
        date_filter.set(0, s);
        s = new Date(((DatePicker) findViewById(R.id.textMax3)).getYear()-1900,
                ((DatePicker) findViewById(R.id.textMax3)).getMonth(),
                ((DatePicker) findViewById(R.id.textMax3)).getDayOfMonth());
        date_filter.set(1, s);
    }

    private void make_ok(){
        Button ok = (Button)findViewById(R.id.ok_button);
        ok.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                get_duration_filter();
                get_price_filter();
                get_date_filter();
                Intent intent = new Intent(CatalogueActivity.this, TourFilteredActivity.class);
                TourFilteredActivity.country_filter = CatalogueActivity.country_filter;
                TourFilteredActivity.kind_filter = CatalogueActivity.kind_filter;
                TourFilteredActivity.city_filter = CatalogueActivity.city_filter;
                TourFilteredActivity.duration_filter = duration_filter;
                TourFilteredActivity.price_filter = price_filter;
                TourFilteredActivity.date_filter = date_filter;
          //      duration_filter.set(0, Integer.parseInt(String.valueOf(rangeSeekbar.getSelectedMinValue())));
          //      duration_filter.set(1, Integer.parseInt(String.valueOf(rangeSeekbar.getSelectedMaxValue())));
           //     try {
            //        Log.d("DURATION MIN VALUE = ", duration_filter.get(0) + "");
            //        Log.d("DURATION MAX VALUE = ", duration_filter.get(1) + "");
           //     }
           //     catch (NullPointerException n){}
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }

    private void make_cancel() {
        Button cancel = (Button) findViewById(R.id.cancel_button);
        cancel.setOnClickListener(new View.OnClickListener() {
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
                Intent intent = new Intent(CatalogueActivity.this, TourFilteredActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }




}
