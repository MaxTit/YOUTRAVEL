package com.youtravel;

import java.util.concurrent.ExecutionException;
        import android.annotation.SuppressLint;
        import android.app.Notification;
        import android.app.NotificationManager;
        import android.app.PendingIntent;
        import android.app.Service;
        import android.content.ContentValues;
        import android.content.Context;
        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.database.Cursor;
        import android.database.sqlite.SQLiteDatabase;
        import android.net.ConnectivityManager;
        import android.net.NetworkInfo;
        import android.os.AsyncTask;
        import android.os.Handler;
        import android.os.IBinder;
        import android.support.v4.app.NotificationCompat;
        import android.support.v4.app.TaskStackBuilder;
        import android.util.Log;
        import static com.youtravel.StartActivityAlternative.update_chat;
        import static com.youtravel.StartActivityAlternative.update_chat_mes;

class DownloadTask extends AsyncTask<String, Void, String> {


    private Context mContext;
    private String id_user;
    //ProgressDialog waitSpinner;
    //ConfigurationContainer configuration = ConfigurationContainer.getInstance();

    public DownloadTask (Context context,String id_user) {
        mContext = context;
        this.id_user = id_user;
        //waitSpinner = new ProgressDialog(this.context);
    }
    protected String doInBackground(String... urls) {
        update_chat(id_user, mContext);
        update_chat_mes(id_user, mContext);
        return null;
        //Put your getServerData-logic here
        //return serverData
    }
    //This Method is called when Network-Request finished
    protected void onPostExecute(String serverData) {

    }
}
public class PushService extends Service {
    static DBHelper dbHelper;
    public static int notId = 1;
    boolean flgl = false;
    static DownloadTask dt;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub

        //Toast.makeText(getApplicationContext(), "Service Created", 1).show();
        notId =1;
        flgl = false;
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        //Toast.makeText(getApplicationContext(), "Service Destroy", 1).show();
        super.onDestroy();
    }

    @SuppressLint("ShowToast")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // TODO Auto-generated method stub
        final SharedPreferences settings;
        Log.d("Start","0:0x001");
        settings = getApplicationContext().getSharedPreferences("my_data", 0);
        final String id_user = settings.getString("id_user", null);
        if(id_user != null) {
            ConnectivityManager cm =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            final boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();

            if (flgl) {
                final Handler handler = new Handler();
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        handler.post(new Runnable() { // This thread runs in the UI
                            @Override
                            public void run() {
                                dbHelper = new DBHelper(getApplicationContext());
                                if (isConnected) {
                                    // подключаемся к БД
                                                dt = new DownloadTask(getApplicationContext(), id_user);
                                                try {
                                                    String str_result = dt.execute().get();
                                                } catch (InterruptedException e) {
                                                    // TODO Auto-generated catch block
                                                    e.printStackTrace();
                                                } catch (ExecutionException e) {
                                                    // TODO Auto-generated catch block
                                                    e.printStackTrace();
                                                }

                                                //}
                                                final Cursor c1;
                                                // подключаемся к БД
                                                SQLiteDatabase db1 = dbHelper.getWritableDatabase();
                                                String p_query1 = "SELECT * FROM chat_mes WHERE isRead == -1 and isPushed == 1";
                                                c1 = db1.rawQuery(p_query1, null);

                                                if (c1.moveToFirst() && c1.getCount() > 0) {
                                                    c1.moveToFirst();
                                                    while (!c1.isAfterLast()) {
                                                        Log.d("Catch","0:0x002 id = " + c1.getString(0));

                                                        NotificationCompat.Builder mBuilder =
                                                                new NotificationCompat.Builder(getApplicationContext())
                                                                        .setSmallIcon(R.drawable.ic_launcher)
                                                                        .setContentTitle(c1.getString(3) + ":")
                                                                        .setDefaults(Notification.DEFAULT_SOUND |
                                                                                Notification.DEFAULT_VIBRATE)
                                                                        .setAutoCancel(true)
                                                                        .setContentText((c1.getString(4).length() > 20 ? ((new StringBuffer(c1.getString(4))).substring(0, 20)+"...") : c1.getString(4)));

                                                        // Creates an explicit intent for an Activity in your app
                                                        Intent resultIntent = new Intent(getApplicationContext(), ChatActivity.class);
                                                        ChatActivity.idChat = c1.getString(1);
                                                        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


                                                        // The stack builder object will contain an artificial back stack for the
                                                        // started Activity.
                                                        // This ensures that navigating backward from the Activity leads out of
                                                        // your application to the Home screen.
                                                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                                                        // Adds the back stack for the Intent (but not the Intent itself)

                                                        stackBuilder.addParentStack(MenuChatActivity.class);
                                                      //  stackBuilder.addParentStack(HomeActivity.class);
                                                        // Adds the Intent that starts the Activity to the top of the stack

                                                        stackBuilder.addNextIntent(resultIntent);
                                                        PendingIntent resultPendingIntent =
                                                                stackBuilder.getPendingIntent(
                                                                        0,
                                                                        PendingIntent.FLAG_UPDATE_CURRENT
                                                                );
                                                        mBuilder.setContentIntent(resultPendingIntent);
                                                        NotificationManager mNotificationManager =
                                                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                                        // mId allows you to update the notification later on.
                                                        notId++;
                                                        mNotificationManager.notify(notId, mBuilder.build());
                                                        ContentValues cv = new ContentValues();
                                                        cv.put("isPushed", "-1");
                                                        db1.update("chat_mes", cv, "id == ?", new String[]{c1.getString(0)});
                                                        ChatActivity.newMessage = 1 == 1;
                                                        c1.moveToNext();
                                                    }
                                                }
                                                c1.close();
                                                db1.close();
                                }
                            }
                        });
                    }
                };
                new Thread(runnable).start();
            }
        }
        flgl = true;
        return super.onStartCommand(intent, flags, startId);
    }




}


