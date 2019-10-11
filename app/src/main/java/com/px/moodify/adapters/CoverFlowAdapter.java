package com.px.moodify.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.px.moodify.R;
import com.px.moodify.entities.Song;
import com.px.moodify.helpers.SongFetcher;

import java.util.List;

/**
 * Created by hunga on 12/8/2016.
 */

public class CoverFlowAdapter extends BaseAdapter {
    private Context context;
    private List<Song> songList;


    public CoverFlowAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return SongFetcher.getInstance(this.context).getSongList().size();
    }

    @Override
    public Song getItem(int i) {
        return SongFetcher.getInstance(this.context).getSongList().get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        final Holder viewHolder;

        if (view == null) {
            view = LayoutInflater.from(this.context).inflate(R.layout.item_song_cover, null);

            viewHolder = new Holder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (Holder) view.getTag();
        }
        viewHolder.image.setImageResource(R.drawable.cover);
        viewHolder.image.setAdjustViewBounds(true);
        viewHolder.image.setLayoutParams(new FrameLayout.LayoutParams(500, 500));

        new AsyncTask<Void, Void, Void>() {
            Bitmap bitmap;

            @Override
            protected Void doInBackground(Void... integers) {
                bitmap = SongFetcher.getInstance(CoverFlowAdapter.this.context).getCover(i);
                publishProgress();
                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }


            @Override
            protected void onProgressUpdate(Void... aVoid) {
                try {
                    viewHolder.image.setImageBitmap(bitmap);
                } catch (Exception e) {
                    viewHolder.image.setImageResource(R.drawable.cover);
                    e.printStackTrace();
                    System.out.println("NO IMAGE");
                }
            }
        }.execute();


        return view;
    }

    private static class Holder {
        private ImageView image;

        public Holder(View view) {
            this.image = (ImageView) view.findViewById(R.id.img_cover);
        }
    }


}
