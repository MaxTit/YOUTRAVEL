package com.youtravel;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
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
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.youtravel.StartActivityAlternative.update_chat;
import static com.youtravel.StartActivityAlternative.update_chat_mes;

public class MenuChatActivity extends AppCompatActivity {

    static DBHelper dbHelper;
    static SharedPreferences settings;
    static RelativeLayout login_v;
    static LinearLayout chat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_menu_chat);
        new MainMenu(this);
         login_v = (RelativeLayout) findViewById(R.id.login);
         chat = (LinearLayout) findViewById(R.id.chat_menu);
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
                    login_v.setVisibility(View.GONE);
                    chat.setVisibility(View.VISIBLE);
                    refresh();
                }
            });
        }
        else {
            login_v.setVisibility(View.GONE);
            chat.setVisibility(View.VISIBLE);
            refreshDialogs();
        }
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
                refreshDialogs();
            }
        }, 1000);
    }

    private  void refreshDialogs()
    {
        dbHelper = new DBHelper(this);

        LinearLayout underframe = (LinearLayout) findViewById(R.id.dialog_grid);
         Cursor c;
        SQLiteDatabase db;

        try {
            underframe.removeAllViewsInLayout();
        } catch(NullPointerException ne) {}

        db = dbHelper.getWritableDatabase();
        String p_query = "SELECT * FROM chat ORDER BY date_update";             //TODO except not actual tours
        c = db.rawQuery(p_query,new String[]{});
        Log.d("chatPrint", c.getCount()+"");
        if (c.moveToFirst() && c!=null && c.getCount()>0 ) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                View v = null;
                v = getLayoutInflater().inflate(R.layout.item_chat_dialog, underframe, false);
                underframe.addView(v);
                final String idChat = c.getString(0);
                v.findViewById(R.id.content).setOnClickListener(new View.OnClickListener()  {
                    @Override
                    public void onClick(View v){
                        Intent intent = new Intent(MenuChatActivity.this, ChatActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        ChatActivity.idChat = idChat;
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                    }
                });

                final TextView title = (TextView)v.findViewById(R.id.text1);
                switch (c.getString(2))
                {
                    case "tour": {
                        String p_query_name = "SELECT name FROM tours WHERE id == ?";                                                   //TODO except not actual tours
                        Cursor c1 = db.rawQuery(p_query_name, new String[]{c.getString(3)});
                        if (c1.moveToFirst() && c1 != null && c1.getCount() > 0) {
                            c1.moveToFirst();
                            title.setText(c1.getString(0));
                        }
                        break;
                    }
                    case "excursion": {
                        String p_query_name = "SELECT name FROM excursions WHERE id == ?";                                                   //TODO except not actual tours
                        Cursor c1 = db.rawQuery(p_query_name, new String[]{c.getString(3)});
                        if (c1.moveToFirst() && c1 != null && c1.getCount() > 0) {
                            c1.moveToFirst();
                            title.setText(c1.getString(0));
                        }
                        break;
                    }
                    case "service": {
                        String p_query_name = "SELECT name FROM services WHERE id == ?";                                                   //TODO except not actual tours
                        Cursor c1 = db.rawQuery(p_query_name, new String[]{c.getString(3)});
                        if (c1.moveToFirst() && c1 != null && c1.getCount() > 0) {
                            c1.moveToFirst();
                            title.setText(c1.getString(0));
                        }
                        break;
                    }
                    case "event": {
                        String p_query_name = "SELECT name FROM events WHERE id == ?";                                                   //TODO except not actual tours
                        Cursor c1 = db.rawQuery(p_query_name, new String[]{c.getString(3)});
                        if (c1.moveToFirst() && c1 != null && c1.getCount() > 0) {
                            c1.moveToFirst();
                            title.setText(c1.getString(0));
                        }
                        break;
                    }
                }

                c.moveToNext();
            }
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
