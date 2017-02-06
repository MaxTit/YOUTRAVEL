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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
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

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

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
    boolean isCreateNewChatField = false;
    static SharedPreferences settings;
    static RelativeLayout login_v;
    static LinearLayout chat;
    static ScrollView scrollView;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_menu_chat);
        scrollView = (ScrollView) findViewById(R.id.scroll_view);
        init_interface();
        login_v = (RelativeLayout) findViewById(R.id.login);
        chat = (LinearLayout) findViewById(R.id.chat_menu);
        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        settings = getApplicationContext().getSharedPreferences("my_data", 0);
        if (settings.getString("id_user", null) == null) {
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
        } else {
            login_v.setVisibility(View.GONE);
            chat.setVisibility(View.VISIBLE);
            refreshDialogs();
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
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
                        if (!isCreateNewChatField) {
                            final LinearLayout underframe = (LinearLayout) findViewById(R.id.dialog_grid);
                            final View candidate = getLayoutInflater().inflate(R.layout.item_new_chat_dialog, underframe, false);
                            underframe.addView(candidate);
                            findViewById(R.id.tick).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    EditText title = (EditText) candidate.findViewById(R.id.text1);
                                    if(title.getText().toString().length() > 0 && !title.getText().toString().equals("-1")) {
                                        // TODO add to db new one
                                        Intent intent = new Intent(MenuChatActivity.this, ChatActivity.class);
                                        ChatActivity.idChat = "-1";
                                        ChatActivity.typeChat = "-1";
                                        ChatActivity.idSource = "-1";
                                        ChatActivity.subjectChat = title.getText().toString();
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                        MenuChatActivity.this.startActivity(intent);
                                        underframe.removeView(candidate);
                                        isCreateNewChatField = false;
                                    } else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(MenuChatActivity.this);
                                        AlertDialog dialog = builder.create();
                                        dialog.setTitle("Введите имя диалога!");
                                        Message listenerDoesNotAccept = null;
                                        dialog.setButton("Ok", listenerDoesNotAccept);
                                        dialog.setCancelable(false);
                                        dialog.show();
                                    }
                                }
                            });
                            findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    underframe.removeView(candidate);
                                    isCreateNewChatField = false;
                                }
                            });
                        }
                        scrollView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
                                scrollView.scrollTo(0, view.getBottom());
                                Log.i("Finish", "Getting down..");
                            }
                        }, 100);
                        isCreateNewChatField = true;
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void refresh() {
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
            @Override
            public void run() {
                // stop refresh
                refreshDialogs();
            }
        }, 1000);
    }

    private void refreshDialogs() {
        dbHelper = new DBHelper(this);

        LinearLayout underframe = (LinearLayout) findViewById(R.id.dialog_grid);
        Cursor c;
        SQLiteDatabase db;

        try {
            underframe.removeAllViewsInLayout();
        } catch (NullPointerException ne) {
        }

        db = dbHelper.getWritableDatabase();
        String p_query = "SELECT * FROM chat ORDER BY date_update";
        c = db.rawQuery(p_query, new String[]{});
        Log.d("chatPrint", c.getCount() + "");
        if (c.moveToFirst() && c != null && c.getCount() > 0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                View v = null;
                v = getLayoutInflater().inflate(R.layout.item_chat_dialog, underframe, false);
                underframe.addView(v);
                final String idChat = c.getString(0);
                Log.d("chat",(c.getString(3) == null ) ? "empty" : c.getString(3));
                final String subject = (c.getString(3) == null || c.getString(3).equals("-1")) ? "" : c.getString(3);
                v.findViewById(R.id.content).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!isCreateNewChatField) {
                            Intent intent = new Intent(MenuChatActivity.this, ChatActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            ChatActivity.idChat = idChat;
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                        }
                    }
                });
                if (db.rawQuery("SELECT * FROM chat_mes WHERE id_chat ==" + c.getString(0) + " AND isRead == -1", null)
                        .getCount() > 0) {
                    v.setBackgroundColor(Color.parseColor("#A9A9A9"));
                }

                final TextView title = (TextView) v.findViewById(R.id.text1);
                switch (c.getString(2)) {
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
                    default: title.setText(subject); break;
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

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("MenuChat Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
