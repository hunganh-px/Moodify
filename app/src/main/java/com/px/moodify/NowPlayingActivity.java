package com.px.moodify;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.px.moodify.adapters.CoverFlowAdapter;
import com.px.moodify.dialogs.MoodEffectChoosingDialog;
import com.px.moodify.entities.Song;
import com.px.moodify.helpers.MediaChangeEvent;
import com.px.moodify.helpers.MediaControlEvent;
import com.px.moodify.helpers.MediaUpdateEvent;
import com.px.moodify.helpers.PreferenceHelper;
import com.px.moodify.helpers.SongFetcher;
import com.px.moodify.services.MediaPlayerService;
import com.vansuita.gaussianblur.GaussianBlur;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import it.moondroid.coverflow.components.ui.containers.FeatureCoverFlow;

import static com.px.moodify.R.drawable.cover;

public class NowPlayingActivity extends AppCompatActivity implements View.OnClickListener {

    private FeatureCoverFlow coverFlow;
    private CoverFlowAdapter adapter;

    private ImageView background;
    private TextView txtTitle;
    private TextView txtArtist;

    private TextView txtDuration;
    private TextView txtTime;
    private SeekBar seekBar;

    private Animator animFadeIn;
    private Animator animFadeOut;

    private int currentPosition = 0;
    private Song currentSong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);

        this.currentPosition = PreferenceHelper.getInstance(this).getLastSongOpened();
        initViews();

        adapter = new CoverFlowAdapter(this);
        coverFlow.setAdapter(adapter);

        initAnims();
        initFunctions();
        updateInfo(this.currentPosition);

        EventBus.getDefault().register(this);
    }

    private void initAnims() {
        animFadeIn = ObjectAnimator.ofFloat(background, "alpha", 0f, 1f);
        animFadeIn.setDuration(300);
        animFadeOut = ObjectAnimator.ofFloat(background, "alpha", 1, 0f);
        animFadeOut.setDuration(200);

    }

    private void initViews() {
        coverFlow = (FeatureCoverFlow) findViewById(R.id.coverflow);
        background = (ImageView) findViewById(R.id.img_NP_bg);
        txtTitle = (TextView) findViewById(R.id.tv_NP_title);
        txtTitle.setSelected(true);
        txtArtist = (TextView) findViewById(R.id.tv_NP_artist);
        txtArtist.setSelected(true);
        txtDuration = (TextView) findViewById(R.id.txt_NP_duration);
        txtTime = (TextView) findViewById(R.id.txt_NP_time);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
    }

    private void initFunctions() {

        coverFlow.setOnScrollPositionListener(new FeatureCoverFlow.OnScrollPositionListener() {
            @Override
            public void onScrolledToPosition(int position) {

                if (position != currentPosition) {
                    currentPosition = position;
                    animFadeIn.start();
                    EventBus.getDefault().post(new MediaControlEvent(MediaPlayerService.MUSIC_COMMAND.START, position));
                }
            }

            @Override
            public void onScrolling() {

            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    EventBus.getDefault().post(new MediaControlEvent(MediaPlayerService.MUSIC_COMMAND.SEEK, i));

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


    }

    private void updateInfo(int position) {
        try {
            currentSong = SongFetcher.getInstance(this).getSongList().get(position);
            this.coverFlow.scrollToPosition(position);
            try {
                GaussianBlur.with(NowPlayingActivity.this).maxSixe(400).radius(25).put(SongFetcher.getInstance(this).getCover(position), background);
            } catch (Exception e) {
                background.setImageResource(cover);
            }
            txtTitle.setText(currentSong.getTitle());
            txtArtist.setText(currentSong.getArtist());
            txtDuration.setText(songTimingResolver(currentSong.getDuration()));
            seekBar.setMax(currentSong.getDuration());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_NP_play:
                EventBus.getDefault().post(new MediaControlEvent(MediaPlayerService.MUSIC_COMMAND.PAUSE, -1));
                break;
            case R.id.btn_NP_prev:
                EventBus.getDefault().post(new MediaControlEvent(MediaPlayerService.MUSIC_COMMAND.PREV, -1));
                break;
            case R.id.btn_NP_next:
                EventBus.getDefault().post(new MediaControlEvent(MediaPlayerService.MUSIC_COMMAND.NEXT, -1));
                break;
            case R.id.btn_NP_repeat:
                EventBus.getDefault().post(new MediaControlEvent(MediaPlayerService.MUSIC_COMMAND.REPEAT, -1));
                break;
            case R.id.btn_NP_shuffle:
                EventBus.getDefault().post(new MediaControlEvent(MediaPlayerService.MUSIC_COMMAND.SHUFFLE, -1));
                break;
            case R.id.btn_NP_mood:
                new MoodEffectChoosingDialog(NowPlayingActivity.this);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMediaChange(MediaChangeEvent event) {
        updateInfo(event.getSongPosition());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMediaUpdate(MediaUpdateEvent event) {
        if (event.getCommand() == MediaPlayerService.MUSIC_COMMAND.SEEK) {
            int currPos = event.getSongPosition();

            txtTime.setText(songTimingResolver(currPos));
            seekBar.setProgress(currPos);
        }
        if (event.getCommand()==MediaPlayerService.MUSIC_COMMAND.PAUSE) {
            ImageView buttonPlay = (ImageView) findViewById(R.id.btn_NP_play);
            buttonPlay.setImageResource((event.getSongPosition()==1) ? R.drawable.pause : R.drawable.play);
        }
    }


    public String songTimingResolver(int time) {
        String minS;
        String secS;
        int min = 0;
        int sec = 0;

        sec = time / 1000;
        min = sec / 60;

        minS = (min < 10) ? "0" + String.valueOf(min) : String.valueOf(min);
        secS = (sec % 60 < 10) ? "0" + String.valueOf(sec % 60) : String.valueOf(sec % 60);

        return minS + ":" + secS;
    }

}
