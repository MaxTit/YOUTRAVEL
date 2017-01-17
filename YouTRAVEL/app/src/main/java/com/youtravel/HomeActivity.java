package com.youtravel;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

public class HomeActivity extends Activity {

	float lastTranslate = 0.0f;
	float lastTranslate1 = 0.0f;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		final ListView mDrawerList = (ListView) findViewById(R.id.left_drawer);
		final DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		final FrameLayout frame = (FrameLayout) findViewById(R.id.content_frame);
		mDrawerLayout.setScrimColor(getResources().getColor(android.R.color.transparent));


		@SuppressWarnings("deprecation")
		ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_launcher, R.string.app_name, R.string.app_name)
		{
			@SuppressLint("NewApi")
			public void onDrawerSlide(View drawerView, float slideOffset)
			{
				float moveFactor = (drawerView.getWidth() * slideOffset);

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				{
					if(drawerView.getId()==mDrawerList.getId())
						frame.setTranslationX(-moveFactor);
					else
						frame.setTranslationX(moveFactor);
				}
				else
				{
					TranslateAnimation anim;
					if(drawerView.getId()==mDrawerList.getId())
						anim= new TranslateAnimation(lastTranslate, -moveFactor, 0.0f, 0.0f);
					else
						anim= new TranslateAnimation(lastTranslate, moveFactor, 0.0f, 0.0f);
					anim.setDuration(0);
					anim.setFillAfter(true);
					frame.startAnimation(anim);

					lastTranslate = moveFactor;
				}
			}
		};

		mDrawerLayout.setDrawerListener(mDrawerToggle);
		/*allp.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//if(mDrawerLayout.isDrawerOpen(mDrawerLayout))
				//mDrawerLayout.closeDrawers();
				//else
				mDrawerLayout.openDrawer(mDrawerList);

			}});*/
		ArrayList<String> listpr = new ArrayList<String>();
		listpr.add("Меню1");
		listpr.add("Меню2");
		listpr.add("Меню3");
		listpr.add("Меню4");
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item,R.id.text1, listpr));
		mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapter, View v, int position,
									long arg3)
			{

			}
		});
		/*Вывод данных пример
					Cursor c;
		    		SQLiteDatabase db = dbHelper.getWritableDatabase();
		    	    String p_query = "select * from reports where name = ?";
		    	    c = db.rawQuery(p_query,new String[]{value});
		    		if (c.moveToFirst()&& c !=null && c.getCount()>0 ) {
		    			c.moveToFirst();
		    			while (!c.isAfterLast()) {
		    				id_rep = c.getString(0);
		    				print_report();
		    				c.moveToNext();
		    			}
		    		}
		    		c.close();
		    		db.close();
		    		*/
	}
}
