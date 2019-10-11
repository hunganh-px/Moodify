package com.px.moodify.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.MediaController;
import android.widget.Toast;

import com.px.moodify.MainActivity;
import com.px.moodify.NowPlayingActivity;
import com.px.moodify.R;
import com.px.moodify.entities.Song;
import com.px.moodify.helpers.MediaChangeEvent;
import com.px.moodify.helpers.MediaControlEvent;
import com.px.moodify.helpers.MediaUpdateEvent;
import com.px.moodify.helpers.MoodControlEvent;
import com.px.moodify.helpers.PreferenceHelper;
import com.px.moodify.helpers.SongFetcher;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewListener;
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewManager;

/**
 * Created by prince on 24/10/2016.
 */

public class MediaPlayerService extends Service implements MediaController.MediaPlayerControl, MediaPlayer.OnPreparedListener, FloatingViewListener {

    public static final String ACTION_SHOW_OVERLAY = "ACTION_SHOW_OVERLAY";
    private static final int NOTIFICATION_ID = -13131;

    private MediaPlayer musicPlayer;
    private MediaPlayer soundPlayer;
    private MediaController mController;
    private List<Song> mSongList;

    private CircleImageView floatingIconView;
    private FloatingViewManager mFloatingViewManager;

    private int currentSong;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            if (ACTION_SHOW_OVERLAY.equals(intent.getAction())) {
                showOverlay();
            }
        }
        return START_NOT_STICKY;
    }

    private void showOverlay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NOTIFICATION_ID, createNotification(this));
        }
        if (isOverlayPermissionGranted() && mFloatingViewManager == null) {
            final DisplayMetrics metrics = new DisplayMetrics();
            final WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            floatingIconView = (CircleImageView) LayoutInflater.from(this).inflate(R.layout.floating_image, null);
            floatingIconView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(MediaPlayerService.this, NowPlayingActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            });
            try {
                floatingIconView.getLayoutParams().width = (int) (64 * getResources().getDisplayMetrics().density);
                floatingIconView.getLayoutParams().height = (int) (64 * getResources().getDisplayMetrics().density);
                floatingIconView.requestLayout();
            } catch (Exception e) {
                e.printStackTrace();
            }
            windowManager.getDefaultDisplay().getMetrics(metrics);
            mFloatingViewManager = new FloatingViewManager(this, this);
            mFloatingViewManager.setFixedTrashIconImage(R.drawable.ic_trash_fixed);
            mFloatingViewManager.setActionTrashIconImage(R.drawable.ic_trash_action);
            final FloatingViewManager.Options options = new FloatingViewManager.Options();
            options.overMargin = (int) (8 * metrics.density);
            mFloatingViewManager.addViewToWindow(floatingIconView, options);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification createNotification(MediaPlayerService mediaPlayerService) {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("Moodify");
        builder.setContentText("Moodify is running in background");
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, -12332, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setChannelId(getPackageName());
        return builder.build();
    }

    private boolean isOverlayPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        } else {
            return true;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        this.musicPlayer = new MediaPlayer();
        this.mController = new MediaController(this);
        this.mController.setMediaPlayer(this);

        this.mSongList = SongFetcher.getInstance(this).getSongList();
        this.currentSong = PreferenceHelper.getInstance(this).getLastSongOpened();
        this.currPausePositon = PreferenceHelper.getInstance(this).getLastSongPosition();
        this.musicPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                next();
            }
        });
        prepareLastInstance(currentSong);
        EventBus.getDefault().register(this);

    }

    @Override
    public void start() {
        musicPlayer.start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isPlaying()) {
                    try {
                        EventBus.getDefault().post(new MediaUpdateEvent(MUSIC_COMMAND.SEEK, getCurrentPosition()));
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void prepare(int pos) {
        musicPlayer.reset();
        try {
            musicPlayer.setDataSource(mSongList.get(pos).getPath());
            musicPlayer.setOnPreparedListener(this);
            musicPlayer.prepareAsync();
        } catch (Exception e) {
            Toast.makeText(this, "Cannot play the selected song", Toast.LENGTH_SHORT).show();
        }
    }

    public void prepareLastInstance(int pos) {
        musicPlayer.reset();
        try {
            musicPlayer.setDataSource(mSongList.get(pos).getPath());
            musicPlayer.prepareAsync();
            setCurrentSong(pos);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot play the last played song", Toast.LENGTH_SHORT).show();
        }
    }

    public void playSingle(int pos) {
        try {
            setCurrentSong(pos);
            EventBus.getDefault().post(new MediaChangeEvent(currentSong));
            prepare(pos);
            try {

                floatingIconView.setImageBitmap(SongFetcher.getInstance(this).getCover(currentSong));
                floatingIconView.getLayoutParams().width = (int) (64 * getResources().getDisplayMetrics().density);
                floatingIconView.getLayoutParams().height = (int) (64 * getResources().getDisplayMetrics().density);
                floatingIconView.requestLayout();
            } catch (Exception e) {
                e.printStackTrace();
            }
            RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF,
                    0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(5000);
            rotate.setRepeatCount(Animation.INFINITE);
            rotate.setInterpolator(new LinearInterpolator());
            floatingIconView.startAnimation(rotate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        pause();
    }

    public void next() {
        currentSong++;
        if (currentSong >= mSongList.size()) {
            currentSong = 0;
        }
        playSingle(currentSong);

    }

    public void prev() {
        if (getCurrentPosition() > 5000) {
            playSingle(currentSong);
        } else {
            currentSong--;
            playSingle(currentSong);
        }
    }

    @Override
    public void pause() {
        musicPlayer.pause();
    }

    public void resume(int currPos) {
        seekTo(currPos);
        start();
    }

    @Override
    public int getDuration() {
        return musicPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return musicPlayer.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        musicPlayer.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return musicPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return musicPlayer.getAudioSessionId();
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        start();
    }

    @Override
    public void onFinishFloatingView() {

    }

    @Override
    public void onTouchFinished(boolean isFinishing, int x, int y) {

    }

    public enum MUSIC_COMMAND {
        START,
        STOP,
        PAUSE,
        REPEAT,
        SHUFFLE,
        SEEK,
        NEXT,
        PREV,
        CHANGE;

    }

    public enum MOOD_COMMAND {
        RAIN,
        OCEAN,
        FOREST,
        NONE;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMediaControl(MediaControlEvent event) {
        switch (event.getCommand()) {
            case START:
                System.out.println("8==============D POS " + event.getPosition());
                playSingle(event.getPosition());
                this.currentSong = event.getPosition();
                PreferenceHelper.getInstance(this).setLastSongOpened(event.getPosition());
                break;
            case STOP:
                stop();
                break;
            case PAUSE:
                if (musicPlayer.isPlaying()) {
                    pause();
                    EventBus.getDefault().post(new MediaUpdateEvent(MUSIC_COMMAND.PAUSE, 1));
                    currPausePositon = getCurrentPosition();
                } else {
                    EventBus.getDefault().post(new MediaUpdateEvent(MUSIC_COMMAND.PAUSE, 0));
                    resume(currPausePositon);
                }
                break;
            case NEXT:
                next();
                break;
            case PREV:
                prev();
                break;

            case SEEK:
                seekTo(event.getPosition());
                currPausePositon = event.getPosition();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMoodControl(MoodControlEvent event) {
        if (soundPlayer != null) {
            soundPlayer.release();
        }
        switch (event.getCommand()) {
            case NONE:
                return;
            case OCEAN:
                soundPlayer = MediaPlayer.create(this, R.raw.ocean_l);
                soundPlayer.setVolume(0.7f, 0.7f);

                break;
            case FOREST:
                soundPlayer = MediaPlayer.create(this, R.raw.forest_l);
                soundPlayer.setVolume(0.7f, 0.7f);

                break;
            case RAIN:
                soundPlayer = MediaPlayer.create(this, R.raw.rain_l);
                soundPlayer.setVolume(0.9f, 0.9f);

                break;
        }
        soundPlayer.setLooping(true);
        soundPlayer.start();
    }


    private void setCurrentSong(int pos) {
        this.currentSong = pos;
        PreferenceHelper.getInstance(this).setLastSongOpened(pos);
    }


    @Override
    public void onDestroy() {
        PreferenceHelper.getInstance(this).setLastSongPausedPosition(getCurrentPosition());
        super.onDestroy();
    }

    private int currPausePositon = 0;


}
