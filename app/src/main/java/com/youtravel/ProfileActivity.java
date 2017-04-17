package com.youtravel;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ProfileActivity extends AppCompatActivity {

    static SharedPreferences settings;
    static DBHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_profile);
        new MainMenu(this, "Мой кабинет");
        final RelativeLayout logincontent = (RelativeLayout) findViewById(R.id.login);
        settings = getApplicationContext().getSharedPreferences("my_data", 0);
        if(settings.getString("id_user", null)==null)
        {

            logincontent.setVisibility(View.VISIBLE);
            new LoginForm(this, new Callback() {
                @Override
                public void invoke() {
                    logincontent.setVisibility(View.GONE);

                }
            });
        }
    }
}
