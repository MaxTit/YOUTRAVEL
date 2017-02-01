package com.youtravel;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
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
    static ScrollView scrollView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_menu_chat);
        scrollView = (ScrollView) findViewById(R.id.scroll_view);
        init_interface();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        menu.findItem(R.id.action_add).setIcon(R.drawable.ic_add);
        return true;
    }

    private void init_interface() {
        final MainMenu menu = new MainMenu(this);
        menu.myToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_add: {
                        /*
                            TODO       1) add a new chat-field,
                            TODO       2) focus on it, and if its empty - delete it,
                            TODO       3) else - create new chat with selected name
                        */
                        LinearLayout underframe = (LinearLayout) findViewById(R.id.dialog_grid);
                        final View candidate = getLayoutInflater().inflate(R.layout.item_new_chat_dialog, underframe, false);
                        underframe.addView(candidate);
                        findViewById(R.id.tick).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // TODO add to db new one
                            }
                        });
                        scrollView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
                                scrollView.scrollTo(0, view.getBottom());
                                Log.i("Finish","Getting down..");
                            }
                        }, 100);

                        return true;
                    }
                }
                return false;
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
        String p_query = "SELECT * FROM chat ORDER BY date_update";
        c = db.rawQuery(p_query,new String[]{});
        Log.d("chatPrint", c.getCount()+"");
        if (c.moveToFirst() && c!=null && c.getCount()>0 ) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                View v = null;
                v = getLayoutInflater().inflate(R.layout.item_chat_dialog, underframe, false);
                underframe.addView(v);
                final String idChat = c.getString(0);
                final String subject = (c.getString(3)==null) ? "" : c.getString(3);
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
                if (db.rawQuery("SELECT * FROM chat_mes WHERE id_chat ==" + c.getString(0) + " AND isRead == -1", null)
                        .getCount() > 0){
                    v.setBackgroundColor(Color.parseColor("#A9A9A9"));
                }

                final TextView title = (TextView)v.findViewById(R.id.text1);
                switch (c.getString(2))
                {
                    case "tour": {
                        String p_query_name = "SELECT name FROM tours WHERE id == ?";
                        Cursor c1 = db.rawQuery(p_query_name, new String[]{c.getString(4)});
                        if (c1.moveToFirst() && c1 != null && c1.getCount() > 0) {
                            c1.moveToFirst();
                            if (subject.isEmpty())
                                title.setText(c1.getString(0));
                            else title.setText(subject);
                        }
                        break;
                    }
                    case "excursion": {
                        String p_query_name = "SELECT name FROM excursions WHERE id == ?";
                        Cursor c1 = db.rawQuery(p_query_name, new String[]{c.getString(4)});
                        if (c1.moveToFirst() && c1 != null && c1.getCount() > 0) {
                            c1.moveToFirst();
                            if (subject.isEmpty())
                                title.setText(c1.getString(0));
                            else title.setText(subject);
                        }
                        break;
                    }
                    case "service": {
                        String p_query_name = "SELECT name FROM services WHERE id == ?";
                        Cursor c1 = db.rawQuery(p_query_name, new String[]{c.getString(4)});
                        if (c1.moveToFirst() && c1 != null && c1.getCount() > 0) {
                            c1.moveToFirst();
                            if (subject.isEmpty())
                                title.setText(c1.getString(0));
                            else title.setText(subject);
                        }
                        break;
                    }
                    case "event": {
                        String p_query_name = "SELECT name FROM events WHERE id == ?";
                        Cursor c1 = db.rawQuery(p_query_name, new String[]{c.getString(4)});
                        if (c1.moveToFirst() && c1 != null && c1.getCount() > 0) {
                            c1.moveToFirst();
                            if (subject.isEmpty())
                                title.setText(c1.getString(0));
                            else title.setText(subject);
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
