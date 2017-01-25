package com.youtravel;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

/**
 * Created by MaximTitarenko on 23.01.17.
 */

public class LoginForm {
    SharedPreferences settings;
    DBHelper dbHelper;
    public LoginForm(final AppCompatActivity context, final Callback callback ){

            settings = context.getApplicationContext().getSharedPreferences("my_data", 0);
            if (android.os.Build.VERSION.SDK_INT > 9) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }
            dbHelper = new DBHelper(context);
            final RelativeLayout logincontent = (RelativeLayout) context.findViewById(R.id.login);
            ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            final boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
            if (settings.getString("email_user", null) != null) {
                StartActivity.hideKeyboard(context);

                /// Пользователь уже авторизирон.
            } else {
                logincontent.setVisibility(View.VISIBLE);
                final EditText login = (EditText) context.findViewById(R.id.editlogin);
                final EditText pas = (EditText) context.findViewById(R.id.editText2);
                if (isConnected) {
                    Button go = (Button) context.findViewById(R.id.go); // In
                    Button reg = (Button) context.findViewById(R.id.reg); // Registration
                    Button forg = (Button) context.findViewById(R.id.forg); // Forgotten password

                    forg.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (isConnected) {
                                ForgottenDialog rd = new ForgottenDialog(context);
                                rd.show();
                            } else {
                                android.app.AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                AlertDialog dialog = builder.create();
                                dialog.setTitle("Для регистрация, подключитесь к Интернету");
                                Message listenerDoesNotAccept = null;
                                dialog.setButton("Ok", listenerDoesNotAccept);
                                dialog.setCancelable(false);
                                dialog.show();
                            }
                        }
                    });
                    reg.setOnClickListener(new View.OnClickListener() {

                        public void onClick(View arg0) {
                            if (isConnected) {
                                RegDialog rd = new RegDialog(context);
                                rd.show();
                            } else {
                                android.app.AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                AlertDialog dialog = builder.create();
                                dialog.setTitle("Для регистрация, подключитесь к Интернету");
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
                            if (isConnected) {

                                String result = null;
                                InputStream is = null;
                                StringBuilder sb = null;
                                Boolean fl = true;

                                //http post
                                try {
                                    HttpClient httpclient = new DefaultHttpClient();
                                    HttpPost httppost = null;
                                    httppost = new HttpPost(StartActivity.server + "/login.php?login=" + login.getText().toString() + "&pas=" + StartActivity.md5(pas.getText().toString()));
                                    //httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                                    HttpResponse response = httpclient.execute(httppost);
                                    HttpEntity entity = response.getEntity();
                                    is = entity.getContent();
                                } catch (Exception e) {
                                    fl = false;
                                    Log.e("log_tag", "Error in http connection" + e.toString());
                                }

                                //convert response to string
                                try {
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
                                    sb = new StringBuilder();
                                    sb.append(reader.readLine() + "\n");
                                    String line = "0";

                                    while ((line = reader.readLine()) != null) {
                                        sb.append(line + "\n");
                                    }

                                    is.close();
                                    result = sb.toString();

                                } catch (Exception e) {
                                    fl = false;
                                    Log.e("log_tag", "Error converting result " + e.toString());
                                }
                                if (fl == true) {
                                    //paring data
                                    try {
                                        JSONArray jArray = new JSONArray(result);
                                        JSONObject json_data = null;
                                        if (jArray != null) {
                                            json_data = jArray.getJSONObject(0);
                                            if (json_data != null) {
                                                if (json_data.getString("email") != "null") {
                                                    SharedPreferences.Editor editor = settings.edit();
                                                    editor.putString("id_user", json_data.getString("id"));
                                                    editor.putString("email_user", json_data.getString("email"));
                                                    editor.putString("name_user", json_data.getString("fio"));
                                                    Log.d("PPPPPPPPPPPPPPP",json_data.getString("fio"));
                                                    editor.putString("phone_user", json_data.getString("phone"));
                                                    editor.apply();
                                                    StartActivity.logind = json_data.getString("id");
                                                }
                                                StartActivity.hideKeyboard(context);
                                                logincontent.setVisibility(View.GONE);
                                                callback.invoke();
                                            }

                                        } else {
                                            android.app.AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                            AlertDialog dialog = builder.create();
                                            dialog.setTitle("Не верные данные");
                                            Message listenerDoesNotAccept = null;
                                            dialog.setButton("Ok", listenerDoesNotAccept);
                                            dialog.setCancelable(false);
                                            dialog.show();
                                        }
                                    } catch (JSONException e1) {
                                        Log.e("log_tag", "Error no ff " + e1.toString());
                                        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                        AlertDialog dialog = builder.create();
                                        dialog.setTitle("Не верные данные");
                                        Message listenerDoesNotAccept = null;
                                        dialog.setButton("Ok", listenerDoesNotAccept);
                                        dialog.setCancelable(false);
                                        dialog.show();
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            } else {
                                android.app.AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                AlertDialog dialog = builder.create();
                                dialog.setTitle("Для входа, подключитесь к Интернету");
                                Message listenerDoesNotAccept = null;
                                dialog.setButton("Ok", listenerDoesNotAccept);
                                dialog.setCancelable(false);
                                dialog.show();
                            }

                        }
                    });
                } else {
                    android.app.AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    AlertDialog dialog = builder.create();
                    dialog.setTitle("Для входа, подключитесь к Интернету");
                    Message listenerDoesNotAccept = null;
                    dialog.setButton("Ok", listenerDoesNotAccept);
                    dialog.setCancelable(false);
                    dialog.show();
                }
            }



    }
}
