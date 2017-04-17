package tools;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;

import com.youtravel.ChatActivity;

/**
 * Created by MaximTitarenko on 20.11.16.
 */

public class ContactMailChat {
    Context c;
    String type;
    String id;
    public ContactMailChat(Context c, String type, String id){
        this.c = c;
        this.type = type;
        this.id = id;
    }
    public AlertDialog callType(final String subject){
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
// Add the buttons
        builder.setPositiveButton("Вернуться", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
            }
        });
        String[] types = {"Написать e-mail","Чат"};
        builder.setItems(types, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { // +38(050) 469 4030
                Intent intent;
                switch (which) {
                    case 0:
                        intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:Olga.Barteva@gmail.com"));
                        intent.putExtra(Intent.EXTRA_EMAIL, "Olga.Barteva@gmail.com");
                        intent.putExtra(Intent.EXTRA_SUBJECT, "YouTravel: " + subject);
                        c.startActivity(Intent.createChooser(intent, "Написать"));
                        break;
                    /*case 2:
                        Uri uri = Uri.parse("tel:" + Uri.encode("+380504694030"));
                        intent = new Intent("android.intent.action.VIEW");
                        intent.setClassName("com.viber.voip", "com.viber.voip.WelcomeActivity");
                        intent.setData(uri);
                        c.startActivity(intent);
                        break;*/
                    case 1:
                        intent = new Intent(c, ChatActivity.class);
                        ChatActivity.idChat ="-1";
                        ChatActivity.typeChat = type;
                        ChatActivity.idSource = id;
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        c.startActivity(intent);
                        break;
                }
            }
        });

// Create the AlertDialog
        return builder.create();
    }

}
