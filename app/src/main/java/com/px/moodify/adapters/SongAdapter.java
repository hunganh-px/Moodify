package com.px.moodify.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.px.moodify.R;
import com.px.moodify.entities.Song;
import com.px.moodify.helpers.MediaControlEvent;
import com.px.moodify.services.MediaPlayerService;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by prince on 02/11/2016.
 */

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    private Context mContext;
    private List<Song> songList;


    public SongAdapter(Context mContext, List<Song> songList) {
        this.mContext = mContext;
        this.songList = songList;
    }

    @Override
    public SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(this.mContext).inflate(R.layout.item_song, null);
        return new SongViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SongViewHolder holder, final int position) {
        final int pos = position;
        Song song = this.songList.get(position);
        holder.title.setText(song.getTitle());
        holder.artist.setText(song.getArtist());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new MediaControlEvent(MediaPlayerService.MUSIC_COMMAND.START, position));
            }
        });

    }

    @Override
    public int getItemCount() {
        return this.songList.size();
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView artist;
        private View itemView;


        public SongViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.title = (TextView) itemView.findViewById(R.id.tv_song_title);
            this.artist = (TextView) itemView.findViewById(R.id.tv_song_artist);
        }

    }
}
