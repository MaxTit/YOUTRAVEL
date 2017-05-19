package com.youtravel;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.File;

public class GalleryAdapter extends PagerAdapter {
    private Context context; // needed to create the view
    private int count_foto;
    private Activity _activity;
    private LayoutInflater inflater;
    String[] urls;
     String subject, id_subject;
    public GalleryAdapter(Context c, Tour tour) {
        context = c;
        Log.d("ininit","b - "+tour.img);
        urls = tour.img.split(",");
        count_foto = urls.length;
        subject = "tour";
        id_subject = String.valueOf(tour.id);
        this._activity = ((Activity) c);
    }
    @Override
    public int getCount() {
        return count_foto;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        final View v;

        // just a simple optimiztaion -
        // we only inflate a new layout if we don't have one to reuse
        inflater = (LayoutInflater) _activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.gallery_item, container,
                false);
        if (!urls[position].isEmpty()) {
            final ImageView imageView = (ImageView) v.findViewById(R.id.image);
            Log.d("ts",urls[position]);
            if ((new File(context.getCacheDir() + "/Images/" + urls[position])).exists()) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap_img = BitmapFactory.decodeFile(context.getCacheDir() + "/Images/" + urls[position], options);
                imageView.setImageBitmap(bitmap_img);
            } else {

                ConnectivityManager cm =
                        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                final boolean isConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();
                if (isConnected)
                    imageView.post(new Runnable() {
                        public void run() {
                            if ((new File(context.getCacheDir() + "/Images").exists())) {
                                Log.i("ImDow", "true");
                            }
                            (new ImageDownloader(context, subject, id_subject)).run();
                            if ((new File(context.getCacheDir() + "/Images/" + urls[position])).exists()) {
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                                Bitmap bitmap_img = BitmapFactory.decodeFile(context.getCacheDir() + "/Images/" + urls[position], options);
                                imageView.setImageBitmap(bitmap_img);
                            }
                        }
                    });
            }
        }
        ((ViewPager) container).addView(v);
        return v;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);

    }


    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }
}
