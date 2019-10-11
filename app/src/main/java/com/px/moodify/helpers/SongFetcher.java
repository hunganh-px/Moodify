package com.px.moodify.helpers;

/**
 * Created by prince on 13/11/2016.
 */

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;

import com.px.moodify.R;
import com.px.moodify.entities.Song;

import java.util.ArrayList;
import java.util.List;

public class SongFetcher {
    private List<Song> songList;
    //    private Context context;
    private static SongFetcher songFetcher;
    private ContentResolver mContentResolver;
    private MediaMetadataRetriever mMediaMetadataRetriever;
    private Bitmap defaultCoverInBitmap;


    private SongFetcher(Context context) {
//        this.context = context;
        this.songList = new ArrayList<>();

        defaultCoverInBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.cover);
        getSongs(context);

    }

    public static SongFetcher getInstance(Context context) {
        if (songFetcher == null) {
            songFetcher = new SongFetcher(context);
        }
        return songFetcher;
    }

    void getSongs(Context context) {
        this.mContentResolver = context.getContentResolver();
        this.mMediaMetadataRetriever = new MediaMetadataRetriever();

        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songsCursor = mContentResolver.query(songUri, null, null, null, null);

        if (songsCursor != null && songsCursor.moveToFirst()) {
            int pathColumn = songsCursor.getColumnIndex(MediaStore.Audio.Media.DATA);

            while (songsCursor.moveToNext()) {
                String path = songsCursor.getString(pathColumn);
                mMediaMetadataRetriever.setDataSource(path);
                Song newSong = new Song();
                newSong.setPath(path);
                newSong.setTitle(mMediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
//                System.out.println(newSong.getTitle());
                newSong.setArtist(mMediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
                newSong.setAlbum(mMediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
                newSong.setDuration(mMediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                newSong.setImagePath(mMediaMetadataRetriever.getEmbeddedPicture());
                this.songList.add(newSong);
            }

            mMediaMetadataRetriever.release();
            songsCursor.close();
        }

    }

    public List<Song> getSongList() {
        return songList;
    }

    public Bitmap getCover(int position) {

        Bitmap bitmap = defaultCoverInBitmap;
        try {
        this.mMediaMetadataRetriever = new MediaMetadataRetriever();
        Song song = songList.get(position);
        mMediaMetadataRetriever.setDataSource(song.getPath());
            byte[] temp = mMediaMetadataRetriever.getEmbeddedPicture();
            bitmap = BitmapFactory.decodeByteArray(temp, 0, temp.length);
        } catch (Exception e) {
            return bitmap;
        }

        return bitmap;
    }
}