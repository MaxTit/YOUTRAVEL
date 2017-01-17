package com.youtravel;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
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

public class StartActivity extends AppCompatActivity {

    public static int mProgressStatus = 0;
    ProgressBar mProgress;

    public class DownloadFileFromURL extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... f_url) {
            try {

                    update_countries();
                publishProgress(mProgressStatus);

                    update_cities();
                publishProgress(mProgressStatus);

                    update_objects();
                publishProgress(mProgressStatus);

                    update_events();
                publishProgress(mProgressStatus);

                    update_journey_kinds();
                publishProgress(mProgressStatus);

                    update_services();
                publishProgress(mProgressStatus);

                    update_excursions();
                publishProgress(mProgressStatus);

                    update_tours();
                publishProgress(mProgressStatus);

                    update_comments();
                publishProgress(mProgressStatus);

                    update_article();
                publishProgress(mProgressStatus);

                    update_chat();
                publishProgress(mProgressStatus);


            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            mProgress.setProgress(progress[0]);
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        @Override
        protected void onPostExecute(String file_url) {
            //dismiss the dialog after the file was downloaded
            //	dismissDialog(progress_bar_type);
            Intent intent = new Intent(StartActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    static DBHelper dbHelper;
    static DownloadFileFromURL dt;
    static String pathtocache;
    static SharedPreferences settings;
    public static String logind = "";
    public static String server = "http://youtravel-su.1gb.ru/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);


        mProgress = (ProgressBar) findViewById(R.id.progressBar);

        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        settings = getApplicationContext().getSharedPreferences("my_data", 0);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        dbHelper = new DBHelper(this);

        pathtocache = getBaseContext().getCacheDir().toString();
        final ImageView img = (ImageView) findViewById(R.id.imageView1);
        final RelativeLayout logincontent = (RelativeLayout) findViewById(R.id.login);
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        final boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (settings.getString("email_user", null) != null) {
            hideKeyboard(this);

            img.setVisibility(View.VISIBLE);
            img.setImageBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.loading));
            logincontent.setVisibility(View.GONE);
            logind = settings.getString("id_user", null);
            if (isConnected) {
                mProgress.setVisibility(View.VISIBLE);
                new DownloadFileFromURL().execute();
            } else {
                android.app.AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
                AlertDialog dialog = builder.create();
                dialog.setTitle("Для входа, подключитесь к Интернету");
                Message listenerDoesNotAccept = null;
                dialog.setButton("Ok", listenerDoesNotAccept);
                dialog.setCancelable(false);
                dialog.show();
            }
        } else {
            img.setVisibility(View.GONE);
            logincontent.setVisibility(View.VISIBLE);
            final EditText login = (EditText) findViewById(R.id.editText1);
            final EditText pas = (EditText) findViewById(R.id.editText2);
                Button go = (Button) findViewById(R.id.go); // In
                Button gog = (Button) findViewById(R.id.gog); // Guest
                Button reg = (Button) findViewById(R.id.reg); // Registration
                reg.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View arg0) {
                        if (isConnected) {
                            RegDialog rd = new RegDialog(StartActivity.this);
                            rd.show();
                        } else {
                            android.app.AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
                            AlertDialog dialog = builder.create();
                            dialog.setTitle("Для регистрация, подключитесь к Интернету");
                            Message listenerDoesNotAccept = null;
                            dialog.setButton("Ok", listenerDoesNotAccept);
                            dialog.setCancelable(false);
                            dialog.show();
                        }

                    }
                });
                gog.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View arg0) {
                        if (isConnected) {
                            hideKeyboard(StartActivity.this);
                            img.setVisibility(View.VISIBLE);
                            img.setImageBitmap(BitmapFactory.decodeResource(StartActivity.this.getResources(), R.drawable.loading));

                            mProgress.setVisibility(ProgressBar.VISIBLE);
                            logincontent.setVisibility(View.GONE);
                            new DownloadFileFromURL().execute();
                           // progressBar.setVisibility(ProgressBar.INVISIBLE);
                        } else {
                            hideKeyboard(StartActivity.this);
                            img.setVisibility(View.VISIBLE);
                            img.setImageBitmap(BitmapFactory.decodeResource(StartActivity.this.getResources(), R.drawable.loading));
                            mProgress.setVisibility(ProgressBar.VISIBLE);
                            logincontent.setVisibility(View.GONE);
                            android.app.AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
                            AlertDialog dialog = builder.create();
                            dialog.setTitle("Отсутствует подключение к Интернету");
                            dialog.setButton("Ok", new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(StartActivity.this, HomeActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }
                            });
                            dialog.setCancelable(false);
                            dialog.show();
                        }

                    }
                });
                go.setOnClickListener(new View.OnClickListener() {

                    @SuppressLint("CommitPrefEdits")
                    @SuppressWarnings("unused")
                    public void onClick(View arg0) {
                        if (isConnected) {

                            String result = null;
                            InputStream is = null;
                            StringBuilder sb = null;
                            Boolean fl = true;
                            //http post
                            try {
                                HttpClient httpclient = new DefaultHttpClient();
                                HttpPost httppost = null;
                                httppost = new HttpPost(server + "/login.php?login=" + login.getText().toString() + "&pas=" + md5(pas.getText().toString()));
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
                                    JSONArray jArray = new JSONArray(result);
                                    JSONObject json_data = null;
                                    if (jArray != null) {
                                        json_data = jArray.getJSONObject(0);
                                        if (json_data != null) {
                                            if (json_data.getString("email") != "null") {
                                                SharedPreferences.Editor editor = settings.edit();
                                                editor.putString("id_user", json_data.getString("id"));
                                                editor.putString("email_user", json_data.getString("email"));
                                                editor.apply();
                                                logind = json_data.getString("id");
                                            }
                                            hideKeyboard(StartActivity.this);

                                            mProgress.setVisibility(View.VISIBLE);
                                            img.setVisibility(View.VISIBLE);
                                            img.setImageBitmap(BitmapFactory.decodeResource(StartActivity.this.getResources(), R.drawable.loading));
                                            logincontent.setVisibility(View.GONE);
                                            new DownloadFileFromURL().execute();
                                        }

                                    } else {
                                        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
                                        AlertDialog dialog = builder.create();
                                        dialog.setTitle("Не верные данные");
                                        Message listenerDoesNotAccept = null;
                                        dialog.setButton("Ok", listenerDoesNotAccept);
                                        dialog.setCancelable(false);
                                        dialog.show();
                                    }
                                } catch (JSONException e1) {
                                    Log.e("log_tag", "Error no ff " + e1.toString());
                                    android.app.AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
                                    AlertDialog dialog = builder.create();
                                    dialog.setTitle("Не верные данные");
                                    Message listenerDoesNotAccept = null;
                                    dialog.setButton("Ok", listenerDoesNotAccept);
                                    dialog.setCancelable(false);
                                    dialog.show();
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            }
                        } else {
                            android.app.AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
                            AlertDialog dialog = builder.create();
                            dialog.setTitle("Для входа, подключитесь к Интернету");
                            Message listenerDoesNotAccept = null;
                            dialog.setButton("Ok", listenerDoesNotAccept);
                            dialog.setCancelable(false);
                            dialog.show();
                        }

                    }
                });
