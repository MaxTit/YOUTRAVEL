package com.youtravel;

import android.app.SearchManager;
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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
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
import java.util.ArrayList;
import java.util.Date;

import tools.ContactMailChat;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.youtravel.StartActivityAlternative.*;

public class ServicesActivity extends AppCompatActivity implements ScrollViewListener {

    static DBHelper dbHelper;
    private static boolean filled;
    private static Bundle freshTours;
    private static Bundle freshEvents;
    private static Bundle freshServices;
    private static Bundle freshExcursions;
    long diff0 = 0;
    static Boolean fl = true;
    static int limit = 0;
    static String search_qu = null;

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


    public static Bundle getFreshTours() {
        return freshTours;
    }

    public static void setFreshTours(Bundle freshTours) {
        ServicesActivity.freshTours = freshTours;
    }

    public static Bundle getFreshServices() {
        return freshServices;
    }

    public static void setFreshServices(Bundle freshServices) {
        ServicesActivity.freshServices = freshServices;
    }

    public static Bundle getFreshEvents() {
        return freshEvents;
    }

    public static void setFreshEvents(Bundle freshEvents) {
        ServicesActivity.freshEvents = freshEvents;
    }

    public static Bundle getFreshExcursions() {
        return freshExcursions;
    }

    public static void setFreshExcursions(Bundle freshExcursions) {
        ServicesActivity.freshExcursions = freshExcursions;
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
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_excursions);
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
            //LinearLayout underframe = (LinearLayout) findViewById(R.id.content_rel);
            //underframe.removeViewAt(0);
            filled = false;
        }
    }


    public void notifyFresh() {
        new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                if (getFreshServices() != null) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            ((getFreshServices().getIntegerArrayList("new").size() > 0) ? "Новых услуг: " + getFreshServices().getIntegerArrayList("new").size() : "") +
                                    ((getFreshServices().getIntegerArrayList("updated").size() > 0) ? "Обновлено услуг: " + getFreshServices().getIntegerArrayList("new").size() : ""),
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        }, 0);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
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
        String search_text = "";
        if(search_qu!=null)
        {
            search_text = " WHERE (name like '%"+search_qu+"%'" +
                    " or annotation like '%"+search_qu+"%'" +
                    " or extra_info like '%"+search_qu+"%'" +
                    " or description like '%"+search_qu+"%')";
        }
        c = db.rawQuery("SELECT * FROM services" + search_text+" ORDER BY name ASC" + " LIMIT 20 OFFSET " + limit, new String[]{});
        Log.i("ScrRef", "" + limit);
        limit+=10;

        Log.d("", "_____" + c.getCount() + "\n ");
        if (c.moveToFirst() && c.getCount()>0 ) {
            c.moveToFirst();
            LinearLayout pair = null;
            while (!c.isAfterLast()) {
                View v;
                final Service service = new Service(c, this, getApplicationContext().getSharedPreferences("my_data", 0));
                if(isTablet)
                {
                    if((c.getPosition()+1)%2 !=0) {
                        pair = (LinearLayout) getLayoutInflater().inflate(R.layout.pair, underframe, false);
                        underframe.addView(pair);
                        v = getLayoutInflater().inflate(R.layout.catalogue_content, pair, false);
                        pair.addView(v);
                    }
                    else
                    {
                        v = getLayoutInflater().inflate(R.layout.catalogue_content2, pair, false);
                        pair.addView(v);
                    }
                }
                else {
                    pair = (LinearLayout) getLayoutInflater().inflate(R.layout.pair, underframe, false);
                    underframe.addView(pair);
                    v = getLayoutInflater().inflate(R.layout.catalogue_content, pair, false);
                    pair.addView(v);
                }

                v.findViewById(R.id.info_button).setOnClickListener(new android.view.View.OnClickListener() {
                    @Override
                    public void onClick(View v){
                        Intent intent = new Intent(ServicesActivity.this, ServiceContentActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                        intent.putExtra("service", service);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                    }
                });
                v.findViewById(R.id.call_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+380504694030"));
                        startActivity(Intent.createChooser(intent, "Позвонить"));
                    }
                });

                final ContactMailChat contact_mail = new ContactMailChat(this, "service", service.id + "");
                v.findViewById(R.id.mail_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog dialog = contact_mail.callType(service.name);
                        dialog.show();
                    }
                });

                // -------------------------------------------------------------------------------------------
                final String[] urls = service.img.split(",");
                if (!urls[0].isEmpty()) {
                    final ImageView imageView = (ImageView) v.findViewById(R.id.image);
                    if ((new File(getCacheDir() + "/Images/" + urls[0])).exists()) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        Bitmap bitmap_img = BitmapFactory.decodeFile(getCacheDir() + "/Images/" + urls[0], options);
                        imageView.setImageBitmap(bitmap_img);
                        Log.i("ImDow", "Subject = " + service.id + urls[0]);
                    } else {
                        final String subject = "service", id_subject = service.id+"";
                        if (isConnected)
                            imageView.post(new Runnable() {
                                public void run() {
                                    if ((new File(getCacheDir() + "/Images").exists())) {
                                        Log.i("ImDow", "true");
                                    }
                                    (new ImageDownloader(ServicesActivity.this, subject, id_subject)).run();
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

                final TextView header = (TextView) v.findViewById(R.id.content);
                final TextView content = (TextView) v.findViewById(R.id.articula);
                final TextView contentDay = (TextView) v.findViewById(R.id.articula1);
                String content_header = service.name;
                header.setText(content_header);
                contentDay.setText(service.annotation);
                content.setText("от " +  String.valueOf(Math.round((double) Math.round(service.price * 100) / 100)) + " " + service.currency);
                c.moveToNext();
            }
        }
        if (mode == 0) {
            final int count = underframe.getChildCount();
            new Handler().postDelayed(new Runnable() {
                @Override public void run() {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Услуг найдено: " + count,
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
        getMenuInflater().inflate(R.menu.main_search, menu);
        menu.findItem(R.id.action_favorite).setIcon(R.drawable.ic_filter);

        // search item
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        if(null!=searchManager ) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                if(query.length()>0)
                {
                    limit=0;
                    search_qu = query;
                    refreshInterface(1);
                }
                else
                {
                    if(search_qu!= null)
                    {
                        limit = 0;
                        search_qu = null;
                        refreshInterface(1);
                    }
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        MenuItemCompat.setOnActionExpandListener(searchItem,new MenuItemCompat.OnActionExpandListener()
        {

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item)
            {
                limit = 0;
                search_qu = null;
                Log.d("close search ", "999");
                refreshInterface(1);
                return true; // Return true to collapse action view
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item)
            {
                // TODO Auto-generated method stub
                return true;
            }
        });
        return true;
    }



    private void init_interface() {
        final MainMenu menu = new MainMenu(this, "Услуги");
        final SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);

        menu.myToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_favorite: {
                        Intent intent = new Intent(ServicesActivity.this, CatalogueActivity.class); //here ff
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
                            fresh = update_services();
                            context.putString("context",   "услуг");
                            context.putIntegerArrayList("new", fresh.drained_new);
                            context.putIntegerArrayList("updated", fresh.drained_updated);
                            msg.setData(context);
                            handler.sendMessage(msg);
                            update_tours();
                            update_countries();
                            update_cities();
                            update_objects();
                            update_events();
                            update_journey_kinds();
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
    }


}
