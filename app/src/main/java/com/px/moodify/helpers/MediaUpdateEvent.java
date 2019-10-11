package com.px.moodify.helpers;

import com.px.moodify.services.MediaPlayerService;

/**
 * Created by prince on 26/12/2016.
 */

public class MediaUpdateEvent {
    private MediaPlayerService.MUSIC_COMMAND command;
    private int songPosition;

    public MediaUpdateEvent(MediaPlayerService.MUSIC_COMMAND command, int position) {
        this.command = command;
        this.songPosition = position;
    }

    public MediaPlayerService.MUSIC_COMMAND getCommand() {
        return command;
    }

    public int getSongPosition() {
        return songPosition;
    }
}