//             else {
//                android.app.AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
//                AlertDialog dialog = builder.create();
//                dialog.setTitle("Для входа, подключитесь к Интернету");
//                Message listenerDoesNotAccept = null;
//                dialog.setButton("Ok", listenerDoesNotAccept);
//                dialog.setCancelable(false);
//                dialog.show();
//            }
        }


    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View view = activity.getCurrentFocus();
        if (view != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static String md5(String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    ////////////////////////////////////

    public static void update_countries() {
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

                                if (json_data.getString("id") != "null")
                                    cv.put("id", json_data.getInt("id"));

                                Log.d("OOO__OOO", json_data.getString("id"));

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

                                if (json_data.getString("date_update") != "null")
                                    cv.put("date_update", json_data.getString("date_update"));

                                if (last_update == null)
                                    db.insert("countries", null, cv);
                                else
                                    db.update("countries", cv, "id == ?", new String[]{json_data.getString("id")});
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
    }

    public static void update_cities() {
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

                                if (json_data.getString("id") != "null")
                                    cv.put("id", json_data.getInt("id"));

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

                                if (last_update == null)
                                    db.insert("cities", null, cv);
                                else
                                    db.update("cities", cv, "id == ?", new String[]{json_data.getString("id")});
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

                                if (last_update == null)
                                    db.insert("objects", null, cv);
                                else
                                    db.update("objects", cv, "id == ?", new String[]{json_data.getString("id")});
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

    public static void update_events() {
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

                                if (last_update == null)
                                    db.insert("events", null, cv);
                                else
                                    db.update("events", cv, "id == ?", new String[]{json_data.getString("id")});
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
        if (fl == true) {
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

                                if (last_update == null)
                                    db.insert("journey_kinds", null, cv);
                                else
                                    db.update("journey_kinds", cv, "id == ?", new String[]{json_data.getString("id")});
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

    public static void update_services() {
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

                                if (last_update == null)
                                    db.insert("services", null, cv);
                                else
                                    db.update("services", cv, "id == ?", new String[]{json_data.getString("id")});
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

    public static void update_excursions() {
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

                                if (json_data.getString("date_update") != "null")
                                    cv.put("date_update", json_data.getString("date_update"));

                                if (last_update == null)
                                    db.insert("excursions", null, cv);
                                else
                                    db.update("excursions", cv, "id == ?", new String[]{json_data.getString("id")});
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

    public static void update_tours() {
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
                                    cv.put("date", make_date(json_data.getString("date")));

                                if (json_data.getString("id_cities") != "null")
                                    cv.put("id_cities", json_data.getString("id_cities"));

                                if (json_data.getString("start_point") != "null")
                                    cv.put("start_point", json_data.getString("start_point"));

                                if (json_data.getString("id_comment") != "null")
                                    cv.put("id_comment", json_data.getString("id_comment"));

                                if (json_data.getString("date_update") != "null")
                                    cv.put("date_update", json_data.getString("date_update"));

                                if (last_update == null)
                                    db.insert("tours", null, cv);
                                else
                                    db.update("tours", cv, "id == ?", new String[]{json_data.getString("id")});
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

                                if (last_update == null)
                                    db.insert("comments", null, cv);
                                else
                                    db.update("comments", cv, "id == ?", new String[]{json_data.getString("id")});
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

                                if (last_update == null)
                                    db.insert("article", null, cv);
                                else
                                    db.update("article", cv, "id == ?", new String[]{json_data.getString("id")});
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

    public static void update_chat() {
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
                    Log.d("LOG_TAG", "Delete all from table " + "chat");
                    if (last_update == null)
                        db.delete("chat", null, null);
                    if (jArray != null) {
                        for (int i = 0; i < jArray.length(); i++) {
                            json_data = jArray.getJSONObject(i);
                            if (json_data != null) {

                                if (json_data.getString("id") != "null")
                                    cv.put("id", json_data.getInt("id"));

                                if (json_data.getString("id_from") != "null")
                                    cv.put("id_from", json_data.getInt("id_from"));

                                if (json_data.getString("id_chat") != "null")
                                    cv.put("id_chat", json_data.getInt("id_chat"));

                                if (json_data.getString("type") != "null")
                                    cv.put("type", json_data.getString("type"));

                                if (json_data.getString("id_source") != "null")
                                    cv.put("id_source", json_data.getInt("id_source"));

                                if (json_data.getString("message") != "null")
                                    cv.put("message", json_data.getString("message"));

                                if (json_data.getString("author") != "null")
                                    cv.put("author", json_data.getString("author"));

                                if (json_data.getString("date_update") != "null")
                                    cv.put("date_update", json_data.getString("date_update"));

                                if (json_data.getString("date_update") != "null")
                                    cv.put("date_update", json_data.getString("date_update"));

                                if (last_update == null)
                                    db.insert("chat", null, cv);
                                else
                                    db.update("chat", cv, "id == ?", new String[]{json_data.getString("id")});
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

    private static String make_date(String reference){
        DateFormat format = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        Date date = null;
        boolean t = true;
        try {
            date = format.parse(reference);
        }
        catch (Exception e){
            Log.d("DATA_ERROR","Date parsing error!");
            t = !t;
        }
        if (t) return format.format(date);
        else return "Null";
    }

    public static String make_date(Date date){
        DateFormat format = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());  // TODO dots between dd mm yyyy
        String answer = "null";
        try { answer = format.format(date); }catch (Exception e){}
        return answer;
    }
}