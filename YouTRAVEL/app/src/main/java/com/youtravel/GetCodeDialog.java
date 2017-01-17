package com.youtravel;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

public class GetCodeDialog extends Dialog implements
View.OnClickListener {

public Activity c;
public Dialog d;

public Button reg, cancel;
public EditText code;

public GetCodeDialog(Activity a) {
super(a);
// TODO Auto-generated constructor stub
this.c = a;
}

@Override
protected void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
requestWindowFeature(Window.FEATURE_NO_TITLE);
setContentView(R.layout.getcode_dialog);
code = (EditText) findViewById(R.id.editText1_code);
reg = (Button) findViewById(R.id.btn_reg);
cancel = (Button) findViewById(R.id.btn_cancel);
cancel.setOnClickListener(this);
}

@Override
public void onClick(View v) {
switch (v.getId()) {
case R.id.btn_cancel:
  dismiss();
  break;
default:
  break;
}
}
}
