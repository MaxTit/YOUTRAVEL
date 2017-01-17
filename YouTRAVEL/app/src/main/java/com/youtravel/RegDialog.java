package com.youtravel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class RegDialog extends Dialog implements
View.OnClickListener {

public Activity c;
public Dialog d;

public Button reg, cancel;
public TextView name, phone, email, pass, reppass;

public RegDialog(Activity a) {
super(a);
// TODO Auto-generated constructor stub
this.c = a;
}

@Override
protected void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
requestWindowFeature(Window.FEATURE_NO_TITLE);
setContentView(R.layout.reg_dialog);
reg = (Button) findViewById(R.id.btn_reg);
cancel = (Button) findViewById(R.id.btn_cancel);
name = (TextView) findViewById(R.id.editText1_name);
email = (TextView) findViewById(R.id.editText1);
phone = (TextView) findViewById(R.id.editText1_phone);
pass = (TextView) findViewById(R.id.editText2);
reppass = (TextView) findViewById(R.id.editText3);
reg.setOnClickListener(this);
cancel.setOnClickListener(this);

}

@SuppressWarnings("deprecation")
@Override
public void onClick(View v) {
switch (v.getId()) {
case R.id.btn_reg:
{
	ConnectivityManager cm =
	        (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);
	 
	NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
	final boolean isConnected = activeNetwork != null &&
	                      activeNetwork.isConnectedOrConnecting();
if(isConnected ){
	if(name.getText().toString().length()>0)
	{
		if(email.getText().toString().length()>0)
		{
			if(phone.getText().toString().length()>0)
			{
			if(pass.getText().toString().length()>0)
			{
				if(reppass.getText().toString().length()>0)
				{
					if(reppass.getText().toString().equals(pass.getText().toString()))
					{
						HttpClient httpclient = new DefaultHttpClient();
					    HttpPost httppost = new HttpPost(StartActivity.server+"/reg_user.php");
					
					    try {
					        // Add your data
					        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
					        nameValuePairs.add(new BasicNameValuePair("email", email.getText().toString()));
					        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					
					        // Execute HTTP Post Request
					        HttpResponse response = httpclient.execute(httppost);
					        final String responseStr = EntityUtils.toString(response.getEntity());
					        Log.d("reg", responseStr);
					        if(!responseStr.equals("isemail"))
					        {
					        final GetCodeDialog gd = new GetCodeDialog(c);
					        gd.show();
					        gd.reg.setOnClickListener(new View.OnClickListener(){

								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									if(gd.code.getText().toString().equals(responseStr))
									{
										try {
										HttpClient httpclient = new DefaultHttpClient();
									    HttpPost httppost = new HttpPost(StartActivity.server+"/set_user_data.php");
										List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
								        nameValuePairs.add(new BasicNameValuePair("email", email.getText().toString()));
								        nameValuePairs.add(new BasicNameValuePair("phone", phone.getText().toString()));
								        nameValuePairs.add(new BasicNameValuePair("name", name.getText().toString()));
								        nameValuePairs.add(new BasicNameValuePair("pass", pass.getText().toString()));
								        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,"UTF-8"));
								
								        // Execute HTTP Post Request
								        HttpResponse response = httpclient.execute(httppost);
								        gd.dismiss();
								        dismiss();
								        AlertDialog.Builder builder = new AlertDialog.Builder(c);
										AlertDialog dialog = builder.create();	
										dialog.setTitle("����������� ������ �������!");
										Message listenerDoesNotAccept = null;
										dialog.setButton("Ok", listenerDoesNotAccept);
										dialog.setCancelable(false);
										dialog.show();
										} catch (ClientProtocolException e) {
									        // TODO Auto-generated catch block
									    } catch (IOException e) {
									        // TODO Auto-generated catch block
									    }
									}
									else
									{
										AlertDialog.Builder builder = new AlertDialog.Builder(c);
										AlertDialog dialog = builder.create();	
										dialog.setTitle("�������� ��� �������������!");
										Message listenerDoesNotAccept = null;
										dialog.setButton("Ok", listenerDoesNotAccept);
										dialog.setCancelable(false);
										dialog.show();
									}
									
								}});
					        }
					        else
							{
								AlertDialog.Builder builder = new AlertDialog.Builder(c);
								AlertDialog dialog = builder.create();	
								dialog.setTitle("����� email ��� ���������������");
								Message listenerDoesNotAccept = null;
								dialog.setButton("Ok", listenerDoesNotAccept);
								dialog.setCancelable(false);
								dialog.show();
							}
					    } catch (ClientProtocolException e) {
					        // TODO Auto-generated catch block
					    } catch (IOException e) {
					        // TODO Auto-generated catch block
					    }
					  
					}
					else
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(c);
						AlertDialog dialog = builder.create();	
						dialog.setTitle("������ ������ ���������");
						Message listenerDoesNotAccept = null;
						dialog.setButton("Ok", listenerDoesNotAccept);
						dialog.setCancelable(false);
						dialog.show();
					}
				}
				else
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(c);
					AlertDialog dialog = builder.create();	
					dialog.setTitle("������� ������������� ������");
					Message listenerDoesNotAccept = null;
					dialog.setButton("Ok", listenerDoesNotAccept);
					dialog.setCancelable(false);
					dialog.show();
				}
			}
			else
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(c);
				AlertDialog dialog = builder.create();	
				dialog.setTitle("������� ������");
				Message listenerDoesNotAccept = null;
				dialog.setButton("Ok", listenerDoesNotAccept);
				dialog.setCancelable(false);
				dialog.show();
			}
			}
			else
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(c);
				AlertDialog dialog = builder.create();	
				dialog.setTitle("������� �������");
				Message listenerDoesNotAccept = null;
				dialog.setButton("Ok", listenerDoesNotAccept);
				dialog.setCancelable(false);
				dialog.show();
			}
		}
		else
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(c);
			AlertDialog dialog = builder.create();	
			dialog.setTitle("������� email");
			Message listenerDoesNotAccept = null;
			dialog.setButton("Ok", listenerDoesNotAccept);
			dialog.setCancelable(false);
			dialog.show();
		}
	}
	else
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(c);
		AlertDialog dialog = builder.create();	
		dialog.setTitle("������� ���� ���");
		Message listenerDoesNotAccept = null;
		dialog.setButton("Ok", listenerDoesNotAccept);
		dialog.setCancelable(false);
		dialog.show();
	}
}
else
{
AlertDialog.Builder builder = new AlertDialog.Builder(c);
AlertDialog dialog = builder.create();	
dialog.setTitle("��� �����������, ������������ � ���������");
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
