package com.px.moodify;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.px.moodify.adapters.SongAdapter;
import com.px.moodify.dialogs.MoodEffectChoosingDialog;
import com.px.moodify.entities.Song;
import com.px.moodify.helpers.MediaChangeEvent;
import com.px.moodify.helpers.MediaControlEvent;
import com.px.moodify.helpers.PreferenceHelper;
import com.px.moodify.helpers.SongFetcher;
import com.px.moodify.services.MediaPlayerService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int PERMISSION_REQUEST_CODE = 1131;
    private static final int OVERLAY_REQUEST_CODE = 13213;

    private SongAdapter mSongAdapter;
    private RecyclerView mSongList;

    private LinearLayout mLilMainInfo;

    private TextView tvTitle;
    private TextView tvArtist;
    private ImageView imgCover;

    private EventBus bus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissionAndInit();
    }

    private void init() {
        initViews();
        initFunctions();
        checkOverlay();
    }

    private void checkPermissionAndInit() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final String channelId = getPackageName();
            final String channelName = "Moodify";
            final NotificationChannel defaultChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_MIN);
            final NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(defaultChannel);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            } else {
                init();
            }
        } else {
            init();
        }
    }

    private void checkOverlay() {
        // check if we already  have permission to draw over other apps
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getApplicationContext().getPackageName()));
                startActivityForResult(intent, OVERLAY_REQUEST_CODE);
            } else {
                requestOverlayService();
            }
        } else {
            requestOverlayService();
        }
    }

    private void requestOverlayService() {
        Intent intent = new Intent(this, MediaPlayerService.class);
        intent.setAction(MediaPlayerService.ACTION_SHOW_OVERLAY);
        startService(intent);
    }

    private void initViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.mLilMainInfo = (LinearLayout) findViewById(R.id.lil_MAIN_info);

        this.tvArtist = (TextView) findViewById(R.id.tv_MAIN_artist);
        this.tvArtist.setSelected(true);
        this.tvTitle = (TextView) findViewById(R.id.tv_MAIN_title);
        this.tvTitle.setSelected(true);
        this.imgCover = (ImageView) findViewById(R.id.img_MAIN_cover);
    }

    private void initFunctions() {
        bus = EventBus.getDefault();
        mSongAdapter = new SongAdapter(this, SongFetcher.getInstance(this).getSongList());
        mSongList = (RecyclerView) findViewById(R.id.rview_list_song);
        mSongList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mSongList.setAdapter(mSongAdapter);
        startService(new Intent(this, MediaPlayerService.class));
        try {
            bus.register(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMediaUpdateEvent(MediaChangeEvent event) {
        updateInfo(event.getSongPosition());
    }


    @Override
    protected void onDestroy() {
        try {
            bus.unregister(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();

    }

    private void updateInfo(int pos) {
        List<Song> songList = SongFetcher.getInstance(this).getSongList();
        if (songList.size() > 0) {
            Song currSong = SongFetcher.getInstance(this).getSongList().get(pos);
            this.tvTitle.setText(currSong.getTitle());
            this.tvArtist.setText(currSong.getArtist());
            this.imgCover.setImageBitmap(SongFetcher.getInstance(this).getCover(pos));
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        updateInfo(PreferenceHelper.getInstance(this).getLastSongOpened());

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_MAIN_play:
                EventBus.getDefault().post(new MediaControlEvent(MediaPlayerService.MUSIC_COMMAND.PAUSE, -1));
                break;
            case R.id.btn_MAIN_prev:
                EventBus.getDefault().post(new MediaControlEvent(MediaPlayerService.MUSIC_COMMAND.PREV, -1));
                break;
            case R.id.btn_MAIN_next:
                EventBus.getDefault().post(new MediaControlEvent(MediaPlayerService.MUSIC_COMMAND.NEXT, -1));
                break;
            case R.id.btn_MAIN_mood:
                new MoodEffectChoosingDialog(MainActivity.this);
                break;
            case R.id.lil_MAIN_info:
                startActivity(new Intent(MainActivity.this, NowPlayingActivity.class));
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        init();
                    } else {
                        Toast.makeText(this, "Required permissions were not granted", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                break;
            case OVERLAY_REQUEST_CODE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.canDrawOverlays(this)) {
                        requestOverlayService();
                    }
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
