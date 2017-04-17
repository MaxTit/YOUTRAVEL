package com.youtravel;

import android.app.AlarmManager;
import android.app.PendingIntent;
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
import android.os.Looper;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.youtravel.StartActivityAlternative.*;

public class HomeActivity extends AppCompatActivity {

    public static AlarmManager alarm;
    static PendingIntent pintent;
    public static FirebaseAnalytics mFirebaseAnalytics;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/SegoeBold.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
        setContentView(R.layout.activity_home);
        notifyFresh();
        init_interface();
        show_last_tours();
        pushrun(getApplicationContext());

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public static void pushrun(final Context base) {
        //////////////////////////////////
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Intent intent = new Intent(base, PushService.class);
        pintent = PendingIntent.getService(base, 0, intent, 0);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {

                alarm = (AlarmManager) base.getSystemService(Context.ALARM_SERVICE);
                //for 30 mint 60*60*1000
                alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTime().getTime(),
                        1000 * 60, pintent);
                base.startService(new Intent(base, PushService.class));
            }
        });
        ///////////////////////////////
    }

    public static void pushcansel() {
        //////////////////////////////////

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {

                alarm.cancel(pintent);
            }
        });
        ///////////////////////////////
    }

    public void notifyFresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (freshTours != null) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            ((freshTours.getIntegerArrayList("new").size() > 0) ? "Новых туров: " + freshTours.getIntegerArrayList("new").size() : "") +
                                    ((freshTours.getIntegerArrayList("updated").size() > 0) ? "Обновлено туров: " + freshTours.getIntegerArrayList("new").size() : ""),
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        }, 0);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (freshEvents != null) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            ((freshEvents.getIntegerArrayList("new").size() > 0) ? "Новых мероприятий: " + freshEvents.getIntegerArrayList("new").size() : "") +
                                    ((freshEvents.getIntegerArrayList("updated").size() > 0) ? "Обновлено мероприятий: " + freshEvents.getIntegerArrayList("new").size() : ""),
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        }, 2100);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (freshServices != null) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            ((freshServices.getIntegerArrayList("new").size() > 0) ? "Новых услуг: " + freshServices.getIntegerArrayList("new").size() : "") +
                                    ((freshServices.getIntegerArrayList("updated").size() > 0) ? "Обновлено услуг: " + freshServices.getIntegerArrayList("new").size() : ""),
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        }, 2100 + 2100);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (freshExcursions != null) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            ((freshExcursions.getIntegerArrayList("new").size() > 0) ? "Новых экскурсий: " + freshExcursions.getIntegerArrayList("new").size() : "") +
                                    ((freshExcursions.getIntegerArrayList("updated").size() > 0) ? "Обновлено экскурсий: " + freshExcursions.getIntegerArrayList("new").size() : ""),
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        }, 2100 + 2100 + 2100);

    }

    DBHelper dbHelper;
    private static Bundle freshTours;
    private static Bundle freshEvents;
    private static Bundle freshServices;
    private static Bundle freshExcursions;
    private static Date earliest;

    private boolean isTablet(Context context) {
        boolean xlarge = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
        boolean large = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
        return (xlarge || large);
    }

    private void show_last_tours() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        final boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        LinearLayout underframe = (LinearLayout) findViewById(R.id.content_tours);
        dbHelper = new DBHelper(this);
        LinearLayout underframe1 = underframe;
//        boolean isTablet = true;
//        try{
//            underframe1 = (LinearLayout) findViewById(R.id.content_rel2);
//        }
//        catch (NullPointerException ne){
//            isTablet = false;
//        }
        boolean isTablet = isTablet(this);
        Cursor c;

        try {
            underframe.removeAllViewsInLayout();
        } catch (NullPointerException ne) {
        }

        SQLiteDatabase db;
