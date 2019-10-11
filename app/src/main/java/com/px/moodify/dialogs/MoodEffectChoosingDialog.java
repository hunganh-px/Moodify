package com.px.moodify.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.px.moodify.R;
import com.px.moodify.helpers.MoodControlEvent;
import com.px.moodify.services.MediaPlayerService;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by prince on 27/12/2016.
 */

public class MoodEffectChoosingDialog {

    private Context context;
    private AlertDialog.Builder builder;

    public MoodEffectChoosingDialog(Context context) {
        this.context = context;
        this.builder = new AlertDialog.Builder(context);
        this.builder.setTitle("Choose an effect");
        this.builder.setItems(context.getResources().getStringArray(R.array.array_mood_menu), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        EventBus.getDefault().post(new MoodControlEvent(MediaPlayerService.MOOD_COMMAND.NONE));
                        break;
                    case 1:
                        EventBus.getDefault().post(new MoodControlEvent(MediaPlayerService.MOOD_COMMAND.OCEAN));
                        break;
                    case 2:
                        EventBus.getDefault().post(new MoodControlEvent(MediaPlayerService.MOOD_COMMAND.FOREST));
                        break;
                    case 3:
                        EventBus.getDefault().post(new MoodControlEvent(MediaPlayerService.MOOD_COMMAND.RAIN));
                        break;
                    default:
                        EventBus.getDefault().post(new MoodControlEvent(MediaPlayerService.MOOD_COMMAND.NONE));
                        break;
                }
            }
        });
        this.builder.show();

    }
}

