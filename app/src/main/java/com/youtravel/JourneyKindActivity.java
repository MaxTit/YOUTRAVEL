package com.youtravel;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import static com.youtravel.StartActivityAlternative.*;

public class JourneyKindActivity extends AppCompatActivity {

    float lastTranslate = 0.0f;
    static DBHelper dbHelper;
    private static boolean filled;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_journey_kind);
        init_interface();
        filled = true;
        LinearLayout underframe = (LinearLayout) findViewById(R.id.content_rel);
        View loading = getLayoutInflater().inflate(R.layout.loading, underframe, false);
        underframe.addView(loading);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if(hasFocus && filled){
            refreshInterface(0);
            //LinearLayout underframe = (LinearLayout) findViewById(R.id.content_rel);
            //underframe.removeViewAt(0);
            filled = false;
        }
    }

    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1)
                refreshInterface(0);
            super.handleMessage(msg);
        }
    };

    private void refreshInterface(int mode) {
        dbHelper = new DBHelper(this);

        LinearLayout underframe = (LinearLayout) findViewById(R.id.content_rel);
        Cursor c;
        SQLiteDatabase db;
        db = dbHelper.getWritableDatabase();

        try {
            underframe.removeAllViewsInLayout();
        } catch(NullPointerException ne) {}

        String p_query = "SELECT * FROM journey_kinds ORDER BY name ASC";                                                   //TODO except not actual tours
        c = db.rawQuery(p_query,new String[]{});
        if (c.moveToFirst() && c!=null && c.getCount()>0 ) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                View v = null;
                final JourneyKind journey_kind = new JourneyKind(c);

                v = getLayoutInflater().inflate(R.layout.kind_content, underframe, false);
                underframe.addView(v);

                v.findViewById(R.id.bar).setOnClickListener(new FilteringListener
                        (null,
                                FilteringListener.make_array(String.valueOf(journey_kind.id)), null) {
                    @Override
                    public void onClick(View v){
                        Intent intent = new Intent(JourneyKindActivity.this, TourFilteredActivity.class);
                        TourFilteredActivity.kind_filter = kind_filter;
                        TourFilteredActivity.country_filter = country_filter;
                        TourFilteredActivity.city_filter = city_filter;
                        TourFilteredActivity.date_filter = null;
                        CatalogueActivity.kind_filter = kind_filter;
                        CatalogueActivity.date_filter = null;
                        CatalogueActivity.fromJK = journey_kind.name;
                        CatalogueActivity.country_filter = null;
                        CatalogueActivity.city_filter = null;
                        CatalogueActivity.duration_filter = null;
                        CatalogueActivity.price_filter = null;
                        CatalogueActivity.city_last_pos = 0;
                        CatalogueActivity.country_last_pos = 0;
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                    }
                });

                final TextView content = (TextView)v.findViewById(R.id.content);
                String content_header = journey_kind.name + "\n" + journey_kind.annotation;
                content.setText(content_header);


                c.moveToNext();
            }
        }
        if (mode == 0) {
            final int count = underframe.getChildCount();
            new Handler().postDelayed(new Runnable() {
                @Override public void run() {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Видов отдыха найдено: " + count,
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
            }, 0);
        }
        c.close();
        db.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_favorite).setIcon(R.drawable.ic_search);
        return true;
    }

    private void init_interface(){
        final MainMenu menu = new MainMenu(this);

        menu.myToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_favorite: {
                        Intent intent = new Intent(JourneyKindActivity.this, CatalogueActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        return true;
                    }
                }
                return false;
            }
        });


        final SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeLayout.setRefreshing(true);
                AsyncTask<String, String, String> refresh = new AsyncTask<String, String, String>() {
                    @Override
                    protected String doInBackground(String... params) {

                        try {
                            update_journey_kinds();
                            Message msg = handler.obtainMessage();
                            handler.sendMessage(msg);
                            msg.what = 1;
                            update_tours();
                            update_countries();
                            update_cities();
                            update_events();
                            update_objects();
                            update_services();
                            update_excursions();
                            update_comments();
                            update_article();
                            if (settings.getString("email_user", null) != null) {
                                update_chat(settings.getString("id_user", null));
                                update_chat_mes(settings.getString("id_user", null));
                            }
                            update_currency();
                            update_currency_data();
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
                        swipeLayout.setRefreshing(false);
                    }
                },  1000);
            }

        });
    }

}
