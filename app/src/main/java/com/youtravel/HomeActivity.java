package com.youtravel;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.youtravel.StartActivityAlternative.*;

public class HomeActivity extends AppCompatActivity {

	public static AlarmManager alarm;
	static PendingIntent pintent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_home);
		notifyFresh();
		init_interface();
		show_last_tours();
		pushrun(getApplicationContext());
	}

	public static void pushrun(final Context base)
	{
		//////////////////////////////////
		final Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		Intent intent = new Intent(base, TestService.class);
		pintent = PendingIntent.getService(base, 0, intent, 0);
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			public void run() {

				alarm = (AlarmManager) base.getSystemService(Context.ALARM_SERVICE);
				//for 30 mint 60*60*1000
				alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTime().getTime(),
						1000*60, pintent);
				base.startService(new Intent(base, TestService.class));
			}
		});
		///////////////////////////////
	}
	public static void pushcansel()
	{
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
			@Override public void run() {
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
			@Override public void run() {
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
			@Override public void run() {
				if (freshServices != null) {
					Toast toast = Toast.makeText(getApplicationContext(),
							((freshServices.getIntegerArrayList("new").size() > 0) ? "Новых услуг: " + freshServices.getIntegerArrayList("new").size() : "") +
									((freshServices.getIntegerArrayList("updated").size() > 0) ? "Обновлено услуг: " + freshServices.getIntegerArrayList("new").size() : ""),
							Toast.LENGTH_SHORT);
					toast.show();
				}
			}
		}, 2100+2100);
		new Handler().postDelayed(new Runnable() {
			@Override public void run() {
				if (freshExcursions != null) {
					Toast toast = Toast.makeText(getApplicationContext(),
							((freshExcursions.getIntegerArrayList("new").size() > 0) ? "Новых экскурсий: " + freshExcursions.getIntegerArrayList("new").size() : "") +
									((freshExcursions.getIntegerArrayList("updated").size() > 0) ? "Обновлено экскурсий: " + freshExcursions.getIntegerArrayList("new").size() : ""),
							Toast.LENGTH_SHORT);
					toast.show();
				}
			}
		}, 2100+2100+2100);

	}

	DBHelper dbHelper;
	private static Bundle freshTours;
	private static Bundle freshEvents;
	private static Bundle freshServices;
	private static Bundle freshExcursions;
	private static Date earliest;


	private void show_last_tours() {

		LinearLayout underframe = (LinearLayout) findViewById(R.id.content_tours);
		dbHelper = new DBHelper(this);

		Cursor c;

		try {
			underframe.removeAllViewsInLayout();
		} catch(NullPointerException ne) {}

		SQLiteDatabase db;
/////////////////////////////////////////////////
		db = dbHelper.getWritableDatabase();
		String b = "SELECT * FROM tours ORDER BY date_update DESC LIMIT 5";
		c = db.rawQuery(b ,new String[]{});
		Log.d("","_____"+c.getCount()+"\n "+b);
		if (c.moveToFirst() && c.getCount()>0 ) {
			c.moveToFirst();
			int i=0;
			while (!c.isAfterLast()) {
				View v;
				final Tour tour = new Tour(c, this, getApplicationContext().getSharedPreferences("my_data", 0));
				v = getLayoutInflater().inflate(R.layout.catalogue_content, underframe, false);
				underframe.addView(v);

				v.findViewById(R.id.bar).setOnClickListener(new android.view.View.OnClickListener() {
					@Override
					public void onClick(View v){
						Intent intent = new Intent(HomeActivity.this, TourContentActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
						intent.putExtra("tour", tour);
						startActivity(intent);
						overridePendingTransition(0, 0);
					}
				});

				final TextView header = (TextView)v.findViewById(R.id.content);
				final TextView content = (TextView)v.findViewById(R.id.articula);
				String content_header = tour.name + "\n" + ((double)Math.round(tour.price*100))/100 + " " + tour.currency;              //+ StartActivity.make_date(tour.date) +"\n"+ tour.annotation;
				header.setText(content_header);
				String city = "", country = "";
				Cursor c_city = db.rawQuery("SELECT * FROM cities WHERE id = " + tour.id_city, new String[]{});
				if (c_city.moveToFirst() && c_city.getCount()>0) {
					c_city.moveToFirst();
					city = c_city.getString(2);
				}

				Cursor c_country = db.rawQuery("SELECT * FROM countries WHERE id = " + tour.id_country, new String[]{});
				if (c_country.moveToFirst() && c_country.getCount()>0) {
					c_country.moveToFirst();
					country = c_country.getString(1);
				}

				for (Date d : tour.date) {
					Log.i("ÀAAAAAAAASFASF", i+" "+ Calendar.getInstance().getTime().toString() +" "+d.toString());
					if (d.after(Calendar.getInstance().getTime())) {
						Log.i("DATE_DEBUG__", d.toString());
						earliest = d;
						break;
					}
				}
				i++;
				content_header = "Дата: " + StartActivity.make_date(earliest)
						+ "\n"   + "Продолжительность: "
						+ tour.duration + " " + ((tour.duration % 10 == 1) ? "день" :
						((tour.duration % 10 > 1 && tour.duration % 10 < 5) ? "дня" : "дней"))
						+ "\n"   + "Тип экскурсии: "     + ""
						+ "\n"   + "Город (Страна): "    + city
						+ " (" + country + ")";
				content.setText(content_header);
				earliest = null;
				c.moveToNext();
				c_city.close();
				c_country.close();
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

	final Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1)
				show_last_tours();
			super.handleMessage(msg);
		}
	};

	private void init_interface(){
		final MainMenu menu = new MainMenu(this);
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
					@Override public void run() {
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

}
