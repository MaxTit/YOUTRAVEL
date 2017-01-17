package com.youtravel;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.youtravel.StartActivityAlternative.*;

public class TourFilteredActivity extends AppCompatActivity {

    static DBHelper dbHelper;
    public static ArrayList<String> country_filter;
    public static ArrayList<String> kind_filter;
    public static ArrayList<String> city_filter;
    public static ArrayList<Integer> duration_filter;
    public static ArrayList<Integer> price_filter;
    public static ArrayList<Date> date_filter;
    private static boolean filled;
    private static Bundle freshTours;
    private static Bundle freshEvents;
    private static Bundle freshServices;
    private static Bundle freshExcursions;
    private static Date earliest;


    public static Bundle getFreshTours() {
        return freshTours;
    }

    public static void setFreshTours(Bundle freshTours) {
        TourFilteredActivity.freshTours = freshTours;
    }

    public static Bundle getFreshServices() {
        return freshServices;
    }

    public static void setFreshServices(Bundle freshServices) {
        TourFilteredActivity.freshServices = freshServices;
    }

    public static Bundle getFreshEvents() {
        return freshEvents;
    }

    public static void setFreshEvents(Bundle freshEvents) {
        TourFilteredActivity.freshEvents = freshEvents;
    }

    public static Bundle getFreshExcursions() {
        return freshExcursions;
    }

