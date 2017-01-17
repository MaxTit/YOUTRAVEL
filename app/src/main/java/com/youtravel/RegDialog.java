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
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class RegDialog extends Dialog implements
        View.OnClickListener {

    public Activity c;
    public Dialog d;

    public Button reg, cancel;
    public TextView name, email, pass, reppass;
    public EditText phone1;

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
        phone1 = (EditText) findViewById(R.id.editText1_phone);
        phone1.addTextChangedListener(new PhoneNumberFormattingTextWatcher() {
            //we need to know if the user is erasing or inputing some new character
            private boolean backspacingFlag = false;
            //we need to block the :afterTextChanges method to be called again after we just replaced the EditText text
            private boolean editedFlag = false;
            //we need to mark the cursor position and restore it after the edition
            private int cursorComplement;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //we store the cursor local relative to the end of the string in the EditText before the edition
                cursorComplement = s.length()-phone1.getSelectionStart();
                //we check if the user ir inputing or erasing a character
                if (count > after) {
                    backspacingFlag = true;
                } else {
                    backspacingFlag = false;
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // nothing to do here =D
            }

            @Override
            public void afterTextChanged(Editable s) {
                String string = s.toString();
                //what matters are the phone digits beneath the mask, so we always work with a raw string with only digits
                String phone = string.replaceAll("[^\\d]", "");

                //if the text was just edited, :afterTextChanged is called another time... so we need to verify the flag of edition
                //if the flag is false, this is a original user-typed entry. so we go on and do some magic
                if (!editedFlag) {

                    //we start verifying the worst case, many characters mask need to be added
                    //example: 999999999 <- 6+ digits already typed
                    // masked: (999) 999-999
                    if (phone.length() > 0 && phone.charAt(0) == '3') {
                        if (phone.length() >= 8 && !backspacingFlag) {
                            //we will edit. next call on this textWatcher will be ignored
                            editedFlag = true;
                            //here is the core. we substring the raw digits and add the mask as convenient
                            String ans = "+" + phone.substring(0, 2) + " (" + phone.substring(2, 5) + ") " + phone.substring(5, 8) + "-" + phone.substring(8);
                            phone1.setText(ans);
                            //we deliver the cursor to its original position relative to the end of the string
                            phone1.setSelection(phone1.getText().length() - cursorComplement);

                            //we end at the most simple case, when just one character mask is needed
                            //example: 99999 <- 3+ digits already typed
                            // masked: (999) 99
                        } else if (phone.length() >= 5 && !backspacingFlag) {
                            editedFlag = true;
                            String ans = "+" + phone.substring(0, 2) + " (" + phone.substring(2, 5) + ") " + phone.substring(5);
                            phone1.setText(ans);
                            phone1.setSelection(phone1.getText().length() - cursorComplement);
                        } else if (phone.length() >= 2 && !backspacingFlag) {
                            editedFlag = true;
                            String ans = "+" + phone.substring(0, 2) + " (" + phone.substring(2) + ") ";
                            phone1.setText(ans);
                            phone1.setSelection(phone1.getText().length() - cursorComplement);
                        } else if (phone.length() < 2 && !backspacingFlag) {
                            editedFlag = true;
                            String ans = "+" + ((phone.length() > 0) ? phone.substring(0) : "");
                            phone1.setText(ans);
                            phone1.setSelection(phone1.getText().length() - cursorComplement);
                        }

                    }
                    else {
                        if (phone.length() >= 7 && !backspacingFlag) {
                            //we will edit. next call on this textWatcher will be ignored
                            editedFlag = true;
                            //here is the core. we substring the raw digits and add the mask as convenient
                            String ans = "+" + phone.substring(0, 1) + " (" + phone.substring(1, 4) + ") " + phone.substring(4, 7) + "-" + phone.substring(7) + " ";
                            phone1.setText(ans);
                            //we deliver the cursor to its original position relative to the end of the string
                            phone1.setSelection(phone1.getText().length() - cursorComplement);

                            //we end at the most simple case, when just one character mask is needed
                            //example: 99999 <- 3+ digits already typed
                            // masked: (999) 99
                        } else if (phone.length() >= 4 && !backspacingFlag) {
                            editedFlag = true;
                            String ans = "+" + phone.substring(0, 1) + " (" + phone.substring(1, 4) + ") " + phone.substring(4) + " ";
                            phone1.setText(ans);
                            phone1.setSelection(phone1.getText().length() - cursorComplement);
                        } else if (phone.length() >= 1 && !backspacingFlag) {
                            editedFlag = true;
                            String ans = "+" + phone.substring(0, 1) + " (" + phone.substring(1) + ") ";
                            phone1.setText(ans);
                            phone1.setSelection(phone1.getText().length() - cursorComplement);
                        } else if (phone.length() < 1 && !backspacingFlag) {
                            editedFlag = true;
                            String ans = "+" + ((phone.length() > 0) ? phone.substring(0) : "");
                            phone1.setText(ans);
                            phone1.setSelection(phone1.getText().length() - cursorComplement);
                        }

                    }
                    // We just edited the field, ignoring this cicle of the watcher and getting ready for the next
                } else {
                    editedFlag = false;
                }
            }
        });
   //     phone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        pass = (TextView) findViewById(R.id.editText2);
        reppass = (TextView) findViewById(R.id.editText3);
        reg.setOnClickListener(this);
        cancel.setOnClickListener(this);

    }

    @SuppressWarnings("deprecation")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_reg: {
                ConnectivityManager cm =
                        (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                final boolean isConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();
                if (isConnected) {
                    if (name.getText().toString().length() > 0) {
                        if (email.getText().toString().length() > 0) {
                            if (phone1.getText().toString().length() > 0) {
                                if (pass.getText().toString().length() > 0) {
                                    if (reppass.getText().toString().length() > 0) {
                                        if (reppass.getText().toString().equals(pass.getText().toString())) {
                                            HttpClient httpclient_email = new DefaultHttpClient();
                                            HttpPost httppost_email = new HttpPost(StartActivity.server + "/reg_user.php");

                                            try {
                                                // Add your data
                                                List<NameValuePair> nameValuePairs_email = new ArrayList<NameValuePair>(2);
                                                nameValuePairs_email.add(new BasicNameValuePair("email", email.getText().toString()));
                                                httppost_email.setEntity(new UrlEncodedFormEntity(nameValuePairs_email));

                                                // Execute HTTP Post Request
                                                HttpResponse response_email = httpclient_email
                                                        .execute(httppost_email);
                                                final String responseStr_email = EntityUtils.toString(response_email.getEntity());
                                                Log.d("reg", responseStr_email);
                                                if (!responseStr_email.equals("isemail")) {
                                                    try {
                                                        HttpClient httpclient = new DefaultHttpClient();
                                                        HttpPost httppost = new HttpPost(StartActivity.server + "/set_user_data.php");
                                                        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                                                        nameValuePairs.add(new BasicNameValuePair("email", email.getText().toString()));
                                                        nameValuePairs.add(new BasicNameValuePair("phone", phone1.getText().toString()));
                                                        nameValuePairs.add(new BasicNameValuePair("name", name.getText().toString()));
                                                        nameValuePairs.add(new BasicNameValuePair("pass", pass.getText().toString()));
                                                        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

                                                        // Execute HTTP Post Request
                                                        HttpResponse response = httpclient.execute(httppost);
                                                        dismiss();
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(c);
                                                        AlertDialog dialog = builder.create();
                                                        dialog.setTitle("Регистрация прошла успешно!");
                                                        Message listenerDoesNotAccept = null;
                                                        dialog.setButton("Ok", listenerDoesNotAccept);
                                                        dialog.setCancelable(false);
                                                        dialog.show();
                                                    } catch (ClientProtocolException e) {
                                                        // TODO Auto-generated catch block
                                                    } catch (IOException e) {
                                                        // TODO Auto-generated catch block
                                                    }
                                                } else {
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(c);
                                                    AlertDialog dialog = builder.create();
                                                    dialog.setTitle("Такой email уже зарегистрирован");
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
                                dialog.setTitle("Введите номер телефона");
                                Message listenerDoesNotAccept = null;
                                dialog.setButton("Ok", listenerDoesNotAccept);
                                dialog.setCancelable(false);
                                dialog.show();
                            }
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(c);
                            AlertDialog dialog = builder.create();
                            dialog.setTitle("Введите email");
                            Message listenerDoesNotAccept = null;
                            dialog.setButton("Ok", listenerDoesNotAccept);
                            dialog.setCancelable(false);
                            dialog.show();
                        }
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(c);
                        AlertDialog dialog = builder.create();
                        dialog.setTitle("Введите имя");
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
