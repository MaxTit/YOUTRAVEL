package com.youtravel;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class MainMenu {
    float lastTranslate = 0.0f;
    public final DrawerLayout mDrawerLayout;
    public final Toolbar myToolbar;

    public MainMenu(final AppCompatActivity context, String name)
    {
        final ListView mDrawerList = (ListView) context.findViewById(R.id.left_drawer);
        mDrawerLayout = (DrawerLayout) context.findViewById(R.id.drawer_layout);
        myToolbar = (Toolbar) context.findViewById(R.id.my_toolbar);
        context.setSupportActionBar(myToolbar);
        context.getSupportActionBar().setDisplayShowTitleEnabled(false);
        mDrawerLayout.setScrimColor(context.getResources().getColor(android.R.color.transparent));
        @SuppressWarnings("deprecation")
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(context, mDrawerLayout, R.drawable.ic_launcher, R.string.app_name, R.string.app_name)
        {
            @SuppressLint("NewApi")
            public void onDrawerSlide(View drawerView, float slideOffset)
            {
                float moveFactor = (drawerView.getWidth() * slideOffset);
                TranslateAnimation anim;
                if(drawerView.getId()==mDrawerList.getId())
                    anim= new TranslateAnimation(lastTranslate, -moveFactor, 0.0f, 0.0f);
                else
                    anim= new TranslateAnimation(lastTranslate, moveFactor, 0.0f, 0.0f);
                anim.setDuration(0);
                anim.setFillAfter(true);
                //	frame.startAnimation(anim);

                lastTranslate = moveFactor;

            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        myToolbar.setNavigationOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }});
        ArrayList<String> listpr = new ArrayList<>();
        int quantity = (new DBHelper(context))
                .getWritableDatabase()
                .rawQuery("SELECT * FROM chat_mes WHERE isRead == -1", null)
                .getCount();


        listpr.add("Каталог туров");
        listpr.add("Страны");
        listpr.add("Виды отдыха");
        listpr.add("Мероприятия");
        listpr.add("Экскурсии");
        listpr.add("Услуги");
        listpr.add("Чат     " + (quantity == 0 ? "" : "(" + String.valueOf(quantity) + ")" ));
        listpr.add("Мой кабинет");
        listpr.add("Настройки");
        mDrawerList.setAdapter(new ArrayAdapter<>(context, R.layout.list_item,R.id.text1, listpr));
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3)
            {
                switch (position){
                    case 0:
                    {
                        Log.d("case 0","BUTTON_TOUCHED");
                        ImageDownloader.deleteImages(context);
                        Intent intent = new Intent(context, TourFilteredActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        context.startActivity(intent);
                        context.overridePendingTransition(0, 0);
                        break;
                    }

                    case 1:
                    {
                        Log.d("case 1","BUTTON_TOUCHED");
                        ImageDownloader.deleteImages(context);
                        Intent intent = new Intent(context, CountryActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        context.startActivity(intent);
                        context.overridePendingTransition(0, 0);
                        break;
                    }

                    case 2:
                    {
                        Log.d("case 2","BUTTON_TOUCHED");
                        ImageDownloader.deleteImages(context);
                        Intent intent = new Intent(context, JourneyKindActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        context.startActivity(intent);
                        context.overridePendingTransition(0, 0);
                        break;
                    }

                    case 3:
                    {
                        Log.d("case 3","BUTTON_TOUCHED");
                        ImageDownloader.deleteImages(context);
                        Intent intent = new Intent(context, Events.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        context.startActivity(intent);
                        context.overridePendingTransition(0, 0);
                        break;
                    }

                    case 4:
                    {
                        Log.d("case 4","BUTTON_TOUCHED");
                        ImageDownloader.deleteImages(context);
                        Intent intent = new Intent(context, ExcursionsActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        context.startActivity(intent);
                        context.overridePendingTransition(0, 0);
                        break;
                    }

                    case 5:
                    {
                        Log.d("case 5","BUTTON_TOUCHED");
                        ImageDownloader.deleteImages(context);
                        Intent intent = new Intent(context, ServicesActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        context.startActivity(intent);
                        context.overridePendingTransition(0, 0);
                        break;
                    }

                    case 6:
                    {
                        Log.d("case 6","BUTTON_TOUCHED");
                        ImageDownloader.deleteImages(context);
                        Intent intent = new Intent(context, MenuChatActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        context.startActivity(intent);
                        context.overridePendingTransition(0, 0);
                        break;
                    }

                    case 7:
                    {
                        Log.d("case 7","BUTTON_TOUCHED");
                        ImageDownloader.deleteImages(context);
                        Intent intent = new Intent(context, ProfileActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        context.startActivity(intent);
                        context.overridePendingTransition(0, 0);
                        break;
                    }

                    case 8:
                    {
                        Log.d("case 8", "BUTTON_TOUCHED");
                        ImageDownloader.deleteImages(context);
                        Intent intent = new Intent(context, SettingsActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        context.startActivity(intent);
                        context.overridePendingTransition(0, 0);
                        break;
                    }
                }
            }
        });

    }
}
