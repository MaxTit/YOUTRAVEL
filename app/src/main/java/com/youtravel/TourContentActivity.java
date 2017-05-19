package com.youtravel;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

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
        //final tools.Contact contact_call = new tools.Contact(this,"tour", tour.id+"");
        output(tour);
       /* (findViewById(R.id.button2)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("lololo","lololol");
                AlertDialog dialog = contact_call.callType(tour.name);
                dialog.show();
            }
        });*/
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
               /* final TextView[] grl = {(TextView)findViewById(R.id.gr_a),(TextView)findViewById(R.id.gr_b),(TextView)findViewById(R.id.gr_c),(TextView)findViewById(R.id.gr_d),(TextView)findViewById(R.id.gr_e),(TextView)findViewById(R.id.gr_f)};

                for (int i=0;i<grl.length;i++)
                {

                    if(i==(position%6))
                        grl[i].setTextColor(Color.parseColor("#e4bd36"));
                    else
                        grl[i].setTextColor(Color.parseColor("#2e5a94"));
                }*/
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
        title.setText(content.name);

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tour_content, menu);
        menu.findItem(R.id.action_favorite).setIcon(R.drawable.ic_search);

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
