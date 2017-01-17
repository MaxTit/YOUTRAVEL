package com.youtravel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    private class Cur{
        ArrayList<Integer> id;
        ArrayList<String> full_name;

        public Cur(){
            id = new ArrayList<>();
            full_name = new ArrayList<>();
        }
    }

    static DBHelper dbHelper;
    static SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        dbHelper = new DBHelper(this);
        settings = getApplicationContext().getSharedPreferences("my_data", 0);
        init_interface();
        fillContacts();
        makeCurrencyPicker(getArray());
        makeInfoAboutNew();
    }



    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = settings.edit();
        if (current_id != -1){
            Cursor c;
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            c = db.rawQuery("SELECT short_name FROM currency WHERE id = " + current_id, new String[]{});
            c.moveToFirst();
            if (c.getCount() == 1){
                editor.putString("currency_short_name", c.getString(0));
                editor.putInt("currency_position", spinner.getSelectedItemPosition());
            }
            c.close();
        }
        editor.putBoolean("push_about_new", !push_new.isChecked());
        editor.apply();
    }

    private Cur getArray(){
        Cursor c;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cur answer = new Cur();
        c = db.rawQuery("SELECT name, short_name, id FROM currency ORDER BY id ASC", new String[]{});
        if (c.moveToFirst() && c.getCount() > 0){
            c.moveToFirst();
            while (!c.isAfterLast()) {
                answer.full_name.add(c.getString(0) + " (" + c.getString(1) + ")");
                answer.id.add(c.getInt(2));
                c.moveToNext();
            }
        }
        c.close();
        return answer;
    }




    TextView name, email, phone;
    CheckBox push_new;
    Spinner spinner;
    static int current_id = -1;

    private void fillContacts() {
        name = (TextView) findViewById(R.id.name);
        email = (TextView) findViewById(R.id.email);
        phone = (TextView) findViewById(R.id.phone);

        settings = getApplicationContext().getSharedPreferences("my_data", 0);

        name.setText(settings.getString("name_user", null));
        email.setText(settings.getString("email_user", null));
        phone.setText(settings.getString("phone_user", null));
    }

    private void makeCurrencyPicker(Cur currency) {
        spinner = (Spinner) findViewById(R.id.currency);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currency.full_name);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setPrompt("Выберите валюту:");
        spinner.setOnItemSelectedListener(new CustomizedItemSelectedListener(currency.id) {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                current_id = id_cur.get(position);
            }
        });
        spinner.setSelection(settings.getInt("currency_position", 0));
    }

    private void makeInfoAboutNew() {
        push_new = (CheckBox) findViewById(R.id.push_about_new);
        push_new.setChecked(!settings.getBoolean("push_about_new", false));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_favorite).setIcon(R.drawable.ic_search);
        return true;
    }

    private void init_interface() {
        final MainMenu menu = new MainMenu(this);
        menu.myToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_favorite: {
                        Intent intent = new Intent(SettingsActivity.this, CatalogueActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public static double convert(String from, String to, DBHelper dbHelper){
        Cursor c = null;
        Double multiplier = 1.0;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            c = db.rawQuery("SELECT multiplier FROM currency_data WHERE (cur_from = ? AND cur_to = ?)", new String[]{from, to});
            if (c.getCount() > 0) {
                c.moveToFirst();
                multiplier = c.getDouble(0);
            }
            else multiplier = 0.0;
        }
        catch (Exception e) {
            Log.e("CONVERTING_ERROR", "FAILED TO CONVERT " + from + " TO " + to);
        }
        c.close();
        return multiplier;
    }
}
