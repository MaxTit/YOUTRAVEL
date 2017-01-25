package com.youtravel;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Message;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChangeDialog extends Dialog implements
        View.OnClickListener {

    public Activity c;
    public Dialog d;
    public String id;
    public Button change, cancel;
    public TextView pass, reppass;

    public ChangeDialog(Activity a, String id) {
        super(a);
// TODO Auto-generated constructor stub
        this.id = id;
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.change_dialog);
        change = (Button) findViewById(R.id.btn_change);
        cancel = (Button) findViewById(R.id.btn_cancel);
        pass = (TextView) findViewById(R.id.editText1_pass);
        reppass = (TextView) findViewById(R.id.editText1_conf);
        change.setOnClickListener(this);
        cancel.setOnClickListener(this);

    }

    @SuppressWarnings("deprecation")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_change: {
                ConnectivityManager cm =
                        (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                final boolean isConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();
                if (isConnected) {
                    if (pass.getText().toString().length() > 0) {
                        if (reppass.getText().toString().length() > 0) {
                            if (reppass.getText().toString().equals(pass.getText().toString())) {
                                HttpClient httpclient_email = new DefaultHttpClient();
                                HttpPost httppost_email = new HttpPost(StartActivity.server + "/change_password.php");

                                try {
                                    // Add your data
                                    List<NameValuePair> nameValuePairs_email = new ArrayList<NameValuePair>(2);
                                    nameValuePairs_email.add(new BasicNameValuePair("id_user", id));
                                    nameValuePairs_email.add(new BasicNameValuePair("pass", pass.getText().toString()));
                                    httppost_email.setEntity(new UrlEncodedFormEntity(nameValuePairs_email));

                                    // Execute HTTP Post Request
                                    HttpResponse response_email = httpclient_email
                                            .execute(httppost_email);
                                    final String responseStr_email = EntityUtils.toString(response_email.getEntity());
                                    Log.d("reg", responseStr_email);


                                    AlertDialog.Builder builder = new AlertDialog.Builder(c);
                                    AlertDialog dialog = builder.create();
                                    dialog.setTitle("Пароль был изменен.");
                                    Message listenerDoesNotAccept = null;
                                    dialog.setButton("Ok", listenerDoesNotAccept);
                                    dialog.setCancelable(false);
                                    dialog.show();
                                    dismiss();
                                } catch (ClientProtocolException e) {
                                    // TODO Auto-generated catch block
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                }

                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(c);
                                AlertDialog dialog = builder.create();
                                dialog.setTitle("Пароли должны совпадать");
                                Message listenerDoesNotAccept = null;
                                dialog.setButton("Ok", listenerDoesNotAccept);
                                dialog.setCancelable(false);
                                dialog.show();
                            }
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(c);
                            AlertDialog dialog = builder.create();
                            dialog.setTitle("Введите подтверждение пароля");
                            Message listenerDoesNotAccept = null;
                            dialog.setButton("Ok", listenerDoesNotAccept);
                            dialog.setCancelable(false);
                            dialog.show();
                        }
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(c);
                        AlertDialog dialog = builder.create();
                        dialog.setTitle("Введите пароль");
                        Message listenerDoesNotAccept = null;
                        dialog.setButton("Ok", listenerDoesNotAccept);
                        dialog.setCancelable(false);
                        dialog.show();
                    }
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(c);
                    AlertDialog dialog = builder.create();
                    dialog.setTitle("Для регистрации подключитесь к интернету!");
                    Message listenerDoesNotAccept = null;
                    dialog.setButton("Ok", listenerDoesNotAccept);
                    dialog.setCancelable(false);
                    dialog.show();
                }
            }
            break;
            case R.id.btn_cancel:
                dismiss();
                break;
            default:
                break;
        }
    }
}
