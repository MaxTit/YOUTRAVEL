package com.youtravel;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.youtravel.StartActivityAlternative.*;

public class CountryActivity extends AppCompatActivity implements ScrollViewListener{

    static DBHelper dbHelper;
    private static boolean filled;
    long diff0 = 0;
    static Boolean fl = true;
    static int limit = 0;

    @Override
    public void onScrollChanged(ScrollViewExt scrollView, int x, int y, int oldx, int oldy) {
        // We take the last son in the scrollview
        //  LinearLayout content =(LinearLayout)findViewById(R.id.linearLayout2);
        View view = (View) scrollView.getChildAt(scrollView.getChildCount() - 1);
        long diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));
        // if diff is zero, then the bottom has been reached
        if (diff == 0 && diff0!=0 ) {
            // do stuff
            if(fl)
            {
                fl = false;
                refreshInterface(3);

            }
        }
        else fl = true;
        diff0 =diff;
        Log.d("nololololo", "" + diff + "" + diff0);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        limit = 0;
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/SegoeBold.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //САМАЯ ВАЖНАЯ СТРОКА В ПРОГРАММЕ. КРАЕУГОЛЬНЫЙ КАМЕНЬ ПРОЕКТА
        setContentView(R.layout.activity_country);
        ScrollViewExt scroll = (ScrollViewExt) findViewById(R.id.scroll_view);
        scroll.setScrollViewListener(this);
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
            filled = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_favorite).setIcon(R.drawable.ic_filter);
        return true;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1)
                refreshInterface(0);
            super.handleMessage(msg);
        }
    };
    public boolean isTablet(Context context) {
        boolean xlarge = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
        boolean large = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
        return (xlarge || large);
    }
    private void refreshInterface(int mode) {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        final boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        dbHelper = new DBHelper(this);

        LinearLayout underframe = (LinearLayout) findViewById(R.id.content_rel);
        Cursor c;
        SQLiteDatabase db;
        boolean isTablet = isTablet(this);

        if (mode != 3)
            try {
                underframe.removeAllViewsInLayout();
                limit = 0;
            } catch(NullPointerException ne) {}

        db = dbHelper.getWritableDatabase();
        String p_query = "SELECT * FROM countries WHERE " +
                "countries.id in (SELECT id_country FROM tours WHERE tours.id_country = countries.id LIMIT 1)" +
                " ORDER BY name ASC";                                                   //TODO except not actual tours
        c = db.rawQuery(p_query + " LIMIT 20 OFFSET " + limit, new String[]{});
        Log.i("ScrRef", "" + limit);
        limit+=10;

        Log.d("", "_____" + c.getCount() + "\n " + p_query);
        if (c.moveToFirst() && c!=null && c.getCount()>0 )
        {
            c.moveToFirst();
            LinearLayout pair = null;
            while (!c.isAfterLast()) {

                View v = null;
                final Country country = new Country(c);
                if(isTablet)
                {
                    if((c.getPosition()+1)%2 !=0) {
                        pair = (LinearLayout) getLayoutInflater().inflate(R.layout.pair, underframe, false);
                        underframe.addView(pair);
                        v = getLayoutInflater().inflate(R.layout.country_content, pair, false);
                        pair.addView(v);
                    }
                    else
                    {
                        v = getLayoutInflater().inflate(R.layout.country_content2, pair, false);
                        pair.addView(v);
                    }
                }
                else {
                    pair = (LinearLayout) getLayoutInflater().inflate(R.layout.pair, underframe, false);
                    underframe.addView(pair);
                    v = getLayoutInflater().inflate(R.layout.country_content, pair, false);
                    pair.addView(v);
                }
                v.findViewById(R.id.bar).setOnClickListener(new FilteringListener
                        (FilteringListener.make_array(String.valueOf(country.id)), null, null) {
                    @Override
                    public void onClick(View v){
                        Intent intent = new Intent(CountryActivity.this, TourFilteredActivity.class);
                        TourFilteredActivity.country_filter = country_filter;
                        TourFilteredActivity.kind_filter = kind_filter;
                        TourFilteredActivity.city_filter = city_filter;
                        TourFilteredActivity.date_filter = null;
                        CatalogueActivity.country_filter = country_filter;
                        CatalogueActivity.fromCountries = country.name;
                        CatalogueActivity.date_filter = null;
                        CatalogueActivity.kind_filter = null;
                        CatalogueActivity.city_filter = null;
                        CatalogueActivity.duration_filter = null;
                        CatalogueActivity.price_filter = null;
                        CatalogueActivity.city_last_pos = 0;
                        CatalogueActivity.kind_last_pos = 0;
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                    }
                });
                // -------------------------------------------------------------------------------------------
                final String[] urls = country.img.split(",");
                if (!urls[0].isEmpty()) {
                    final ImageView imageView = (ImageView) v.findViewById(R.id.image);
                    if ((new File(getCacheDir() + "/Images/" + urls[0])).exists()) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        Bitmap bitmap_img = BitmapFactory.decodeFile(getCacheDir() + "/Images/" + urls[0], options);
                        imageView.setImageBitmap(bitmap_img);
                        Log.i("ImDow", "Subject = " + country.id + urls[0]);
                    } else {
                        final String subject = "country", id_subject = country.id+"";
                        if (isConnected)
                            imageView.post(new Runnable() {
                                public void run() {
                                    if ((new File(getCacheDir() + "/Images").exists())) {
                                        Log.i("ImDow", "true");
                                    }
                                    (new ImageDownloader(CountryActivity.this, subject, id_subject)).run();
                                    if ((new File(getCacheDir() + "/Images/" + urls[0])).exists()) {
                                        BitmapFactory.Options options = new BitmapFactory.Options();
                                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                                        Bitmap bitmap_img = BitmapFactory.decodeFile(getCacheDir() + "/Images/" + urls[0], options);
                                        imageView.setImageBitmap(bitmap_img);
                                    }
                                }
                            });
                    }
                }
                // -------------------------------------------------------------------------------------------
                final TextView content = (TextView)v.findViewById(R.id.content);
                String content_header = country.name;
                content.setText(content_header);
                c.moveToNext();
            }
        }
        /*if (underframe.getChildCount() == 0){
            LinearLayout underframe2 = (LinearLayout) findViewById(R.id.c);
            View mes = getLayoutInflater().inflate(R.layout.dump_text, underframe2, false);
            underframe2.addView(mes);

        }
        else*/ if (mode == 0) {
            final int count = underframe.getChildCount();
            new Handler().postDelayed(new Runnable() {
                @Override public void run() {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Стран найдено: " + count,
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
            }, 0);
        } else if (mode == 1){
        }
        c.close();
        db.close();
    }

    private void init_interface(){
        final MainMenu menu = new MainMenu(this, "Страны");

        menu.myToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_favorite: {
                        Intent intent = new Intent(CountryActivity.this, CatalogueActivity.class);
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
                            update_countries();
                            update_cities();
                            Message msg = handler.obtainMessage();
                            handler.sendMessage(msg);
                            msg.what = 1;
                            update_events();
                            update_tours();
                            update_objects();
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
                }, 1000);
            }

        });
    }

}
