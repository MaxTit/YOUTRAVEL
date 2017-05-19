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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.builders.Actions;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import tools.ContactMailChat;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.youtravel.StartActivityAlternative.*;

public class TourFilteredActivity extends AppCompatActivity implements ScrollViewListener {

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

    static String orderBy = "name ASC";

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        limit = 0;
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/SegoeBold.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_tour_filtered);
        ScrollViewExt scroll = (ScrollViewExt) findViewById(R.id.scroll_view);
        scroll.setScrollViewListener(this);
        init_interface();
        filled = true;
        LinearLayout underframe = (LinearLayout) findViewById(R.id.c);
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
            //underframe.removeViewAt(0);
            filled = false;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
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

    public boolean isTablet(Context context) {
        boolean xlarge = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
        boolean large = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
        return (xlarge || large);
    }

    private void refreshInterface(int mode){

        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        final boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        dbHelper = new DBHelper(this);
        LinearLayout underframe = (LinearLayout) findViewById(R.id.content_rel);
        //LinearLayout underframe1 = underframe;
        ArrayList<Integer> imageViews = new ArrayList<>();
        ArrayList<String> subjects = new ArrayList<>(), id_subjects = new ArrayList<>();
        String content_header;
        boolean isTablet = isTablet(this);
        String day;

//        int splitter = -1;
//        try{
//            underframe1 = (LinearLayout) findViewById(R.id.content_rel2);
//        }
//        catch (NullPointerException ne){
//            isTablet = false;
//        }
        Cursor c;
        SQLiteDatabase db;
        Log.i("REFRESHED", "accessed refreshInterface() method");
        if (mode != 3)
            try {
                underframe.removeAllViewsInLayout();
                limit = 0;
            } catch(NullPointerException ne) {}

        db = dbHelper.getWritableDatabase();
        String b = make_filtered_query(country_filter, kind_filter, city_filter, duration_filter, orderBy);
        c = db.rawQuery(b + " LIMIT 20 OFFSET " + limit, new String[]{});
        Log.i("ScrRef", "" + limit);
        limit+=10;

        Log.d("", "_____" + c.getCount() + "\n " + b);
        if (c.moveToFirst() && c.getCount() > 0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                LinearLayout pair;
                View v = null;
                final Tour tour = new Tour(c, this, getApplicationContext().getSharedPreferences("my_data", 0));
                ArrayList<Date> period = new ArrayList<Date>();
                period.add(tour.period_from);
                period.add(tour.period_to);
                //Log.d("period=",period.get(0)+"");
                if (date_filter != null && !date_filter.isEmpty() && StartActivity.make_date((tour.period_from != null)?tour.period_from:null) != "null" &&
                        outOfRange(period, date_filter.get(0), date_filter.get(1))) {
                    Log.d("", "+++++++++++++++++" + StartActivity.make_date(date_filter.get(0)));
                    c.moveToNext();
                    continue;
                } else if (price_filter != null && !price_filter.isEmpty() &&
                        (tour.price < price_filter.get(0) || tour.price > price_filter.get(1))) {
                    Log.d("", "+++++++++++++++++" + StartActivity.make_date(date_filter.get(0)));
                    c.moveToNext();
                    continue;
                }
                else {
                    pair = (LinearLayout) getLayoutInflater().inflate(R.layout.pair, underframe, false);
                    underframe.addView(pair);

                    v = getLayoutInflater().inflate(R.layout.catalogue_content, pair, false);
                    pair.addView(v);
                    v.findViewById(R.id.info_button).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(TourFilteredActivity.this, TourContentActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            intent.putExtra("tour", tour);
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

                    final ContactMailChat contact_mail = new ContactMailChat(this, "tour", tour.id + "");
                    v.findViewById(R.id.mail_button).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog dialog = contact_mail.callType(tour.name);
                            dialog.show();
                        }
                    });

                    content_header = tour.name;


                    final TextView header = (TextView) v.findViewById(R.id.content);
                    final TextView content = (TextView) v.findViewById(R.id.articula);
                    final TextView contentDay = (TextView) v.findViewById(R.id.articula1);
                    header.setText(content_header);
                    String days = "", city = "", duration = "";
                    Cursor c_tour = db.rawQuery("SELECT * FROM tours WHERE id = " + tour.id, new String[]{});
                    if (c_tour.moveToFirst() && c_tour.getCount() > 0) {
                        c_tour.moveToFirst();
                        days = tour.days;
                        duration = c_tour.getString(4);
                    }

                    Cursor c_city = db.rawQuery("SELECT * FROM cities WHERE id = " + tour.id_city, new String[]{});
                    if (c_city.moveToFirst() && c_city.getCount() > 0) {
                        c_city.moveToFirst();
                        city = c_city.getString(2);
                    }
                    Calendar cal = Calendar.getInstance();
                    cal.set(2900, 11, 11);
                    if (earliest == null)
                        outOfRange(period, Calendar.getInstance().getTime(), cal.getTime());
                    content_header = String.valueOf(Math.round((double) Math.round(tour.price * 100) / 100)) + " " + tour.currency;

                    // -------------------------------------------------------------------------------------------
                    final String[] urls = tour.img.split(",");
                    if (!urls[0].isEmpty()) {
                        final ImageView imageView = (ImageView) v.findViewById(R.id.image);
                        if ((new File(getCacheDir() + "/Images/" + urls[0])).exists()) {
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                            Bitmap bitmap_img = BitmapFactory.decodeFile(getCacheDir() + "/Images/" + urls[0], options);
                            imageView.setImageBitmap(bitmap_img);
                            Log.i("ImDow", "Subject = " + tour.id + urls[0]);
                        } else {
                            final String subject = "tour", id_subject = tour.id+"";
                            if (isConnected)
                                imageView.post(new Runnable() {
                                    public void run() {
                                        if ((new File(getCacheDir() + "/Images").exists())) {
                                            Log.i("ImDow", "true");
                                        }
                                        (new ImageDownloader(TourFilteredActivity.this, subject, id_subject)).run();
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
                    earliest = null;
                    c.moveToNext();
                    c_city.close();
                    c_tour.close();

                    int dur = Integer.parseInt(duration);
                    if ((dur > 10 && dur < 20)) day = "дней";
                    else if (dur % 10 == 1) day = "день";
                    else if (dur % 10 <= 4 && dur % 10 != 0) day = "дня";
                    else day = "дней";
                    contentDay.setText(city + ", " + duration + " " + day + (!days.isEmpty() ? (": " + days) : ""));
                    content.setText("от " + content_header);


                }
                // **********************************************************************************************************************************


                if (!c.isAfterLast() && isTablet) {
                    View v1;
                    final Tour tour1 = new Tour(c, this, getApplicationContext().getSharedPreferences("my_data", 0));
                    ArrayList<Date> period1 = new ArrayList<Date>();
                    period1.add(tour1.period_from);
                    period1.add(tour1.period_to);
                    while ((date_filter != null && !date_filter.isEmpty() && StartActivity.make_date((tour1.period_from != null ) ? tour1.period_from : null) != "null" &&
                            outOfRange(period1, date_filter.get(0), date_filter.get(1))) || (price_filter != null && !price_filter.isEmpty() &&
                            (tour1.price < price_filter.get(0) || tour1.price > price_filter.get(1)))) {
                        if (c.isAfterLast()) break;
                        Log.d("", "+++++++++++++++++" + StartActivity.make_date(date_filter.get(0)));
                        c.moveToNext();
                        Log.d("", "+++++++++++++++++" + StartActivity.make_date(date_filter.get(0)));
                    }
                    if (c.isAfterLast()) {
                        View v2 = getLayoutInflater().inflate(R.layout.catalogue_content2, pair, false);
                        v2.setVisibility(View.INVISIBLE);
                        pair.addView(v2);
                        break;
                    }


                    v1 = getLayoutInflater().inflate(R.layout.catalogue_content2, pair, false);
                    pair.addView(v1);
                    v1.findViewById(R.id.info_button).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(TourFilteredActivity.this, TourContentActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            intent.putExtra("tour", tour1);
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                        }
                    });

                    v1.findViewById(R.id.call_button).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+380504694030"));
                            startActivity(Intent.createChooser(intent, "Позвонить"));
                        }
                    });

                    final ContactMailChat contact_mail = new ContactMailChat(this, "tour", tour1.id + "");
                    v1.findViewById(R.id.mail_button).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog dialog = contact_mail.callType(tour1.name);
                            dialog.show();
                        }
                    });

                    content_header = tour1.name;


                    final TextView header1 = (TextView) v1.findViewById(R.id.content);
                    final TextView content1 = (TextView) v1.findViewById(R.id.articula);
                    final TextView contentDay1 = (TextView) v1.findViewById(R.id.articula1);
                    header1.setText(content_header);
                    String days = "";
                    String city = "";
                    String duration = "";
                    Cursor c_tour = db.rawQuery("SELECT * FROM tours WHERE id = " + tour1.id, new String[]{});
                    if (c_tour.moveToFirst() && c_tour.getCount() > 0) {
                        c_tour.moveToFirst();
                        days = tour1.days;
                        duration = c_tour.getString(4);
                    }

                    Cursor c_city = db.rawQuery("SELECT * FROM cities WHERE id = " + tour1.id_city, new String[]{});
                    if (c_city.moveToFirst() && c_city.getCount() > 0) {
                        c_city.moveToFirst();
                        city = c_city.getString(2);
                    }
                    Calendar cal = Calendar.getInstance();
                    cal.set(2900, 11, 11);
                    if (earliest == null)
                        outOfRange(period1, Calendar.getInstance().getTime(), cal.getTime());
                    content_header = String.valueOf(Math.round((double) Math.round(tour1.price * 100) / 100)) + " " + tour1.currency;

                    // -------------------------------------------------------------------------------------------

                    final String [] urls1 = tour1.img.split(",");
                    if (!urls1[0].isEmpty()){
                        final ImageView imageView = (ImageView)v1.findViewById(R.id.image);
                        if ((new File(getCacheDir() + "/Images/" + urls1[0])).exists()) {
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                            Bitmap bitmap_img = BitmapFactory.decodeFile(getCacheDir() + "/Images/" + urls1[0], options);
                            imageView.setImageBitmap(bitmap_img);
                            Log.i("ImDow", "Subject = " + tour1.id + urls1[0]);
                        }
                        else{
                            final String subject = "tour", id_subject = tour1.id+"";

                            if (isConnected)
                                imageView.post(new Runnable() {
                                    public void run() {
                                        if ((new File(getCacheDir() + "/Images").exists())) {
                                            Log.i("ImDow", "true");
                                        }
                                        (new ImageDownloader(TourFilteredActivity.this, subject, id_subject)).run();
                                        if ((new File(getCacheDir() + "/Images/" + urls1[0])).exists()) {
                                            BitmapFactory.Options options = new BitmapFactory.Options();
                                            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                                            Bitmap bitmap_img = BitmapFactory.decodeFile(getCacheDir() + "/Images/" + urls1[0], options);
                                            imageView.setImageBitmap(bitmap_img);
                                        }
                                    }
                                });
                        }
                    }
                    // -------------------------------------------------------------------------------------------

                    earliest = null;
                    c.moveToNext();
                    c_city.close();
                    c_tour.close();
                    int dur = Integer.parseInt(duration);
                    if ((dur > 10 && dur < 20)) day = "дней";
                    else if (dur % 10 == 1) day = "день";
                    else if (dur % 10 <= 4 && dur % 10 != 0) day = "дня";
                    else day = "дней";
                    contentDay1.setText(city + ", " + duration + " " + day + (!days.isEmpty() ? (": " + days) : ""));
                    content1.setText("от " + content_header);

                }
                else if (isTablet){
                    View v1 = getLayoutInflater().inflate(R.layout.catalogue_content2, pair, false);
                    v1.setVisibility(View.INVISIBLE);
                    pair.addView(v1);
                }
            }



            // -------------------------------------------------------------------------------------------
        }

        if (underframe.getChildCount() == 0){
            LinearLayout underframe2 = (LinearLayout) findViewById(R.id.c);
            View mes = getLayoutInflater().inflate(R.layout.dump_text, underframe2, false);
            underframe2.addView(mes);
            ImageView show_all = (ImageView) mes.findViewById(R.id.button);
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
        final MainMenu menu = new MainMenu(this, "Каталог туров");
        final SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
   //     menu.myToolbar.setTitle("Каталог туров");



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

        final TextView sortName = (TextView) findViewById(R.id.sort_name);
        final TextView sortPrice = (TextView) findViewById(R.id.sort_price);
        final TextView sortNew = (TextView) findViewById(R.id.sort_new);
        if (orderBy.equals("name ASC")) {
            SpannableString content = new SpannableString(sortName.getText());
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            sortName.setText(content);
            sortName.setTextColor(getResources().getColor(R.color.blue));
        }
        else if (orderBy.equals("price ASC")) {
            SpannableString content = new SpannableString(sortPrice.getText());
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            sortPrice.setText(content);
            sortPrice.setTextColor(getResources().getColor(R.color.blue));
        }
        else if (orderBy.equals("date DESC")) {
            SpannableString content = new SpannableString(sortNew.getText());
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            sortNew.setText(content);
            sortNew.setTextColor(getResources().getColor(R.color.blue));
        }

        sortName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpannableString content = new SpannableString(sortName.getText());
                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                sortName.setText(content);
                sortPrice.setText("цене");
                sortNew.setText("новизне");
                orderBy = "name ASC";
                refreshInterface(1);
                sortName.setTextColor(getResources().getColor(R.color.blue));
                sortPrice.setTextColor(getResources().getColor(R.color.gray_light));
                sortNew.setTextColor(getResources().getColor(R.color.gray_light));
            }
        });

        sortPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpannableString content = new SpannableString(sortPrice.getText());
                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                sortPrice.setText(content);
                sortName.setText("названию");
                sortNew.setText("новизне");
                orderBy = "price ASC";
                refreshInterface(1);
                sortPrice.setTextColor(getResources().getColor(R.color.blue));
                sortName.setTextColor(getResources().getColor(R.color.gray_light));
                sortNew.setTextColor(getResources().getColor(R.color.gray_light));
            }
        });

        sortNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpannableString content = new SpannableString(sortNew.getText());
                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                sortNew.setText(content);
                sortPrice.setText("цене");
                sortName.setText("названию");
                orderBy = "date DESC";
                refreshInterface(1);
                sortNew.setTextColor(getResources().getColor(R.color.blue));
                sortPrice.setTextColor(getResources().getColor(R.color.gray_light));
                sortName.setTextColor(getResources().getColor(R.color.gray_light));
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
                                       ArrayList<String> city_filter, ArrayList<Integer> duration_filter, String orderBy) {
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
                + ") ORDER BY " + orderBy;
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
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        FirebaseUserActions.getInstance().start(getIndexApiAction0());
    }

    @Override
    public void onStop() {
        super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
        FirebaseUserActions.getInstance().end(getIndexApiAction0());

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public com.google.firebase.appindexing.Action getIndexApiAction0() {
        return Actions.newView("TourFiltered", "http://[ENTER-YOUR-URL-HERE]");
    }
}