    public static void setFreshExcursions(Bundle freshExcursions) {
        TourFilteredActivity.freshExcursions = freshExcursions;
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_tour_filtered);
        init_interface();
        filled = true;
        LinearLayout underframe = (LinearLayout) findViewById(R.id.content_rel);
        View loading = getLayoutInflater().inflate(R.layout.loading, underframe, false);
        underframe.addView(loading);


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if(hasFocus && filled){
            refreshInterface(0);
            LinearLayout underframe = (LinearLayout) findViewById(R.id.content_rel);
            //underframe.removeViewAt(0);
            filled = false;
        }
    }

    private boolean outOfRange(ArrayList<Date> dateList, Date dateMin, Date dateMax) {
        boolean KNF = true;
        for (Date d : dateList) {
            KNF = KNF && (d.before(dateMin) || d.after(dateMax));
            if (!KNF) {
                earliest = d;
                break;
            }
        }
        return KNF;
    }
    //////////////


    public void notifyFresh() {
        new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                if (getFreshTours() != null) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            ((getFreshTours().getIntegerArrayList("new").size() > 0) ? "Новых туров: " + getFreshTours().getIntegerArrayList("new").size() : "") +
                                    ((getFreshTours().getIntegerArrayList("updated").size() > 0) ? "Обновлено туров: " + getFreshTours().getIntegerArrayList("new").size() : ""),
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        }, 0);
    }



    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                refreshInterface(1);
                Bundle data = msg.getData();
                Log.i("HANDLED", "HANDLED");
                switch (data.getString("context", "")) {
                    case "туров":
                        if (data.getIntegerArrayList("new").size() + data.getIntegerArrayList("updated").size() > 0)
                            setFreshTours(data);
                        break;
                    case "услуг":
                        if (data.getIntegerArrayList("new").size() + data.getIntegerArrayList("updated").size() > 0)
                            setFreshServices(data);
                        break;
                    case "мероприятий":
                        if (data.getIntegerArrayList("new").size() + data.getIntegerArrayList("updated").size() > 0)
                            setFreshEvents(data);
                        break;
                    case "экскурсий":
                        if (data.getIntegerArrayList("new").size() + data.getIntegerArrayList("updated").size() > 0)
                            setFreshExcursions(data);
                        break;
                }
            }
            super.handleMessage(msg);
        }
    };

    private void refreshInterface(int mode){


        dbHelper = new DBHelper(this);
        LinearLayout underframe = (LinearLayout) findViewById(R.id.content_rel);
        Cursor c;
        SQLiteDatabase db;
        Log.i("REFRESHED", "accessed refreshInterface() method");

        try {
            underframe.removeAllViewsInLayout();
        } catch(NullPointerException ne) {}

        db = dbHelper.getWritableDatabase();
        String b = make_filtered_query(country_filter, kind_filter, city_filter, duration_filter);
        c = db.rawQuery(b, new String[]{});
        Log.d("", "_____" + c.getCount() + "\n " + b);
        if (c.moveToFirst() && c.getCount() > 0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                View v;
                final Tour tour = new Tour(c, this, getApplicationContext().getSharedPreferences("my_data", 0));
                if (date_filter != null && !date_filter.isEmpty() && StartActivity.make_date((tour.date != null && !tour.date.isEmpty())?tour.date.get(0):null) != "null" &&
                        outOfRange(tour.date, date_filter.get(0), date_filter.get(1))) {
                    Log.d("", "+++++++++++++++++" + StartActivity.make_date(date_filter.get(0)));
                    c.moveToNext();
                    continue;
                } else if (price_filter != null && !price_filter.isEmpty() &&
                        (tour.price < price_filter.get(0) || tour.price > price_filter.get(1))) {
                    Log.d("", "+++++++++++++++++" + StartActivity.make_date(date_filter.get(0)));
                    c.moveToNext();
                    continue;
                }
                v = getLayoutInflater().inflate(R.layout.catalogue_content, underframe, false);
                underframe.addView(v);

                v.findViewById(R.id.bar).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(TourFilteredActivity.this, TourContentActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        intent.putExtra("tour", tour);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                    }
                });

                String content_header = tour.name + "\n" + ((double) Math.round(tour.price * 100)) / 100 + " " + tour.currency;              //+ StartActivity.make_date(tour.date) +"\n"+ tour.annotation;

                final TextView header = (TextView) v.findViewById(R.id.content);
                final TextView content = (TextView) v.findViewById(R.id.articula);
                header.setText(content_header);
                String city = "", country = "";
                Cursor c_city = db.rawQuery("SELECT * FROM cities WHERE id = " + tour.id_city, new String[]{});
                if (c_city.moveToFirst() && c_city.getCount() > 0) {
                    c_city.moveToFirst();
                    city = c_city.getString(2);
                }

                Cursor c_country = db.rawQuery("SELECT * FROM countries WHERE id = " + tour.id_country, new String[]{});
                if (c_country.moveToFirst() && c_country.getCount() > 0) {
                    c_country.moveToFirst();
                    country = c_country.getString(1);
                }
                Calendar cal = Calendar.getInstance();
                cal.set(2900,11,11);
                if (earliest==null) outOfRange(tour.date, Calendar.getInstance().getTime(), cal.getTime());
                content_header = "Дата: " + StartActivity.make_date(earliest)
                        + "\n" + "Продолжительность: "
                        + tour.duration + " " + ((tour.duration % 10 == 1) ? "день" :
                        ((tour.duration % 10 > 1 && tour.duration % 10 < 5) ? "дня" : "дней"))
                        + "\n" + "Тип экскурсии: " + ""
                        + "\n" + "Город (Страна): " + city
                        + " (" + country + ")";


                earliest = null;
                c.moveToNext();
                c_city.close();
                c_country.close();

                content.setText(content_header);
            }
        }

        if (underframe.getChildCount() == 0){
            View mes = getLayoutInflater().inflate(R.layout.dump_text, underframe, false);
            underframe.addView(mes);
            Button show_all = (Button) mes.findViewById(R.id.button);
            show_all.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                    CatalogueActivity.country_last_pos = 0;
                    CatalogueActivity.kind_last_pos = 0;
                    CatalogueActivity.city_last_pos = 0;
                    CatalogueActivity.first_time = true;
                    refreshInterface(1);
                }
            });
        }
        else if (mode == 0) {
            final int count = underframe.getChildCount();
            new Handler().postDelayed(new Runnable() {
                @Override public void run() {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "Туров найдено: " + count,
                                    Toast.LENGTH_SHORT);
                            toast.show();
                }
            }, 0);
        } else if (mode == 1){
            notifyFresh();
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


    private void init_interface() {
        final MainMenu menu = new MainMenu(this);
        final SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);

        menu.myToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_favorite: {
                        Intent intent = new Intent(TourFilteredActivity.this, CatalogueActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        return true;
                    }
                }
                return false;
            }
        });

        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeLayout.setRefreshing(true);
                AsyncTask<String, String, String> refresh = new AsyncTask<String, String, String>() {
                    @Override
                    protected String doInBackground(String... params) {

                        try {
                            news fresh;
                            Message msg = handler.obtainMessage();
                            Bundle context = new Bundle();
                            msg.what = 1;
                            fresh = update_tours();
                            context.putString("context",   "туров");
                            context.putIntegerArrayList("new", fresh.drained_new);
                            context.putIntegerArrayList("updated", fresh.drained_updated);
                            msg.setData(context);
                            handler.sendMessage(msg);
                            update_countries();
                            update_cities();
                            update_objects();
                            update_events();
                            update_journey_kinds();
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
                }, 3000);
            }

        });
      //  new ShowContentTask().execute(0, 0, 0);
    }


    private String make_filtered_query(ArrayList<String> country_filter, ArrayList<String> kind_filter,
                                       ArrayList<String> city_filter, ArrayList<Integer> duration_filter) {
        final String name_country = " id_country = ";
        final String name_kind = " id_kind = ";
        final String name_city = " id_city = ";

        return "SELECT * FROM tours WHERE ("
                + ((!transcript_filter(country_filter, name_country).isEmpty()) ? (transcript_filter(country_filter, name_country))
                : "(id_country IS NOT NULL)")
                + ((!transcript_filter(kind_filter, name_kind).isEmpty()) ? (" AND " + transcript_filter(kind_filter, name_kind))
                : " ")
                + ((!transcript_filter(city_filter, name_city).isEmpty()) ? (" AND " + transcript_filter(city_filter, name_city))
                : " ")
                + ((duration_filter != null) ? "AND duration >= " + duration_filter.get(0).toString() + " AND duration <= " + duration_filter.get(1).toString() : "")
                + ") ORDER BY name ASC";
    }

    private String transcript_filter(ArrayList<String> filter, String name) {
        String query = "";
        if (filter != null) {
            query += "(";
            for (int i = 0; i < filter.size(); i++) {
                query += name + filter.get(i) + ((i != (filter.size() - 1)) ? " OR" : ")");
            }
        }
        return query;
    }


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("TourFiltered Page") // TODO: Define a title for the content shown.
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

    @Override
    protected void onResume() {
        super.onResume();

    }
}
