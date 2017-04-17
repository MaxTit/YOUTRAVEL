package com.youtravel;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyRep;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.preference.DialogPreference;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class StartActivityAlternative extends AppCompatActivity {


    public static class news{
        ArrayList<Integer> drained_new;
        ArrayList<Integer> drained_updated;

        public news(ArrayList<Integer> drained_new, ArrayList<Integer> drained_updated){
            this.drained_new = drained_new;
            this.drained_updated = drained_updated;
        }
    }

    public static int mProgressStatus = 0;
    ProgressBar mProgress;
    ////////
    final Handler handler_news = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();
            Log.i("HANDLED","HANDLED");
            switch (data.getString("context", "")){
                case "туров":
                    if (data.getIntegerArrayList("new").size() + data.getIntegerArrayList("updated").size() > 0)
                        HomeActivity.setFreshTours(data);
                    break;
                case "услуг":
                    if (data.getIntegerArrayList("new").size() + data.getIntegerArrayList("updated").size() > 0)
                        HomeActivity.setFreshServices(data);
                    break;
                case "мероприятий":
                    if (data.getIntegerArrayList("new").size() + data.getIntegerArrayList("updated").size() > 0)
                        HomeActivity.setFreshEvents(data);
                    break;
                case "экскурсий":
                    if (data.getIntegerArrayList("new").size() + data.getIntegerArrayList("updated").size() > 0)
                        HomeActivity.setFreshExcursions(data);
                    break;
            }
            super.handleMessage(msg);
        }
    };
    //////////////

    public class DownloadFileFromURL extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... f_url) {
            try {
                news fresh;

                update_countries();
                publishProgress(mProgressStatus);

                update_cities();
                publishProgress(mProgressStatus);

                update_objects();
                publishProgress(mProgressStatus);

                // updating events..
                Message msg;
                Bundle context = new Bundle();
                msg = handler_news.obtainMessage();
                msg.what = 1;
                fresh = update_events();
                context.putString("context",   "мероприятий");
                context.putIntegerArrayList("new", fresh.drained_new);
                context.putIntegerArrayList("updated", fresh.drained_updated);
                msg.setData(context);
                handler_news.sendMessage(msg);
                publishProgress(mProgressStatus);


                // updating journey_kinds
                update_journey_kinds();
                publishProgress(mProgressStatus);

                // updating services
                context = new Bundle();
                msg = handler_news.obtainMessage();
                msg.what = 1;
                fresh = update_services();
                context.putString("context",   "услуг");
                context.putIntegerArrayList("new", fresh.drained_new);
                context.putIntegerArrayList("updated", fresh.drained_updated);
                msg.setData(context);
                handler_news.sendMessage(msg);
                publishProgress(mProgressStatus);

                // updating excursions
                context = new Bundle();
                msg = handler_news.obtainMessage();
                msg.what = 1;
                fresh = update_excursions();
                context.putString("context",   "экскурсий");
                context.putIntegerArrayList("new", fresh.drained_new);
                context.putIntegerArrayList("updated", fresh.drained_updated);
                msg.setData(context);
                handler_news.sendMessage(msg);
                publishProgress(mProgressStatus);


                // updating tours
                context = new Bundle();
                msg = handler_news.obtainMessage();
                msg.what = 1;
                fresh = update_tours();
                context.putString("context",   "туров");
                context.putIntegerArrayList("new", fresh.drained_new);
                context.putIntegerArrayList("updated", fresh.drained_updated);
                msg.setData(context);
                handler_news.sendMessage(msg);
                publishProgress(mProgressStatus);
                update_comments();

                update_article();
                publishProgress(mProgressStatus);

                if (settings.getString("email_user", null) != null) {
                    update_chat(settings.getString("id_user", null));
                    update_chat_mes(settings.getString("id_user", null));
                    publishProgress(mProgressStatus);
                }

                update_currency();
                update_currency_data();


            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            mProgress.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String file_url) {
            Intent intent = new Intent(StartActivityAlternative.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }


    static DBHelper dbHelper;
    static String pathtocache;
    static SharedPreferences settings;

    public static String server = "http://youtravel-su.1gb.ru/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_alternative);
        mProgress = (ProgressBar) findViewById(R.id.progressBar);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        settings = getApplicationContext().getSharedPreferences("my_data", 0);

        if (settings.getString("currency_short_name", null) == null){

        }
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        dbHelper = new DBHelper(this);
        pathtocache = getBaseContext().getCacheDir().toString();
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        final boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (settings.getString("email_user", null) != null) {
            initEntrance(isConnected);
        }
        else {
            initEntrance(isConnected); //isn't logged in..
        }


    }

    private void initEntrance(Boolean isConnected){
        if (isConnected) {
            mProgress.setVisibility(ProgressBar.VISIBLE);
            new DownloadFileFromURL().execute();
        } else {
            android.app.AlertDialog.Builder builder = new AlertDialog.Builder(StartActivityAlternative.this);
            AlertDialog dialog = builder.create();
            dialog.setTitle("Отсутствует подключение к Интернету");
            dialog.setButton("Ok", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(StartActivityAlternative.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            });
            dialog.setCancelable(false);
            dialog.show();
        }
    }

    ////////////////////////////////////
    private static void checkImage(String subject, String id_subject, String urls, SQLiteDatabase db){
        for ( String url : urls.split(",")) {
            Cursor c;
            String p_query = "SELECT * FROM images WHERE url = ?";
            c = db.rawQuery(p_query,new String[]{url});
            if (c.getCount() == 0) {
                ContentValues cv = new ContentValues();
                cv.put("subject", subject);
                cv.put("id_subject", id_subject);
                cv.put("url", url);
                cv.put("is_downloaded", -1);
                db.insert("images", null, cv);
            }
            c.close();
        }
    }

    public static int update_countries() {
        //creating counter for new objects
        int drain_counter = 0;
        //creating object for data
        ContentValues cv = new ContentValues();
        //connecting to DB


        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String result = null;
        InputStream is = null;
        StringBuilder sb = null;
        boolean fl = true;
        Cursor c;
        String last_update = null;
        String p_query = "SELECT date_update FROM countries ORDER BY date_update DESC LIMIT 1";
        c = db.rawQuery(p_query, new String[]{});
        if (c.moveToFirst() && c!=null && c.getCount()>0 ) {
            last_update = c.getString(0);
        }
        // http post
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = null;
            httppost = new HttpPost(server + "?signal=Countries");

            if (last_update != null) {
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
                nameValuePair.add(new BasicNameValuePair("date", last_update));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            }

            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();

        } catch (Exception e) {
            fl = false;
            Log.e("log_tag", "Error in http connection" + e.toString());
        }

        //convert response to string
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
            sb = new StringBuilder();
            sb.append(reader.readLine() + "\n");
            String line = "0";

            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            is.close();
            result = sb.toString();

        } catch (Exception e) {
            fl = false;
            Log.e("log_tag", "Error converting result " + e.toString());
        }
        if (fl == true) {
            //paring data
            try {
                if (!result.contains("null")) {
                    Log.d("result",result);
                    JSONArray jArray = new JSONArray(result);
                    JSONObject json_data = null;
                    Log.d("LOG_TAG", "Delete all from table " + "countries");
                    if (last_update == null)
                        db.delete("countries", null, null);
                    if (jArray != null) {
                        for (int i = 0; i < jArray.length(); i++) {
                            json_data = jArray.getJSONObject(i);
                            if (json_data != null) {

                                if (last_update != null)
                                    db.delete("countries", "id == ?", new String[]{json_data.getString("id")});

                                drain_counter++;

                                if (json_data.getString("id") != "null")
                                    cv.put("id", json_data.getInt("id"));

                                if (json_data.getString("name") != "null")
                                    cv.put("name", json_data.getString("name"));

                                if (json_data.getString("annotation") != "null")
                                    cv.put("annotation", json_data.getString("annotation"));

                                if (json_data.getString("description") != "null")
                                    cv.put("description", json_data.getString("description"));

                                if (json_data.getString("html") != "null")
                                    cv.put("html", json_data.getString("html"));

                                if (json_data.getString("latitude") != "null")
                                    cv.put("latitude", json_data.getString("latitude"));

                                if (json_data.getString("longitude") != "null")
                                    cv.put("longitude", json_data.getString("longitude"));

                                if (json_data.getString("id_status") != "null")
                                    cv.put("id_status", json_data.getInt("id_status"));

                                if (json_data.getString("link") != "null")
                                    cv.put("link", json_data.getString("link"));

                                if (json_data.getString("img") != "null")
                                    cv.put("img", json_data.getString("img"));

                                checkImage("county", json_data.getString("id"), json_data.getString("img"), db);

                                if (json_data.getString("date_update") != "null")
                                    cv.put("date_update", json_data.getString("date_update"));

                                    db.insert("countries", null, cv);
                            }

                        }
                    }
                }
            } catch (JSONException e1) {
                Log.e("log_tag", "Error no ff " + e1.toString());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        db.close();
        mProgressStatus +=10;
        return drain_counter;
    }

    public static int update_cities() {
        int drain_counter = 0;
        //creating object for data
        ContentValues cv = new ContentValues();
        //connecting to DB

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String result = null;
        InputStream is = null;
        StringBuilder sb = null;
        Boolean fl = true;
        Cursor c;
        String last_update = null;
        String p_query = "SELECT date_update FROM cities ORDER BY date_update DESC LIMIT 1";
        c = db.rawQuery(p_query, new String[]{});
        if (c.moveToFirst() && c!=null && c.getCount()>0 ) {
            last_update = c.getString(0);
        }
        // http post
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = null;
            httppost = new HttpPost(server + "?signal=Cities");
            if (last_update != null) {
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
                nameValuePair.add(new BasicNameValuePair("date", last_update));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            }
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
        } catch (Exception e) {
            fl = false;
            Log.e("log_tag", "Error in http connection" + e.toString());
        }

        //convert response to string
        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
            sb = new StringBuilder();
            sb.append(reader.readLine() + "\n");

            String line = "0";

            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            is.close();
            result = sb.toString();

        } catch (Exception e) {
            fl = false;
            Log.e("log_tag", "Error converting result " + e.toString());
        }
        if (fl == true) {
            //paring data
            try {
                if (!result.contains("null")) {
                    JSONArray jArray = new JSONArray(result);
                    JSONObject json_data = null;
                    Log.d("LOG_TAG", "Delete all from table " + "cities");
                    if (last_update == null)
                        db.delete("cities", null, null);
                    if (jArray != null) {
                        for (int i = 0; i < jArray.length(); i++) {
                            json_data = jArray.getJSONObject(i);
                            if (json_data != null) {

                                if (last_update != null)
                                    db.delete("cities", "id == ?", new String[]{json_data.getString("id")});

                                if (json_data.getString("id") != "null")
                                    cv.put("id", json_data.getInt("id"));

                                drain_counter++;

                                if (json_data.getString("id_country") != "null")
                                    cv.put("id_country", json_data.getInt("id_country"));

                                if (json_data.getString("name") != "null")
                                    cv.put("name", json_data.getString("name"));

                                if (json_data.getString("annotation") != "null")
                                    cv.put("annotation", json_data.getString("annotation"));

                                if (json_data.getString("description") != "null")
                                    cv.put("description", json_data.getString("description"));

                                if (json_data.getString("html") != "null")
                                    cv.put("html", json_data.getString("html"));

                                if (json_data.getString("latitude") != "null")
                                    cv.put("latitude", json_data.getString("latitude"));

                                if (json_data.getString("longitude") != "null")
                                    cv.put("longitude", json_data.getString("longitude"));

                                if (json_data.getString("id_status") != "null")
                                    cv.put("id_status", json_data.getInt("id_status"));

                                if (json_data.getString("link") != "null")
                                    cv.put("link", json_data.getString("link"));

                                if (json_data.getString("img") != "null")
                                    cv.put("img", json_data.getString("img"));

                                if (json_data.getString("id_comment") != "null")
                                    cv.put("id_comment", json_data.getString("id_comment"));

                                if (json_data.getString("date_update") != "null")
                                    cv.put("date_update", json_data.getString("date_update"));

                                checkImage("city", json_data.getString("id"), json_data.getString("img"), db);

                                    db.insert("cities", null, cv);
                            }

                        }
                    }
                }
            } catch (JSONException e1) {
                Log.e("log_tag", "Error no ff " + e1.toString());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        db.close();
        mProgressStatus += 10;
        return drain_counter;
    }

    public static void update_objects() {
        //creating object for data
        ContentValues cv = new ContentValues();
        //connecting to DB

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String result = null;
        InputStream is = null;
        StringBuilder sb = null;
        Boolean fl = true;
        Cursor c;
        String last_update = null;
        String p_query = "SELECT date_update FROM objects ORDER BY date_update DESC LIMIT 1";
        c = db.rawQuery(p_query, new String[]{});
        if (c.moveToFirst() && c!=null && c.getCount()>0 ) {
            last_update = c.getString(0);
        }
        // http post
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = null;
            httppost = new HttpPost(server + "?signal=Objects");
            if (last_update != null) {
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
                nameValuePair.add(new BasicNameValuePair("date", last_update));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            }
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
        } catch (Exception e) {
            fl = false;
            Log.e("log_tag", "Error in http connection" + e.toString());
        }

        //convert response to string
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
            sb = new StringBuilder();
            sb.append(reader.readLine() + "\n");
            String line = "0";

            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            is.close();
            result = sb.toString();

        } catch (Exception e) {
            fl = false;
            Log.e("log_tag", "Error converting result " + e.toString());
        }
        if (fl == true) {
            //paring data
            try {
                if (!result.contains("null")) {
                    JSONArray jArray = new JSONArray(result);
                    JSONObject json_data = null;
                    Log.d("LOG_TAG", "Delete all from table " + "objects");
                    if (last_update == null)
                        db.delete("objects", null, null);
                    if (jArray != null) {
                        for (int i = 0; i < jArray.length(); i++) {
                            json_data = jArray.getJSONObject(i);
                            if (json_data != null) {

                                if (last_update != null)
                                    db.delete("objects", "id == ?", new String[]{json_data.getString("id")});

                                if (json_data.getString("id") != "null")
                                    cv.put("id", json_data.getInt("id"));

                                if (json_data.getString("id_country") != "null")
                                    cv.put("id_country", json_data.getInt("id_country"));

                                if (json_data.getString("id_city") != "null")
                                    cv.put("id_city", json_data.getInt("id_city"));

                                if (json_data.getString("name") != "null")
                                    cv.put("name", json_data.getString("name"));

                                if (json_data.getString("annotation") != "null")
                                    cv.put("annotation", json_data.getString("annotation"));

                                if (json_data.getString("description") != "null")
                                    cv.put("description", json_data.getString("description"));

                                if (json_data.getString("html") != "null")
                                    cv.put("html", json_data.getString("html"));

                                if (json_data.getString("latitude") != "null")
                                    cv.put("latitude", json_data.getString("latitude"));

                                if (json_data.getString("longitude") != "null")
                                    cv.put("longitude", json_data.getString("longitude"));

                                if (json_data.getString("id_status") != "null")
                                    cv.put("id_status", json_data.getInt("id_status"));

                                if (json_data.getString("link") != "null")
                                    cv.put("link", json_data.getString("link"));

                                if (json_data.getString("img") != "null")
                                    cv.put("img", json_data.getString("img"));

                                if (json_data.getString("extra_info") != "null")
                                    cv.put("extra_info", json_data.getString("extra_info"));

                                if (json_data.getString("mail") != "null")
                                    cv.put("mail", json_data.getString("mail"));

                                if (json_data.getString("t_number") != "null")
                                    cv.put("t_number", json_data.getString("t_number"));

                                if (json_data.getString("web_site") != "null")
                                    cv.put("web_site", json_data.getString("web_site"));

                                if (json_data.getString("object_type") != "null")
                                    cv.put("object_type", json_data.getString("object_type"));

                                if (json_data.getString("children_info") != "null")
                                    cv.put("children_info", json_data.getString("children_info"));

                                if (json_data.getString("id_comment") != "null")
                                    cv.put("id_comment", json_data.getString("id_comment"));

                                if (json_data.getString("date_update") != "null")
                                    cv.put("date_update", json_data.getString("date_update"));

                                    db.insert("objects", null, cv);
                            }

                        }
                    }
                }
            } catch (JSONException e1) {
                Log.e("log_tag", "Error no ff " + e1.toString());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        db.close();
        mProgressStatus += 10;
    }

    public static news update_events() {
        ArrayList<Integer> drain_new = new ArrayList<>();
        ArrayList<Integer> drain_updated = new ArrayList<>();
        //creating object for data
        ContentValues cv = new ContentValues();
        //connecting to DB

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String result = null;
        InputStream is = null;
        StringBuilder sb = null;
        Boolean fl = true;
        Cursor c;
        String last_update = null;
        String p_query = "SELECT date_update FROM events ORDER BY date_update DESC LIMIT 1";
        c = db.rawQuery(p_query, new String[]{});
        if (c.moveToFirst() && c!=null && c.getCount()>0 ) {
            last_update = c.getString(0);
        }

        // http post
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = null;
            httppost = new HttpPost(server + "?signal=Events");
            if (last_update != null) {
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
                nameValuePair.add(new BasicNameValuePair("date", last_update));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            }
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
        } catch (Exception e) {
            fl = false;
            Log.e("log_tag", "Error in http connection" + e.toString());
        }

        //convert response to string
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
            sb = new StringBuilder();
            sb.append(reader.readLine() + "\n");
            String line = "0";

            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            is.close();
            result = sb.toString();

        } catch (Exception e) {
            fl = false;
            Log.e("log_tag", "Error converting result " + e.toString());
        }
        if (fl == true) {
            //paring data
            try {
                if (!result.contains("null")) {
                    JSONArray jArray = new JSONArray(result);
                    JSONObject json_data = null;
                    Log.d("LOG_TAG", "Delete all from table " + "events");
                    if (last_update == null)
                        db.delete("events", null, null);
                    if (jArray != null) {
                        for (int i = 0; i < jArray.length(); i++) {
                            json_data = jArray.getJSONObject(i);
                            if (json_data != null) {

                                if (last_update != null)
                                    db.delete("events", "id == ?", new String[]{json_data.getString("id")});

                                if (json_data.getString("id") != "null")
                                    cv.put("id", json_data.getInt("id"));

                                if (json_data.getString("event_type") != "null")
                                    cv.put("event_type", json_data.getString("event_type"));

                                if (json_data.getString("name") != "null")
                                    cv.put("name", json_data.getString("name"));

                                if (json_data.getString("annotation") != "null")
                                    cv.put("annotation", json_data.getString("annotation"));

                                if (json_data.getString("description") != "null")
                                    cv.put("description", json_data.getString("description"));

                                if (json_data.getString("extra_info") != "null")
                                    cv.put("extra_info", json_data.getString("extra_info"));

                                if (json_data.getString("html") != "null")
                                    cv.put("html", json_data.getString("html"));

                                if (json_data.getString("id_status") != "null")
                                    cv.put("id_status", json_data.getInt("id_status"));

                                if (json_data.getString("link") != "null")
                                    cv.put("link", json_data.getString("link"));

                                if (json_data.getString("img") != "null")
                                    cv.put("img", json_data.getString("img"));

                                if (json_data.getString("price") != "null")
                                    cv.put("price", json_data.getDouble("price"));

                                if (json_data.getString("currency") != "null")
                                    cv.put("currency", json_data.getString("currency"));

                                if (json_data.getString("location_and_time") != "null")
                                    cv.put("location_and_time", json_data.getString("location_and_time"));

                                if (json_data.getString("id_country") != "null")
                                    cv.put("id_country", json_data.getString("id_country"));

                                if (json_data.getString("id_city") != "null")
                                    cv.put("id_city", json_data.getString("id_city"));

                                if (json_data.getString("id_comment") != "null")
                                    cv.put("id_comment", json_data.getString("id_comment"));

                                if (json_data.getString("date_update") != "null")
                                    cv.put("date_update", json_data.getString("date_update"));

                                checkImage("event", json_data.getString("id"), json_data.getString("img"), db);

                                if (last_update == null) {
                                    db.insert("events", null, cv);
                                    drain_new.add(json_data.getInt("id"));
                                }
                                else {
                                    db.insert("events", null, cv);
                                    drain_updated.add(json_data.getInt("id"));
                                }
                            }

                        }
                    }
                }
            } catch (JSONException e1) {
                Log.e("log_tag", "Error no ff " + e1.toString());

            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        db.close();
        mProgressStatus += 10;
        return new news(drain_new, drain_updated);
    }

    public static void update_journey_kinds() {
        //creating object for data
        ContentValues cv = new ContentValues();
        //connecting to DB

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String result = null;
        InputStream is = null;
        StringBuilder sb = null;
        Boolean fl = true;
        Cursor c;
        String last_update = null;
        String p_query = "SELECT date_update FROM journey_kinds ORDER BY date_update DESC LIMIT 1";
        c = db.rawQuery(p_query, new String[]{});
        if (c.moveToFirst() && c!=null && c.getCount()>0 ) {
            last_update = c.getString(0);
        }
        // http post
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = null;
            httppost = new HttpPost(server + "?signal=Journey_kinds");
            if (last_update != null) {
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
                nameValuePair.add(new BasicNameValuePair("date", last_update));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            }
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
        } catch (Exception e) {
            fl = false;
            Log.e("log_tag", "Error in http connection" + e.toString());
        }

        //convert response to string
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
            sb = new StringBuilder();
            sb.append(reader.readLine() + "\n");
            String line = "0";

            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            is.close();
            result = sb.toString();

        } catch (Exception e) {
            fl = false;
            Log.e("log_tag", "Error converting result " + e.toString());
        }
        if (fl) {
            //paring data
            try {
                if (!result.contains("null")) {
                    JSONArray jArray = new JSONArray(result);
                    JSONObject json_data = null;
                    Log.d("LOG_TAG", "Delete all from table " + "journey_kinds");
                    if (last_update == null)
                        db.delete("journey_kinds", null, null);
                    if (jArray != null) {
                        for (int i = 0; i < jArray.length(); i++) {
                            json_data = jArray.getJSONObject(i);
                            if (json_data != null) {

                                if (last_update != null)
                                    db.delete("journey_kinds", "id == ?", new String[]{json_data.getString("id")});

                                if (json_data.getString("id") != "null")
                                    cv.put("id", json_data.getInt("id"));

                                if (json_data.getString("name") != "null")
                                    cv.put("name", json_data.getString("name"));

                                if (json_data.getString("annotation") != "null")
                                    cv.put("annotation", json_data.getString("annotation"));

                                if (json_data.getString("description") != "null")
                                    cv.put("description", json_data.getString("description"));

                                if (json_data.getString("html") != "null")
                                    cv.put("html", json_data.getString("html"));

                                if (json_data.getString("id_status") != "null")
                                    cv.put("id_status", json_data.getInt("id_status"));

                                if (json_data.getString("link") != "null")
                                    cv.put("link", json_data.getString("link"));

                                if (json_data.getString("img") != "null")
                                    cv.put("img", json_data.getString("img"));

                                if (json_data.getString("date_update") != "null")
                                    cv.put("date_update", json_data.getString("date_update"));

                                checkImage("journey_kind", json_data.getString("id"), json_data.getString("img"), db);

                                    db.insert("journey_kinds", null, cv);
                            }

                        }
                    }
                }
            } catch (JSONException e1) {
                Log.e("log_tag", "Error no ff " + e1.toString());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        db.close();
        mProgressStatus += 5;
    }

    public static news update_services() {
        ArrayList<Integer> drain_new = new ArrayList<>();
        ArrayList<Integer> drain_updated = new ArrayList<>();
        //creating object for data
        ContentValues cv = new ContentValues();
        //connecting to DB

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String result = null;
        InputStream is = null;
        StringBuilder sb = null;
        Boolean fl = true;
        Cursor c;
        String last_update = null;
        String p_query = "SELECT date_update FROM services ORDER BY date_update DESC LIMIT 1";
        c = db.rawQuery(p_query, new String[]{});
        if (c.moveToFirst() && c!=null && c.getCount()>0 ) {
            last_update = c.getString(0);
        }
        // http post
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = null;
            httppost = new HttpPost(server + "?signal=Services");
            if (last_update != null) {
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
                nameValuePair.add(new BasicNameValuePair("date", last_update));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            }
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
        } catch (Exception e) {
            fl = false;
            Log.e("log_tag", "Error in http connection" + e.toString());
        }

        //convert response to string
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
            sb = new StringBuilder();
            sb.append(reader.readLine() + "\n");
            String line = "0";

            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            is.close();
            result = sb.toString();

        } catch (Exception e) {
            fl = false;
            Log.e("log_tag", "Error converting result " + e.toString());
        }
        if (fl == true) {
            //paring data
            try {
                if (!result.contains("null")) {
                    JSONArray jArray = new JSONArray(result);
                    JSONObject json_data = null;
                    Log.d("LOG_TAG", "Delete all from table " + "services");
                    if (last_update == null)
                        db.delete("services", null, null);
                    if (jArray != null) {
                        for (int i = 0; i < jArray.length(); i++) {
                            json_data = jArray.getJSONObject(i);
                            if (json_data != null) {

                                if (last_update != null)
                                    db.delete("services", "id == ?", new String[]{json_data.getString("id")});

                                if (json_data.getString("id") != "null")
                                    cv.put("id", json_data.getInt("id"));

                                if (json_data.getString("name") != "null")
                                    cv.put("name", json_data.getString("name"));

                                if (json_data.getString("annotation") != "null")
                                    cv.put("annotation", json_data.getString("annotation"));

                                if (json_data.getString("description") != "null")
                                    cv.put("description", json_data.getString("description"));

                                if (json_data.getString("extra_info") != "null")
                                    cv.put("extra_info", json_data.getString("extra_info"));

                                if (json_data.getString("html") != "null")
                                    cv.put("html", json_data.getString("html"));

                                if (json_data.getString("id_status") != "null")
                                    cv.put("id_status", json_data.getInt("id_status"));

                                if (json_data.getString("link") != "null")
                                    cv.put("link", json_data.getString("link"));

                                if (json_data.getString("img") != "null")
                                    cv.put("img", json_data.getString("img"));

                                if (json_data.getString("price") != "null")
                                    cv.put("price", json_data.getDouble("price"));

                                if (json_data.getString("currency") != "null")
                                    cv.put("currency", json_data.getString("currency"));

                                if (json_data.getString("id_comment") != "null")
                                    cv.put("id_comment", json_data.getString("id_comment"));

                                if (json_data.getString("date_update") != "null")
                                    cv.put("date_update", json_data.getString("date_update"));

                                checkImage("service", json_data.getString("id"), json_data.getString("img"), db);

                                if (last_update == null) {
                                    db.insert("services", null, cv);
                                    drain_new.add(json_data.getInt("id"));
                                }
                                else {
                                    db.insert("services", null, cv);
                                    drain_updated.add(json_data.getInt("id"));
                                }
                            }

                        }
                    }
                }
            } catch (JSONException e1) {
                Log.e("log_tag", "Error no ff " + e1.toString());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        db.close();
        mProgressStatus += 5;
        return new news(drain_new, drain_updated);
    }

    public static news update_excursions() {

        ArrayList<Integer> drain_new = new ArrayList<>();
        ArrayList<Integer> drain_updated = new ArrayList<>();
        //creating object for data

        ContentValues cv = new ContentValues();
        //connecting to DB

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String result = null;
        InputStream is = null;
        StringBuilder sb = null;
        Boolean fl = true;
        Cursor c;
        String last_update = null;
        String p_query = "SELECT date_update FROM excursions ORDER BY date_update DESC LIMIT 1";
        c = db.rawQuery(p_query, new String[]{});
        if (c.moveToFirst() && c!=null && c.getCount()>0 ) {
            last_update = c.getString(0);
        }
        // http post
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = null;
            httppost = new HttpPost(server + "?signal=Excursions");
            if (last_update != null) {
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
                nameValuePair.add(new BasicNameValuePair("date", last_update));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            }
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
        } catch (Exception e) {
            fl = false;
            Log.e("log_tag", "Error in http connection" + e.toString());
        }

        //convert response to string
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
            sb = new StringBuilder();
            sb.append(reader.readLine() + "\n");
            String line = "0";

            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            is.close();
            result = sb.toString();

        } catch (Exception e) {
            fl = false;
            Log.e("log_tag", "Error converting result " + e.toString());
        }
        if (fl == true) {
            //paring data
            try {
                if (!result.contains("null")) {
                    JSONArray jArray = new JSONArray(result);
                    JSONObject json_data = null;
                    Log.d("LOG_TAG", "Delete all from table " + "excursions");
                    if (last_update == null)
                        db.delete("excursions", null, null);
                    if (jArray != null) {
                        for (int i = 0; i < jArray.length(); i++) {
                            json_data = jArray.getJSONObject(i);
                            if (json_data != null) {

                                if (last_update != null)
                                    db.delete("excursions", "id == ?", new String[]{json_data.getString("id")});

                                if (json_data.getString("id") != "null")
                                    cv.put("id", json_data.getInt("id"));

                                if (json_data.getString("id_country") != "null")
                                    cv.put("id_country", json_data.getInt("id_country"));

                                if (json_data.getString("id_city") != "null")
                                    cv.put("id_city", json_data.getInt("id_city"));

                                if (json_data.getString("excursion_type") != "null")
                                    cv.put("excursion_type", json_data.getString("excursion_type"));

                                if (json_data.getString("individual") != "null")
                                    cv.put("individual", json_data.getInt("individual") == 1);

                                if (json_data.getString("duration") != "null")
                                    cv.put("duration", json_data.getInt("duration"));

                                if (json_data.getString("name") != "null")
                                    cv.put("name", json_data.getString("name"));

                                if (json_data.getString("annotation") != "null")
                                    cv.put("annotation", json_data.getString("annotation"));

                                if (json_data.getString("description") != "null")
                                    cv.put("description", json_data.getString("description"));

                                if (json_data.getString("extra_info") != "null")
                                    cv.put("extra_info", json_data.getString("extra_info"));

                                if (json_data.getString("html") != "null")
                                    cv.put("html", json_data.getString("html"));

                                if (json_data.getString("id_status") != "null")
                                    cv.put("id_status", json_data.getInt("id_status"));

                                if (json_data.getString("link") != "null")
                                    cv.put("link", json_data.getString("link"));

                                if (json_data.getString("img") != "null")
                                    cv.put("img", json_data.getString("img"));

                                if (json_data.getString("price") != "null")
                                    cv.put("price", json_data.getDouble("price"));

                                if (json_data.getString("currency") != "null")
                                    cv.put("currency", json_data.getString("currency"));

                                if (json_data.getString("location") != "null")
                                    cv.put("location", json_data.getString("location"));

                                if (json_data.getString("latitude") != "null")
                                    cv.put("latitude", json_data.getString("latitude"));

                                if (json_data.getString("longitude") != "null")
                                    cv.put("longitude", json_data.getString("longitude"));

                                if (json_data.getString("date") != "null")
                                    cv.put("date", json_data.getString("date"));

                                if (json_data.getString("id_comment") != "null")
                                    cv.put("id_comment", json_data.getString("id_comment"));

                                if (json_data.getString("days") != "null")
                                    cv.put("days", json_data.getString("days"));

                                if (json_data.getString("date_update") != "null")
                                    cv.put("date_update", json_data.getString("date_update"));

                                checkImage("excursion", json_data.getString("id"), json_data.getString("img"), db);

                                if (last_update == null) {
                                    db.insert("excursions", null, cv);
                                    drain_new.add(json_data.getInt("id"));
                                }
                                else {
                                    db.insert("excursions", null, cv);
                                    drain_updated.add(json_data.getInt("id"));
                                }
                            }

                        }
                    }
                }
            } catch (JSONException e1) {
                Log.e("log_tag", "Error no ff " + e1.toString());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        db.close();
        mProgressStatus += 10;
        return new news(drain_new, drain_updated);
    }

    public static news update_tours() {

        ArrayList<Integer> drain_new = new ArrayList<>();
        ArrayList<Integer> drain_updated = new ArrayList<>();
        //creating object for data
        ContentValues cv = new ContentValues();
        //connecting to DB

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String result = null;
        InputStream is = null;
        StringBuilder sb = null;
        Boolean fl = true;
        Cursor c;
        String last_update = null;
        String p_query = "SELECT date_update FROM tours ORDER BY date_update DESC LIMIT 1";
        c = db.rawQuery(p_query, new String[]{});
        if (c.moveToFirst() && c!=null && c.getCount()>0 ) {
            last_update = c.getString(0);
        }
        // http post
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = null;
            httppost = new HttpPost(server + "?signal=Tours");
            if (last_update != null) {
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
                nameValuePair.add(new BasicNameValuePair("date", last_update));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            }
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
        } catch (Exception e) {
            fl = false;
            Log.e("log_tag", "Error in http connection" + e.toString());
        }

        //convert response to string
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
            sb = new StringBuilder();
            sb.append(reader.readLine() + "\n");
            String line = "0";

            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            is.close();
            result = sb.toString();

        } catch (Exception e) {
            fl = false;
            Log.e("log_tag", "Error converting result " + e.toString());
        }
        if (fl == true) {
            //paring data
            try {
                if (!result.contains("null")) {
                    JSONArray jArray = new JSONArray(result);
                    JSONObject json_data = null;
                    Log.d("LOG_TAG", "Delete all from table " + "tours");
                    if (last_update == null)
                        db.delete("tours", null, null);
                    if (jArray != null) {
                        for (int i = 0; i < jArray.length(); i++) {
                            json_data = jArray.getJSONObject(i);
                            if (json_data != null) {

                                if (last_update != null)
                                    db.delete("tours", "id == ?", new String[]{json_data.getString("id")});


                                if (json_data.getString("id") != "null")
                                    cv.put("id", json_data.getInt("id"));

                                if (json_data.getString("id_country") != "null")
                                    cv.put("id_country", json_data.getInt("id_country"));

                                if (json_data.getString("id_city") != "null")
                                    cv.put("id_city", json_data.getInt("id_city"));

                                if (json_data.getString("id_kind") != "null")
                                    cv.put("id_kind", json_data.getString("id_kind"));

                                if (json_data.getString("duration") != "null")
                                    cv.put("duration", json_data.getInt("duration"));

                                if (json_data.getString("name") != "null")
                                    cv.put("name", json_data.getString("name"));

                                if (json_data.getString("annotation") != "null")
                                    cv.put("annotation", json_data.getString("annotation"));

                                if (json_data.getString("description") != "null")
                                    cv.put("description", json_data.getString("description"));

                                if (json_data.getString("extra_info") != "null")
                                    cv.put("extra_info", json_data.getString("extra_info"));

                                if (json_data.getString("html") != "null")
                                    cv.put("html", json_data.getString("html"));

                                if (json_data.getString("id_status") != "null")
                                    cv.put("id_status", json_data.getInt("id_status"));

                                if (json_data.getString("link") != "null")
                                    cv.put("link", json_data.getString("link"));

                                if (json_data.getString("img") != "null")
                                    cv.put("img", json_data.getString("img"));

                                if (json_data.getString("price") != "null")
                                    cv.put("price", json_data.getDouble("price"));

                                if (json_data.getString("currency") != "null")
                                    cv.put("currency", json_data.getString("currency"));

                                if (json_data.getString("date") != "null")
                                    cv.put("date", json_data.getString("date"));

                                if (json_data.getString("id_cities") != "null")
                                    cv.put("id_cities", json_data.getString("id_cities"));

                                if (json_data.getString("start_point") != "null")
                                    cv.put("start_point", json_data.getString("start_point"));

                                if (json_data.getString("id_comment") != "null")
                                    cv.put("id_comment", json_data.getString("id_comment"));

                                if (json_data.getString("days") != "null")
                                    cv.put("days", json_data.getString("days"));

                                if (json_data.getString("date_update") != "null")
                                    cv.put("date_update", json_data.getString("date_update"));

                                checkImage("tour", json_data.getString("id"), json_data.getString("img"), db);

                                if (last_update == null) {
                                    db.insert("tours", null, cv);
                                    drain_new.add(json_data.getInt("id"));
                                }
                                else {
                                    db.insert("tours", null, cv);
                                    drain_updated.add(json_data.getInt("id"));
                                }
                            }

                        }
                    }
                }
            } catch (JSONException e1) {
                Log.e("log_tag", "Error no ff " + e1.toString());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        db.close();
        mProgressStatus += 10;
        return new news(drain_new, drain_updated);
    }

    public static void update_comments() {
        //creating object for data
        ContentValues cv = new ContentValues();
        //connecting to DB

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String result = null;
        InputStream is = null;
        StringBuilder sb = null;
        Boolean fl = true;
        Cursor c;
        String last_update = null;
        String p_query = "SELECT date_update FROM comments ORDER BY date_update DESC LIMIT 1";
        c = db.rawQuery(p_query, new String[]{});
        if (c.moveToFirst() && c!=null && c.getCount()>0 ) {
            last_update = c.getString(0);
        }
        // http post
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = null;
            httppost = new HttpPost(server + "?signal=Comments");
            if (last_update != null) {
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
                nameValuePair.add(new BasicNameValuePair("date", last_update));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            }
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
        } catch (Exception e) {
            fl = false;
            Log.e("log_tag", "Error in http connection" + e.toString());
        }

        //convert response to string
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
            sb = new StringBuilder();
            sb.append(reader.readLine() + "\n");
            String line = "0";

            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            is.close();
            result = sb.toString();

        } catch (Exception e) {
            fl = false;
            Log.e("log_tag", "Error converting result " + e.toString());
        }
        if (fl == true) {
            //paring data
            try {
                if (!result.contains("null")) {
                    JSONArray jArray = new JSONArray(result);
                    JSONObject json_data = null;
                    Log.d("LOG_TAG", "Delete all from table " + "comments");
                    if (last_update == null)
                        db.delete("comments", null, null);
                    if (jArray != null) {
                        for (int i = 0; i < jArray.length(); i++) {
                            json_data = jArray.getJSONObject(i);
                            if (json_data != null) {

                                if (last_update != null)
                                    db.delete("comments", "id == ?", new String[]{json_data.getString("id")});

                                if (json_data.getString("id") != "null")
                                    cv.put("id", json_data.getInt("id"));

                                if (json_data.getString("author") != "null")
                                    cv.put("author", json_data.getString("author"));

                                if (json_data.getString("date") != "null")
                                    cv.put("date", json_data.getString("date"));

                                if (json_data.getString("description") != "null")
                                    cv.put("description", json_data.getString("description"));

                                if (json_data.getString("rate") != "null")
                                    cv.put("rate", json_data.getInt("rate"));

                                if (json_data.getString("id_status") != "null")
                                    cv.put("id_status", json_data.getInt("id_status"));

                                if (json_data.getString("link") != "null")
                                    cv.put("link", json_data.getString("link"));

                                if (json_data.getString("img") != "null")
                                    cv.put("img", json_data.getString("img"));

                                if (json_data.getString("date_update") != "null")
                                    cv.put("date_update", json_data.getString("date_update"));

                                checkImage("comment", json_data.getString("id"), json_data.getString("img"), db);

                                    db.insert("comments", null, cv);
                            }

                        }
                    }
                }
            } catch (JSONException e1) {
                Log.e("log_tag", "Error no ff " + e1.toString());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        db.close();
        mProgressStatus += 10;
    }

    public static void update_article() {
        //creating object for data
        ContentValues cv = new ContentValues();
        //connecting to DB

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String result = null;
        InputStream is = null;
        StringBuilder sb = null;
        Boolean fl = true;
        Cursor c;
        String last_update = null;
        String p_query = "SELECT date_update FROM article ORDER BY date_update DESC LIMIT 1";
        c = db.rawQuery(p_query, new String[]{});
        if (c.moveToFirst() && c!=null && c.getCount()>0 ) {
            last_update = c.getString(0);
        }
        // http post
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = null;
            httppost = new HttpPost(server + "?signal=Article");
            if (last_update != null) {
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
                nameValuePair.add(new BasicNameValuePair("date", last_update));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            }
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
        } catch (Exception e) {
            fl = false;
            Log.e("log_tag", "Error in http connection" + e.toString());
        }

        //convert response to string
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
            sb = new StringBuilder();
            sb.append(reader.readLine() + "\n");
            String line = "0";

            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            is.close();
            result = sb.toString();

        } catch (Exception e) {
            fl = false;
            Log.e("log_tag", "Error converting result " + e.toString());
        }
        if (fl == true) {
            //paring data
            try {
                if (!result.contains("null")) {
                    JSONArray jArray = new JSONArray(result);
                    JSONObject json_data = null;
                    Log.d("LOG_TAG", "Delete all from table " + "article");
                    if (last_update == null)
                        db.delete("article", null, null);
                    if (jArray != null) {
                        for (int i = 0; i < jArray.length(); i++) {
                            json_data = jArray.getJSONObject(i);
                            if (json_data != null) {

                                if (last_update != null)
                                    db.delete("article", "id == ?", new String[]{json_data.getString("id")});

                                if (json_data.getString("id") != "null")
                                    cv.put("id", json_data.getInt("id"));

                                if (json_data.getString("date") != "null")
                                    cv.put("date", json_data.getString("date"));

                                if (json_data.getString("annotation") != "null")
                                    cv.put("annotation", json_data.getString("annotation"));

                                if (json_data.getString("id_status") != "null")
                                    cv.put("id_status", json_data.getInt("id_status"));

                                if (json_data.getString("link") != "null")
                                    cv.put("link", json_data.getString("link"));

                                if (json_data.getString("img") != "null")
                                    cv.put("img", json_data.getString("img"));

                                if (json_data.getString("date_update") != "null")
                                    cv.put("date_update", json_data.getString("date_update"));

                                    db.insert("article", null, cv);
                            }

                        }
                    }
                }
            } catch (JSONException e1) {
                Log.e("log_tag", "Error no ff " + e1.toString());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        db.close();
        mProgressStatus += 10;
    }

    public static void update_chat(String id_user) {
        //creating object for data
        ContentValues cv = new ContentValues();
        //connecting to DB

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String result = null;
        InputStream is = null;
        StringBuilder sb = null;
        Boolean fl = true;
        Cursor c;
        String last_update = null;
        String p_query = "SELECT date_update FROM chat ORDER BY date_update DESC LIMIT 1";
        c = db.rawQuery(p_query, new String[]{});
        if (c.moveToFirst() && c!=null && c.getCount()>0 ) {
            last_update = c.getString(0);
        }
        // http post
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = null;
            httppost = new HttpPost(server + "?signal=Chat");
            List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
            nameValuePair.add(new BasicNameValuePair("id_user", id_user));
            if (last_update != null) {
                nameValuePair.add(new BasicNameValuePair("date", last_update));
            }
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
        } catch (Exception e) {
            fl = false;
            Log.e("log_tag", "Error in http connection" + e.toString());
        }

        //convert response to string
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
            sb = new StringBuilder();
            sb.append(reader.readLine() + "\n");
            String line = "0";

            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            is.close();
            result = sb.toString();

        } catch (Exception e) {
            fl = false;
            Log.e("log_tag", "Error converting result " + e.toString());
        }
        if (fl == true) {
            //paring data
            try {
                if (!result.contains("null")) {
                    JSONArray jArray = new JSONArray(result);
                    JSONObject json_data = null;
                    Log.d("LOG_TAG", "Delete all from table " + "chat");
                    if (last_update == null)
                        db.delete("chat", null, null);
                    if (jArray != null) {
                        for (int i = 0; i < jArray.length(); i++) {
                            json_data = jArray.getJSONObject(i);
                            if (json_data != null) {

                                if (last_update != null)
                                    db.delete("chat", "id == ?", new String[]{json_data.getString("id")});

                                if (json_data.getString("id") != "null")
                                    cv.put("id", json_data.getInt("id"));

                                if (json_data.getString("id_order") != "null")
                                    cv.put("id_order", json_data.getInt("id_order"));

                                if (json_data.getString("type") != "null")
                                    cv.put("type", json_data.getString("type"));

                                if (json_data.getString("subject") != "null")
                                    cv.put("subject", json_data.getString("subject"));

                                if (json_data.getString("id_source") != "null")
                                    cv.put("id_source", json_data.getInt("id_source"));

                                if (json_data.getString("fio") != "null")
                                    cv.put("author", json_data.getString("fio"));

                                if (json_data.getString("date_update") != "null")
                                    cv.put("date_update", json_data.getString("date_update"));

                                    db.insert("chat", null, cv);
                            }

                        }
                    }
                }
            } catch (JSONException e1) {
                Log.e("log_tag", "Error no ff " + e1.toString());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        db.close();
        mProgressStatus += 10;
    }

    public static void update_chat_mes(String id_user) {
        //creating object for data
        ContentValues cv = new ContentValues();
        //connecting to DB
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String result = null;
        InputStream is = null;
        StringBuilder sb = null;
        Boolean fl = true;
        Cursor c;
        String last_update = null;
        String p_query = "SELECT date_update FROM chat_mes ORDER BY date_update DESC LIMIT 1";
        c = db.rawQuery(p_query, new String[]{});
        if (c.moveToFirst() && c!=null && c.getCount()>0 ) {
            last_update = c.getString(0);
        }
        // http post
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = null;
            httppost = new HttpPost(server + "?signal=Chat_mes");
            List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
            nameValuePair.add(new BasicNameValuePair("id_user", id_user));
            if (last_update != null) {
                nameValuePair.add(new BasicNameValuePair("date", last_update));
            }
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
        } catch (Exception e) {
            fl = false;
            Log.e("log_tag", "Error in http connection" + e.toString());
        }

        //convert response to string
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
            sb = new StringBuilder();
            sb.append(reader.readLine() + "\n");
            String line = "0";

            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            is.close();
            result = sb.toString();

        } catch (Exception e) {
            fl = false;
            Log.e("log_tag", "Error converting result " + e.toString());
        }
        if (fl) {
            //paring data
            try {
                if (!result.contains("null")) {
                    JSONArray jArray = new JSONArray(result);
                    JSONObject json_data = null;
                    Log.d("LOG_TAG", "Delete all from table " + "chat_mes");
                    if (last_update == null)
                        db.delete("chat_mes", null, null);
                    if (jArray != null) {
                        for (int i = 0; i < jArray.length(); i++) {
                            json_data = jArray.getJSONObject(i);
                            if (json_data != null) {

                                if (last_update != null) {
                                    db.delete("chat_mes", "id == ?", new String[]{json_data.getString("id")});
                                    cv.put("isRead", -1);
                                }
                                else cv.put("isRead", 1);
                                cv.put("isPushed", -1);
                                if (json_data.getString("id") != "null")
                                    cv.put("id", json_data.getInt("id"));

                                if (json_data.getString("id_chat") != "null")
                                    cv.put("id_chat", json_data.getInt("id_chat"));

                                if (json_data.getString("id_member") != "null")
                                    cv.put("id_member", json_data.getInt("id_member"));

                                if (json_data.getString("fio") != "null")
                                    cv.put("author", json_data.getString("fio"));
                                Log.d("idchat",json_data.getString("fio"));

                                if (json_data.getString("message") != "null")
                                    cv.put("message", json_data.getString("message"));

                                if (json_data.getString("date_update") != "null")
                                    cv.put("date_update", json_data.getString("date_update"));
                                db.insert("chat_mes", null, cv);

                            }

                        }
                    }
                }
            } catch (JSONException e1) {
                Log.e("log_tag", "Error no ff " + e1.toString());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        db.close();
        mProgressStatus += 10;
    }

    public static void update_chat(String id_user, Context context) {
        //creating object for data
        ContentValues cv = new ContentValues();
        //connecting to DB
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String result = null;
        InputStream is = null;
        StringBuilder sb = null;
        Boolean fl = true;
        Cursor c;
        String last_update = null;
        String p_query = "SELECT date_update FROM chat ORDER BY date_update DESC LIMIT 1";
        c = db.rawQuery(p_query, new String[]{});
        if (c.moveToFirst() && c!=null && c.getCount()>0 ) {
            last_update = c.getString(0);
        }
        // http post
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = null;
            httppost = new HttpPost(server + "?signal=Chat");
            List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
            nameValuePair.add(new BasicNameValuePair("id_user", id_user));
            if (last_update != null) {
                nameValuePair.add(new BasicNameValuePair("date", last_update));
            }
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
        } catch (Exception e) {
            fl = false;
            Log.e("log_tag", "Error in http connection" + e.toString());
        }

        //convert response to string
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
            sb = new StringBuilder();
            sb.append(reader.readLine() + "\n");
            String line = "0";

            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            is.close();
            result = sb.toString();

        } catch (Exception e) {
            fl = false;
            Log.e("log_tag", "Error converting result " + e.toString());
        }
        if (fl == true) {
            //paring data
            try {
                if (!result.contains("null")) {
                    JSONArray jArray = new JSONArray(result);
                    JSONObject json_data = null;
                    Log.d("LOG_TAG", "Delete all from table " + "chat");
                    if (last_update == null)
                        db.delete("chat", null, null);
                    if (jArray != null) {
                        for (int i = 0; i < jArray.length(); i++) {
                            json_data = jArray.getJSONObject(i);
                            if (json_data != null) {

                                if (last_update != null)
                                    db.delete("chat", "id == ?", new String[]{json_data.getString("id")});

                                if (json_data.getString("id") != "null")
                                    cv.put("id", json_data.getInt("id"));

                                if (json_data.getString("id_order") != "null")
                                    cv.put("id_order", json_data.getInt("id_order"));

                                if (json_data.getString("type") != "null")
                                    cv.put("type", json_data.getString("type"));

                                if (json_data.getString("subject") != "null")
                                    cv.put("subject", json_data.getString("subject"));

                                if (json_data.getString("id_source") != "null")
                                    cv.put("id_source", json_data.getInt("id_source"));

                                if (json_data.getString("fio") != "null")
                                    cv.put("author", json_data.getString("fio"));

                                if (json_data.getString("date_update") != "null")
                                    cv.put("date_update", json_data.getString("date_update"));

                                db.insert("chat", null, cv);
                            }

                        }
                    }
                }
            } catch (JSONException e1) {
                Log.e("log_tag", "Error no ff " + e1.toString());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        db.close();
        mProgressStatus += 10;
    }

    public static void update_chat_mes(String id_user, Context context) {
        //creating object for data
        ContentValues cv = new ContentValues();
        //connecting to DB
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String result = null;
        InputStream is = null;
        StringBuilder sb = null;
        Boolean fl = true;
        Cursor c;
        String last_update = null;
        Log.d("Start","0:0x002");
        String p_query = "SELECT date_update FROM chat_mes ORDER BY date_update DESC LIMIT 1";
        c = db.rawQuery(p_query, new String[]{});
        if (c.moveToFirst() && c!=null && c.getCount()>0 ) {
            last_update = c.getString(0);
        }
        // http post
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = null;
            httppost = new HttpPost(server + "?signal=Chat_mes");
            List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
            nameValuePair.add(new BasicNameValuePair("id_user", id_user));
            if (last_update != null) {
                nameValuePair.add(new BasicNameValuePair("date", last_update));
            }
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
        } catch (Exception e) {
            fl = false;
            Log.e("log_tag", "Error in http connection" + e.toString());
        }

        //convert response to string
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
            sb = new StringBuilder();
            sb.append(reader.readLine() + "\n");
            String line = "0";

            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            is.close();
            result = sb.toString();

        } catch (Exception e) {
            fl = false;
            Log.e("log_tag", "Error converting result " + e.toString());
        }
        if (fl) {
            //paring data
            try {
                if (!result.contains("null")) {
                    JSONArray jArray = new JSONArray(result);
                    JSONObject json_data = null;
                    Log.d("LOG_TAG", "Delete all from table " + "chat_mes");
                    if (last_update == null)
                        db.delete("chat_mes", null, null);
                    if (jArray != null) {
                        for (int i = 0; i < jArray.length(); i++) {

                            json_data = jArray.getJSONObject(i);

                            if (json_data != null) {

                                if (last_update != null) {
                                    db.delete("chat_mes", "id == ?", new String[]{json_data.getString("id")});
                                    cv.put("isRead", -1);
                                    cv.put("isPushed", 1);
                                }
                                else {
                                    cv.put("isRead", 1);
                                    cv.put("isPushed", 1);
                                }

                                if (json_data.getString("id") != "null")
                                    cv.put("id", json_data.getInt("id"));

                                if (json_data.getString("id_chat") != "null")
                                    cv.put("id_chat", json_data.getInt("id_chat"));

                                if (json_data.getString("id_member") != "null")
                                    cv.put("id_member", json_data.getInt("id_member"));

                                if (json_data.getString("fio") != "null")
                                    cv.put("author", json_data.getString("fio"));
                                Log.d("idchat",json_data.getString("fio"));

                                if (json_data.getString("message") != "null")
                                    cv.put("message", json_data.getString("message"));

                                if (json_data.getString("date_update") != "null")
                                    cv.put("date_update", json_data.getString("date_update"));

                                db.insert("chat_mes", null, cv);

                            }

                        }
                    }
                }
            } catch (JSONException e1) {
                Log.e("log_tag", "Error no ff " + e1.toString());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        db.close();
        mProgressStatus += 10;
    }

    public static void update_currency() {
        //creating object for data
        ContentValues cv = new ContentValues();
        //connecting to DB

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String result = null;
        InputStream is = null;
        StringBuilder sb = null;
        Boolean fl = true;
        Cursor c;
        String last_update = null;
        String p_query = "SELECT date_update FROM currency ORDER BY date_update DESC LIMIT 1";
        c = db.rawQuery(p_query, new String[]{});
        if (c.moveToFirst() && c!=null && c.getCount()>0 ) {
            last_update = c.getString(0);
        }
        // http post
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = null;
            httppost = new HttpPost(server + "?signal=Currency");
            if (last_update != null) {
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
                nameValuePair.add(new BasicNameValuePair("date", last_update));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            }
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
        } catch (Exception e) {
            fl = false;
            Log.e("log_tag", "Error in http connection" + e.toString());
        }

        //convert response to string
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
            sb = new StringBuilder();
            sb.append(reader.readLine() + "\n");
            String line = "0";

            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            is.close();
            result = sb.toString();

        } catch (Exception e) {
            fl = false;
            Log.e("log_tag", "Error converting result " + e.toString());
        }
        if (fl == true) {
            //paring data
            try {
                if (!result.contains("null")) {
                    JSONArray jArray = new JSONArray(result);
                    JSONObject json_data = null;
                    Log.d("LOG_TAG", "Delete all from table " + "currency");
                    if (last_update == null)
                        db.delete("currency", null, null);
                    if (jArray != null) {
                        for (int i = 0; i < jArray.length(); i++) {
                            json_data = jArray.getJSONObject(i);
                            if (json_data != null) {

                                if (last_update != null)
                                    db.delete("currency", "id == ?", new String[]{json_data.getString("id")});

                                if (json_data.getString("id") != "null")
                                    cv.put("id", json_data.getInt("id"));

                                if (json_data.getString("name") != "null")
                                    cv.put("name", json_data.getString("name"));

                                if (json_data.getString("short_name") != "null")
                                    cv.put("short_name", json_data.getString("short_name"));

                                Log.i("CURRENCY LOADED", "_ " + json_data.getString("short_name"));

                                if (json_data.getString("date_update") != "null")
                                    cv.put("date_update", json_data.getString("date_update"));

                                    db.insert("currency", null, cv);

                            }

                        }
                    }
                }
            } catch (JSONException e1) {
                Log.e("log_tag", "Error no ff " + e1.toString());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        db.close();
    }

    public static void update_currency_data() {
        //creating object for data
        ContentValues cv = new ContentValues();
        //connecting to DB

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String result = null;
        InputStream is = null;
        StringBuilder sb = null;
        Boolean fl = true;
        Cursor c;
        String last_update = null;
        String p_query = "SELECT date_update FROM currency_data ORDER BY date_update DESC LIMIT 1";
        c = db.rawQuery(p_query, new String[]{});
        if (c.moveToFirst() && c!=null && c.getCount()>0 ) {
            last_update = c.getString(0);
        }
        // http post
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = null;
            httppost = new HttpPost(server + "?signal=Currency_data");
            if (last_update != null) {
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
                nameValuePair.add(new BasicNameValuePair("date", last_update));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            }
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
        } catch (Exception e) {
            fl = false;
            Log.e("log_tag", "Error in http connection" + e.toString());
        }

        //convert response to string
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
            sb = new StringBuilder();
            sb.append(reader.readLine() + "\n");
            String line = "0";

            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            is.close();
            result = sb.toString();

        } catch (Exception e) {
            fl = false;
            Log.e("log_tag", "Error converting result " + e.toString());
        }
        if (fl == true) {
            //paring data
            try {
                if (!result.contains("null")) {
                    JSONArray jArray = new JSONArray(result);
                    JSONObject json_data = null;
                    Log.d("LOG_TAG", "Delete all from table " + "currency_data");
                    if (last_update == null)
                        db.delete("currency_data", null, null);
                    if (jArray != null) {
                        for (int i = 0; i < jArray.length(); i++) {
                            json_data = jArray.getJSONObject(i);
                            if (json_data != null) {

                                if (last_update != null)
                                    db.delete("currency_data", "id == ?", new String[]{json_data.getString("id")});

                                if (json_data.getString("id") != "null")
                                    cv.put("id", json_data.getInt("id"));

                                if (json_data.getString("cur_from") != "null")
                                    cv.put("cur_from", json_data.getString("cur_from"));

                                if (json_data.getString("cur_to") != "null")
                                    cv.put("cur_to", json_data.getString("cur_to"));

                                Log.i("cur_from", "_" + json_data.getString("cur_from"));
                                Log.i("cur_to", "_" + json_data.getString("cur_to"));

                                if (json_data.getString("multiplier") != "null")
                                    cv.put("multiplier", json_data.getDouble("multiplier"));

                                if (json_data.getString("date_update") != "null")
                                    cv.put("date_update", json_data.getString("date_update"));

                                db.insert("currency_data", null, cv);
                            }

                        }
                    }
                }
            } catch (JSONException e1) {
                Log.e("log_tag", "Error no ff " + e1.toString());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        db.close();
    }

}