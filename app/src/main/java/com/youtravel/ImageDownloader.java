package com.youtravel;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class ImageDownloader {

    private Context context;
    private String subjects;
    private String id_subjects;

    public ImageDownloader(Context context, String subjects,
                           String id_subjects){
        this.context = context;
        this.subjects = subjects;
        this.id_subjects = id_subjects;
    }

    public void run(){
        try {
            (new DownloadFileFromURL1(subjects, id_subjects)).execute().get();
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
        private class DownloadFileFromURL1 extends AsyncTask<String, Integer, String> {
        private String subject, id_subject, fileName;

        protected DownloadFileFromURL1(String subject, String id_subject){
            this.id_subject = id_subject;
            this.subject = subject;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... f_url) {
            try {
                DBHelper dbHelper;
                dbHelper = new DBHelper(context);
                int count;
                File folder = new File(context.getCacheDir() + "/Images");
                if (!folder.exists()) {
                    folder.mkdir();
                }
                Cursor c;
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                String p_query = "SELECT * FROM images WHERE is_downloaded = ? and subject = ? and id_subject = ?";
                c = db.rawQuery(p_query,new String[]{"-1", subject, id_subject});
                if (c.moveToFirst() && c.getCount() > 0) {
                    c.moveToFirst();
                    while (!c.isAfterLast()) {
                        URL url = new URL(StartActivityAlternative.server.concat("images/").concat(c.getString(2)));
                        URLConnection connection = url.openConnection();
                        connection.connect();
                        InputStream input = new BufferedInputStream(url.openStream(), 8192);
                        fileName = c.getString(2);
                        OutputStream output = new FileOutputStream(context.getCacheDir() + "/Images/" + fileName);
                        byte data[] = new byte[1024];
                        while ((count = input.read(data)) != -1) {
                            output.write(data, 0, count);
                        }
                        output.flush();
                        output.close();
                        input.close();
                        ContentValues cv = new ContentValues();
                        cv.put("is_downloaded", 1);
                        db.update("images", cv, "subject = ? and id_subject = ?",
                                new String[] { subject, id_subject });
                        c.moveToNext();
                    }
                }
                c.close();
                db.close();
            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String file_url) {
        }
    }



}
