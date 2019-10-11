package com.px.moodify.helpers;

/**
 * Created by prince on 26/12/2016.
 */

public class MediaChangeEvent {

    public int songPosition;

    public MediaChangeEvent(int songPosition) {
        this.songPosition = songPosition;
    }

    public int getSongPosition() {

        return songPosition;
    }
}
