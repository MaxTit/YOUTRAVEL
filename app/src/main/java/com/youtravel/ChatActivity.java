package com.youtravel;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
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
import android.Manifest;

public class ChatActivity extends AppCompatActivity {
    public static String idChat = null;
    public static String typeChat = "-1";
    public static String subjectChat = "-1";
    public static String idSource = "-1";
    public static boolean newMessage = false;
    static boolean paused = true;
    static DBHelper dbHelper;
    static SharedPreferences settings;
    static RelativeLayout login_v;
    static LinearLayout chat;
    static ScrollView scrollView;
    static Button enter;
    static EditText mess;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        menu.findItem(R.id.action_add).setIcon(R.drawable.ic_add);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                // Check the SDK version and whether the permission is already granted or not.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
                    //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
                } else {
                    showContacts();
                }
            } else {
                Toast.makeText(this, "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showContacts()
    {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                ChatActivity.this);
        //builderSingle.setIcon(R.drawable.ic_launcher);
        builderSingle.setTitle("Контакты");
        final ArrayAdapter<String> names = new ArrayAdapter<String>(
                ChatActivity.this,
                android.R.layout.select_dialog_singlechoice);
        final ArrayList<String> emails = new ArrayList<String>();
        final ArrayList<String> phones = new ArrayList<String>();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                Cursor pCur = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{id}, null);
                Cursor emailCur = cr.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID
                                + " = ?", new String[] { id }, null);
                names.add(name);
                if (pCur.moveToNext()) {
                    String phoneNo = pCur.getString(pCur.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NUMBER));



                    if (phoneNo!=null )
                    {
                        phones.add(phoneNo);
                    }
                    else phones.add("nophone");
                }
                if (emailCur.moveToNext()) {
                    // This would allow you get several email addresses
                    // if the email addresses were stored in an array
                    String email = emailCur
                            .getString(emailCur
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                    if ( email!= null)
                    {
                        emails.add(email);
                    }
                    else emails.add("noemail");

                }
                pCur.close();

            }
            /////////
            builderSingle.setAdapter(names,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //String strName = arrayAdapter.getItem(which);
                            Log.d("contact","phone: "+phones.get(which)+" email: "+emails.get(which));
                            ConnectivityManager cm =
                                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                            final boolean isConnected = activeNetwork != null &&
                                    activeNetwork.isConnectedOrConnecting();
                            if (isConnected) {
                                // TODO Auto-generated method stub
                                try {
                                    HttpClient httpclient = new DefaultHttpClient();
                                    HttpPost httppost = new HttpPost(StartActivity.server + "/send_chat_mes.php");
                                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                                    nameValuePairs.add(new BasicNameValuePair("email", emails.get(which)));
                                    nameValuePairs.add(new BasicNameValuePair("phone", phones.get(which))); // TODO: Телефон с вида +7 915 387-32-72 к виду +7 (917) 625-6998 может бть nophone
                                    nameValuePairs.add(new BasicNameValuePair("id_chat", idChat));
                                    nameValuePairs.add(new BasicNameValuePair("id_member", settings.getString("id_user", null)));
                                    nameValuePairs.add(new BasicNameValuePair("message", "Я добавил "+names.getItem(which)+" к диалогу."));
                                    nameValuePairs.add(new BasicNameValuePair("type", typeChat));
                                    nameValuePairs.add(new BasicNameValuePair("subject", subjectChat));
                                    nameValuePairs.add(new BasicNameValuePair("id_source", idSource));
                                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
                                    // Execute HTTP Post Request
                                    HttpResponse response = httpclient.execute(httppost);
                                    String res = EntityUtils.toString(response.getEntity());
                                    if(!res.equals("nouser")) {
                                        if(!res.equals("useradd")) {
                                            idChat = res;
                                            Log.d("idchat", idChat);
                                            StartActivity.hideKeyboard(ChatActivity.this);
                                            refresh();
                                        } else {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                                            AlertDialog dialog0 = builder.create();
                                            dialog0.setTitle("Пользователь уже находится в этом диалоге");
                                            dialog0.setButton("Ok", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                }
                                            });
                                            dialog0.setCancelable(false);
                                            dialog0.show();
                                        }
                                    } else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                                        AlertDialog dialog0 = builder.create();
                                        dialog0.setTitle("Пользователь не зарегистрирован в системе");
                                        dialog0.setButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                                        });
                                        dialog0.setCancelable(false);
                                        dialog0.show();
                                    }
                                } catch (ClientProtocolException e) {
                                    // TODO Auto-generated catch block
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                }


                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                                AlertDialog dialog0 = builder.create();
                                dialog0.setTitle("Отсутствует подключение к Интернету");
                                dialog0.setButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                                dialog0.setCancelable(false);
                                dialog0.show();
                            }

                        }
                    });
            builderSingle.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                public void onClick(final DialogInterface dialog, final int which) {

                }});
            builderSingle.setCancelable(true);
            builderSingle.show();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_chat);
        newMessage = false;
        mess = (EditText) findViewById(R.id.editText1);
        login_v = (RelativeLayout) findViewById(R.id.login);
        chat = (LinearLayout) findViewById(R.id.chat);
        enter = (Button) findViewById(R.id.enter);
        scrollView = (ScrollView) findViewById(R.id.scroll_view);
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
                    refresh();
                    init();
                }
            });
        } else {
            init();
        }
        final MainMenu menu = new MainMenu(this);
        menu.myToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_add: {
                        showContacts();
                        return true;
                    }
                }
                return false;
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        paused = false;
        tim();
    }

    private void tim() {
        if (!paused)
            new CountDownTimer(10000, 10000) {
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    if (newMessage) {
                        refresh();
                        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
                    }
                    tim();
                }
            }.start();
    }

    private void init() {
        login_v.setVisibility(View.GONE);
        chat.setVisibility(View.VISIBLE);
        if (idChat.equals("-1") && !typeChat.equals("-1")) {
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
        } else {
            ContentValues cv = new ContentValues();
            cv.put("isRead", "1");
            (new DBHelper(this)).getWritableDatabase().update("chat_mes", cv, "id_chat == ? AND isRead == -1", new String[]{idChat});
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
                            nameValuePairs.add(new BasicNameValuePair("subject", subjectChat));
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
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
                ContentValues cv = new ContentValues();
                cv.put("isRead", "1");
                (new DBHelper(ChatActivity.this)).getWritableDatabase().update("chat_mes", cv, "id_chat == ? AND isRead == -1", new String[]{idChat});
                refreshMes();
            }
        }, 1000);
    }

    private void refreshMes() {
        dbHelper = new DBHelper(this);

        LinearLayout underframe = (LinearLayout) findViewById(R.id.messages);
        Cursor c;
        SQLiteDatabase db;

        try {
            underframe.removeAllViewsInLayout();
        } catch (NullPointerException ne) {
        }

        db = dbHelper.getWritableDatabase();
        String p_query = "SELECT * FROM chat_mes WHERE id_chat = ? ORDER BY date_update";                                                   //TODO except not actual tours
        c = db.rawQuery(p_query, new String[]{idChat});
        Log.d("chatPrint", c.getCount() + "");
        if (c.moveToFirst() && c != null && c.getCount() > 0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                View v = null;
                v = getLayoutInflater().inflate(R.layout.item_mes_chat, underframe, false);
                underframe.addView(v);
                Log.d("member", settings.getString("id_user", null) + " " + c.getString(2) + " " + c.getString(3));
                if (settings.getString("id_user", null).equals(c.getString(2))) {
                    final RelativeLayout content = (RelativeLayout) v.findViewById(R.id.content_left);
                    content.setVisibility(View.VISIBLE);
                    final TextView from = (TextView) v.findViewById(R.id.from_left);
                    from.setText("Я:");
                    final TextView mes = (TextView) v.findViewById(R.id.message_left);
                    mes.setText(c.getString(4));
                } else {
                    final RelativeLayout content = (RelativeLayout) v.findViewById(R.id.content_right);
                    content.setVisibility(View.VISIBLE);
                    final TextView from = (TextView) v.findViewById(R.id.from_right);
                    from.setText(c.getString(3));
                    final TextView mes = (TextView) v.findViewById(R.id.message_right);
                    mes.setText(c.getString(4));
                }

                c.moveToNext();
            }
            scrollView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    View view = (View) scrollView.getChildAt(scrollView.getChildCount() - 1);
                    scrollView.scrollTo(0, view.getBottom());

                    Log.i("FILL", "FILLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL");
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

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Chat Page") // TODO: Define a title for the content shown.
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
