package com.px.moodify.helpers;

import com.px.moodify.services.MediaPlayerService;

/**
 * Created by prince on 27/12/2016.
 */

public class MoodControlEvent {
    private MediaPlayerService.MOOD_COMMAND command;

    public MoodControlEvent(MediaPlayerService.MOOD_COMMAND command) {
        this.command = command;
    }

    public MediaPlayerService.MOOD_COMMAND getCommand() {
        return command;
    }
}
