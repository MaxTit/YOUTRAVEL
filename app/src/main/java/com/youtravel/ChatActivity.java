package com.youtravel;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static com.youtravel.StartActivityAlternative.settings;
import static com.youtravel.StartActivityAlternative.update_article;
import static com.youtravel.StartActivityAlternative.update_chat;
import static com.youtravel.StartActivityAlternative.update_chat_mes;
import static com.youtravel.StartActivityAlternative.update_cities;
import static com.youtravel.StartActivityAlternative.update_comments;
import static com.youtravel.StartActivityAlternative.update_countries;
import static com.youtravel.StartActivityAlternative.update_currency;
import static com.youtravel.StartActivityAlternative.update_currency_data;
import static com.youtravel.StartActivityAlternative.update_events;
import static com.youtravel.StartActivityAlternative.update_excursions;
import static com.youtravel.StartActivityAlternative.update_journey_kinds;
import static com.youtravel.StartActivityAlternative.update_objects;
import static com.youtravel.StartActivityAlternative.update_services;
import static com.youtravel.StartActivityAlternative.update_tours;

public class ChatActivity extends AppCompatActivity {
    public static String idChat = null;
    public static String typeChat = "-1";
    public static String idSource = "-1";
    static DBHelper dbHelper;
    static SharedPreferences settings;
    static RelativeLayout login_v;
    static LinearLayout chat;

    static ScrollView scrollView;
    static Button enter;
    static EditText mess;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_chat);
        new MainMenu(this);
        mess = (EditText) findViewById(R.id.editText1);
        login_v = (RelativeLayout) findViewById(R.id.login);
        chat = (LinearLayout) findViewById(R.id.chat);
        enter = (Button) findViewById(R.id.enter);
        scrollView = (ScrollView) findViewById(R.id.scroll_view);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        settings = getApplicationContext().getSharedPreferences("my_data", 0);
         if(settings.getString("id_user", null)==null)
         {
             login_v.setVisibility(View.VISIBLE);
             chat.setVisibility(View.GONE);
             new LoginForm(this, new Callback() {
                 @Override
                 public void invoke() {
                     refresh();
                     init();
                 }
             });
         }
        else {
           init();
         }
    }

    private  void init()
    {
        login_v.setVisibility(View.GONE);
        chat.setVisibility(View.VISIBLE);
        if (idChat.equals("-1")) {
            dbHelper = new DBHelper(this);
            SQLiteDatabase db;
            db = dbHelper.getWritableDatabase();
            String p_query = "SELECT id FROM chat WHERE type = ? and id_source = ? LIMIT 1";                                                   //TODO except not actual tours
            Cursor c = db.rawQuery(p_query, new String[]{typeChat, idSource});
            //Log.d("chatPrint", c.getCount()+"");
            if (c.moveToFirst() && c != null && c.getCount() > 0) {
                idChat = c.getString(0);
            }
            c.close();
            db.close();
        }
        refreshMes();

        enter.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ConnectivityManager cm =
                        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                final boolean isConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();
                if (isConnected) {
                    if (mess.getText().toString().length() > 0) {
                        // TODO Auto-generated method stub
                        try {
                            HttpClient httpclient = new DefaultHttpClient();
                            HttpPost httppost = new HttpPost(StartActivity.server + "/send_chat_mes.php");
                            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                            nameValuePairs.add(new BasicNameValuePair("id_chat", idChat));
                            nameValuePairs.add(new BasicNameValuePair("id_member", settings.getString("id_user", null)));
                            nameValuePairs.add(new BasicNameValuePair("message", mess.getText().toString()));
                            nameValuePairs.add(new BasicNameValuePair("type", typeChat));
                            nameValuePairs.add(new BasicNameValuePair("id_source", idSource));
                            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

                            mess.setText("");
                            // Execute HTTP Post Request
                            HttpResponse response = httpclient.execute(httppost);
                            idChat = EntityUtils.toString(response.getEntity());
                            Log.d("idchat", idChat);
                            StartActivity.hideKeyboard(ChatActivity.this);
                            refresh();
                        } catch (ClientProtocolException e) {
                            // TODO Auto-generated catch block
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                        }

                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                        AlertDialog dialog = builder.create();
                        dialog.setTitle("Введите сообщение!");
                        Message listenerDoesNotAccept = null;
                        dialog.setButton("Ok", listenerDoesNotAccept);
                        dialog.setCancelable(false);
                        dialog.show();
                    }
                } else {
                    android.app.AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                    AlertDialog dialog = builder.create();
                    dialog.setTitle("Отсутствует подключение к Интернету");
                    dialog.setButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    dialog.setCancelable(false);
                    dialog.show();
                }


            }
        });
    }
    private void refresh()
    {
        AsyncTask<String, String, String> refresh = new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... params) {

                try {
                    if (settings.getString("email_user", null) != null) {
                        update_chat(settings.getString("id_user", null));
                        update_chat_mes(settings.getString("id_user", null));

                    }
                } catch (Exception e) {
                    Log.e("Error: ", e.getMessage());
                }

                return null;
            }
        };
        try {
            refresh.execute();

        } catch (Exception ee) {
        }

        new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                // stop refresh
                refreshMes();
            }
        }, 1000);
    }
    private  void refreshMes()
    {
        dbHelper = new DBHelper(this);

        LinearLayout underframe = (LinearLayout) findViewById(R.id.messages);
        Cursor c;
        SQLiteDatabase db;

        try {
            underframe.removeAllViewsInLayout();
        } catch(NullPointerException ne) {}

        db = dbHelper.getWritableDatabase();
        String p_query = "SELECT * FROM chat_mes WHERE id_chat = ? ORDER BY date_update";                                                   //TODO except not actual tours
        c = db.rawQuery(p_query,new String[]{idChat});
        Log.d("chatPrint", c.getCount()+"");
        if (c.moveToFirst() && c!=null && c.getCount()>0 ) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                View v = null;
                v = getLayoutInflater().inflate(R.layout.item_mes_chat, underframe, false);
                underframe.addView(v);
                Log.d("member",settings.getString("id_user", null)+" "+c.getString(2)+" "+c.getString(3));
                if(settings.getString("id_user", null).equals(c.getString(2)))
                {
                    final RelativeLayout content = (RelativeLayout)v.findViewById(R.id.content_left);
                    content.setVisibility(View.VISIBLE);
                    final TextView from = (TextView)v.findViewById(R.id.from_left);
                    from.setText("Я:");
                    final TextView mes = (TextView)v.findViewById(R.id.message_left);
                    mes.setText(c.getString(4));
                }
                else
                {
                    final RelativeLayout content = (RelativeLayout)v.findViewById(R.id.content_right);
                    content.setVisibility(View.VISIBLE);
                    final TextView from = (TextView)v.findViewById(R.id.from_right);
                    from.setText(c.getString(3));
                    final TextView mes = (TextView)v.findViewById(R.id.message_right);
                    mes.setText(c.getString(4));
                }

                c.moveToNext();
            }
            scrollView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    View view = (View) scrollView.getChildAt(scrollView.getChildCount() - 1);
                    scrollView.scrollTo(0, view.getBottom());

                    Log.i("FILL","FILLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL");
                }
            }, 100);
        }
        /*if (mode == 0) {
            final int count = underframe.getChildCount();
            new Handler().postDelayed(new Runnable() {
                @Override public void run() {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Стран найдено: " + count,
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
            }, 0);
        }*/
        c.close();
        db.close();
    }
}
