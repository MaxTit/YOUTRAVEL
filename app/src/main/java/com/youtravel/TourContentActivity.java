package com.youtravel;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Debug;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import tools.Contact;

public class TourContentActivity extends AppCompatActivity {
    TabHost tabhost;
    @SuppressWarnings("deprecation")
    static ViewPager gallery;

    @Override
    protected void onResume()
    {
        System.gc();
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        System.gc();
    }

    protected void onDestroy() {

        unbindDrawables(findViewById(R.id.drawer_layout));
        System.gc();
        Runtime.getRuntime().gc();
        super.onDestroy();
    }

    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            if (!(view instanceof AdapterView<?>))
                ((ViewGroup) view).removeAllViews();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_tour_content);
        tabhost = (TabHost) findViewById(android.R.id.tabhost);
        final FrameLayout frame = (FrameLayout) findViewById(R.id.content_frame);
        init_interface();
        final Tour tour = (Tour)getIntent().getSerializableExtra("tour");
        final tools.Contact contact_call = new tools.Contact(this,"tour", tour.id+"");
        output(tour);
       (findViewById(R.id.button2)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("lololo","lololol");
                AlertDialog dialog = contact_call.callType(tour.name);
                dialog.show();
            }
        });
        tabhost.setup();
        TabHost.TabSpec tabspec;

        tabspec = tabhost.newTabSpec("tag1");
        tabspec.setIndicator(getLayoutInflater().inflate (R.layout.tab_info, null));
        tabspec.setContent(R.id.tab1);
        tabhost.addTab(tabspec);

        tabspec = tabhost.newTabSpec("tag2");
        tabspec.setIndicator(getLayoutInflater().inflate (R.layout.tab_desc, null));
        tabspec.setContent(R.id.tab2);
        tabhost.addTab(tabspec);

        tabspec = tabhost.newTabSpec("tag3");
        tabspec.setIndicator(getLayoutInflater().inflate (R.layout.tab_coments, null));
        tabspec.setContent(R.id.tab3);
        tabhost.addTab(tabspec);

        tabhost.setCurrentTabByTag("tag1");

        tabhost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            public void onTabChanged(String tabId) {
                //Toast.makeText(getBaseContext(), "tabId = " + tabId, Toast.LENGTH_SHORT).show();

                switch(tabId)
                {
                    case "tag1": break;
                    case "tag2": break;
                    case "tag3":
                    {
                    }break;
                }
            }
        });
        gallery = (ViewPager) findViewById(R.id.gallery1);
        gallery.setAdapter(new GalleryAdapter(this,tour));
        gallery.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected ( final int position) {
                LinearLayout content = (LinearLayout) findViewById(R.id.gallery_btns);
                ArrayList<ImageView> grl = new ArrayList<ImageView>();
                for (int j =0;j<content.getChildCount();j++)
                    grl.add((ImageView)((RelativeLayout)content.getChildAt(j)).getChildAt(0));

                for (int i=0;i<grl.size();i++)
                {

                    if(i==position)
                        grl.get(i).setImageResource(R.drawable.img_full);
                    else
                        grl.get(i).setImageResource(R.drawable.img_empty);
                }
            }

        });
        /// Analytics
        Bundle params = new Bundle();
        SharedPreferences settings = getApplicationContext().getSharedPreferences("my_data", 0);
        if (settings.getString("id_user", null) != null)
            params.putInt("user_id", Integer.parseInt(settings.getString("id_user", "-1")));
        params.putInt("source_id",tour.id);
        HomeActivity.mFirebaseAnalytics.logEvent("Tour",params);
        ////
    }


    private void output(Tour content){
        TextView title = (TextView) findViewById(R.id.title_tour);
        Log.d("imgtour",content.img);
        title.setText(content.name);
        TextView time = (TextView) findViewById(R.id.time_tour);
        TextView cost = (TextView) findViewById(R.id.cost_tour);
        TextView people = (TextView) findViewById(R.id.people_tour);
        TextView articula1 = (TextView) findViewById(R.id.articula1);
        TextView articula2 = (TextView) findViewById(R.id.articula2);
        TextView articula3 = (TextView) findViewById(R.id.articula3);
        TextView articula4 = (TextView) findViewById(R.id.articula4);
        int dur = content.duration;
        String day;
        if ((dur > 10 && dur < 20)) day = "дней";
        else if (dur % 10 == 1) day = "день";
        else if (dur % 10 <= 4 && dur % 10 != 0) day = "дня";
        else day = "дней";
        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db;
        db = dbHelper.getWritableDatabase();
        String city = "", country = "";
        Cursor c_city = db.rawQuery("SELECT * FROM cities WHERE id = " + content.id_city, new String[]{});
        if (c_city.moveToFirst() && c_city.getCount() > 0) {
            c_city.moveToFirst();
            city = c_city.getString(2);
        }
        c_city.close();
        Cursor c_country = db.rawQuery("SELECT * FROM countries WHERE id = " + content.id_country, new String[]{});
        if (c_country.moveToFirst() && c_country.getCount()>0) {
            c_country.moveToFirst();
            country = c_country.getString(1);
        }
        c_country.close();
        try {
            JSONArray jArray = new JSONArray(content.id_kind);
            JSONObject json_data = null;
            if (jArray != null) {
                String kinds = " ";
                for (int i = 0; i < jArray.length(); i++) {
                    json_data = jArray.getJSONObject(i);
                    if (json_data != null) {
                        Cursor c_kinds = db.rawQuery("SELECT * FROM journey_kinds WHERE id = " + json_data.getString("Тип"), new String[]{});
                        if (c_kinds.moveToFirst() && c_kinds.getCount()>0) {
                            c_kinds.moveToFirst();
                            while (!c_kinds.isAfterLast()) {
                                if(kinds.equals(" "))
                                    kinds += c_kinds.getString(1);
                                else
                                    kinds += ", "+c_kinds.getString(1);
                                c_kinds.moveToNext();
                            }
                        }
                        c_kinds.close();
                    }
                }
                articula1.setText("Вид отдыха:"+kinds);
            }
        } catch (JSONException e1) {
            Log.e("log_tag", "Error no ff " + e1.toString());

        } catch (Exception e1) {
            e1.printStackTrace();
        }
        Spanned htmlAsSpannedTime = Html.fromHtml("<b>"+dur+"</b> "+day); // used by TextView
        time.setText(htmlAsSpannedTime);
        Spanned htmlAsSpannedPeople = Html.fromHtml("<b>"+1+"</b> чел"); // used by TextView
        people.setText(htmlAsSpannedPeople);
        Spanned htmlAsSpannedCost = Html.fromHtml("от <b>"+content.price+"</b> "+content.currency); // used by TextView
        cost.setText(htmlAsSpannedCost);
        articula2.setText("Город (страна): "+city+" ("+country+")");
        articula3.setText("Продолжительность: "+dur+" "+day);
        Spanned htmlAsSpannedCost1 = Html.fromHtml("Цена: <b>"+content.price+"</b> "+content.currency); // used by TextView
        articula4.setText(htmlAsSpannedCost1);
        /////////////////////////////////////////////////tab 2
        TextView info_tour = (TextView) findViewById(R.id.info_tour);
        Spanned htmlAsSpannedInfo = Html.fromHtml(content.extra_info); // used by TextView
        info_tour.setText(htmlAsSpannedInfo);

        try {
            JSONArray jArray = new JSONArray(content.program);
            JSONObject json_data = null;
            LinearLayout program_content = (LinearLayout) findViewById(R.id.program_content);
            if (jArray != null) {
                for (int i = 0; i < jArray.length(); i++) {
                    json_data = jArray.getJSONObject(i);
                    if (json_data != null) {
                        View v = getLayoutInflater().inflate(R.layout.program_content, program_content, false);
                        program_content.addView(v);
                        final TextView date = (TextView) v.findViewById(R.id.program_date);
                        final TextView info = (TextView) v.findViewById(R.id.program_info);
                        date.setText(json_data.getString("Период"));
                        Spanned htmlAsSpanned = Html.fromHtml(json_data.getString("Описание")); // used by TextView
                        info.setText(htmlAsSpanned);
                        if((i+1)%2!=0)
                            v.setBackgroundColor(Color.parseColor("#e5e5e5"));
                    }
                }
            }
        } catch (JSONException e1) {
            Log.e("log_tag", "Error no ff " + e1.toString());

        } catch (Exception e1) {
            e1.printStackTrace();
        }
        db.close();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tour_content, menu);
        menu.findItem(R.id.action_favorite).setIcon(R.drawable.ic_filter);

        return true;
    }



    private void init_interface(){
        final MainMenu menu = new MainMenu(this, "Каталог туров");

        menu.myToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_favorite: {
                        Intent intent = new Intent(TourContentActivity.this, CatalogueActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        return true;
                    }
                }
                return false;
            }
        });

    }

}
