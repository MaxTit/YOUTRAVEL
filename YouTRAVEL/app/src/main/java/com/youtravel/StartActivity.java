package com.youtravel;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class StartActivity extends Activity {
	public class DownloadFileFromURL extends AsyncTask<String, String, String> {
	    @Override
	    protected void onPreExecute() {
	        super.onPreExecute();
	    }
	    
	    @Override
	    protected String doInBackground(String... f_url) {	  
	        try {
	            	update_countries();
	            	update_cities();
	            	update_objects();
	            	update_events();
	            	update_journey_kinds();
	            	update_services();
	            	update_excursions();
	            	update_tours();
	            	update_comments();
	            	update_article();
	            	update_chat();
	            	
	         //   	download_docs();
	         //   	download_foto();
	            
	        }catch (Exception e) {
	        	Log.e("Error: ", e.getMessage());
	        }

	        return null;
	    }

	    protected void onProgressUpdate(String... progress) {
	        // setting progress percentage
	    }

	    /**
	     * After completing background task Dismiss the progress dialog
	     * **/
	    @Override
	    protected void onPostExecute(String file_url) {
	    //dismiss the dialog after the file was downloaded
	    //	dismissDialog(progress_bar_type);
	     	Intent intent = new Intent(StartActivity.this,HomeActivity.class);
	    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
	    }
	}
	static DBHelper dbHelper;
	static DownloadFileFromURL dt;
	static String pathtocache;
	static SharedPreferences settings;
	private static String logind = "";
	public static String server = "http://dssclub.com.ua/you_travel";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
		settings = getApplicationContext().getSharedPreferences("my_data", 0);
		if (android.os.Build.VERSION.SDK_INT > 9) {
		     StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		     StrictMode.setThreadPolicy(policy);
		 }
		dbHelper = new DBHelper(this);
		pathtocache = getBaseContext().getCacheDir().toString();
		final ImageView img = (ImageView)findViewById(R.id.imageView1);
		final RelativeLayout logincontent = (RelativeLayout) findViewById(R.id.login);
		ConnectivityManager cm =
		        (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		 
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		final boolean isConnected = activeNetwork != null &&
		                      activeNetwork.isConnectedOrConnecting();
		if(settings.getString("email_user",null)!=null) {

			img.setVisibility(View.VISIBLE);
			img.setImageBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.loading));
			logind =settings.getString("id_user", null);
			if (isConnected) {

				new DownloadFileFromURL().execute();


			} else {
				android.app.AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
				AlertDialog dialog = builder.create();
				dialog.setTitle("Для входа, подключитесь к Интернету");
				Message listenerDoesNotAccept = null;
				dialog.setButton("Ok", listenerDoesNotAccept);
				dialog.setCancelable(false);
				dialog.show();
			}
		}
		else {
			img.setVisibility(View.GONE);
			logincontent.setVisibility(View.VISIBLE);
			final EditText login = (EditText) findViewById(R.id.editText1);
			final EditText pas = (EditText) findViewById(R.id.editText2);
			if (isConnected) {
				Button go = (Button) findViewById(R.id.go);
				Button gog = (Button) findViewById(R.id.gog);
				Button reg = (Button) findViewById(R.id.reg);
				reg.setOnClickListener(new View.OnClickListener() {

					public void onClick(View arg0) {
						if(isConnected ){
							RegDialog rd = new RegDialog(StartActivity.this);
							rd.show();
						}
						else
						{
							android.app.AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
							AlertDialog dialog = builder.create();
							dialog.setTitle("Для регистрация, подключитесь к Интернету");
							Message listenerDoesNotAccept = null;
							dialog.setButton("Ok", listenerDoesNotAccept);
							dialog.setCancelable(false);
							dialog.show();
						}

					}
				});
				gog.setOnClickListener(new View.OnClickListener() {

					public void onClick(View arg0) {
						if(isConnected ){
							new DownloadFileFromURL().execute();
						}
						else
						{
							android.app.AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
							AlertDialog dialog = builder.create();
							dialog.setTitle("Для входа, подключитесь к Интернету");
							Message listenerDoesNotAccept = null;
							dialog.setButton("Ok", listenerDoesNotAccept);
							dialog.setCancelable(false);
							dialog.show();
						}

					}
				});
				go.setOnClickListener(new View.OnClickListener() {

					@SuppressLint("CommitPrefEdits")
					@SuppressWarnings("unused")
					public void onClick(View arg0) {
						if(isConnected ){

							String result = null;
							InputStream is = null;
							StringBuilder sb=null;
							Boolean fl = true;

							//http post
							try{
								HttpClient httpclient = new DefaultHttpClient();
								HttpPost httppost = null;
								httppost = new HttpPost(server+"/login.php?login="+login.getText().toString()+"&pas="+md5(pas.getText().toString()));
								//httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
								HttpResponse response = httpclient.execute(httppost);
								HttpEntity entity = response.getEntity();
								is = entity.getContent();
							}catch(Exception e){
								fl=false;
								Log.e("log_tag", "Error in http connection"+e.toString());
							}

							//convert response to string
							try{
								BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"),8);
								sb = new StringBuilder();
								sb.append(reader.readLine() + "\n");
								String line="0";

								while ((line = reader.readLine()) != null) {
									sb.append(line + "\n");
								}

								is.close();
								result=sb.toString();

							}catch(Exception e){
								fl=false;
								Log.e("log_tag", "Error converting result "+e.toString());
							}
							if (fl==true)
							{
								//paring data
								try{
									JSONArray jArray = new JSONArray(result);
									JSONObject json_data=null;
									if(jArray!=null){
										json_data = jArray.getJSONObject(0);
										if(json_data!=null){
											if (json_data.getString("email")!="null"){
												SharedPreferences.Editor editor = settings.edit();
												editor.putString("id_user", json_data.getString("id"));
												editor.putString("email_user", json_data.getString("email"));
												editor.apply();
												logind = json_data.getString("id");
											}
											hideKeyboard();

											img.setVisibility(View.VISIBLE);
											img.setImageBitmap(BitmapFactory.decodeResource(StartActivity.this.getResources(), R.drawable.loading));
											logincontent.setVisibility(View.GONE);
											new DownloadFileFromURL().execute();
										}
										//Log.d("LOG_TAG", " "+json_data.getString("date_change"));//+"__"+json_data.getString("country")+"__"+json_data.getString("gr"));


									}
									else
									{
										android.app.AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
										AlertDialog dialog = builder.create();
										dialog.setTitle("Не верные данные");
										Message listenerDoesNotAccept = null;
										dialog.setButton("Ok", listenerDoesNotAccept);
										dialog.setCancelable(false);
										dialog.show();
									}
								}catch(JSONException e1){
									Log.e("log_tag", "Error no ff "+e1.toString());
									android.app.AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
									AlertDialog dialog = builder.create();
									dialog.setTitle("Не верные данные");
									Message listenerDoesNotAccept = null;
									dialog.setButton("Ok", listenerDoesNotAccept);
									dialog.setCancelable(false);
									dialog.show();
								}catch (Exception e1){
									e1.printStackTrace();
								}
							}
						}
						else
						{
							android.app.AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
							AlertDialog dialog = builder.create();
							dialog.setTitle("Для входа, подключитесь к Интернету");
							Message listenerDoesNotAccept = null;
							dialog.setButton("Ok", listenerDoesNotAccept);
							dialog.setCancelable(false);
							dialog.show();
						}

					}
				});
			}
			else
			{
				android.app.AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
				AlertDialog dialog = builder.create();
				dialog.setTitle("Для входа, подключитесь к Интернету");
				Message listenerDoesNotAccept = null;
				dialog.setButton("Ok",  listenerDoesNotAccept);
				dialog.setCancelable(false);
				dialog.show();
			}
		}
		
		
		
	}
	private void hideKeyboard() {
		InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

		// check if no view has focus:
		View view = this.getCurrentFocus();
		if (view != null) {
			inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}
	private String md5(String s) {
		final String MD5 = "MD5";
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest
					.getInstance(MD5);
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuilder hexString = new StringBuilder();
			for (byte aMessageDigest : messageDigest) {
				String h = Integer.toHexString(0xFF & aMessageDigest);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}
////////////////////////////////////
	public static void update_countries(){
		//creating object for data
		ContentValues cv = new ContentValues();
		//connecting to DB
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		String result = null;
		InputStream is = null;
		StringBuilder sb=null;
		Boolean fl = true;
		
		// http post
		try{
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = null;
			httppost = new HttpPost(server+"?signal=Countries");
			//httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
		}
		catch(Exception e){
			fl = false;
			Log.e("log_tag", "Error in http connection"+e.toString());
		}
		
		//convert response to string
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"),8);
			sb = new StringBuilder();
			sb.append(reader.readLine() + "\n");
			String line = "0";
			
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			
			is.close();
			result = sb.toString();
			
		}catch(Exception e){
			fl = false;
			Log.e("log_tag", "Error converting result " + e.toString());
		}
		if (fl == true)
		{
			//paring data
			try{
				JSONArray jArray = new JSONArray(result);
				JSONObject json_data = null;
				Log.d("LOG_TAG", "Delete all from table " + "countries");
				db.delete("countries", null, null);
				if(jArray != null){
					for(int i = 0; i < jArray.length(); i++){
						json_data = jArray.getJSONObject(i);
						if(json_data != null){
							
							if (json_data.getString("id")!="null")
								cv.put("id", json_data.getInt("id"));
							
							if (json_data.getString("name")!="null")
								cv.put("name", json_data.getString("name"));
							
							if (json_data.getString("annotation")!="null")
								cv.put("annotation", json_data.getString("annotation"));
							
							if (json_data.getString("description")!="null")
								cv.put("description", json_data.getString("description"));
							
							if (json_data.getString("html")!="null")
								cv.put("html", json_data.getString("html"));
							
							if (json_data.getString("latitude")!="null")
								cv.put("latitude", json_data.getString("latitude"));
							
							if (json_data.getString("longitude")!="null")
								cv.put("longitude", json_data.getString("longitude"));
							
							if (json_data.getString("id_status")!="null")
								cv.put("id_status", json_data.getInt("id_status"));
							
							if (json_data.getString("link")!="null")
								cv.put("link", json_data.getString("link"));
							
							if (json_data.getString("img")!="null")
								cv.put("img", json_data.getString("img"));
							
							db.insert("countries", null, cv);
						}
					
					}
				}
			}catch(JSONException e1){
				Log.e("log_tag", "Error no ff " + e1.toString());
			}catch (Exception e1){
				e1.printStackTrace();
			}
		}
		db.close();
	}

	public static void update_cities(){
		//creating object for data
		ContentValues cv = new ContentValues();
		//connecting to DB
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		String result = null;
		InputStream is = null;
		StringBuilder sb=null;
		Boolean fl = true;
		
		// http post
		try{
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = null;
			httppost = new HttpPost(server+"?signal=Cities");
			//httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
		}
		catch(Exception e){
			fl = false;
			Log.e("log_tag", "Error in http connection"+e.toString());
		}
		
		//convert response to string
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"),8);
			sb = new StringBuilder();
			sb.append(reader.readLine() + "\n");
	
			String line = "0";
			
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			
			is.close();
			result = sb.toString();
			
		}catch(Exception e){
			fl = false;
			Log.e("log_tag", "Error converting result " + e.toString());
		}
		if (fl == true)
		{
			//paring data
			try{
				JSONArray jArray = new JSONArray(result);
				JSONObject json_data = null;
				Log.d("LOG_TAG", "Delete all from table " + "cities");
				db.delete("cities", null, null);
				if(jArray != null){
					for(int i = 0; i < jArray.length(); i++){
						json_data = jArray.getJSONObject(i);
						if(json_data != null){
							
							if (json_data.getString("id")!="null")
								cv.put("id", json_data.getInt("id"));
							
							if (json_data.getString("id_country")!="null")
								cv.put("id_country", json_data.getInt("id_country"));
							
							if (json_data.getString("name")!="null")
								cv.put("name", json_data.getString("name"));
							
							if (json_data.getString("annotation")!="null")
								cv.put("annotation", json_data.getString("annotation"));
							
							if (json_data.getString("description")!="null")
								cv.put("description", json_data.getString("description"));
							
							if (json_data.getString("html")!="null")
								cv.put("html", json_data.getString("html"));
							
							if (json_data.getString("latitude")!="null")
								cv.put("latitude", json_data.getString("latitude"));
							
							if (json_data.getString("longitude")!="null")
								cv.put("longitude", json_data.getString("longitude"));
							
							if (json_data.getString("id_status")!="null")
								cv.put("id_status", json_data.getInt("id_status"));
							
							if (json_data.getString("link")!="null")
								cv.put("link", json_data.getString("link"));
							
							if (json_data.getString("img")!="null")
								cv.put("img", json_data.getString("img"));
							
							if (json_data.getString("id_comment")!="null")
								cv.put("id_comment", json_data.getString("id_comment"));
							
							db.insert("cities", null, cv);
						}
					
					}
				}
			}catch(JSONException e1){
				Log.e("log_tag", "Error no ff " + e1.toString());
			}catch (Exception e1){
				e1.printStackTrace();
			}
		}
		db.close();
	}

	public static void update_objects(){
		//creating object for data
		ContentValues cv = new ContentValues();
		//connecting to DB
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		String result = null;
		InputStream is = null;
		StringBuilder sb=null;
		Boolean fl = true;
		
		// http post
		try{
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = null;
			httppost = new HttpPost(server+"?signal=Objects");
			//httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
		}
		catch(Exception e){
			fl = false;
			Log.e("log_tag", "Error in http connection"+e.toString());
		}
		
		//convert response to string
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"),8);
			sb = new StringBuilder();
			sb.append(reader.readLine() + "\n");
			String line = "0";
			
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			
			is.close();
			result = sb.toString();
			
		}catch(Exception e){
			fl = false;
			Log.e("log_tag", "Error converting result " + e.toString());
		}
		if (fl == true)
		{
			//paring data
			try{
				JSONArray jArray = new JSONArray(result);
				JSONObject json_data = null;
				Log.d("LOG_TAG", "Delete all from table " + "objects");
				db.delete("objects", null, null);
				if(jArray != null){
					for(int i = 0; i < jArray.length(); i++){
						json_data = jArray.getJSONObject(i);
						if(json_data != null){
							
							if (json_data.getString("id")!="null")
								cv.put("id", json_data.getInt("id"));
							
							if (json_data.getString("id_country")!="null")
								cv.put("id_country", json_data.getInt("id_country"));
							
							if (json_data.getString("id_city")!="null")
								cv.put("id_city", json_data.getInt("id_city"));
							
							if (json_data.getString("name")!="null")
								cv.put("name", json_data.getString("name"));
							
							if (json_data.getString("annotation")!="null")
								cv.put("annotation", json_data.getString("annotation"));
							
							if (json_data.getString("description")!="null")
								cv.put("description", json_data.getString("description"));
							
							if (json_data.getString("html")!="null")
								cv.put("html", json_data.getString("html"));
							
							if (json_data.getString("latitude")!="null")
								cv.put("latitude", json_data.getString("latitude"));
							
							if (json_data.getString("longitude")!="null")
								cv.put("longitude", json_data.getString("longitude"));
							
							if (json_data.getString("id_status")!="null")
								cv.put("id_status", json_data.getInt("id_status"));
							
							if (json_data.getString("link")!="null")
								cv.put("link", json_data.getString("link"));
							
							if (json_data.getString("img")!="null")
								cv.put("img", json_data.getString("img"));
							
							if (json_data.getString("extra_info")!="null")
								cv.put("extra_info", json_data.getString("extra_info"));
							
							if (json_data.getString("mail")!="null")
								cv.put("mail", json_data.getString("mail"));
							
							if (json_data.getString("t_number")!="null")
								cv.put("t_number", json_data.getString("t_number"));
							
							if (json_data.getString("web_site")!="null")
								cv.put("web_site", json_data.getString("web_site"));
							
							if (json_data.getString("object_type")!="null")
								cv.put("object_type", json_data.getString("object_type"));

							if (json_data.getString("children_info")!="null")
								cv.put("children_info", json_data.getString("children_info"));
							
							if (json_data.getString("id_comment")!="null")
								cv.put("id_comment", json_data.getString("id_comment"));
							
							db.insert("objects", null, cv);
						}
					
					}
				}
			}catch(JSONException e1){
				Log.e("log_tag", "Error no ff " + e1.toString());
			}catch (Exception e1){
				e1.printStackTrace();
			}
		}
		db.close();
	}

	public static void update_events(){
		//creating object for data
		ContentValues cv = new ContentValues();
		//connecting to DB
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		String result = null;
		InputStream is = null;
		StringBuilder sb=null;
		Boolean fl = true;
		
		// http post
		try{
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = null;
			httppost = new HttpPost(server+"?signal=Events");
			//httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
		}
		catch(Exception e){
			fl = false;
			Log.e("log_tag", "Error in http connection"+e.toString());
		}
		
		//convert response to string
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"),8);
			sb = new StringBuilder();
			sb.append(reader.readLine() + "\n");
			String line = "0";
			
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			
			is.close();
			result = sb.toString();
			
		}catch(Exception e){
			fl = false;
			Log.e("log_tag", "Error converting result " + e.toString());
		}
		if (fl == true)
		{
			//paring data
			try{
				JSONArray jArray = new JSONArray(result);
				JSONObject json_data = null;
				Log.d("LOG_TAG", "Delete all from table " + "events");
				db.delete("events", null, null);
				if(jArray != null){
					for(int i = 0; i < jArray.length(); i++){
						json_data = jArray.getJSONObject(i);
						if(json_data != null){
							
							if (json_data.getString("id")!="null")
								cv.put("id", json_data.getInt("id"));
							
							if (json_data.getString("event_type")!="null")
								cv.put("event_type", json_data.getString("event_type"));
							
							if (json_data.getString("name")!="null")
								cv.put("name", json_data.getString("name"));
							
							if (json_data.getString("annotation")!="null")
								cv.put("annotation", json_data.getString("annotation"));
							
							if (json_data.getString("description")!="null")
								cv.put("description", json_data.getString("description"));
							
							if (json_data.getString("extra_info")!="null")
								cv.put("extra_info", json_data.getString("extra_info"));
							
							if (json_data.getString("html")!="null")
								cv.put("html", json_data.getString("html"));
														
							if (json_data.getString("id_status")!="null")
								cv.put("id_status", json_data.getInt("id_status"));
							
							if (json_data.getString("link")!="null")
								cv.put("link", json_data.getString("link"));
							
							if (json_data.getString("img")!="null")
								cv.put("img", json_data.getString("img"));
														
							if (json_data.getString("price")!="null")
								cv.put("price", json_data.getDouble("price"));
							
							if (json_data.getString("currency")!="null")
								cv.put("currency", json_data.getString("currency"));
							
							if (json_data.getString("location_and_time")!="null")
								cv.put("location_and_time", json_data.getString("location_and_time"));
							
							if (json_data.getString("id_comment")!="null")
								cv.put("id_comment", json_data.getString("id_comment"));

							db.insert("events", null, cv);
						}
					
					}
				}
			}catch(JSONException e1){
				Log.e("log_tag", "Error no ff " + e1.toString());
			}catch (Exception e1){
				e1.printStackTrace();
			}
		}
		db.close();
	}
	
	public static void update_journey_kinds(){
		//creating object for data
		ContentValues cv = new ContentValues();
		//connecting to DB
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		String result = null;
		InputStream is = null;
		StringBuilder sb=null;
		Boolean fl = true;
		
		// http post
		try{
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = null;
			httppost = new HttpPost(server+"?signal=Journey_kinds");
			//httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
		}
		catch(Exception e){
			fl = false;
			Log.e("log_tag", "Error in http connection"+e.toString());
		}
		
		//convert response to string
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"),8);
			sb = new StringBuilder();
			sb.append(reader.readLine() + "\n");
			String line = "0";
			
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			
			is.close();
			result = sb.toString();
			
		}catch(Exception e){
			fl = false;
			Log.e("log_tag", "Error converting result " + e.toString());
		}
		if (fl == true)
		{
			//paring data
			try{
				JSONArray jArray = new JSONArray(result);
				JSONObject json_data = null;
				Log.d("LOG_TAG", "Delete all from table " + "journey_kinds");
				db.delete("journey_kinds", null, null);
				if(jArray != null){
					for(int i = 0; i < jArray.length(); i++){
						json_data = jArray.getJSONObject(i);
						if(json_data != null){
							
							if (json_data.getString("id")!="null")
								cv.put("id", json_data.getInt("id"));
							
							if (json_data.getString("name")!="null")
								cv.put("name", json_data.getString("name"));
							
							if (json_data.getString("annotation")!="null")
								cv.put("annotation", json_data.getString("annotation"));
							
							if (json_data.getString("description")!="null")
								cv.put("description", json_data.getString("description"));	
							
							if (json_data.getString("html")!="null")
								cv.put("html", json_data.getString("html"));
														
							if (json_data.getString("id_status")!="null")
								cv.put("id_status", json_data.getInt("id_status"));
							
							if (json_data.getString("link")!="null")
								cv.put("link", json_data.getString("link"));
							
							if (json_data.getString("img")!="null")
								cv.put("img", json_data.getString("img"));
						
							db.insert("journey_kinds", null, cv);
						}
					
					}
				}
			}catch(JSONException e1){
				Log.e("log_tag", "Error no ff " + e1.toString());
			}catch (Exception e1){
				e1.printStackTrace();
			}
		}
		db.close();
	}

	public static void update_services(){
		//creating object for data
		ContentValues cv = new ContentValues();
		//connecting to DB
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		String result = null;
		InputStream is = null;
		StringBuilder sb=null;
		Boolean fl = true;
		
		// http post
		try{
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = null;
			httppost = new HttpPost(server+"?signal=Services");
			//httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
		}
		catch(Exception e){
			fl = false;
			Log.e("log_tag", "Error in http connection"+e.toString());
		}
		
		//convert response to string
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"),8);
			sb = new StringBuilder();
			sb.append(reader.readLine() + "\n");
			String line = "0";
			
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			
			is.close();
			result = sb.toString();
			
		}catch(Exception e){
			fl = false;
			Log.e("log_tag", "Error converting result " + e.toString());
		}
		if (fl == true)
		{
			//paring data
			try{
				JSONArray jArray = new JSONArray(result);
				JSONObject json_data = null;
				Log.d("LOG_TAG", "Delete all from table " + "services");
				db.delete("services", null, null);
				if(jArray != null){
					for(int i = 0; i < jArray.length(); i++){
						json_data = jArray.getJSONObject(i);
						if(json_data != null){
							
							if (json_data.getString("id")!="null")
								cv.put("id", json_data.getInt("id"));
							
							if (json_data.getString("name")!="null")
								cv.put("name", json_data.getString("name"));
							
							if (json_data.getString("annotation")!="null")
								cv.put("annotation", json_data.getString("annotation"));
							
							if (json_data.getString("description")!="null")
								cv.put("description", json_data.getString("description"));
							
							if (json_data.getString("extra_info")!="null")
								cv.put("extra_info", json_data.getString("extra_info"));
							
							if (json_data.getString("html")!="null")
								cv.put("html", json_data.getString("html"));
														
							if (json_data.getString("id_status")!="null")
								cv.put("id_status", json_data.getInt("id_status"));
							
							if (json_data.getString("link")!="null")
								cv.put("link", json_data.getString("link"));
							
							if (json_data.getString("img")!="null")
								cv.put("img", json_data.getString("img"));
														
							if (json_data.getString("price")!="null")
								cv.put("price", json_data.getDouble("price"));
							
							if (json_data.getString("currency")!="null")
								cv.put("currency", json_data.getString("currency"));
							Log.d("a","aaaaaaaaaaaaa");
							Log.d("base",json_data.getString("currency")+"-------------------");
							if (json_data.getString("id_comment")!="null")
								cv.put("id_comment", json_data.getString("id_comment"));

							
							db.insert("services", null, cv);
						}
					
					}
				}
			}catch(JSONException e1){
				Log.e("log_tag", "Error no ff " + e1.toString());
			}catch (Exception e1){
				e1.printStackTrace();
			}
		}
		db.close();
	}
	
	public static void update_excursions(){
		//creating object for data
		ContentValues cv = new ContentValues();
		//connecting to DB
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		String result = null;
		InputStream is = null;
		StringBuilder sb=null;
		Boolean fl = true;
		
		// http post
		try{
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = null;
			httppost = new HttpPost(server+"?signal=Excursions");
			//httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
		}
		catch(Exception e){
			fl = false;
			Log.e("log_tag", "Error in http connection"+e.toString());
		}
		
		//convert response to string
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"),8);
			sb = new StringBuilder();
			sb.append(reader.readLine() + "\n");
			String line = "0";
			
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			
			is.close();
			result = sb.toString();
			
		}catch(Exception e){
			fl = false;
			Log.e("log_tag", "Error converting result " + e.toString());
		}
		if (fl == true)
		{
			//paring data
			try{
				JSONArray jArray = new JSONArray(result);
				JSONObject json_data = null;
				Log.d("LOG_TAG", "Delete all from table " + "excursions");
				db.delete("excursions", null, null);
				if(jArray != null){
					for(int i = 0; i < jArray.length(); i++){
						json_data = jArray.getJSONObject(i);
						if(json_data != null){
							
							if (json_data.getString("id")!="null")
								cv.put("id", json_data.getInt("id"));
							
							if (json_data.getString("id_country")!="null")
								cv.put("id_country", json_data.getInt("id_country"));
							
							if (json_data.getString("id_city")!="null")
								cv.put("id_city", json_data.getInt("id_city"));
							
							if (json_data.getString("excursion_type")!="null")
								cv.put("excursion_type", json_data.getString("excursion_type"));
							
							if (json_data.getString("individual")!="null")
								cv.put("individual", json_data.getInt("individual") == 1);
							
							if (json_data.getString("duration")!="null")
								cv.put("duration", json_data.getInt("duration"));
							
							if (json_data.getString("name")!="null")
								cv.put("name", json_data.getString("name"));
							
							if (json_data.getString("annotation")!="null")
								cv.put("annotation", json_data.getString("annotation"));
							
							if (json_data.getString("description")!="null")
								cv.put("description", json_data.getString("description"));
							
							if (json_data.getString("extra_info")!="null")
								cv.put("extra_info", json_data.getString("extra_info"));
							
							if (json_data.getString("html")!="null")
								cv.put("html", json_data.getString("html"));
														
							if (json_data.getString("id_status")!="null")
								cv.put("id_status", json_data.getInt("id_status"));
							
							if (json_data.getString("link")!="null")
								cv.put("link", json_data.getString("link"));
							
							if (json_data.getString("img")!="null")
								cv.put("img", json_data.getString("img"));
														
							if (json_data.getString("price")!="null")
								cv.put("price", json_data.getDouble("price"));
							
							if (json_data.getString("currency")!="null")
								cv.put("currency", json_data.getString("currency"));
							
							if (json_data.getString("location")!="null")
								cv.put("location", json_data.getString("location"));
							
							if (json_data.getString("latitude")!="null")
								cv.put("latitude", json_data.getString("latitude"));
							
							if (json_data.getString("longitude")!="null")
								cv.put("longitude", json_data.getString("longitude"));
							
							if (json_data.getString("date")!="null")
								cv.put("date", json_data.getString("date"));
							
							if (json_data.getString("id_comment")!="null")
								cv.put("id_comment", json_data.getString("id_comment"));
							
							db.insert("excursions", null, cv);
						}
					
					}
				}
			}catch(JSONException e1){
				Log.e("log_tag", "Error no ff " + e1.toString());
			}catch (Exception e1){
				e1.printStackTrace();
			}
		}
		db.close();
	}

	public static void update_tours(){
		//creating object for data
		ContentValues cv = new ContentValues();
		//connecting to DB
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		String result = null;
		InputStream is = null;
		StringBuilder sb=null;
		Boolean fl = true;
		
		// http post
		try{
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = null;
			httppost = new HttpPost(server+"?signal=Tours");
			//httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
		}
		catch(Exception e){
			fl = false;
			Log.e("log_tag", "Error in http connection"+e.toString());
		}
		
		//convert response to string
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"),8);
			sb = new StringBuilder();
			sb.append(reader.readLine() + "\n");
			String line = "0";
			
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			
			is.close();
			result = sb.toString();
			
		}catch(Exception e){
			fl = false;
			Log.e("log_tag", "Error converting result " + e.toString());
		}
		if (fl == true)
		{
			//paring data
			try{
				JSONArray jArray = new JSONArray(result);
				JSONObject json_data = null;
				Log.d("LOG_TAG", "Delete all from table " + "tours");
				db.delete("tours", null, null);
				if(jArray != null){
					for(int i = 0; i < jArray.length(); i++){
						json_data = jArray.getJSONObject(i);
						if(json_data != null){
							
							if (json_data.getString("id")!="null")
								cv.put("id", json_data.getInt("id"));
							
							if (json_data.getString("id_country")!="null")
								cv.put("id_country", json_data.getInt("id_country"));
							
							if (json_data.getString("id_city")!="null")
								cv.put("id_city", json_data.getInt("id_city"));
							
							if (json_data.getString("duration")!="null")
								cv.put("duration", json_data.getInt("duration"));
							
							if (json_data.getString("name")!="null")
								cv.put("name", json_data.getString("name"));
							
							if (json_data.getString("annotation")!="null")
								cv.put("annotation", json_data.getString("annotation"));
							
							if (json_data.getString("description")!="null")
								cv.put("description", json_data.getString("description"));
							
							if (json_data.getString("extra_info")!="null")
								cv.put("extra_info", json_data.getString("extra_info"));
							
							if (json_data.getString("html")!="null")
								cv.put("html", json_data.getString("html"));
														
							if (json_data.getString("id_status")!="null")
								cv.put("id_status", json_data.getInt("id_status"));
							
							if (json_data.getString("link")!="null")
								cv.put("link", json_data.getString("link"));
							
							if (json_data.getString("img")!="null")
								cv.put("img", json_data.getString("img"));
														
							if (json_data.getString("price")!="null")
								cv.put("price", json_data.getDouble("price"));
							
							if (json_data.getString("currency")!="null")
								cv.put("currency", json_data.getString("currency"));
														
							if (json_data.getString("date")!="null")
								cv.put("date", json_data.getString("date"));
							
							if (json_data.getString("id_cities")!="null")
								cv.put("id_cities", json_data.getString("id_cities"));
							
							if (json_data.getString("start_point")!="null")
								cv.put("start_point", json_data.getString("start_point"));
							
							if (json_data.getString("id_comment")!="null")
								cv.put("id_comment", json_data.getString("id_comment"));
							
							db.insert("tours", null, cv);
						}
					
					}
				}
			}catch(JSONException e1){
				Log.e("log_tag", "Error no ff " + e1.toString());
			}catch (Exception e1){
				e1.printStackTrace();
			}
		}
		db.close();
	}

	public static void update_comments(){
		//creating object for data
		ContentValues cv = new ContentValues();
		//connecting to DB
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		String result = null;
		InputStream is = null;
		StringBuilder sb=null;
		Boolean fl = true;
		
		// http post
		try{
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = null;
			httppost = new HttpPost(server+"?signal=Comments");
			//httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
		}
		catch(Exception e){
			fl = false;
			Log.e("log_tag", "Error in http connection"+e.toString());
		}
		
		//convert response to string
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"),8);
			sb = new StringBuilder();
			sb.append(reader.readLine() + "\n");
			String line = "0";
			
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			
			is.close();
			result = sb.toString();
			
		}catch(Exception e){
			fl = false;
			Log.e("log_tag", "Error converting result " + e.toString());
		}
		if (fl == true)
		{
			//paring data
			try{
				JSONArray jArray = new JSONArray(result);
				JSONObject json_data = null;
				Log.d("LOG_TAG", "Delete all from table " + "comments");
				db.delete("comments", null, null);
				if(jArray != null){
					for(int i = 0; i < jArray.length(); i++){
						json_data = jArray.getJSONObject(i);
						if(json_data != null){
							
							if (json_data.getString("id")!="null")
								cv.put("id", json_data.getInt("id"));
							
							if (json_data.getString("author")!="null")
								cv.put("author", json_data.getString("author"));

							if (json_data.getString("date")!="null")
								cv.put("date", json_data.getString("date"));
																					
							if (json_data.getString("description")!="null")
								cv.put("description", json_data.getString("description"));

							if (json_data.getString("rate")!="null")
								cv.put("rate", json_data.getInt("rate"));
							
							if (json_data.getString("id_status")!="null")
								cv.put("id_status", json_data.getInt("id_status"));
							
							if (json_data.getString("link")!="null")
								cv.put("link", json_data.getString("link"));
							
							if (json_data.getString("img")!="null")
								cv.put("img", json_data.getString("img"));
						
							db.insert("comments", null, cv);
						}
					
					}
				}
			}catch(JSONException e1){
				Log.e("log_tag", "Error no ff " + e1.toString());
			}catch (Exception e1){
				e1.printStackTrace();
			}
		}
		db.close();
	}

	public static void update_article(){
		//creating object for data
		ContentValues cv = new ContentValues();
		//connecting to DB
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		String result = null;
		InputStream is = null;
		StringBuilder sb=null;
		Boolean fl = true;
		
		// http post
		try{
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = null;
			httppost = new HttpPost(server+"?signal=Article");
			//httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
		}
		catch(Exception e){
			fl = false;
			Log.e("log_tag", "Error in http connection"+e.toString());
		}
		
		//convert response to string
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"),8);
			sb = new StringBuilder();
			sb.append(reader.readLine() + "\n");
			String line = "0";
			
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			
			is.close();
			result = sb.toString();
			
		}catch(Exception e){
			fl = false;
			Log.e("log_tag", "Error converting result " + e.toString());
		}
		if (fl == true)
		{
			//paring data
			try{
				JSONArray jArray = new JSONArray(result);
				JSONObject json_data = null;
				Log.d("LOG_TAG", "Delete all from table " + "article");
				db.delete("article", null, null);
				if(jArray != null){
					for(int i = 0; i < jArray.length(); i++){
						json_data = jArray.getJSONObject(i);
						if(json_data != null){
							
							if (json_data.getString("id")!="null")
								cv.put("id", json_data.getInt("id"));
							
							if (json_data.getString("date")!="null")
								cv.put("date", json_data.getString("date"));
																					
							if (json_data.getString("annotation")!="null")
								cv.put("annotation", json_data.getString("annotation"));
							
							if (json_data.getString("id_status")!="null")
								cv.put("id_status", json_data.getInt("id_status"));
							
							if (json_data.getString("link")!="null")
								cv.put("link", json_data.getString("link"));
							
							if (json_data.getString("img")!="null")
								cv.put("img", json_data.getString("img"));
						
							db.insert("article", null, cv);
						}
					
					}
				}
			}catch(JSONException e1){
				Log.e("log_tag", "Error no ff " + e1.toString());
			}catch (Exception e1){
				e1.printStackTrace();
			}
		}
		db.close();
	}

	public static void update_chat(){
		//creating object for data
		ContentValues cv = new ContentValues();
		//connecting to DB
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		String result = null;
		InputStream is = null;
		StringBuilder sb=null;
		Boolean fl = true;
		
		// http post
		try{
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = null;
			httppost = new HttpPost(server+"?signal=Chat");
			//httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
		}
		catch(Exception e){
			fl = false;
			Log.e("log_tag", "Error in http connection"+e.toString());
		}
		
		//convert response to string
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"),8);
			sb = new StringBuilder();
			sb.append(reader.readLine() + "\n");
			String line = "0";
			
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			
			is.close();
			result = sb.toString();
			
		}catch(Exception e){
			fl = false;
			Log.e("log_tag", "Error converting result " + e.toString());
		}
		if (fl == true)
		{
			//paring data
			try{
				JSONArray jArray = new JSONArray(result);
				JSONObject json_data = null;
				Log.d("LOG_TAG", "Delete all from table " + "chat");
				db.delete("chat", null, null);
				if(jArray != null){
					for(int i = 0; i < jArray.length(); i++){
						json_data = jArray.getJSONObject(i);
						if(json_data != null){
							
							if (json_data.getString("id")!="null")
								cv.put("id", json_data.getInt("id"));
							
							if (json_data.getString("id_from")!="null")
								cv.put("id_from", json_data.getInt("id_from"));
							
							if (json_data.getString("id_chat")!="null")
								cv.put("id_chat", json_data.getInt("id_chat"));
								
							if (json_data.getString("type")!="null")
								cv.put("type", json_data.getString("type"));
							
							if (json_data.getString("id_source")!="null")
								cv.put("id_source", json_data.getInt("id_source"));
							
							if (json_data.getString("message")!="null")
								cv.put("message", json_data.getString("message"));
							
							if (json_data.getString("author")!="null")
								cv.put("author", json_data.getString("author"));

							if (json_data.getString("date")!="null")
								cv.put("date", json_data.getString("date"));
						
							db.insert("chat", null, cv);
						}
					
					}
				}
			}catch(JSONException e1){
				Log.e("log_tag", "Error no ff " + e1.toString());
			}catch (Exception e1){
				e1.printStackTrace();
			}
		}
		db.close();
	}

	
}