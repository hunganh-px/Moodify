package com.px.moodify.helpers;

import com.px.moodify.services.MediaPlayerService;

/**
 * Created by prince on 27/12/2016.
 */

public class MediaControlEvent {
    private MediaPlayerService.MUSIC_COMMAND command;
    private int position;

    public MediaControlEvent(MediaPlayerService.MUSIC_COMMAND command, int position) {
        this.command = command;
        this.position = position;
    }

    public MediaPlayerService.MUSIC_COMMAND getCommand() {
        return command;
    }

    public int getPosition() {
        return position;
    }
}