/////////////////////////////////////////////////
        db = dbHelper.getWritableDatabase();
        String b = "SELECT * FROM tours ORDER BY date_update DESC LIMIT 5";
        c = db.rawQuery(b, new String[]{});
        Log.d("", "_____" + c.getCount() + "\n " + b);
        if (c.moveToFirst() && c.getCount() > 0) {
            c.moveToFirst();
            int i = 0;
            while (!c.isAfterLast()) {
                LinearLayout pair = (LinearLayout) getLayoutInflater().inflate(R.layout.pair, underframe, false);
                underframe.addView(pair);
                View v;
                final Tour tour = new Tour(c, this, getApplicationContext().getSharedPreferences("my_data", 0));

                v = getLayoutInflater().inflate(R.layout.catalogue_content, pair, false);
                pair.addView(v);
                v.findViewById(R.id.info_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(HomeActivity.this, TourContentActivity.class);
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

                final tools.ContactMailChat contact_mail = new tools.ContactMailChat(this, "tour", tour.id + "");
                v.findViewById(R.id.mail_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog dialog = contact_mail.callType(tour.name);
                        dialog.show();
                    }
                });

                String content_header = tour.name;


                final TextView header = (TextView) v.findViewById(R.id.content);
                final TextView content = (TextView) v.findViewById(R.id.articula);
                final TextView contentDay = (TextView) v.findViewById(R.id.articula1);
                header.setText(content_header);
                String days = "", city = "", duration = "";
                Cursor c_tour = db.rawQuery("SELECT * FROM tours WHERE id = " + tour.id, new String[]{});
                if (c_tour.moveToFirst() && c_tour.getCount() > 0) {
                    c_tour.moveToFirst();
                    days = c_tour.getString(19);
                    duration = c_tour.getString(4);
                }

                Cursor c_city = db.rawQuery("SELECT * FROM cities WHERE id = " + tour.id_city, new String[]{});
                if (c_city.moveToFirst() && c_city.getCount() > 0) {
                    c_city.moveToFirst();
                    city = c_city.getString(2);
                }
                Calendar cal = Calendar.getInstance();
                cal.set(2900, 11, 11);
                content_header = String.valueOf(Math.round((double) Math.round(tour.price * 100) / 100)) + " " + tour.currency;

// -------------------------------------------------------------------------------------------
                final String[] urls = c.getString(12).split(",");
                if (!urls[0].isEmpty()) {
                    final ImageView imageView = (ImageView) v.findViewById(R.id.image);
                    if ((new File(getCacheDir() + "/Images/" + urls[0])).exists()) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        Bitmap bitmap_img = BitmapFactory.decodeFile(getCacheDir() + "/Images/" + urls[0], options);
                        imageView.setImageBitmap(bitmap_img);
                        Log.i("ImDow", "Subject = " + c.getString(0) + urls[0]);
                    } else {
                        final String subject = "tour", id_subject = c.getString(0);
                        if (isConnected)
                            imageView.post(new Runnable() {
                                public void run() {
                                    if ((new File(getCacheDir() + "/Images").exists())) {
                                        Log.i("ImDow", "true");
                                    }
                                    (new ImageDownloader(HomeActivity.this, subject, id_subject)).run();
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
                String day;
                int dur = Integer.parseInt(duration);
                if ((dur > 10 && dur < 20)) day = "дней";
                else if (dur % 10 == 1) day = "день";
                else if (dur % 10 <= 4 && dur % 10 != 0) day = "дня";
                else day = "дней";
                contentDay.setText(city + ", " + duration + " " + day + (!days.isEmpty() ? (": " + days) : ""));
                content.setText("от " + content_header);


                // **********************************************************************************************************************************


                if (!c.isAfterLast() && isTablet) {
                    View v1;
                    final Tour tour1 = new Tour(c, this, getApplicationContext().getSharedPreferences("my_data", 0));


                    v1 = getLayoutInflater().inflate(R.layout.catalogue_content2, pair, false);
                    pair.addView(v1);
                    v1.findViewById(R.id.info_button).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(HomeActivity.this, TourContentActivity.class);
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

                    final tools.ContactMailChat contact_mail1 = new tools.ContactMailChat(this, "tour", tour1.id + "");
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
                    days = "";
                    city = "";
                    duration = "";
                    c_tour = db.rawQuery("SELECT * FROM tours WHERE id = " + tour1.id, new String[]{});
                    if (c_tour.moveToFirst() && c_tour.getCount() > 0) {
                        c_tour.moveToFirst();
                        days = c_tour.getString(19);
                        duration = c_tour.getString(4);
                    }

                    c_city = db.rawQuery("SELECT * FROM cities WHERE id = " + tour1.id_city, new String[]{});
                    if (c_city.moveToFirst() && c_city.getCount() > 0) {
                        c_city.moveToFirst();
                        city = c_city.getString(2);
                    }
                    cal = Calendar.getInstance();
                    cal.set(2900, 11, 11);
                    content_header = String.valueOf(Math.round((double) Math.round(tour1.price * 100) / 100)) + " " + tour1.currency;

// -------------------------------------------------------------------------------------------
                    final String[] urls1 = c.getString(12).split(",");
                    if (!urls1[0].isEmpty()) {
                        final ImageView imageView = (ImageView) v.findViewById(R.id.image);
                        if ((new File(getCacheDir() + "/Images/" + urls1[0])).exists()) {
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                            Bitmap bitmap_img = BitmapFactory.decodeFile(getCacheDir() + "/Images/" + urls1[0], options);
                            imageView.setImageBitmap(bitmap_img);
                            Log.i("ImDow", "Subject = " + c.getString(0) + urls1[0]);
                        } else {
                            final String subject = "tour", id_subject = c.getString(0);
                            if (isConnected)
                                imageView.post(new Runnable() {
                                    public void run() {
                                        if ((new File(getCacheDir() + "/Images").exists())) {
                                            Log.i("ImDow", "true");
                                        }
                                        (new ImageDownloader(HomeActivity.this, subject, id_subject)).run();
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
                    dur = Integer.parseInt(duration);
                    if ((dur > 10 && dur < 20)) day = "дней";
                    else if (dur % 10 == 1) day = "день";
                    else if (dur % 10 <= 4 && dur % 10 != 0) day = "дня";
                    else day = "дней";
                    contentDay1.setText(city + ", " + duration + " " + day + (!days.isEmpty() ? (": " + days) : ""));
                    content1.setText("от " + content_header);

                }
                else if (isTablet){
                    //LinearLayout.LayoutParams p = (LinearLayout.LayoutParams)(v.getLayoutParams());
                    View v1 = getLayoutInflater().inflate(R.layout.catalogue_content2, pair, false);
                    v1.setVisibility(View.INVISIBLE);
                    pair.addView(v1);
                    //v.setLayoutParams(p);
                }
            }
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

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1)
                show_last_tours();
            super.handleMessage(msg);
        }
    };

    private void init_interface() {
        final MainMenu menu = new MainMenu(this, "Горячие туры");
        final ImageView showElse = (ImageView)findViewById(R.id.show_else);
        final SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        menu.myToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_favorite: {
                        Intent intent = new Intent(HomeActivity.this, CatalogueActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        return true;
                    }
                }
                return false;
            }
        });

        showElse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, TourFilteredActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                TourFilteredActivity.orderBy = "date DESC";
                startActivity(intent);
                overridePendingTransition(0, 0);
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
                            update_tours();
                            Message msg = handler.obtainMessage();
                            msg.what = 1;
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
                    @Override
                    public void run() {
                        // stop refresh
                        swipeLayout.setRefreshing(false);
                    }
                }, 3000);
            }

        });


    }


    public static Bundle getFreshTours() {
        return freshTours;
    }

    public static void setFreshTours(Bundle freshTours) {
        HomeActivity.freshTours = freshTours;
    }

    public static Bundle getFreshServices() {
        return freshServices;
    }

    public static void setFreshServices(Bundle freshServices) {
        HomeActivity.freshServices = freshServices;
    }

    public static Bundle getFreshEvents() {
        return freshEvents;
    }

    public static void setFreshEvents(Bundle freshEvents) {
        HomeActivity.freshEvents = freshEvents;
    }

    public static Bundle getFreshExcursions() {
        return freshExcursions;
    }

    public static void setFreshExcursions(Bundle freshExcursions) {
        HomeActivity.freshExcursions = freshExcursions;
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Home Page") // TODO: Define a title for the content shown.
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
